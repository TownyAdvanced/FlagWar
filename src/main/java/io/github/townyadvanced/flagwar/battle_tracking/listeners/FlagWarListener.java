package io.github.townyadvanced.flagwar.battle_tracking.listeners;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedBattle;
import io.github.townyadvanced.flagwar.battle_tracking.TrackedBattleManager;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.FlagStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.BattleStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.events.*;
import io.github.townyadvanced.flagwar.objects.Cell;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/** Listens for FlagWar and BannerWar related events, in order to know when to stop tracking battles or track flags. */
public class FlagWarListener implements Listener {

     private final Map<String, FlagOccurrence> FLAG_OCCURRENCES = new HashMap<>();
     private final TrackedBattleManager TRACKED_BATTLE_MANAGER;

     public FlagWarListener(TrackedBattleManager trackedBattleManager) {
         this.TRACKED_BATTLE_MANAGER = trackedBattleManager;
     }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBattleStart(BattleStartEvent e) {
        TRACKED_BATTLE_MANAGER.trackBattle(e.getBattle());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBattleEnd(BattleEndEvent e) {
        TRACKED_BATTLE_MANAGER.finalizeBattle(e.getBattle(),
            e.isDefenseWon() ? BattleStatus.DEFENDER_VICTORY : BattleStatus.ATTACKER_VICTORY);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBattlePrematureEnd(BattlePrematureEndEvent e) {
        TRACKED_BATTLE_MANAGER.finalizeBattle(e.getBattle(), BattleStatus.PREMATURELY_ENDED);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagPlace(CellAttackEvent e) {
        TrackedBattle battle = TRACKED_BATTLE_MANAGER.getBattleAt(e.getFlagBlock().getLocation());
        String placerName = e.getPlayer().getName();
        if (battle != null) {
            FLAG_OCCURRENCES.put(placerName, FlagOccurrence.create(placerName));
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagWon(CellWonEvent e) {
        var cell = e.getCellUnderAttack();

        TrackedBattle battle = TRACKED_BATTLE_MANAGER.getBattleAt(cell.getFlagBaseBlock().getLocation());
        if (battle != null) {
            battle.flagSuccessEvent(
                FLAG_OCCURRENCES.get(cell.getNameOfFlagOwner()).completed(FlagStatus.FLAG_SUCCESS, Strings.EMPTY)
            );
            FLAG_OCCURRENCES.remove(cell.getNameOfFlagOwner());
        }
    }


    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagDefend(CellDefendedEvent e) {
        Player player = e.getPlayer();
        Cell cell = e.getCell();
        String flagOwner = cell.getAttackData().getNameOfFlagOwner();
        TrackedBattle battle = TRACKED_BATTLE_MANAGER.getBattleAt(cell.getAttackData().getFlagBaseBlock().getLocation());
        if (battle != null) {
            battle.flagBreakEvent(
                FLAG_OCCURRENCES.get(flagOwner).completed(FlagStatus.FLAG_DEFENDED, player.getName())
            );
            FLAG_OCCURRENCES.remove(flagOwner);
        }

    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagCancel(CellAttackCanceledEvent e) {
        var cellUnderAttack = e.getCell();
        String flagOwner = cellUnderAttack.getNameOfFlagOwner();

        TrackedBattle battle = TRACKED_BATTLE_MANAGER.getBattleAt(cellUnderAttack.getFlagBaseBlock().getLocation());
        if (battle != null) {
            battle.flagCancelEvent(
                FLAG_OCCURRENCES.get(flagOwner).completed(FlagStatus.ATTACK_CANCELLED, Strings.EMPTY)
            );
            FLAG_OCCURRENCES.remove(flagOwner);
        }

    }

}
