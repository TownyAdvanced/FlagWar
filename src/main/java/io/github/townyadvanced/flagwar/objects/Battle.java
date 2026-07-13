package io.github.townyadvanced.flagwar.objects;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.TownRuinUtil;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.events.*;
import io.github.townyadvanced.flagwar.worldedit.WorldEditService;
import io.github.townyadvanced.flagwar.managers.BattleManager;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

/** Contains all the information required to host a BannerWar battle. */
public class Battle {
    /** Holds the {@link Nation} that initiated the battle. */
    private final Nation ATTACKER;

    /** Holds the {@link Nation} that accommodates the town at which the battle is held. */
    private final Nation DEFENDER;

    /** Holds the {@link Town} at which the battle is held. */
    private final Town CONTESTED_TOWN;

    /** Holds a {@link List} of {@link String}s of every player that has placed a {@link CellUnderAttack} relevant to this battle. */
    private final List<String> flags;

    /** Holds the {@link WorldCoord} of the critical {@link TownBlock} that, when won, ends the battle. */
    private final WorldCoord HOME_BLOCK_COORDS;

    /** Holds the {@link Duration} of every stage of this war. */
    private Map<BattleStage, Duration> STAGE_DURATIONS = new EnumMap<>(BattleStage.class);

    /** Holds the {@link BattleStage} of this battle. */
    private BattleStage stage;

    /** Holds a {@link Collection} of the {@link WorldCoord}s of all{@link TownBlock}s that belong to the town before the battle, for restoration. */
    private final Collection<WorldCoord> INITIAL_TOWN_BLOCK_COORDS;

    /** Holds the {@link Resident} who was mayor of the {@link #CONTESTED_TOWN} at the time of the attack. */
    private final Resident INITIAL_MAYOR;

    /** Holds the town spawn of the {@link #CONTESTED_TOWN} at the time of the attack. */
    private final Location INITIAL_SPAWN;

    /** Holds the outpost spawns of the {@link #CONTESTED_TOWN} at the time of the attack. */
    private final List<Location> INITIAL_OUTPOST_SPAWNS;

    /** Holds the Unix Epoch time in milliseconds at which the current {@link #stage} started. */
    private long stageStartTimeMillis;

    /** Holds whether this battle's {@link #CONTESTED_TOWN} is a City State or not. */
    private final boolean isCityState;

    /** Holds the {@link BossBar} of this battle. */
    private BossBar bossBar;

