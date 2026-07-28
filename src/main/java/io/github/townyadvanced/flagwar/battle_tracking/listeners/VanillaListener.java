package io.github.townyadvanced.flagwar.battle_tracking.listeners;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedBattleManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/** Listens for events that occur in Vanilla Minecraft (killing, damaging, consuming items, throwing potions). */
public class VanillaListener implements Listener {

    private final TrackedBattleManager TRACKED_BATTLE_MANAGER;

    public VanillaListener(TrackedBattleManager trackedBattleManager) {
        this.TRACKED_BATTLE_MANAGER = trackedBattleManager;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Location loc = e.getDamager().getLocation();

        var battle = TRACKED_BATTLE_MANAGER.getBattleAt(loc);
        if (battle != null)
            battle.addDamageOccurrence(e.getDamager(), e.getEntity(), e.getFinalDamage());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKilling(PlayerDeathEvent e) {
        Player deadPlayer = e.getPlayer();
        Entity killer = deadPlayer.getKiller();
        ItemStack itemHeld = killer instanceof Player p ? p.getInventory().getItemInMainHand() : null;
        Location loc = deadPlayer.getLocation();
        EntityDamageEvent.DamageCause damageCause = deadPlayer.getLastDamageCause() != null ? deadPlayer.getLastDamageCause().getCause() : null;

        var battle = TRACKED_BATTLE_MANAGER.getBattleAt(loc);
        if (battle != null)
            battle.addKillOccurrence(killer, deadPlayer, itemHeld, damageCause);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        var battle = TRACKED_BATTLE_MANAGER.getBattleAt(p.getLocation());
        if (battle != null)
            battle.onConsume(p, item);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSplash(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        Player player = projectile.getShooter() instanceof Player p ? p : null;

        if (player != null && projectile instanceof ThrownPotion thrownPotion) {
            var battle = TRACKED_BATTLE_MANAGER.getBattleAt(projectile.getLocation());
            if (battle != null)
                battle.onPotionThrow(player, thrownPotion);
        }
    }
}
