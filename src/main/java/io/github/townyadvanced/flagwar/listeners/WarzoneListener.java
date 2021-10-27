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

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyActionEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBurnEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.actions.TownyItemuseEvent;
import com.palmergames.bukkit.towny.event.actions.TownySwitchEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.war.common.WarZoneConfig;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.objects.Cell;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * WarZone Listener, ported from Towny.
 **/
public class WarzoneListener implements Listener {

    /** Holds the {@link Towny} instance. */
    private final Towny towny;

    /** Sets the {@link Towny} instance at construction. */
    public WarzoneListener() {
        towny = Towny.getPlugin();
    }

    /**
     * When the {@link TownyDestroyEvent} is fired, check if {@link TownBlockStatus#WARZONE} is accurate and that the
     * expression {@link WarZoneConfig#isEditableMaterialInWarZone(Material)} is true.
     *
     * @param townyDestroyEvent the {@link TownyDestroyEvent}.
     */
    @EventHandler
    @SuppressWarnings("unused")
    public void onDestroy(final TownyDestroyEvent townyDestroyEvent) {
        var player = townyDestroyEvent.getPlayer();
        TownBlockStatus status = towny.getCache(player).getStatus();
        var mat = townyDestroyEvent.getMaterial();

        if (isFastFailing(status, townyDestroyEvent)) {
            return;
        }

        if (!WarZoneConfig.isEditableMaterialInWarZone(mat)) {
            townyDestroyEvent.setCancelled(true);
            townyDestroyEvent.setMessage(msgCannotEdit("destroy", mat));
        }
        townyDestroyEvent.setCancelled(false);
    }

    /**
     * When the {@link TownyBuildEvent} is fired, check if {@link TownBlockStatus#WARZONE} is accurate and that the
     * expression {@link WarZoneConfig#isEditableMaterialInWarZone(Material)} is true.
     *
     * @param townyBuildEvent the {@link TownyBuildEvent}.
     */
    @EventHandler
    @SuppressWarnings("unused")
    public void onBuild(final TownyBuildEvent townyBuildEvent) {
        var player = townyBuildEvent.getPlayer();
        var mat = townyBuildEvent.getMaterial();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (isFastFailing(status, townyBuildEvent)) {
            return;
        }

        if (!WarZoneConfig.isEditableMaterialInWarZone(mat)) {
            townyBuildEvent.setCancelled(true);
            townyBuildEvent.setMessage(msgCannotEdit("build", mat));
            return;
        }
        townyBuildEvent.setCancelled(false);
    }

    /**
     * When Towny reports an item has been used: check if the TownBlock (Plot) is in a WarZone and that the item being
     * used is in the allowed items list in the WarZone section of the Towny Configuration.
     *
     * @param townyItemuseEvent the {@link TownyItemuseEvent}.
     */
    @EventHandler
    @SuppressWarnings("unused")
    public void onItemUse(final TownyItemuseEvent townyItemuseEvent) {
        var player = townyItemuseEvent.getPlayer();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (isFastFailing(status, townyItemuseEvent)) {
            return;
        }

        if (!WarZoneConfig.isAllowingItemUseInWarZone()) {
            townyItemuseEvent.setCancelled(true);
            townyItemuseEvent.setMessage(Translate.from("error.warzone.cannot-use-item"));
            return;
        }
        townyItemuseEvent.setCancelled(false);
    }

    /**
     * When Towny reports a switch has been used: check if the {@link TownBlockStatus} is a WARZONE, and that the use of
     * switches is enabled in the WarZone section of the Towny Configuration.
     *
     * @param townySwitchEvent the {@link TownySwitchEvent}.
     */
    @EventHandler
    @SuppressWarnings("unused")
    public void onSwitchUse(final TownySwitchEvent townySwitchEvent) {
        var player = townySwitchEvent.getPlayer();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (isFastFailing(status, townySwitchEvent)) {
            return;
        }

        if (!WarZoneConfig.isAllowingSwitchesInWarZone()) {
            townySwitchEvent.setCancelled(true);
            townySwitchEvent.setMessage(Translate.from("error.warzone.cannot-use-switch"));
            return;
        }
        townySwitchEvent.setCancelled(false);
    }

