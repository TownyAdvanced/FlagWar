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

import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.palmergames.bukkit.towny.Towny;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Event fired when a Towny Cell is being attacked.
 */
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
    private Duration phaseDuration;

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
        setPhaseDuration(FlagWarConfig.getFlagPhasesDuration());
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
        return new CellUnderAttack(plugin, player.getName(), flagBlock, phaseDuration);
    }

    /**
     * @return The duration of an attack phase, as a long representing milliseconds.
     * @deprecated Use {@link #getPhaseDuration()}.
     */
    @Deprecated (since = "0.5.2", forRemoval = true)
    public long getTime() {
        final int milliMultiplier = 50;
        return phaseDuration.toMillis() / milliMultiplier;
    }

    /**
     * Get the {@link Duration} of each phase of a war flag.
     * @return The Duration.
     */
    public Duration getPhaseDuration() {
        return phaseDuration;
    }

    /**
     * Sets the duration of the attack phases in ticks.
     * @param ticks the time, in ticks, for when the attack started.
     * @deprecated Use {@link #setPhaseDuration(Duration)}. Formerly, it was unclear at first glance if this method used
     * ticks or milliseconds to store the time. Use of {@link Duration} provides up-to nanosecond accuracy, and can be
     * converted to a number of ticks as-needed.
     */
    @Deprecated (since = "0.5.2", forRemoval = true)
    public void setTime(final long ticks) {
        final int milliMultiplier = 50; // 1 tick == 50ms
        setPhaseDuration(Duration.ofMillis(ticks * milliMultiplier));
    }

    /**
     * Set the {@link Duration} of each timer-material phase of a war flag.
     * @param duration The parent Duration.
     */
    public void setPhaseDuration(final Duration duration) {
        this.phaseDuration = duration;
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
