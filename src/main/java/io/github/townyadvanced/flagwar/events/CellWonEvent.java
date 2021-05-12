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

package io.github.townyadvanced.flagwar.events;

import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CellWonEvent extends Event implements Cancellable {

    /** Stores the {@link HandlerList} for the {@link CellWonEvent}. */
    private static final HandlerList HANDLERS = new HandlerList();
    /** Stores the cancellation state of the event. */
    private boolean cancelled = false;
    /** Stores the CellUnderAttack being won. */
    private final CellUnderAttack cellUnderAttack;

    /** Return the event's {@link HandlerList}. */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /** @return {@link #HANDLERS} statically. */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Associates a {@link CellUnderAttack} with the {@link CellWonEvent} to be later parsed for information.
     * @param cellAttackData the CellUnderAttack to associate to the event.
     */
    public CellWonEvent(final CellUnderAttack cellAttackData) {
        this.cellUnderAttack = cellAttackData;
    }

    /**
     * Returns the {@link CellUnderAttack} from the event to be further parsed for data.
     * @return The CellUnderAttack from the event.
     */
    public CellUnderAttack getCellUnderAttack() {
        return cellUnderAttack;
    }

    /**
     * Check if the CellWonEvent was cancelled.
     * @return true if the event was cancelled.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the CellWonEvent as Canceled.
     * @param cancel if event should cancel.
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }
}