    /**
     * When Towny reports a burn event, check if this is a {@link TownBlockStatus} which corresponds to a Cell which
     * is under attack, and the Towny WarZoneConfig is configured to allow fires in WarZones.
     *
     * @param townyBurnEvent the {@link TownyBurnEvent}.
     */
    @EventHandler
    public void onBurn(TownyBurnEvent townyBurnEvent) {
        if (!FlagWarConfig.isAllowingAttacks()
        || townyBurnEvent.isInWilderness()
        || !WarZoneConfig.isAllowingFireInWarZone()
        || !Cell.parse(townyBurnEvent.getLocation()).isUnderAttack()) {
            return;
        }
        townyBurnEvent.setCancelled(false);
    }

    /**
     * When Towny reports an explosion damaging an entity event, check if this is a {@link TownBlockStatus} which
     * corresponds to a Cell which is under attack, and the Towny WarZoneConfig is configured to allow those
     * explosions in WarZones.
     *
     * @param townyExplosionDamagesEntityEvent the {@link TownyExplosionDamagesEntityEvent}
     */
    @EventHandler
    public void onExplosionDamagingEntity(TownyExplosionDamagesEntityEvent townyExplosionDamagesEntityEvent) {
        if (!FlagWarConfig.isAllowingAttacks()
        || townyExplosionDamagesEntityEvent.isInWilderness()
        || !WarZoneConfig.isAllowingExplosionsInWarZone()
        || !Cell.parse(townyExplosionDamagesEntityEvent.getLocation()).isUnderAttack()) {
            return;
        }
        townyExplosionDamagesEntityEvent.setCancelled(false);
    }

    /**
     * When Towny reports an explosion damaging some blocks, check if this is a {@link TownBlockStatus} which
     * corresponds to a Cell which is under attack, and the Towny WarZoneConfig is configured to allow those
     * explosions in WarZones.
     *
     * @param event the {@link TownyExplodingBlocksEvent}.
     */
    @EventHandler
    public void onExplosionDamagingBlocks(TownyExplodingBlocksEvent event) {
        if (!FlagWarConfig.isAllowingAttacks()
        || !WarZoneConfig.isAllowingExplosionsInWarZone()) {
            return;
        }
        List<Block> toAllow = new ArrayList<Block>();
        for (Block block : event.getVanillaBlockList()) {
            // Wilderness or not located inside of a Cell which is under attack, skip it.
            if (TownyAPI.getInstance().isWilderness(block)
            || !Cell.parse(block.getLocation()).isUnderAttack()) {
                continue;
            }
            // This is an allowed explosion, so add it to our War-allowed list.
            toAllow.add(block);
        }
        // Add all TownyFilteredBlocks to our list, since our list will be used.
        toAllow.addAll(event.getTownyFilteredBlockList());

        // Return the list of allowed blocks for this block explosion event.
        event.setBlockList(toAllow);
    }

    /**
     * Tell the calling method to fail fast if any of the following expressions are true:
     * <ul>
     *     <li>The {@link TownBlockStatus} is not {@link TownBlockStatus#WARZONE},</li>
     *     <li>FlagWar is not allowing attacks (effectively disabled), or</li>
     *     <li>{@link TownyActionEvent#isInWilderness()} is true.</li>
     * </ul>
     *
     * @param townBlockStatus The {@link TownBlockStatus} passed to, or established by, the original method.
     * @param townyActionEvent  The {@link TownyActionEvent} member being checked by the originating method.
     * @return True if any of the conditions are met.
     */
    private boolean isFastFailing(final TownBlockStatus townBlockStatus, final TownyActionEvent townyActionEvent) {
        return !townBlockStatus.equals(TownBlockStatus.WARZONE)
            || !FlagWarConfig.isAllowingAttacks()
            || townyActionEvent.isInWilderness();
    }

    /**
     * Helper function constructing the cancellation messages for {@link #onBuild(TownyBuildEvent)} and
     * {@link #onDestroy(TownyDestroyEvent)}.
     *
     * @param args or if the cancellation message is related to 'build' or 'destroy'
     * @param material the {@link Material} that cannot be modified.
     * @return The cancellation string for the calling method, translated from the 'error.warzone.cannot-edit' key.
     */
    private String msgCannotEdit(final String args, final Material material) {
        return Translate.fromPrefixed("error.warzone.cannot-edit", args, material.toString().toLowerCase());
    }
}
