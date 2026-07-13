package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.town.TownKickEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.managers.BattleManager;
import io.github.townyadvanced.flagwar.util.CivicsUtil;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import io.github.townyadvanced.flagwar.events.BattleEndEvent;
import io.github.townyadvanced.flagwar.events.CellAttackEvent;
import io.github.townyadvanced.flagwar.events.CellDefendedEvent;
import io.github.townyadvanced.flagwar.events.CellWonEvent;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.util.Broadcasts;
import io.papermc.paper.event.player.ChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class WearinessListener implements Listener {

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link BattleManager} instance. */
    private final BattleManager BATTLE_MANAGER;

    public WearinessListener(JavaPlugin plugin, BattleManager battleManager) {
        this.PLUGIN = plugin;
        this.BATTLE_MANAGER = battleManager;
    }

    @EventHandler
    public void onCellAttack(CellAttackEvent e) {
        Battle battle = BannerWarAPI.getBattleAt(TownyAPI.getInstance().getTownBlock(e.getFlagBlock().getLocation()));
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag placed by " + e.getData().getNameOfFlagOwner() + " is flagging during a null battle!");
            return;
        }

        Resident r = TownyAPI.getInstance().getResident(e.getPlayer());

        double wearinessIncrease = BannerWarConfig.getFlagPlaceAttackerIncrease();

        CivicsUtil.increaseWeariness(r, wearinessIncrease);

    }

    @EventHandler
    public void onCellDefend(CellDefendedEvent e) {
        Player player = e.getPlayer();
        if (player == null) return;
        WorldCoord coord = new WorldCoord(player.getWorld(), e.getCell().getX(), e.getCell().getZ());

        Battle battle = BannerWarAPI.getBattleAt(TownyAPI.getInstance().getTownBlock(coord));
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag defended by " + player.getName() + " is during a null battle!");
            return;
        }

        Resident r = TownyAPI.getInstance().getResident(e.getCell().getAttackData().getNameOfFlagOwner());

        CivicsUtil.increaseWeariness(r, BannerWarConfig.getFlagDefendAttackerWeariness());


        double weariness = BannerWarConfig.getFlagDefendDefenderWeariness();

        // we don't want to risk using decreaseWeariness(initialMayor) because what if the mayor
        // left for any reason?

        if (CivicsUtil.isFederation(battle.getDefender()))
            CivicsUtil.decreaseWeariness(battle.getContestedTown(), weariness);
        else
            CivicsUtil.decreaseWeariness(battle.getDefender(), weariness);
    }


    @EventHandler
    public void onCellWon(CellWonEvent e) {

        Battle battle = BannerWarAPI.getBattleAt(TownyAPI.getInstance().getTownBlock(
            e.getCellUnderAttack().getFlagBaseBlock().getLocation())
        );

        String flagOwner = e.getCellUnderAttack().getNameOfFlagOwner();

        if (battle == null) {
            PLUGIN.getLogger().warning("The flag won by " + flagOwner + " is during a null battle!");
            return;
        }

        Resident r = TownyAPI.getInstance().getResident(flagOwner);

        CivicsUtil.increaseWeariness(r, BannerWarConfig.getFlagWinAttackerIncrease());

    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent e) {
        Battle battle = e.getBattle();

        var att = battle.getAttacker();
        var def = battle.getDefender();

        if (e.isDefenseWon()) {

            double attackerIncrease = BannerWarConfig.getDefenseWonAttackerIncrease(CivicsUtil.isAutocracy(att));
            double defenderDecrease = BannerWarConfig.getDefenseWonDefenderDecrease(CivicsUtil.isAutocracy(def));

            CivicsUtil.increaseWeariness(att, attackerIncrease);
            CivicsUtil.decreaseWeariness(def, defenderDecrease);
        }
        else {

            double attackerDecrease = BannerWarConfig.getDefenseLostAttackerDecrease(CivicsUtil.isAutocracy(att));
            double defenderIncrease = BannerWarConfig.getDefenseLostDefenderIncrease(CivicsUtil.isAutocracy(def));

            CivicsUtil.decreaseWeariness(att, attackerDecrease);
            CivicsUtil.increaseWeariness(def, defenderIncrease);
        }
    }

    @EventHandler
    public void onTownLeave(TownLeaveEvent e) {

        Town t = e.getTown();
        Nation n = t.getNationOrNull();
        double threshold = BannerWarConfig.getTownLeaveWearinessThreshold();

        if (CivicsUtil.isFederation(n)) {
            if (CivicsUtil.getWearinessAsPercentage(t) >= threshold) {
                e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot leave this town as its war weariness exceeds " + threshold + "!"));
                e.setCancelled(true);
            }
        }
        else if (CivicsUtil.getWearinessAsPercentage(n) >= threshold) {
            e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot leave this town as its nation's war weariness exceeds " + threshold + "!"));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTownKick(TownKickEvent e) {
        Town t = e.getTown();
        Nation n = t.getNationOrNull();
        double threshold = BannerWarConfig.getTownLeaveWearinessThreshold();

        if (CivicsUtil.isFederation(n)) {
            if (CivicsUtil.getWearinessAsPercentage(t) >= threshold) {
                e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot kick " + e.getKickedResident().getName() + " because your town's war weariness exceeds " + threshold + "!"));
                e.setCancelled(true);
            }
        }
        else if (CivicsUtil.getWearinessAsPercentage(n) >= threshold) {
            e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot kick " + e.getKickedResident().getName() + " because your nation's war weariness exceeds " + threshold + "!"));
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onNewTownyDay(NewDayEvent e) {

        BannerWarConfig.incrementTownyDay();
        double guaranteedDecrease = BannerWarConfig.getNewDayWearinessDecrease();
        double expiredDecrease = BannerWarConfig.getNewDayExpiredDecrease();

        // guaranteed weariness decrease.
        Collection<Nation> allNations = TownyUniverse.getInstance().getNations();
        for (Nation n : allNations) {
            if (CivicsUtil.isFederation(n)) CivicsUtil.decreaseWeariness(n, guaranteedDecrease);

            else for (var town : n.getTowns())
                    CivicsUtil.decreaseWeariness(town, guaranteedDecrease);
        }

        // expiration decrease.
        BATTLE_MANAGER.getAllExpiredBannerPlacers(BannerWarConfig.getDaysUntilBannerPlacerExpired()).thenAccept(bannerPlacers ->
            bannerPlacers.forEach(placer -> {
                if (placer.getNationOrNull() != null
                    && !CivicsUtil.isFederation(placer.getNationOrNull()))
                        CivicsUtil.decreaseWeariness(placer.getNationOrNull(), expiredDecrease);

                else CivicsUtil.decreaseWeariness(placer, expiredDecrease);
            })
        );
    }
}
