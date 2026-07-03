package io.github.townyadvanced.flagwar.listeners;

import com.google.common.base.Objects;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.object.*;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import io.github.townyadvanced.flagwar.managers.BattleManager;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import io.github.townyadvanced.flagwar.util.Broadcasts;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.events.*;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import io.github.townyadvanced.flagwar.util.FormatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.flintstqne.townyCivics.api.TownyCivicsAPI;

public class BattleListener implements Listener {

    /** Holds the {@link BattleManager} instance. */
    private final BattleManager BATTLE_MANAGER;

    /** The error message for a minimum number of players. */
    private static final String PLAYERS_ONLINE_ERROR =
        "There must be a minimum of %s online in %s for a battle to occur!";

    public BattleListener(final BattleManager manager) {
        this.BATTLE_MANAGER = manager;
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBannerBlockPlace(TownyBuildEvent event) {
        TownBlock townBlock = event.getTownBlock();

        if (townBlock == null
            || townBlock.getTownOrNull() == null
            || !Tag.BANNERS.isTagged(event.getMaterial()))
                return;

        Resident r = TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId());
        Town town = townBlock.getTownOrNull();

        if (r == null || r.getTownOrNull() == null
            || r.getTownOrNull().getNationOrNull() == null)
                return;

        Nation defender = town.getNationOrNull();
        Nation attacker = r.getTownOrNull().getNationOrNull();
        int minOnInTown = FlagWarConfig.getMinPlayersOnlineInTownForWar();
        int minOnInNation = FlagWarConfig.getMinPlayersOnlineInNationForWar();
        int minOnInAttackerNation = FlagWarConfig.getMinAttackingPlayersOnlineInNationForWar();
        int minOnInAttackerTown = FlagWarConfig.getMinAttackingPlayersOnlineInTownForWar();

        int onlineResidents =
            town.getResidents().stream().filter(Resident::isOnline).toList().size();

        if (BannerWarAPI.isAssociatedWithNation(r, defender)) return;

        Battle battle = BannerWarAPI.getBattleAt(townBlock);

        if (!townBlock.getWorld().isWarAllowed()) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "The world is not allowed to war!");
            return;
        }

        if (!town.isAllowedToWar()) {
            Broadcasts.sendErrorMessage(event.getPlayer(),  "The town is not allowed to war!");
            return;
        }

        if (!FlagWarConfig.isAllowingAttacks()) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "This server is not configured to allow attacks!");
            return;
        }

        if (r.getTownOrNull().getTownBlocks().isEmpty()) {
            Broadcasts.sendErrorMessage(event.getPlayer(),  "You cannot attack another nation if your town has 0 claims!");
            return;
        }


        if (defender == null && battle == null) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "This town is not part of a nation!");
            return;
        }

        if (!town.hasHomeBlock() || Objects.equal(town.getHomeBlockOrNull(), null)) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "A town with no home block is a town not worth attacking!");
            return;
        }


        if (defender != null && TownyCivicsAPI.getInstance().isNationNeutral(defender)) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "You cannot attack a peaceful nation!");
            return;
        }

        if (attacker != null && attacker.isNeutral()) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "You cannot attack as a peaceful nation!");
            return;
        }

        if (battle != null) {

            if (battle.getCurrentStage() == BattleStage.DORMANT)
                Broadcasts.sendErrorMessage(event.getPlayer(),  "This town has recently been in a battle! " +
                    "You can attack it again in " + FormatUtil.getFormattedTime(battle.getTimeRemainingForCurrentStage()) + ".");
            else
                Broadcasts.sendErrorMessage(event.getPlayer(),  "This town is already under a battle!");

            return;
        }

        if (attacker != null && !attacker.hasEnemy(defender)) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "You are not enemies with this nation!");
            return;
        }

        if (onlineResidents < minOnInTown) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInTown), town.getName()));
            return;
        }

        if (onlineResidents < minOnInNation) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInNation), defender.getName()));
            return;
        }

        if (onlineResidents < minOnInAttackerNation) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInAttackerNation), attacker));
            return;
        }

        if (onlineResidents < minOnInAttackerTown) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInAttackerTown), r.getTownOrNull().getName()));
            return;
        }

        event.setCancelled(false);
        BATTLE_MANAGER.startBattle(town, attacker, defender, r.getTownOrNull());
    }

    @EventHandler
    public void onBattleStart(BattleStartEvent event) {
        Battle battle = event.getBattle();
        Town contestedTown = battle.getContestedTown();
        Nation defender = battle.getDefender();
        Nation attacker = battle.getAttacker();

        Broadcasts.broadcastMessage( attacker.getName() + " has initiated a battle on " + defender.getName() + " at " + contestedTown.getName() + "!");
        Broadcasts.broadcastMessage( "The battle will last " + FormatUtil.getFormattedTime(BattleUtil.getActivePeriod(battle)) + "!");
    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
        Battle battle = event.getBattle();
        String def = battle.getDefender().getName();
        String att = battle.getAttacker().getName();
        String twn = battle.getContestedTown().getName();

        String message = event.isDefenseWon() ?
            def + " has successfully defended " + twn + " from " + att + ". The defender is victorious!" :
            att + " has defeated " + def + " in battle, and reduced " + twn + " to ruins! The attacker is victorious!";

        Broadcasts.broadcastMessage(message);
    }

    @EventHandler
    public void onFlaggable(BattleFlaggableEvent event) {
        Battle battle = event.getBattle();
        Broadcasts.broadcastMessage(
            "The battle at " + battle.getContestedTown().getName()
                + " has begun its " + ChatColor.AQUA + "FLAG" + ChatColor.RESET + " state. Flags may now be placed!");
    }

     @EventHandler
     public void onPrematureEnd(BattlePrematureEndEvent event) {
        Battle battle = event.getBattle();
         Broadcasts.broadcastMessage("The battle at " + battle.getContestedTown().getName()
             + " has prematurely ended!");
     }

    @EventHandler
    public void onTownDisband(DeleteTownEvent e) {
        for (var b : BattleManager.getActiveBattles()) {
            if (b.isInactive()) continue;
            if (b.getContestedTown().getName().equals(e.getTownName())) b.prematurelyEndBattle();
        }
    }

    @EventHandler
    public void onNationDisband(DeleteNationEvent e) {
        for (var b : BattleManager.getActiveBattles()) {
            if (b.isInactive()) continue;
            if (b.getAttacker().getName().equals(e.getNationName())) b.prematurelyEndBattle();
            if (b.getDefender().getName().equals(e.getNationName())) b.prematurelyEndBattle();
        }
    }

    @EventHandler
    public void onTownBlockClaim(TownPreClaimEvent e) {
        if (BannerWarAPI.isInBattle(e.getTown()) && BannerWarAPI.isNotDormant(e.getTown())) {
            e.setCancelled(true);
            e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot claim town blocks while under battle!"));
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagStart(CellAttackEvent e) {

        var c = e.getData();
        String flagOwner = c.getNameOfFlagOwner();
        Block fbb = c.getFlagBaseBlock();

        BATTLE_MANAGER.registerAttackStarted(flagOwner, fbb);

    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagAttackWon(CellWonEvent e) {
        var c = e.getCellUnderAttack();

        if (BATTLE_MANAGER.registerAttackWon(c)) {
            e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagAttackLost(CellDefendedEvent e) {
        var c = e.getCell();
        BATTLE_MANAGER.registerAttackLost(c);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onAddFlagLife(PlayerInteractEvent e) {

        Block b = e.getClickedBlock();
        Resident adder = TownyAPI.getInstance().getResident(e.getPlayer());

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND && b != null) {

            TownBlock tb = TownyAPI.getInstance().getTownBlock(b.getLocation());
            Battle battle = BannerWarAPI.getBattleAt(tb);

            if (adder != null && tb != null && battle != null) {
                CellUnderAttack cell = battle.getCellUnderAttack(tb.getX(), tb.getZ());

                if (cell != null) {
                    Resident placer = TownyAPI.getInstance().getResident(cell.getNameOfFlagOwner());

                    if ((adder.isAlliedWith(placer) ||
                        adder.getTownOrNull().getNationOrNull().equals(placer.getTownOrNull().getNationOrNull()))) {

                        ItemStack held = e.getPlayer().getInventory().getItemInMainHand();

                        Material required = BannerWarConfig.getFlagLifePaymentItem();

                        // how much the next life costs, hence + 1.
                        int price = BannerWarConfig.getFlagLifePrice(cell.getLifeAdditions() + 1);

                        if (held.getType() == required && held.getAmount() >= price && cell.tryAddLife(e.getPlayer())) {

                            held.setAmount(held.getAmount() - price);
                            e.getPlayer().getInventory().setItemInMainHand(held);
                        }
                    }
                }
            }
        }
    }
}
