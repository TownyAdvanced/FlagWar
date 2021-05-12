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

public class CellAttackCanceledEvent extends Event implements Cancellable {

    /** Holds the event's {@link HandlerList}. */
    private static final HandlerList HANDLERS = new HandlerList();
    /** Holds if the event state has been canceled, or not. */
    private boolean cancelled = false;
    /** Holds the {@link CellUnderAttack} being canceled. */
    private final CellUnderAttack cell;

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
     * Constructs the {@link CellAttackCanceledEvent}.
     * @param cellUnderAttack the {@link CellUnderAttack} to be canceled.
     */
    public CellAttackCanceledEvent(final CellUnderAttack cellUnderAttack) {
        super();
        this.cell = cellUnderAttack;
    }

    /** @return the {@link CellUnderAttack} being processed for cancellation. */
    public CellUnderAttack getCell() {
        return cell;
    }

    /**
     * Gets the cancellation state of this event.
     * @return true if this event is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event.
     * @param cancel true if you wish to cancel this event.
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }
}
