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

import com.palmergames.bukkit.towny.event.actions.TownyActionEvent;
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

/**
 * Listens for interactions with Blocks, then runs a check if qualified.
 * Used for flag protections and triggering CellAttackEvents.
 */
public class FlagWarBlockListener implements Listener {
    /** Retains the {@link Towny} instance, after construction.  */
    private Towny towny;

    /**
     * Constructs the FlagWarBlockListener, setting {@link #towny}.
     *
     * @param flagWar The FlagWar instance.
     */
    public FlagWarBlockListener(final FlagWar flagWar) {
        if (flagWar.getServer().getPluginManager().getPlugin("Towny") != null) {
            this.towny = Towny.getPlugin();
        }
    }

    /**
     * Check if the {@link Player} from {@link TownyBuildEvent#getPlayer()} is attempting to build inside enemy lands,
     * and if so, {@link #tryCallCellAttack(TownyActionEvent, Player, Block, WorldCoord)}.
     *
     * @param townyBuildEvent the {@link TownyBuildEvent}.
     */
    @EventHandler (priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onFlagWarFlagPlace(final TownyBuildEvent townyBuildEvent) {
        if (townyBuildEvent.getTownBlock() == null
            || !townyBuildEvent.getTownBlock().getWorld().isWarAllowed()
            || !townyBuildEvent.getTownBlock().getTownOrNull().isAllowedToWar()
            || !FlagWarConfig.isAllowingAttacks()
            || !townyBuildEvent.getMaterial().equals(FlagWarConfig.getFlagBaseMaterial())) {

            return;
        }

        var player = townyBuildEvent.getPlayer();
        var block = player.getWorld().getBlockAt(townyBuildEvent.getLocation());
        var worldCoord = new WorldCoord(block.getWorld().getName(), Coord.parseCoord(block));

        if (towny.getCache(player).getStatus().equals(TownBlockStatus.ENEMY)) {
            tryCallCellAttack(townyBuildEvent, player, block, worldCoord);
        }
    }

    /**
     * Runs {@link FlagWar#checkBlock(Player, Block, org.bukkit.event.Cancellable)} using the event's {@link Player},
     * {@link Block}, and the {@link BlockBreakEvent} itself.
     *
     * @param blockBreakEvent the {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockBreak(final BlockBreakEvent blockBreakEvent) {
        FlagWar.checkBlock(blockBreakEvent.getPlayer(), blockBreakEvent.getBlock(), blockBreakEvent);
    }

    /**
     * Runs {@link FlagWar#checkBlock(Player, Block, org.bukkit.event.Cancellable)} using the event's {@link Player},
     * {@link Block}, and the {@link BlockBurnEvent} itself.
     *
     * @param blockBurnEvent the {@link BlockBurnEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockBurn(final BlockBurnEvent blockBurnEvent) {
        FlagWar.checkBlock(null, blockBurnEvent.getBlock(), blockBurnEvent);
    }


    /**
     * Iteratively runs over {@link FlagWar#checkBlock(Player, Block, org.bukkit.event.Cancellable)} using the event's
     * {@link Player}, {@link Block} ({@link BlockPistonExtendEvent#getBlocks()}), and the
     * {@link BlockPistonExtendEvent} itself.
     *
     * @param blockPistonExtendEvent the {@link BlockPistonExtendEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockPistonExtend(final BlockPistonExtendEvent blockPistonExtendEvent) {
        for (Block block : blockPistonExtendEvent.getBlocks()) {
            FlagWar.checkBlock(null, block, blockPistonExtendEvent);
        }
    }

    /**
     * Iteratively runs over {@link FlagWar#checkBlock(Player, Block, org.bukkit.event.Cancellable)} using the event's
     * {@link Player}, {@link Block} ({@link BlockPistonRetractEvent#getBlocks()}), and the
     * {@link BlockPistonRetractEvent} itself.
     * <br/>
     * Fails fast if {@link BlockPistonRetractEvent#isSticky()} is false.
     *
     * @param blockPistonRetractEvent the {@link BlockPistonRetractEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onBlockPistonRetract(final BlockPistonRetractEvent blockPistonRetractEvent) {
        if (!blockPistonRetractEvent.isSticky()) {
            return;
        }
        for (Block block : blockPistonRetractEvent.getBlocks()) {
            FlagWar.checkBlock(null, block, blockPistonRetractEvent);
        }
    }

    /**
     * Wrapper for {@link TownyActionEvent} methods needing to run the
     * {@link FlagWar#callAttackCellEvent(Towny, Player, Block, WorldCoord)} method and, if it would return true,
     * {@link TownyActionEvent#setCancelled(boolean)} to {@link Boolean#FALSE}.
     *
     * @param event the calling {@link TownyActionEvent}
     * @param p the {@link Player}, typically result of {@link TownyActionEvent#getPlayer()}, being passed along.
     * @param b the {@link Block} being passed along.
     * @param wC the {@link WorldCoord} being passed along.
     */
    private void tryCallCellAttack(final TownyActionEvent event, final Player p, final Block b, final WorldCoord wC) {
        try {
            if (FlagWar.callAttackCellEvent(towny, p, b, wC)) {
                event.setCancelled(false);
            }
        } catch (TownyException townyException) {
            event.setCancelMessage(townyException.getMessage());
        }
    }
}
