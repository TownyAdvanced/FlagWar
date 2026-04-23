package io.github.townyadvanced.flagwar.listeners;

import com.battria.boutique.Boutique;
import io.github.townyadvanced.flagwar.events.CellAttackEvent;
import io.github.townyadvanced.flagwar.events.CellDefendedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class BoutiqueListener implements Listener {

    @EventHandler
    public void onFlagPlace(CellAttackEvent e) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Boutique") == null) return;
        Location flagLightBlockLocation = e.getFlagLightLocation();
        Block flagLightBlock = flagLightBlockLocation.getBlock();
        Player player = e.getPlayer();

        Boutique.getAPI().getFlagTopper(player).ifPresent(flagLightBlock::setType);
        Boutique.getAPI().playFlagEffect(player, e.getFlagTimerLocation());
    }

    @EventHandler
    public void onFlagBreak(CellDefendedEvent e) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Boutique") == null) return;
        Player player = e.getPlayer();
        Location timerBlockLocation = e.getCell().getAttackData().getFlagTimerBlockLocation();

        Boutique.getAPI().playFlagEffect(player, timerBlockLocation);
    }
}
