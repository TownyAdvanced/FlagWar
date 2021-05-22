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

import io.github.townyadvanced.flagwar.FlagWarAPI;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.palmergames.bukkit.towny.Towny;
import org.jetbrains.annotations.NotNull;

public class CellAttackEvent extends Event implements Cancellable {

    /** Holds the {@link HandlerList} for the {@link CellAttackEvent}. */
    private static final HandlerList HANDLERS = new HandlerList();
    /** Holds the {@link Towny} instance. */
    private final Towny plugin;
    /** Holds the attacking {@link Player}. */
    private final Player player;
    /** Holds the base {@link Block} for the War Flag. */
    private final Block flagBlock;
    /** Stores the cancellation State of the {@link CellAttackEvent}. */
    private boolean cancelState = false;
    /** Holds the reason for the cancellation, if the cancellation state is set. Defaults to "None". */
    private String reason = "None";
    /** Holds the time of the attack, as a {@link Long} value. */
    private long time;

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
     * Constructs the {@link CellAttackEvent}.
     * @param townyInstance the instance of {@link Towny} at runtime. (Use {@link Towny#getPlugin()}.)
     * @param attacker the attacking {@link Player}.
     * @param flagBaseBlock the {@link Block} representing the War Flag base/pole.
     */
    public CellAttackEvent(final Towny townyInstance, final Player attacker, final Block flagBaseBlock) {
        super();
        this.plugin = townyInstance;
        this.player = attacker;
        this.flagBlock = flagBaseBlock;
        setTime(FlagWarAPI.getMaterialShiftTime());
    }

    /** @return the attacking {@link Player}. */
    public Player getPlayer() {
        return player;
    }

    /** @return the base {@link Block} of the War Flag. */
    public Block getFlagBlock() {
        return flagBlock;
    }

    /** @return a new {@link CellUnderAttack} with the Towny instance, attacker, flag base, and attack time stored.  */
    public CellUnderAttack getData() {
        return new CellUnderAttack(plugin, player.getName(), flagBlock, time);
    }

    /** @return the time of the attack (when the event was constructed.) */
    public long getTime() {
        return time;
    }

    /**
     * Sets the time of the attack.
     * @param timeOfAttack the time for when the attack started.
     */
    public void setTime(final long timeOfAttack) {
        this.time = timeOfAttack;
    }

    /**
     * Gets the cancellation state of this event.
     * @return true if this event is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return cancelState;
    }

    /**
     * Sets the cancellation state of this event.
     * @param cancelled true if you wish to cancel this event.
     */
    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelState = cancelled;
    }

    /** @return the reason for the cancellation. */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the cancellation reason.
     * @param cancelReason the cancellation reason.
     */
    public void setReason(final String cancelReason) {
        this.reason = cancelReason;
    }

    /** @return true if the event has a cancel reason, besides "None" (default reason). */
    public boolean hasReason() {
        return !reason.equals("None");
    }
}
