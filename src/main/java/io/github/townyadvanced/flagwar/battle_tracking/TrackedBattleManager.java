package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.BattleStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.BattleSnapshot;
import io.github.townyadvanced.flagwar.database.TrackerDatabase;
import io.github.townyadvanced.flagwar.managers.BattleManager;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** A class that facilitates the initiating, retrieving, storing and completing of {@link TrackedBattle}s. */
public final class TrackedBattleManager {

    /** Holds a map of all tracked battles and the name of their towns. */
    private final Map<String, TrackedBattle> TRACKED_BATTLES = new HashMap<>();

    /** Maps each world and chunk to the battles whose regions include that chunk. */
    private final Map<UUID, Map<Long, Set<TrackedBattle>>> BATTLES_BY_CHUNK = new HashMap<>();

    /** Holds the {@link BukkitTask} that runs every period of time to update and save tracked battles. */
    private BukkitTask heartbeatTask;

    /** Holds the {@link TrackerDatabase} instance. */
    private final TrackerDatabase DATABASE;

    /** Uploads completed battle packages without blocking the server thread. */
    private final BattleResultUploader RESULT_UPLOADER = new BattleResultUploader();

    /** Holds the {@link Plugin} instance. */
    private final Plugin PLUGIN = FlagWar.getInstance();

    /** Returns the tracked battle whose battle region contains this location. This can be null. */
    public TrackedBattle getBattleAt(Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        Map<Long, Set<TrackedBattle>> battlesInWorld = BATTLES_BY_CHUNK.get(world.getUID());
        if (battlesInWorld == null) return null;

        Set<TrackedBattle> candidates = battlesInWorld.get(chunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4));
        if (candidates == null) return null;

