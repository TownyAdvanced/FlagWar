package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.DamageOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.BattleSnapshot;
import io.github.townyadvanced.flagwar.battle_tracking.util.BattleRegionDeterminer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/** A class representing all tracked events and players pertaining to a BannerWar battle. */
public class TrackedBattle {

    // very shoddily Javadoced and im sorry for that

    /** Holds the {@link Town} that this battle is concerned with. */
    private final Town TOWN;

    /** Holds the attacking {@link Nation} of this battle. */
    private final Nation ATTACKER;

    /** Holds the defending {@link Nation} of this battle. */
    private final Nation DEFENDER;

    /** Holds the Unix epoch timestamp when this battle began. */
    private final long UNIX_START_TIME;

    /** Holds a collection of bounding boxes that together make up the battle region of this battle. */
    private final Collection<BoundingBox> BATTLE_REGION;

    /** Holds a {@link Map} of all {@link TrackedPlayer}s that are being logged in this battle. */
    private final Map<UUID, TrackedPlayer> TRACKED_PLAYERS;

    /** Holds damage occurrences waiting to be persisted in the append-only damage table. */
    private final Queue<DamageOccurrence> PENDING_DAMAGE_OCCURRENCES;

    private TrackedBattle(Town town, Nation attacker, Nation defender, long startTime, Map<UUID, TrackedPlayer> trackedPlayers, Collection<DamageOccurrence> damageOccurrences) {
        this.TOWN = town;
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.UNIX_START_TIME = startTime;
        BATTLE_REGION = BattleRegionDeterminer.determineRegionFor(town);
        TRACKED_PLAYERS = trackedPlayers;
        PENDING_DAMAGE_OCCURRENCES = new ArrayDeque<>(damageOccurrences);
    }

    /**
     * Starts tracking a new BannerWar battle
     * @param town the town where the battle is taking place
     * @param attacker the attacking nation of this battle
     * @param defender the defending nation of this battle
     */
    TrackedBattle(Town town, Nation attacker, Nation defender) {
        this(
            town,
            attacker,
            defender,
            System.currentTimeMillis(),
            new HashMap<>(),
            new ArrayDeque<>()
        );
    }

    /**
     * Resumes the tracking of a BannerWar battle from a {@link BattleSnapshot}.
     * @param battleSnapshot the snapshot
     */
    TrackedBattle(BattleSnapshot battleSnapshot) {
        this(
            TownyAPI.getInstance().getTown(battleSnapshot.townName()),
            TownyAPI.getInstance().getNation(battleSnapshot.attackerNationName()),
            TownyAPI.getInstance().getNation(battleSnapshot.defenderNationName()),
            battleSnapshot.unixStartTime(),
            TrackedPlayer.fromMap(battleSnapshot.playerResultMap()),
            battleSnapshot.damageOccurrences()
        );
    }

    /** Returns whether the specified vector position is in the battle region of this battle. */
    boolean isInBattleRegion(Vector position) {
        for (BoundingBox b : BATTLE_REGION)
            if (b.contains(position)) return true;
        return false;
    }

    /** Returns the bounding boxes that make up this battle's region. */
    Collection<BoundingBox> getBattleRegion() {
        return Collections.unmodifiableCollection(BATTLE_REGION);
    }

    /** Logs a new {@link KillOccurrence} relevant to the tracked players of this battle. */
    public void addKillOccurrence(Entity killer, Player killed, ItemStack weapon, EntityDamageEvent.DamageCause cause) {
        var killOccurrence = KillOccurrence.from(killer, killed, weapon, cause);
        getTrackedPlayer(killed).incrementDeaths(killOccurrence);

        if (killer instanceof OfflinePlayer killerPlayer) {
            getTrackedPlayer(killerPlayer).incrementKills(killOccurrence);
        }
    }

    /** Logs a new {@link DamageOccurrence} relevant to the tracked players of this battle. */
    public void addDamageOccurrence(Entity hurter, Entity hurted, double damage) {
        if (hurted instanceof Player hurtedPlayer) {
            PENDING_DAMAGE_OCCURRENCES.add(DamageOccurrence.from(hurter, hurted, damage));
            getTrackedPlayer(hurtedPlayer).addDamageTaken(damage);

            if (hurter instanceof Player hurterPlayer) {
                getTrackedPlayer(hurterPlayer).addDamageDealt(damage);
            }
        }
    }

    /** Increments the number of items of that material (could be a potion) consumed for the relevant player. */
    public void onConsume(Player consumer, ItemStack item) {
        if (item.getItemMeta() instanceof PotionMeta pm)
            getTrackedPlayer(consumer).registerConsumedPotion(pm.getBasePotionData().getType());

        else getTrackedPlayer(consumer).registerConsumedItem(item.getType());
    }

    /** Increments the number of potions of that material consumed (via throwing or otherwise) for the relevant player. */
    public void onPotionThrow(@NotNull Player thrower, @NotNull ThrownPotion throwable) {
        getTrackedPlayer(thrower).registerConsumedPotion(throwable.getPotionMeta().getBasePotionData().getType());
    }

