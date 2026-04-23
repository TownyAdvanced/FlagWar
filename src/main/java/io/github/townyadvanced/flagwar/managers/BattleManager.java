package io.github.townyadvanced.flagwar.managers;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.chunk.ChunkCopy;
import io.github.townyadvanced.flagwar.database.BattleDatabase;
import io.github.townyadvanced.flagwar.database.DatabaseInteraction;
import io.github.townyadvanced.flagwar.events.BattleResumeEvent;
import io.github.townyadvanced.flagwar.events.BattleStartEvent;
import io.github.townyadvanced.flagwar.objects.*;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import town.sheepy.townyAI.TownyAI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class BattleManager {

    /** Holds the {@link BattleDatabase} instance. */
    private final BattleDatabase DATABASE;

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link BukkitScheduler} instance. */
    private final BukkitScheduler SCHEDULER;

    /** Holds the {@link WaypointManager} instance. */
    private final WaypointManager WAYPOINT_MANAGER;

    /** Holds a {@link HashMap} of every {@link Battle} and its associated contested town's name. */
    private static final Map<String, Battle> ACTIVE_BATTLES = new HashMap<>();

    public BattleManager(JavaPlugin plugin, BattleDatabase db, WaypointManager waypointManager) {
        DATABASE = db;
        PLUGIN = plugin;
        SCHEDULER = Bukkit.getScheduler();
        WAYPOINT_MANAGER = waypointManager;
        resumeBattles();
    }

    /**
     * Starts the last saved {@link Battle}s from the database.
     */
    private void resumeBattles() {
        ACTIVE_BATTLES.clear();

        DATABASE.getBattles().thenAccept(battleRecords -> {
            for (BattleRecord r : battleRecords) {
                Battle battle = new Battle(r, this);
                ACTIVE_BATTLES.put(r.contestedTown(), battle);
                PLUGIN.getLogger().info("Battle " + r.contestedTown() + " has been resumed");

                CompletableFuture.runAsync(() ->
                    Bukkit.getPluginManager().callEvent(new BattleResumeEvent(battle)),
                runnable -> SCHEDULER.runTask(PLUGIN, runnable));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Returns every currently ongoing battle, dormant or not.
     */
    public static Collection<Battle> getActiveBattles() {
        return ACTIVE_BATTLES.values();
    }

    /**
     * Refreshes the battles' states, waypoints, and saves them to the database.
     */
    public void updateBattles() {

        for (Map.Entry<String, Battle> entry : ACTIVE_BATTLES.entrySet()) {
            Battle battle = entry.getValue();

            if (battle.isPendingStageAdvance()) battle.advanceStage(true);
            battle.updateBossBar();

            BannerWarAPI.getAllBots().thenAccept(bots -> {
                var associated = BannerWarAPI.getAssociatedPlayers(battle);
                var notAssociated = BannerWarAPI.getNonAssociatedPlayers(battle);

                associated.removeAll(bots);
                notAssociated.removeAll(bots);

                CompletableFuture.runAsync(() -> {

                    for (String flagOwner : battle.getFlagOwners()) {

                        WAYPOINT_MANAGER.removePlayersFromWaypoint(
                            (Collection<Player>) Bukkit.getOnlinePlayers(), flagOwner
                        );

                        WAYPOINT_MANAGER.addPlayersToWaypoint(
                            associated, flagOwner
                        );
                    }
                }, runnable -> SCHEDULER.runTask(PLUGIN, runnable));
            }).exceptionally(ex -> {
                PLUGIN.getLogger().severe("Error occurred while trying to update the battle " +
                    "for " + battle.getContestedTown().getName() + ": " + ex.getMessage());
                return null;
            });

            BattleRecord rec = BattleRecord.of(battle);
            if (battle.getHomeBlock() != null && battle.getContestedTown() != null && rec != null)
                DATABASE.insertOrUpdate(rec);
        }
    }

    /**
     * Begins a battle.
     * <p>
     * Logs the banner placement onto the database, puts the battle into {@link #ACTIVE_BATTLES},
     * copies the chunks of the claims of the contested town, and fires a new {@link BattleStartEvent}.
     * @param contestedTown the {@link Town} where the {@link Battle} is hosted
     * @param attacker the nation that initiated the {@link Battle}
     * @param defender the nation that houses the {@link Town} where the {@link Battle} is hosted
     */
    public void startBattle(Town contestedTown, Nation attacker, Nation defender, Town bannerPlacer) {

        TownyAI.getTownyAIAPI().isCityStateAsync(contestedTown.getName()).thenAccept(result ->
            CompletableFuture.runAsync(() -> {

                Battle battle = new Battle(attacker, defender, contestedTown, result, this);
                ACTIVE_BATTLES.put(contestedTown.getName(), battle);

                logBannerPlacer(BannerPlacerRecord.of(bannerPlacer));

                Bukkit.getServer().getPluginManager().callEvent(new BattleStartEvent(battle, bannerPlacer));

            }, runnable -> SCHEDULER.runTask(PLUGIN, runnable))
        );
    }

    /**
     * Returns the {@link Battle} associated by the specified town name.
     * @param townName the specified town name
     */
    public static Battle getBattle(String townName) {
        return ACTIVE_BATTLES.getOrDefault(townName, null);
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map.
     * @param battle the specified {@link Battle}
     */
    public static void removeBattle(Battle battle) {
        removeBattle(battle.getContestedTown());
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map and the database.
     * @param battle the specified {@link Battle}
     * */
    public void removeBattleAndDB(Battle battle) {
        removeBattle(battle);
        DATABASE.deleteBattle(battle.getContestedTown().getName());
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map.
     * @param town the specified {@link Battle}'s contested town.
     */
    public static void removeBattle(Town town) {
        removeBattle(town.getName());
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map.
     * @param townName the specified {@link Battle}'s contested town's name.
     */
    public static void removeBattle(String townName) {
        ACTIVE_BATTLES.remove(townName);
    }

    /**
     * Removes the boss bar of every {@link Battle} to prevent duplicates.
     */
    public static void deleteBossBars() {
        for (var battle : ACTIVE_BATTLES.values()) battle.deleteBossBar();

    }

    /**
     * Updates the database to store a record of this banner placement to be retrieved later.
     * @param bannerPlacerRecord the {@link BannerPlacerRecord}
     */
    public void logBannerPlacer(BannerPlacerRecord bannerPlacerRecord) {
        DATABASE.insertOrUpdate(bannerPlacerRecord);
    }

    /**
     * Returns every {@link Town} whose last banner placement happened a number of days ago that is greater than or equal to the specified number of days.
     * @param days the specified number of days
     */
    public CompletableFuture<Collection<Town>> getAllExpiredBannerPlacers(long days) {
        return DATABASE.getBannerPlacers().thenApply(bannerPlacerRecords -> {

            Collection<Town> out = new ArrayList<>();
            bannerPlacerRecords.forEach(bp -> {
                if (BattleUtil.daysSince(bp.dayOfAttack()) >= days) out.add(bp.town());
            });
            return out;
        });
    }

    /**
     * Registers to the relevant {@link Battle} that a {@link CellUnderAttack} was won by the attacker.
     * @param c the {@link CellUnderAttack} in question
     * @return whether the victory of this {@link CellUnderAttack} resulted in the end of the battle due to home block capturing.
     */
    public boolean registerAttackWon(CellUnderAttack c) {

        WorldCoord wc = new WorldCoord(c.getWorldName(), c.getX(), c.getZ());
        Battle battle = BannerWarAPI.getBattleAt(wc);

        if (battle == null) {
            PLUGIN.getLogger().warning("The flag placed by " + c.getNameOfFlagOwner() + " is flagging during a null battle!");
            return false;
        }

        WAYPOINT_MANAGER.deleteWaypoint(c.getNameOfFlagOwner());

        if (battle.getHomeBlockCoords().equals(wc)) {
            battle.loseDefense(); // check if this code works, if not, bring it back.
            return true;
        }

        else {
            battle.removeFlag(c.getNameOfFlagOwner());
            return false;
        }
    }

    /**
     * Registers to the relevant {@link Battle} that a {@link CellUnderAttack} was lost by the attacker.
     * @param c the {@link CellUnderAttack} in question
     */
    public void registerAttackLost(Cell c) {

        TownBlock tb = TownyAPI.getInstance().getTownBlock(new WorldCoord(c.getWorldName(), c.getX(), c.getZ()));

        Battle battle = BannerWarAPI.getBattleAt(tb);
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag" + c.getX() + "-" + c.getZ() + " is flagging during a null battle!");
            return;
        }

        String flagOwner = c.getAttackData().getNameOfFlagOwner();
        battle.removeFlag(flagOwner);
        WAYPOINT_MANAGER.deleteWaypoint(flagOwner);
    }

    /**
     * Registers to the relevant {@link Battle} that a {@link CellUnderAttack} has been started by the attacker.
     * @param flagBase the flag base block of the new cell
     * @param nameOfFlagOwner the name of the player who placed the flag
     */
    public void registerAttackStarted(String nameOfFlagOwner, Block flagBase) {

        Battle battle = BannerWarAPI.getBattleAt(TownyAPI.getInstance().getTownBlock(flagBase.getLocation()));
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag placed by " + nameOfFlagOwner + " is flagging during a null battle!");
            return;
        }

        var cells = FlagWar.getCellsUnderAttackByPlayer(nameOfFlagOwner);
        if (!cells.isEmpty())
            WAYPOINT_MANAGER.createWaypoint(cells.get(0));

        battle.addFlag(nameOfFlagOwner);
    }
}
