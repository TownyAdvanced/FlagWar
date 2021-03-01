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
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.event.actions.TownyItemuseEvent;
import com.palmergames.bukkit.towny.event.actions.TownySwitchEvent;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.war.common.WarZoneConfig;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.i18n.LocaleUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WarzoneListener implements Listener {
    private final Towny towny;

    public WarzoneListener() {
        towny = Towny.getPlugin();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDestroy(TownyDestroyEvent townyDestroyEvent) {
        if (townyDestroyEvent.isInWilderness()) return;

        Player player = townyDestroyEvent.getPlayer();
        Material material = townyDestroyEvent.getMaterial();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (status == TownBlockStatus.WARZONE && FlagWarConfig.isAllowingAttacks()) {
            if (!WarZoneConfig.isEditableMaterialInWarZone(material)) {
                townyDestroyEvent.setCancelled(true);
                townyDestroyEvent.setMessage(String.format(LocaleUtil.getMessages().getString("error.warzone.cannot-edit"), "destroy", material.toString().toLowerCase()));
            }
            townyDestroyEvent.setCancelled(false);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onBuild(TownyBuildEvent townyBuildEvent) {
        if (townyBuildEvent.isInWilderness())
            return;

        Player player = townyBuildEvent.getPlayer();
        Material mat = townyBuildEvent.getMaterial();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (status == TownBlockStatus.WARZONE && FlagWarConfig.isAllowingAttacks()) {
            if (!WarZoneConfig.isEditableMaterialInWarZone(mat)) {
                townyBuildEvent.setCancelled(true);
                townyBuildEvent.setMessage(String.format(LocaleUtil.getMessages().getString("error.warzone.cannot-edit"), "build", mat.toString().toLowerCase()));
                return;
            }
            townyBuildEvent.setCancelled(false);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onItemUse(TownyItemuseEvent townyItemuseEvent) {
        if (townyItemuseEvent.isInWilderness())
            return;

        Player player = townyItemuseEvent.getPlayer();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (status == TownBlockStatus.WARZONE && FlagWarConfig.isAllowingAttacks()) {
            if (!WarZoneConfig.isAllowingItemUseInWarZone()) {
                townyItemuseEvent.setCancelled(true);
                townyItemuseEvent.setMessage(LocaleUtil.getMessages().getString("error.warzone.cannot-use-item"));
                return;
            }
            townyItemuseEvent.setCancelled(false);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onSwitchUse(TownySwitchEvent townySwitchEvent) {
        if (townySwitchEvent.isInWilderness())
            return;

        Player player = townySwitchEvent.getPlayer();
        TownBlockStatus status = towny.getCache(player).getStatus();

        if (status == TownBlockStatus.WARZONE && FlagWarConfig.isAllowingAttacks()) {
            if (!WarZoneConfig.isAllowingSwitchesInWarZone()) {
                townySwitchEvent.setCancelled(true);
                townySwitchEvent.setMessage(LocaleUtil.getMessages().getString("error.warzone.cannot-use-switch"));
                return;
            }
            townySwitchEvent.setCancelled(false);
        }
    }
}
