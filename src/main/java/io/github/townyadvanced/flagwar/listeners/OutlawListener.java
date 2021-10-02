/*
 * Copyright (c) 2021 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.teleport.OutlawTeleportEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.github.townyadvanced.flagwar.FlagWarAPI;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Listener} class for Outlaw-related events and balancing.
 * @author FlagCourier
 */
public class OutlawListener implements Listener {

    /**
     * Listens to the {@link OutlawTeleportEvent}, then cancels it if the appropriate conditions are met.
     * Typically, the event should only ever be fired when an outlaw would attempt to enter a town that has barred them,
     * and therefore acts similar to a border-patrol.
     * @param event {@link OutlawTeleportEvent} thrown by Towny.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onOutlawTeleport(final OutlawTeleportEvent event) {
        if (event.getOutlaw().getPlayer() == null) {
            return;
        }

        var outlaw = event.getOutlaw();
        var outlawNation = outlaw.getNationOrNull();
        var outlawLocation = event.getOutlawLocation();
        var locTownBlock = TownyAPI.getInstance().getTownBlock(outlawLocation);
        var townAtLocation = TownyAPI.getInstance().getTown(outlawLocation);
        Nation nationOfLocation;

        // Assign nationOfLocation to either the Nation of the townAtLocation, or the nearest Nation.
        if (townAtLocation != null) {
            nationOfLocation = townAtLocation.getNationOrNull();
        } else {
            // Town is null, return if not a NationZone or if the locTownBlock is null
            if (!TownyAPI.getInstance().isNationZone(outlawLocation) || locTownBlock == null) {
                return;
            }
            nationOfLocation = getClosestNation(locTownBlock);
        }

        // Ignore if we cannot get the (nearest) nation. This should never happen when this event is fired.
        if (nationOfLocation == null) {
            Messaging.debug("Outlaw Teleport Not Canceled: nationOfLocation is NULL");
            return;
        }

        // Ignore if [own|allied] nation lands. (Skipped if outlaw does not have a nation.)
        if (outlawNation != null && (outlawNation.equals(nationOfLocation) || outlawNation.hasAlly(nationOfLocation))) {
            Messaging.debug("Outlaw Teleport Not Canceled: Own / Allied Nation Area.");
            return;
        }

        // Require location to be under attack, and for appropriate outlaw association.
        if (FlagWarAPI.isUnderAttack(nationOfLocation) && nationOfLocation.getOutlaws().contains(outlaw)) {
            Messaging.send(outlaw.getPlayer(),
                Translate.fromPrefixed("message.outlaw.bypass-outlaw-teleport-event", nationOfLocation.toString()));
            Messaging.debug(String.format("Outlawed Player (%s) Teleport Canceled", outlaw.getName()));
            event.setCancelled(true);
        }
    }

    /**
     * Get the closest nation from a supplied {@link TownBlock}, or null.
     * @param townBlock The supplied TownBlock.
     * @return The closest Nation, or null if there is not a next closest town block with a nation in the same world.
     */
    @Nullable
    private Nation getClosestNation(final TownBlock townBlock) {
        var closestNatTB = townBlock.getWorld().getClosestTownblockWithNationFromCoord(townBlock.getCoord());
        Town closestTown = null;
        if (closestNatTB != null) {
            closestTown = closestNatTB.getTownOrNull();
        }
        if (closestTown == null) {
            return null;
        }
        return closestTown.getNationOrNull();
    }
}

