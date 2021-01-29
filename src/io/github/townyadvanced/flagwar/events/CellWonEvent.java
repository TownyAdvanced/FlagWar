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

package io.github.townyadvanced.flagwar.events;

import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CellWonEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;

    @Override @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

	private final CellUnderAttack cellAttackData;

	public CellWonEvent(CellUnderAttack cellAttackData) {
		super();
		this.cellAttackData = cellAttackData;
	}

	public CellUnderAttack getCellAttackData() {
		return cellAttackData;
	}

	@Override
    public boolean isCancelled() {
        return cancelled;
    }

     @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
