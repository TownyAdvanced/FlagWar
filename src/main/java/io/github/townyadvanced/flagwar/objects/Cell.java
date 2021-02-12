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

package io.github.townyadvanced.flagwar.objects;

import io.github.townyadvanced.flagwar.FlagWarAPI;
import java.util.Objects;
import org.bukkit.Location;

import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.World;

public class Cell {
	private final String worldName;
	private final int x;
	private final int z;

	public Cell(String worldName, int x, int z) {

		this.worldName = worldName;
		this.x = x;
		this.z = z;
	}

	public Cell(Cell cell) {

		this.worldName = cell.getWorldName();
		this.x = cell.getX();
		this.z = cell.getZ();
	}

	public Cell(Location location) {

		this(Cell.parse(location));
	}

	public int getX() {

		return x;
	}

    public int getZ() {

        return z;
    }

    public String getWorldName() {

        return worldName;
    }



	public static Cell parse(String worldName, int x, int z) {

		int cellSize = Coord.getCellSize();
		int xResult = x / cellSize;
		int zResult = z / cellSize;
		boolean xNeedFix = x % cellSize != 0;
		boolean zNeedFix = z % cellSize != 0;
		return new Cell(worldName, xResult - (x < 0 && xNeedFix ? 1 : 0), zResult - (z < 0 && zNeedFix ? 1 : 0));
	}

    public static Cell parse(Location loc) {
	    World world = Objects.requireNonNull(loc.getWorld());
	    return parse(world.getName(), loc.getBlockX(), loc.getBlockZ());
	}

	@Override
	public int hashCode() {

		int hash = 17;
		hash = hash * 27 + (worldName == null ? 0 : worldName.hashCode());
		hash = hash * 27 + x;
		hash = hash * 27 + z;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof Cell))
			return false;

		Cell that = (Cell) obj;
		return this.x == that.x && this.z == that.z && (Objects.equals(this.worldName, that.worldName));
	}

	public boolean isUnderAttack() {
		return FlagWarAPI.isUnderAttack(this);
	}

	public CellUnderAttack getAttackData() {
		return FlagWarAPI.getAttackData(this);
	}
}
