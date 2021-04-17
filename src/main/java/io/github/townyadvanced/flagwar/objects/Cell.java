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
    /** Holds the Cell's associated world's name. */
    private final String cellsWorldName;
    /** Holds the Cell's associated x coordinate value. */
    private final int xVal;
    /** Holds the Cell's associated z coordinate value. */
    private final int zVal;
    /** Holds the base value for calculating the Cell's {@link #hashCode()}. */
    private static final int HASH_BASE = 17;
    /** Holds the multiplier value for calculating the Cell's {@link #hashCode()}. */
    private static final int HASH_MULTIPLIER = 27;

    /**
     * Constructs the {@link Cell} for a given WorldName, and x/z coordinates.
     * <p>
     * See {@link Cell#parse(String, int, int)} for an example at implementation.
     *
     * @param worldName Name of the world to which the cell should be associated with.
     * @param x The 'longitudinal' (x) value, in terms of the Towny coordinates.
     * @param z The 'latitudinal' (z) value, in terms of the Towny coordinates.
     */
    public Cell(final String worldName, final int x, final int z) {
        cellsWorldName = worldName;
        xVal = x;
        zVal = z;
    }

    /**
     * Constructs the {@link Cell} based on a supplied {@link Cell} (or {@link CellUnderAttack}).
     *
     * @param cell the supplied Cell to clone.
     */
    public Cell(final Cell cell) {
        cellsWorldName = cell.getWorldName();
        xVal = cell.getX();
        zVal = cell.getZ();
    }

    /**
     * Constructs the {@link Cell} based on a supplied {@link Location}. Runs through {@link Cell#parse(Location)}.
     * @param location the Location of the Cell.
     */
    public Cell(final Location location) {
        this(Cell.parse(location));
    }

    /** @return the {@link #xVal} value of the {@link Cell}. */
    public int getX() {
        return xVal;
    }

    /** @return the {@link #zVal} of the {@link Cell}. */
    public int getZ() {
        return zVal;
    }

    /** @return the {@link #cellsWorldName} value of the {@link Cell}. */
    public String getWorldName() {
        return cellsWorldName;
    }

    /**
     * Parse raw {@link #xVal} and {@link #zVal}, as well as the world name to construct a new Cell.
     * @param worldName the name of the {@link World} the cell is found in.
     * @param x the base x value of the cell.
     * @param z the base z value of the cell.
     * @return a new Cell for the given world name, with x and z values adjusted for the appropriate Cell Size
     * ({@link Coord#getCellSize}).
     */
    public static Cell parse(final String worldName, final int x, final int z) {
        int cellSize = Coord.getCellSize();
        int xResult = x / cellSize;
        int zResult = z / cellSize;
        boolean xNeedFix = x % cellSize != 0;
        boolean zNeedFix = z % cellSize != 0;
        return new Cell(worldName, xResult - (x < 0 && xNeedFix ? 1 : 0), zResult - (z < 0 && zNeedFix ? 1 : 0));
    }

    /**
     * Gets the {@link World} from the supplied {@link Location}.
     * @param loc the supplied location.
     * @return sends the {@link World#getName()} and coordinates through {@link #parse(String, int, int)}
     */
    public static Cell parse(final Location loc) {
        World world = Objects.requireNonNull(loc.getWorld());
        return parse(world.getName(), loc.getBlockX(), loc.getBlockZ());
    }

    /** @return a hash for the {@link Cell} using the {@link #xVal}, {@link #zVal}, and {@link #cellsWorldName}. */
    @Override
    public int hashCode() {
        int hash = HASH_BASE * HASH_MULTIPLIER + (cellsWorldName == null ? 0 : cellsWorldName.hashCode());
        hash = hash * HASH_MULTIPLIER + xVal;
        hash = hash * HASH_MULTIPLIER + zVal;
        return hash;
    }

    /**
     * Determine if the supplied {@link Object} is the same as the {@link Cell}.
     * @param obj the supplied Object.
     * @return True if the Object is equivalent, or if it is an instance of a Cell with matching x, z, and world values.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Cell)) {
            return false;
        }
        Cell that = (Cell) obj;
        return xVal == that.xVal && zVal == that.zVal && (Objects.equals(this.cellsWorldName, that.cellsWorldName));
    }

    /**
     * Checks if the {@link Cell} is currently also a {@link CellUnderAttack}.
     * @return if the Cell is in the CellsUnderAttack map
     */
    public boolean isUnderAttack() {
        return FlagWarAPI.isUnderAttack(this);
    }

    /** @return a CellUnderAttack for the {@link Cell} in the cellsUnderAttack list (in the FlagWar class). */
    public CellUnderAttack getAttackData() {
        return FlagWarAPI.getAttackData(this);
    }
}
