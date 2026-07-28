package io.github.townyadvanced.flagwar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.*;
import io.github.townyadvanced.flagwar.managers.BattleManager;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import io.github.townyadvanced.flagwar.battle_tracking.util.BattleRegionDeterminer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import town.sheepy.townyAI.TownyAI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class BannerWarAPI {
    private BannerWarAPI() {}

    /**
     * Returns whether the {@link Town} is already under a battle by the same or a different {@link Nation}.
     * @param town the specified {@link Town}
     */
    public static boolean isInBattle(Town town) {
        return BattleManager.getBattle(town.getName()) != null;
    }

    /**
     * Returns whether the {@link Town} is already under a battle by the same or a different {@link Nation}, and is not in the {@link BattleStage#DORMANT} stage.
     * @param town the specified {@link Town}
     */
    public static boolean isNotDormant(Town town) {
        Battle battle = BattleManager.getBattle(town.getName());
        if (battle == null) return false;
        return battle.getCurrentStage() != BattleStage.DORMANT;
    }

    /**
     * Returns whether the {@link Town} is already under a battle by the same or a different {@link Nation}.
     * @param townName the specified {@link Town}'s name
     */
    public static boolean isInBattle(String townName) {
        return BattleManager.getBattle(townName) != null;
    }

    /**
     * Returns the {@link Battle} object associated with this {@link Town}.
     * @param town the specified {@link Town}
     */
    public static Battle getBattle(Town town) {
        return BattleManager.getBattle(town.getName());
    }

    /**
     * Returns the {@link Battle} object that contains this {@link TownBlock} as an initial town block.
     * @param townBlock the specified {@link TownBlock}
     */
    public static Battle getBattleAt(TownBlock townBlock) {

        if (townBlock == null) return null;

        for (Battle b : BattleManager.getActiveBattles()) {
            for (TownBlock other : b.getInitialTownBlocks()) {
                int x = other.getX();
                int z = other.getZ();
                if (x == townBlock.getX() && z == townBlock.getZ()) return b;
            }
        }

        return null;
    }

    public static Battle getBattleAt(WorldCoord coord) {
        return getBattleAt(TownyAPI.getInstance().getTownBlock(coord));
    }

    /**
     * Returns the {@link Battle} object associated with the {@link Town} of this name.
     * @param townName the specified {@link Town}'s name
     */
    public static Battle getBattle(String townName) {
        return BattleManager.getBattle(townName);
    }

    /**
     * Returns the expanded regions in which a battle is tracked. Regions are derived from the claims
     * captured when the battle began, rather than the town's current claims, and are therefore stable
     * across server restarts.
     *
     * @param battle the battle whose regions to retrieve
     * @return newly-created bounding boxes covering the battle regions
     */
    public static Collection<BoundingBox> getBattleRegions(Battle battle) {
        return BattleRegionDeterminer.determineRegionFor(battle);
    }

    /**
     * Returns whether the {@link Resident} in question is part of that {@link Nation} or part of a {@link Nation} that is allied with it.
     * @param nat the {@link Nation}
     * @param res the {@link Resident}
     */
    public static boolean isAssociatedWithNation(Resident res, Nation nat) {
        if (res == null) return false;
        Town resTown = res.getTownOrNull();
        if (resTown == null) return false;
        Nation resNation = resTown.getNationOrNull();
        if (resNation == null || nat == null) return false;

        return nat.hasAlly(resNation) || resNation.equals(nat);
    }

    /**
     * Returns whether the {@link Resident} in question is part of the attacking {@link Nation} of this {@link Battle}, or part of a {@link Nation} that is allied with it.
     * @param r the {@link Resident}
     * @param battle the {@link Battle}
     */
    public static boolean isAssociatedWithAttacker(Resident r, Battle battle) {
        return isAssociatedWithNation(r, battle.getAttacker());
    }

    /**
     * Returns whether the {@link Resident} in question is part of the defending {@link Nation} of this {@link Battle}, or part of a {@link Nation} that is allied with it.
     * @param r the {@link Resident}
     * @param battle the {@link Battle}
     */
    public static boolean isAssociatedWithDefender(Resident r, Battle battle) {
        return isAssociatedWithNation(r, battle.getDefender());
    }

    /**
     * Returns whether the {@link Resident} in question is part of the defending {@link Nation} of this {@link Battle}, or part of a {@link Nation} that is allied with it.
     * @param r the {@link Resident}'s name
     * @param battle the {@link Battle}
     */
    public static boolean isAssociatedWithDefender(String r, Battle battle) {
        return isAssociatedWithDefender(TownyAPI.getInstance().getResident(r), battle);
    }

    /**
     * Returns whether the {@link Resident} in question is part of the attacking {@link Nation} of this {@link Battle}, or part of a {@link Nation} that is allied with it.
     * @param r the {@link Resident}'s name
     * @param battle the {@link Battle}
     */
    public static boolean isAssociatedWithAttacker(String r, Battle battle) {
        return isAssociatedWithAttacker(TownyAPI.getInstance().getResident(r), battle);
    }

    /**
     * Returns whether the {@link Resident} in question is part of either of the two rival {@link Nation}s of this {@link Battle}, or part of a {@link Nation} that is allied with either or both.
     * @param r the {@link Resident}
     * @param battle the {@link Battle}
     */
    public static boolean isAssociatedWithBattle(Resident r, Battle battle) {
        return isAssociatedWithAttacker(r, battle) || isAssociatedWithDefender(r, battle);
    }

    /**
     * Returns a {@link Collection} of every associated player to a {@link Battle}
     * by adding them if {@link #isAssociatedWithBattle(Resident, Battle)} returns true for them.
     * <p>
     * @param battle the battle
     */
    public static Collection<Player> getAssociatedPlayers(Battle battle) {
        Collection<Player> out = new ArrayList<>();

        for (var p : Bukkit.getOnlinePlayers()) {
            Resident r = TownyAPI.getInstance().getResident(p);
            if (isAssociatedWithBattle(r, battle)) out.add(p);
        }

        return out;
    }

    /**
     * Returns a {@link Collection} of every player that is NOT associated to a {@link Battle}
     * by adding them if they are part of {@link Bukkit#getOnlinePlayers()} and not part of {@link #getAssociatedPlayers(Battle)}.
     * <p>
     * @param battle the battle
     */
    public static Collection<Player> getNonAssociatedPlayers(Battle battle) {

        ArrayList<Player> out = new ArrayList<>(Bukkit.getOnlinePlayers());
        out.removeAll(getAssociatedPlayers(battle));

        return out;
    }

    /**
     * Returns a {@link Collection} of every associated player to a {@link Battle}
     * by adding them if {@link #isAssociatedWithBattle(Resident, Battle)} returns true for them.
     * <p>
     * This collection excludes townyAI bots.
     * @param battle the battle
     */
    public static CompletableFuture<Collection<Player>> getAssociatedNonBots(Battle battle) {
        return CompletableFuture.supplyAsync(() -> {
        Collection<Player> out = getAssociatedPlayers(battle);
            getAllBots().thenAccept(out::removeAll);
            return out;
        });
    }

    /**
     * Returns a {@link Collection} of every player that is NOT associated to a {@link Battle}
     * by adding them if they are part of {@link Bukkit#getOnlinePlayers()} and not part of {@link #getAssociatedPlayers(Battle)}.
     * <p>
     * This collection excludes townyAI bots.
     * @param battle the battle
     */
    public static CompletableFuture<Collection<Player>> getNonAssociatedNonBots(Battle battle) {

        return CompletableFuture.supplyAsync(() -> {
            Collection<Player> out = getNonAssociatedPlayers(battle);

            getAllBots().thenAccept(out::removeAll);
            return out;
        });
    }

    /**
     * Returns a {@link Collection} of every player that is a TownyAI bot.
     */
    public static CompletableFuture<Collection<Player>> getAllBots() {

        if (Bukkit.getServer().getPluginManager().getPlugin("townyAI") == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

            Collection<Player> out = new ArrayList<>();
            Collection<Resident> residents = new ArrayList<>();

            return TownyAI.getTownyAIAPI().getAllCityStatesAsync().thenApply(cityStates -> {

                for (var cityState : cityStates) {
                    Town town = TownyAPI.getInstance().getTown(cityState);

                    if (town != null)
                        residents.addAll(town.getResidents());
                }

                for (Resident res : residents) {
                    out.add(res.getPlayer());
                }

                return out;
            }
        );
    }
}