    /** Holds the {@link BattleManager} instance. */
    private final BattleManager MANAGER;

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * @param attacker the attacking nation
     * @param defender the defending nation
     * @param contestedTown the town at which the battle is held
     * @param stm the system time in milliseconds (Unix Epoch) at which the battle started
     * @param preWarBlocks the {@link List} of the {@link WorldCoord} of every {@link TownBlock} that belonged to the town before the battle
     * @param homeBlock the homeblock of the contested town
     * @param isCityState whether this battle's town is a City State or not
     * @param stage the {@link BattleStage} of the battle
     * @param initialMayor the {@link Resident} who was mayor of the {@link #CONTESTED_TOWN} at the time of the attack
     */
    private Battle(Nation attacker, Nation defender, Town contestedTown, Collection<WorldCoord> preWarBlocks, long stm, TownBlock homeBlock, Location spawn, List<Location> outpostSpawns, boolean isCityState, BattleStage stage, Resident initialMayor, BattleManager mgr) {
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.CONTESTED_TOWN = contestedTown;
        this.flags = new ArrayList<>(); // every flag is lost after a resume.
        this.INITIAL_TOWN_BLOCK_COORDS = preWarBlocks;
        this.stageStartTimeMillis = stm;
        this.isCityState = isCityState;
        this.stage = stage;
        this.HOME_BLOCK_COORDS = homeBlock.getWorldCoord();
        this.INITIAL_MAYOR = initialMayor;
        this.INITIAL_SPAWN = copyLocation(spawn);
        this.INITIAL_OUTPOST_SPAWNS = copyLocations(outpostSpawns);
        this.STAGE_DURATIONS = BattleUtil.computeStageTimes(this);
        this.MANAGER = mgr;

        createBossBar();
    }

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for starting a new battle.
     * @param attacker the attacking nation
     * @param defender the defending nation
     * @param contestedTown the town at which the battle is held
     * @param isCityState whether this battle's town is a City State or not.
     */
    public Battle(Nation attacker, Nation defender, Town contestedTown, boolean isCityState, BattleManager mgr) {
        this(attacker,
            defender,
            contestedTown,
            BattleUtil.toWorldCoords(contestedTown.getTownBlocks()),
            System.currentTimeMillis(),

            // before a battle begins, the homeblock existence is checked at the battle listener
            Objects.requireNonNull(contestedTown.getHomeBlockOrNull()),
            contestedTown.getSpawnOrNull(),
            contestedTown.getAllOutpostSpawns(),

            isCityState,
            BattleStage.PRE_FLAG,
            contestedTown.getMayor(),
            mgr
        );
        var chunks = BattleUtil.chunksFrom(getInitialTownBlocks());
        WorldEditService.copyToDisk(contestedTown, BattleUtil.boundingBoxFrom(chunks));
    }

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for resuming an existing battle.
     * @param br the {@link BattleRecord} from a database or another persistent storage
     */
    public Battle(BattleRecord br, BattleManager mgr) {
        this(!br.attacker().equals("_") ? TownyAPI.getInstance().getNation(br.attacker()) : null,
            !br.defender().equals("_") ? TownyAPI.getInstance().getNation(br.defender()) : null,
            TownyAPI.getInstance().getTown(br.contestedTown()),
            br.townBlocksCoords(),
            br.stageStartTime(),
            TownyAPI.getInstance().getTownBlock(
            new WorldCoord(Bukkit.getWorld(br.worldID()), br.homeX(), br.homeZ())),
            br.spawn(),
            br.outpostSpawns(),
            br.isCityState(),
            br.stage(),
            TownyAPI.getInstance().getResident(br.initialMayorID()),
            mgr
        );
    }

    /** Returns the attacking nation. */
    public Nation getAttacker() {
        return ATTACKER;
    }

    /** Returns the defending nation. */
    public Nation getDefender() {
        return DEFENDER;
    }

    /** Returns the home block of the town where the battle is held. */
    public TownBlock getHomeBlock() {
        return TownyAPI.getInstance().getTownBlock(HOME_BLOCK_COORDS);
    }

    /** Returns the home block {@link WorldCoord} of the town where the battle is held. */
    public WorldCoord getHomeBlockCoords() {
        return HOME_BLOCK_COORDS;
    }


    /** Returns the town where the battle is held. */
    public Town getContestedTown() {
        return CONTESTED_TOWN;
    }

    /** Returns the {@link Resident} who was mayor of the {@link #CONTESTED_TOWN} at the time of the attack. */
    public @NotNull Resident getInitialMayor() {
        return INITIAL_MAYOR;
    }

    /** Returns the town spawn that existed before this battle began, or {@code null} if the town had no spawn. */
    public Location getInitialSpawn() {
        return copyLocation(INITIAL_SPAWN);
    }

    /** Returns the outpost spawns that existed before this battle began. */
    public List<Location> getInitialOutpostSpawns() {
        return copyLocations(INITIAL_OUTPOST_SPAWNS);
    }

    /**
     * Returns the {@link Duration} of the given {@link BattleStage} for this battle, or {@code null}.
     * @param s the given {@link BattleStage}
     */
    public Duration getDuration(BattleStage s) {
        return STAGE_DURATIONS.getOrDefault(s, null);
    }

    /** Returns the {@link Collection} of {@link TownBlock}s that belonged to this {@link #CONTESTED_TOWN} before the battle. */
    public Collection<TownBlock> getInitialTownBlocks() {

        // town blocks are not reliable; for some reason some player actions mutate them such that they are no longer
        // equal to the initial town blocks, messing up battle lookups

        // the only thing that remains constant is their WorldCoord, because god knows.

        Collection<TownBlock> out = new ArrayList<>();

        for (var WC : INITIAL_TOWN_BLOCK_COORDS) {
            out.add(TownyAPI.getInstance().getTownBlock(WC));
        }

        return out;
    }

    /** Returns the {@link #INITIAL_TOWN_BLOCK_COORDS}. */
    public Collection<WorldCoord> getInitialTownBlocksAsWorldCoords() {
        return INITIAL_TOWN_BLOCK_COORDS;
    }

    /** Returns the {@link Collection} of {@link TownBlock}s that have been captured by the {@link #ATTACKER} during the battle. */
    public Collection<TownBlock> getCapturedTownBlocks() {
        Collection<TownBlock> out = getInitialTownBlocks();
        out.removeAll(getContestedTown().getTownBlocks());

        return out;
    }

    /** Returns the {@link Duration} left for the current {@link BattleStage}.
     * If this time is negative, it returns a {@link Duration} of zero seconds.
     */
    public Duration getTimeRemainingForCurrentStage() {
        long elapsedMillis = System.currentTimeMillis() - stageStartTimeMillis;
        Duration remainingTime = STAGE_DURATIONS.get(getCurrentStage()).minusMillis(elapsedMillis);
        return remainingTime.isNegative() ? Duration.ZERO : remainingTime;
    }

    /** Returns whether more time has elapsed than the duration of a {@link BattleStage} of this battle. */
    public boolean isPendingStageAdvance() {
        return getTimeRemainingForCurrentStage().isZero();
    }

    /** Returns the current {@link BattleStage} for this battle. */
    public BattleStage getCurrentStage() {
        return stage;
    }

    /** Returns the Unix Epoch start time, in milliseconds, of the current {@link #stage} of this battle. */
    public long getStageStartTime() {
        return stageStartTimeMillis;
    }

    /**
     * Sets the {@link BattleStage} for this battle and resets the {@link Battle#stageStartTimeMillis}
     * @param stage the {@link BattleStage} to be set
     */
    public void setStage(BattleStage stage) {
        stageStartTimeMillis = System.currentTimeMillis();
        this.stage = stage;
    }

    /** Returns whether this battle's {@link #CONTESTED_TOWN} is a City State or not. */
    public boolean isCityState() {
        return isCityState;
    }

    /** Adds a new flag to the list of flags.
     * @param name the name of the flag owner
     */
    public void addFlag(String name) {
        flags.add(name);
    }

    /** Removes an existing flag from the list of flags.
     * @param name the name of the flag owner
     */
    public void removeFlag(String name) {
        flags.remove(name);
    }

    /**
     * Advances the stage of this battle to the next one.
     * @param winDefense whether the battle is to be won by the {@link #DEFENDER} if the next stage ends it.
     * @return the new {@link BattleStage}
     */
    @CanIgnoreReturnValue
    public BattleStage advanceStage(boolean winDefense) {
        switch (stage) {
            case PRE_FLAG -> makeFlaggable();
            case FLAG -> { if (winDefense) winDefense(); else loseDefense(); }
            case RUINED -> unRuin();
            case DORMANT -> { setStage(BattleStage.END); MANAGER.removeBattleAndDB(this); }
            case END -> {/* do nothing; it's done. */}
        }
        return getCurrentStage();
    }

    /**
     *  The function to be called when the battle is to allow flags.
     */
    public void makeFlaggable() {
        setStage(BattleStage.FLAG);
        Bukkit.getPluginManager().callEvent(new BattleFlaggableEvent(this));
    }

    /**
     * The function to be called when a defense is lost (homeblock is won).
     */
    public void loseDefense() {
        endWarProcedures();
        ruin();
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, false));
    }

    /**
     * Returns whether this {@link Battle} is NOT in its {@link BattleStage#PRE_FLAG} or {@link BattleStage#FLAG} states.
     */
    public boolean isInactive() {
        return getCurrentStage() != BattleStage.PRE_FLAG && !isFlagging();
    }

    /**
     * Returns whether this {@link Battle} is in its {@link BattleStage#PRE_FLAG} or {@link BattleStage#FLAG} states.
     */
    public boolean isActive() {
        return !isInactive();
    }

    /**
     * The function to be called when a defense is won (time runs out before homeblock is won).
     */
    public void winDefense() {
        endWarProcedures();
        makeDormant();
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, true));
    }

    /**
     * Deletes the {@link Battle#bossBar} and sets the {@link Battle#stage} to {@link BattleStage#DORMANT}.
     * Also restores the town and fires a {@link TownRegenerationFinishEvent}.
     * This effectively ends the battle and begins the battle cooldown.
     */
    private void makeDormant() {
        setStage(BattleStage.DORMANT);
        deleteBossBar();

        WorldEditService.pasteToWorld(getContestedTown()).thenRun(() ->
            Bukkit.getPluginManager().callEvent(new TownRegenerationFinishEvent(this))
        );
    }

    public void prematurelyEndBattle() {
        deleteBossBar();
        MANAGER.removeBattleAndDB(this);
        Bukkit.getPluginManager().callEvent(new BattlePrematureEndEvent(this));
    }

    /** Puts the {@link #CONTESTED_TOWN} into a ruined state. */
    private void ruin() {
        setStage(BattleStage.RUINED);
        if (getContestedTown() != null)
            TownRuinUtil.putTownIntoRuinedState(getContestedTown());
        Bukkit.getPluginManager().callEvent(new BattleRuinEvent(this));
    }

    /** Puts the {@link #CONTESTED_TOWN} out of its ruined state and turns it {@link BattleStage#DORMANT}. */
    private void unRuin() {
        makeDormant();
        if (getContestedTown().isRuined())
            TownRuinUtil.reclaimTown(getInitialMayor(), getContestedTown());
    }

    /** Procedures to be performed at the end of a war, regardless of the result,
     * such as transferring ownership of {@link TownBlock}s back and cancelling ongoing flags. */
    private void endWarProcedures() {
        for (String n : flags) FlagWar.removeAttackerFlags(n);
        transferBlockOwnership(getContestedTown(), getInitialTownBlocks(), getHomeBlock());
    }

    /**
     * Reclaims the ownership of the specified {@link TownBlock}s
     * @param town the {@link Town}
     * @param townBlocks the {@link TownBlock}s to be reclaimed by this {@link Town}
     * @param homeBlock the new homeblock of this {@link Town}
     */
    private void transferBlockOwnership(final Town town, final Collection<TownBlock> townBlocks, final TownBlock homeBlock) {
        try {
            restorePreWarTownMetadata(town, townBlocks, homeBlock);
        } catch (Exception E) {
            // Couldn't claim it.
            TownyMessaging.sendErrorMsg(E.getMessage());
            E.printStackTrace();
        }
    }

    /**
     * Restores the townblocks, homeblock, spawn, and outpost spawns captured before the battle, then verifies and saves them.
     * @param town the {@link Town} to restore
     * @param townBlocks the pre-war {@link TownBlock}s to restore
     * @param homeBlock the pre-war homeblock to restore
     */
    private void restorePreWarTownMetadata(final Town town, final Collection<TownBlock> townBlocks, final TownBlock homeBlock) {
        Set<TownBlock> affectedTownBlocks = new HashSet<>();

        for (TownBlock townBlock : townBlocks) {
            townBlock.setTown(town);
            affectedTownBlocks.add(townBlock);
        }

        town.setHomeBlock(homeBlock);
        if (homeBlock != null) affectedTownBlocks.add(homeBlock);
        verifyHomeBlock(town, homeBlock, "after setHomeBlock");

        town.setSpawn(copyLocation(INITIAL_SPAWN));
        verifySpawnMatchesHomeBlock(town, homeBlock);

        List<Location> outpostSpawns = copyLocations(INITIAL_OUTPOST_SPAWNS);
        town.setOutpostSpawns(outpostSpawns);
        verifyOutpostSpawns(town, outpostSpawns);
        affectedTownBlocks.addAll(getTownBlocksAt(outpostSpawns));

        town.save();
        for (TownBlock townBlock : affectedTownBlocks)
            townBlock.save();

        verifyHomeBlock(town, homeBlock, "after save");
    }

    /**
     * Logs a warning if Towny is not reporting the expected homeblock.
     */
    private void verifyHomeBlock(final Town town, final TownBlock expectedHomeBlock, final String phase) {
        TownBlock actualHomeBlock = town.getHomeBlockOrNull();
        if (expectedHomeBlock == null || actualHomeBlock == null || !expectedHomeBlock.getWorldCoord().equals(actualHomeBlock.getWorldCoord()))
            FlagWar.getInstance().getLogger().warning(String.format(
                "Towny reported homeblock %s for %s %s; expected %s.",
                formatTownBlock(actualHomeBlock),
                town.getName(),
                phase,
                formatTownBlock(expectedHomeBlock)
            ));
    }

    /**
     * Logs a warning if the restored spawn is not in Towny's reported homeblock.
     */
    private void verifySpawnMatchesHomeBlock(final Town town, final TownBlock expectedHomeBlock) {
        Location spawn = town.getSpawnOrNull();
        if (spawn == null) {
            if (INITIAL_SPAWN != null)
                FlagWar.getInstance().getLogger().warning("Towny did not retain the restored spawn for " + town.getName() + ".");
            return;
        }

        WorldCoord spawnCoord = WorldCoord.parseWorldCoord(spawn);
        if (expectedHomeBlock == null || !expectedHomeBlock.getWorldCoord().equals(spawnCoord))
            FlagWar.getInstance().getLogger().warning(String.format(
                "Towny reported spawn %s for %s outside the restored homeblock %s.",
                formatLocation(spawn),
                town.getName(),
                formatTownBlock(expectedHomeBlock)
            ));
    }

    /**
     * Logs a warning if Towny's outpost list differs from the battle's pre-war snapshot.
     */
    private void verifyOutpostSpawns(final Town town, final List<Location> expectedOutpostSpawns) {
        List<Location> actualOutpostSpawns = town.getAllOutpostSpawns();
        if (!locationsMatch(expectedOutpostSpawns, actualOutpostSpawns))
            FlagWar.getInstance().getLogger().warning(String.format(
                "Towny reported outpost spawns %s for %s; expected %s.",
                formatLocations(actualOutpostSpawns),
                town.getName(),
                formatLocations(expectedOutpostSpawns)
            ));
    }

    /**
     * Returns the {@link TownBlock}s that contain the given locations.
     */
    private Collection<TownBlock> getTownBlocksAt(final Collection<Location> locations) {
        Collection<TownBlock> townBlocks = new ArrayList<>();
        for (Location location : locations) {
            if (location == null) continue;
            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(WorldCoord.parseWorldCoord(location));
            if (townBlock != null) townBlocks.add(townBlock);
        }

        return townBlocks;
    }

    private static Location copyLocation(final Location location) {
        return location == null ? null : location.clone();
    }

    private static List<Location> copyLocations(final Collection<Location> locations) {
        List<Location> out = new ArrayList<>();
        if (locations == null) return out;

        for (Location location : locations)
            out.add(copyLocation(location));

        return out;
    }

    private static boolean locationsMatch(final List<Location> expected, final List<Location> actual) {
        return normalizeLocations(expected).equals(normalizeLocations(actual));
    }

    private static List<String> normalizeLocations(final Collection<Location> locations) {
        return locations.stream().map(Battle::formatLocation).sorted().toList();
    }

    private static String formatLocations(final Collection<Location> locations) {
        return normalizeLocations(locations).toString();
    }

    private static String formatLocation(final Location location) {
        if (location == null || location.getWorld() == null) return "null";
        return String.format(
            "%s:%s:%s:%s:%s:%s",
            location.getWorld().getUID(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }

    private static String formatTownBlock(final TownBlock townBlock) {
        return townBlock == null ? "null" : townBlock.getWorldCoord().toString();
    }

    /**
     * Creates the {@link Battle#bossBar}.
     */
    private void createBossBar() {

        String bossBarMessage = "[BATTLE] %s - %s"; // probably going to make this configurable?

        bossBar = Bukkit.createBossBar(
            String.format(bossBarMessage, getContestedTown().getName(), getCurrentStage().name().toUpperCase()),
            BarColor.RED,
            BarStyle.SOLID
        );
    }

    /**
     * Updates the {@link Battle#bossBar}.
     */
    public void updateBossBar() {

        if (getCurrentStage() == BattleStage.DORMANT) return;

        String bossBarMessage = "[BATTLE] %s - %s"; // probably going to make this configurable?

        if (bossBar == null) return;

        bossBar.setProgress(1.0 - (
            (double) getTimeRemainingForCurrentStage().toSeconds() / getDuration(getCurrentStage()).toSeconds())
        );

        bossBar.setTitle(
            String.format(bossBarMessage, getContestedTown().getName(), getCurrentStage().name().toUpperCase().replace("_", " "))
        );

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Resident r = TownyAPI.getInstance().getResident(p);
            if (BannerWarAPI.isAssociatedWithBattle(r, this)) bossBar.addPlayer(p);
            else bossBar.removePlayer(p);
        }
    }

    /**
     * Deletes the {@link Battle#bossBar}.
     */
    public void deleteBossBar() {
        if (bossBar == null) return;
        bossBar.removeAll();
        bossBar = null;
    }


    /**
     * Returns the {@link CellUnderAttack} with the specified X and Z chunk coordinates.
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public CellUnderAttack getCellUnderAttack(int x, int z) {

        if (flags.isEmpty()) return null;

        for (var name : flags) {

            var cuas = FlagWar.getCellsUnderAttackByPlayer(name);

            if (!cuas.isEmpty()) {
                CellUnderAttack cua = cuas.get(0); // there is only one flag per player.

                if (cua.getX() == x && cua.getZ() == z)
                    return cua;
            }
        }

        return null;
    }

    /**
     * Gets every flag's flag owner associated with this {@link Battle},
     * where the {@link CellUnderAttack} can be looked up using {@link FlagWar#getCellsUnderAttackByPlayer(String)}.
     */
    public Collection<String> getFlagOwners() {
        return flags;
    }

    /** Returns whether the {@link #stage} of this battle is equal to the {@link BattleStage#FLAG} stage.*/
    public boolean isFlagging() {
        return getCurrentStage() == BattleStage.FLAG;
    }
}
