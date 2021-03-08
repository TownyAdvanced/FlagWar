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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import io.github.townyadvanced.flagwar.FlagWar;

public class FlagWarEntityListener implements Listener {

    /** Listens for instances of the {@link EntityExplodeEvent},
     * and runs a {@link FlagWar#checkBlock(org.bukkit.entity.Player, Block, org.bukkit.event.Cancellable)}
     * for each block against a null {@link org.bukkit.entity.Player}.
     * @param event the {@link EntityExplodeEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onEntityExplode(final EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            FlagWar.checkBlock(null, block, event);
        }
    }
}
