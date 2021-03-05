/*
 * Copyright 2021 TownyAdvanced
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.github.townyadvanced.flagwar.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;

public class FlagWarBlockListener implements Listener {

    private Towny towny;

    public FlagWarBlockListener(FlagWar flagWar) {

        if (flagWar.getServer().getPluginManager().getPlugin("Towny") != null)
            this.towny = Towny.getPlugin();
    }

    @EventHandler (priority=EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onFlagWarFlagPlace(TownyBuildEvent event) {
        if (event.getTownBlock() == null)
            return;

        if (!(FlagWarConfig.isAllowingAttacks() && event.getMaterial() == FlagWarConfig.getFlagBaseMaterial()))
            return;
        Player player = event.getPlayer();
        Block block = player.getWorld().getBlockAt(event.getLocation());
        WorldCoord worldCoord = new WorldCoord(block.getWorld().getName(), Coord.parseCoord(block));

        if (towny.getCache(player).getStatus() == TownBlockStatus.ENEMY)
            try {
                if (FlagWar.callAttackCellEvent(towny, player, block, worldCoord))
                    event.setCancelled(false);
            } catch (TownyException e) {
                event.setMessage(e.getMessage());
            }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockBreak(BlockBreakEvent event) {

        FlagWar.checkBlock(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockBurn(BlockBurnEvent event) {

        FlagWar.checkBlock(null, event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

        for (Block block : event.getBlocks())
            FlagWar.checkBlock(null, block, event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {

        if (event.isSticky())
            for (Block block : event.getBlocks())
                FlagWar.checkBlock(null, block, event);
    }
}
