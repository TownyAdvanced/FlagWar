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
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.github.townyadvanced.flagwar.FlagWarAPI;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Location;
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
     * Listens to the OutlawTeleportEvent, then cancels it if the appropriate conditions are met.
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
        var townAtLocation = TownyAPI.getInstance().getTownOrNull(locTownBlock);
        Nation nationOfLocation;

        // Assign nationOfLocation to either the Nation of the townAtLocation, or the next nearest Nation.
        if (townAtLocation != null) {
            nationOfLocation = townAtLocation.getNationOrNull();
        } else {
            // Town is null, need to check if we're in a Nation Zone
            if (!isNationZone(outlawLocation) || locTownBlock == null) {
                return;
            }
            nationOfLocation = getClosestNation(locTownBlock);
        }

        // Ignore if we cannot get the (nearest) nation. This should only happen on worlds without claims.
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
            Messaging.debug("Outlawed Player Teleport Canceled");
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

    /**
     * Checks the {@link com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus} of a given {@link Location}
     * and returns TRUE if that location would be considered a NATION_ZONE.
     *
     * Self-integrates an isWilderness check, which will return false on failure.
     *
     * @param location the Location needing to be checked.
     * @return True, if the isWilderness check passes, and if the TownBlockStatus is equal to
     *         {@link com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus#NATION_ZONE}.
     */
    private boolean isNationZone(final Location location) {
        if (!TownyAPI.getInstance().isWilderness(location)) {
            return false;
        }
        var blockStatus = TownyAPI.getInstance().hasNationZone(location);
        return blockStatus.equals(PlayerCache.TownBlockStatus.NATION_ZONE);
    }
}