        Vector position = location.toVector();
        for (TrackedBattle battle : candidates) {
            if (battle.isInBattleRegion(position)) return battle;
        }
        return null;
    }

    /** Begins tracking the specified BannerWar {@link Battle}. */
    public void trackBattle(Battle battle) {
        Town town = battle.getContestedTown();
        removeTrackedBattle(town.getName());

        TrackedBattle trackedBattle = new TrackedBattle(town, battle.getAttacker(), battle.getDefender());
        TRACKED_BATTLES.put(town.getName(), trackedBattle);
        indexBattle(trackedBattle);
    }

    /** Stops recording a battle, flushes its final state, and packages its immutable result. */
    public void finalizeBattle(Battle battle, BattleStatus status) {
        TrackedBattle trackedBattle = removeTrackedBattle(battle.getContestedTown().getName());
        if (trackedBattle == null) return;

        BattleSnapshot result = BattleSnapshot.parse(trackedBattle, status);
        var pendingDamageOccurrences = trackedBattle.drainPendingDamageOccurrences();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                if (!DATABASE.insertDamageOccurrencesSync(pendingDamageOccurrences, result.townName())) return;
                DATABASE.insertOrUpdatePlayersSync(result.playerResultMap().values(), result.townName());
                var battlePackage = DATABASE.finalizeBattleSync(result);
                if (battlePackage != null) RESULT_UPLOADER.upload(battlePackage);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public TrackedBattleManager(TrackerDatabase database, BattleManager battleManager) {
        this.DATABASE = database;
        battleManager.whenBattlesResumed()
            .thenRun(() -> Bukkit.getScheduler().runTask(PLUGIN, this::start))
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }

    /**
     * A series of operations to be run upon startup. This includes initiating the {@link #heartbeatTask}
     * and retrieving battles from the database via {@link #populateBattles()}.
     */
    private void start() {
        DATABASE.recoverPendingBattlePackages()
            .thenCompose(ignored -> populateBattles())
            .thenRunAsync(this::startHeartbeat, runnable -> Bukkit.getScheduler().runTask(PLUGIN, runnable));
    }

    /** Starts the periodic persistence task after all tracked battles have been restored. */
    private void startHeartbeat() {
        heartbeatTask = Bukkit.getScheduler().runTaskTimer(PLUGIN,
            () -> {
                for (var TB : TRACKED_BATTLES.values()) {
                    var result = BattleSnapshot.parse(TB, BattleStatus.ONGOING);
                    var pendingDamageOccurrences = TB.drainPendingDamageOccurrences();
                    DATABASE.insertOrUpdateBattle(result).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    }).thenRun(() -> {
                        try {
                            DATABASE.insertOrUpdatePlayersSync(result.playerResultMap().values(), result.townName());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if (!DATABASE.insertDamageOccurrencesSync(pendingDamageOccurrences, result.townName())) {
                            Bukkit.getScheduler().runTask(PLUGIN,
                                () -> TB.restorePendingDamageOccurrences(pendingDamageOccurrences));
                        }
                    });
                }
            }, 400L, 400L); // runs every 20 seconds.
    }

    /** Stops all operations of the manager by stopping the {@link #heartbeatTask} and clearing the {@link #TRACKED_BATTLES} map. */
    public void stop() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }

        TRACKED_BATTLES.clear();
        BATTLES_BY_CHUNK.clear();
    }

    /** Retrieves all battles from the {@link #DATABASE} to be resumed to support persistence beyond restarts and crashes. */
    private CompletableFuture<Void> populateBattles() {
        return DATABASE.getTrackedBattles().thenAcceptAsync(trackedBattleResults -> {
            for (var tbr : trackedBattleResults) {
                if (BattleManager.getBattle(tbr.townName()) == null) {
                    FlagWar.getInstance().getLogger().warning("Skipping stale tracked battle " + tbr.townName() + ".");
                    continue;
                }

                TrackedBattle trackedBattle = new TrackedBattle(tbr);
                removeTrackedBattle(tbr.townName());
                TRACKED_BATTLES.put(tbr.townName(), trackedBattle);
                indexBattle(trackedBattle);
            }
        }, runnable -> Bukkit.getScheduler().runTask(PLUGIN, runnable))
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }

    /** Adds every chunk covered by a battle region to the lookup index. */
    private void indexBattle(TrackedBattle battle) {
        UUID worldId = battle.getTown().getWorld().getUID();
        Map<Long, Set<TrackedBattle>> battlesInWorld = BATTLES_BY_CHUNK.computeIfAbsent(worldId, ignored -> new HashMap<>());

        for (BoundingBox region : battle.getBattleRegion()) {
            int minChunkX = (int) Math.floor(region.getMinX() / 16d);
            int maxChunkX = (int) Math.floor(region.getMaxX() / 16d);
            int minChunkZ = (int) Math.floor(region.getMinZ() / 16d);
            int maxChunkZ = (int) Math.floor(region.getMaxZ() / 16d);

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    battlesInWorld.computeIfAbsent(chunkKey(chunkX, chunkZ), ignored -> new HashSet<>()).add(battle);
                }
            }
        }
    }

    /** Removes a tracked battle from the battle map and every indexed chunk. */
    private TrackedBattle removeTrackedBattle(String townName) {
        TrackedBattle trackedBattle = TRACKED_BATTLES.remove(townName);
        if (trackedBattle == null) return null;

        UUID worldId = trackedBattle.getTown().getWorld().getUID();
        Map<Long, Set<TrackedBattle>> battlesInWorld = BATTLES_BY_CHUNK.get(worldId);
        if (battlesInWorld == null) return trackedBattle;

        for (BoundingBox region : trackedBattle.getBattleRegion()) {
            int minChunkX = (int) Math.floor(region.getMinX() / 16d);
            int maxChunkX = (int) Math.floor(region.getMaxX() / 16d);
            int minChunkZ = (int) Math.floor(region.getMinZ() / 16d);
            int maxChunkZ = (int) Math.floor(region.getMaxZ() / 16d);

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    long key = chunkKey(chunkX, chunkZ);
                    Set<TrackedBattle> battles = battlesInWorld.get(key);
                    if (battles == null) continue;
                    battles.remove(trackedBattle);
                    if (battles.isEmpty()) battlesInWorld.remove(key);
                }
            }
        }

        if (battlesInWorld.isEmpty()) BATTLES_BY_CHUNK.remove(worldId);
        return trackedBattle;
    }

    /** Returns a collision-free key for a pair of chunk coordinates. */
    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }
}