    /** Logs a {@link FlagOccurrence} relevant to the tracked players of this battle. */
    public void flagSuccessEvent(FlagOccurrence flagOccurrence) {
        registerFlag(flagOccurrence);
    }

    /** Logs a defended {@link FlagOccurrence} relevant to the tracked players of this battle. */
    public void flagBreakEvent(FlagOccurrence flagOccurrence) {
        String flagPlacer = flagOccurrence.flagPlacer();
        String flagDestroyer = flagOccurrence.flagDestroyer();

        if (flagPlacer.equals(flagDestroyer)) return; // if you broke your own flag.
        Resident rBreaker = TownyAPI.getInstance().getResident(flagDestroyer);
        Resident rFlagOwner = TownyAPI.getInstance().getResident(flagPlacer);

        if (rFlagOwner == null
            || rFlagOwner.getTownOrNull() == null
            || rFlagOwner.getTownOrNull().getNationOrNull() == null) return;

        // if you broke your ally's flag.
        if (BannerWarAPI.isAssociatedWithNation(rBreaker, rFlagOwner.getTownOrNull().getNationOrNull())) return;

        registerFlag(flagOccurrence);
    }

    /** Logs a canceled {@link FlagOccurrence} relevant to the tracked players of this battle. */
    public void flagCancelEvent(FlagOccurrence flagOccurrence) {
        registerFlag(flagOccurrence);
    }

    /** Logs a completed {@link FlagOccurrence} relevant to the tracked players of this battle. */
    public void registerFlag(FlagOccurrence flagOccurrence) {
        getTrackedPlayer(flagOccurrence.flagPlacer()).registerFlag(flagOccurrence);
        String destroyer = flagOccurrence.flagDestroyer(); // we are destroyers
        if (destroyer == null || destroyer.isEmpty()) return;
        getTrackedPlayer(destroyer).registerFlag(flagOccurrence);
    }

    /** Returns a {@link TrackedPlayer} from the specified {@link OfflinePlayer}. */
    public @NotNull TrackedPlayer getTrackedPlayer(OfflinePlayer p) {
        return TRACKED_PLAYERS.computeIfAbsent(p.getUniqueId(),
            id -> new TrackedPlayer(p, determineAffiliation(id)));
    }

    /** Returns a {@link TrackedPlayer} from the specified name. */
    public @NotNull TrackedPlayer getTrackedPlayer(String name) {
        return getTrackedPlayer(Bukkit.getOfflinePlayer(name));
    }

    /** Computes the {@link Affiliation} of the player with this UUID. */
    private Affiliation determineAffiliation(UUID id) {
        Resident resident = TownyAPI.getInstance().getResident(id);

        if (BannerWarAPI.isAssociatedWithNation(resident, ATTACKER)) return Affiliation.ATTACKER;
        if (BannerWarAPI.isAssociatedWithNation(resident, DEFENDER)) return Affiliation.DEFENDER;
        return Affiliation.VAGRANT;
    }

    /**
     * Returns the {@link #TRACKED_PLAYERS}'s value collection, where every player's
     * {@link Affiliation} is updated.
     * <p>
     * This returns an unmodifiable view of the collection to prevent undesired manipulation of its elements.
     */
    public Collection<TrackedPlayer> getTrackedPlayers() {
        TRACKED_PLAYERS.values().forEach(tp -> {
            UUID id = tp.getOfflinePlayer().getUniqueId();
            var affiliation = determineAffiliation(id);
            tp.setAffiliation(affiliation);

        });
        return Collections.unmodifiableCollection(TRACKED_PLAYERS.values());
    }

    /** Returns the Unix epoch timestamp when this battle started. */
    public long getStartTime() {
        return UNIX_START_TIME;
    }

    /** Returns the {@link Town} that this battle is concerned with. */
    public Town getTown() {
        return TOWN;
    }

    /** Returns the attacking {@link Nation} of this battle. */
    public Nation getAttacker() {
        return ATTACKER;
    }

    /** Returns the defending {@link Nation} of this battle. */
    public Nation getDefender() {
        return DEFENDER;
    }

    /**
     * Removes and returns the damage occurrences waiting to be written to persistent storage.
     * The caller must restore the returned occurrences if the write fails.
     */
    public Collection<DamageOccurrence> drainPendingDamageOccurrences() {
        Collection<DamageOccurrence> occurrences = new ArrayList<>(PENDING_DAMAGE_OCCURRENCES);
        PENDING_DAMAGE_OCCURRENCES.clear();
        return occurrences;
    }

    /** Restores damage occurrences whose persistent write did not complete. */
    public void restorePendingDamageOccurrences(Collection<DamageOccurrence> occurrences) {
        PENDING_DAMAGE_OCCURRENCES.addAll(occurrences);
    }
}
