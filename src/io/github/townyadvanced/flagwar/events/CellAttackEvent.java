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
import io.github.townyadvanced.flagwar.object.CellUnderAttack;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.palmergames.bukkit.towny.Towny;

public class CellAttackEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {

		return getHandlerList();
	}

	public static HandlerList getHandlerList() {

	    return handlers;
    }

	private final Towny plugin;
	private final Player player;
	private final Block flagBaseBlock;
	private boolean cancelled = false;
	private String reason = null;
	private long time;

	public CellAttackEvent(Towny plugin, Player player, Block flagBaseBlock) {
		super();
		this.plugin = plugin;
		this.player = player;
		this.flagBaseBlock = flagBaseBlock;
		this.time = FlagWarConfig.getFlagWaitingTime();
	}

    @SuppressWarnings("unused")
    public Player getPlayer() {
		return player;
	}

    @SuppressWarnings("unused")
    public Block getFlagBaseBlock() {
		return flagBaseBlock;
	}

	public CellUnderAttack getData() {
		return new CellUnderAttack(plugin, player.getName(), flagBaseBlock, time);
	}

    @SuppressWarnings("unused")
    public long getTime() {
		return time;
	}

    @SuppressWarnings("unused")
    public void setTime(long time) {
		this.time = time;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean hasReason() {
		return reason != null;
	}
}
