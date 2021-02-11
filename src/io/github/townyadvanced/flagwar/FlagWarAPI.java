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

package io.github.townyadvanced.flagwar;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.objects.Cell;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *  FlagWar Public API
 */
public class FlagWarAPI {

    private FlagWarAPI() {
        super();
    }

    /**
     * Check if a given {@link Cell} is a {@link CellUnderAttack}
     * @param cell The given Cell
     * @return True or False
     */
    public static boolean isUnderAttack (Cell cell) {
        return FlagWar.isUnderAttack(cell);
    }

    /**
     * Check if a Town has any active flag war cells.
     * @param town Target Town to check.
     * @return True if there is a {@link CellUnderAttack} related to the given Town.
     */
    public static boolean isUnderAttack(Town town) {
        return FlagWar.isUnderAttack(town);
    }

    /**
     * Get all cells under attack.
     * @return A {@link CellUnderAttack} list containing all Cells under attack.
     */
    public static List<CellUnderAttack> getCellsUnderAttack() {
        return FlagWar.getCellsUnderAttack();
    }

    /**
     * Returns a list of cells under attack within a given Town.
     * @param town Target Town to check.
     * @return All {@link CellUnderAttack} objects related to the town.
     */
    public static List<CellUnderAttack> getCellsUnderAttack(Town town) {
        return FlagWar.getCellsUnderAttack(town);
    }

    /**
     * Retrieves a {@link CellUnderAttack} list associated with a {@link Player}
     * @param player The Player object to check against.
     * @return a CellUnderAttack list tied to a Player.
     */
    public static List<CellUnderAttack> getCellsUnderAttack(Player player) {
        return FlagWar.getCellsUnderAttackByPlayer(player.getName());
    }

    /**
     * Retrieves a {@link CellUnderAttack} list associated with a player's name.
     * @param playerName The player's name to check against.
     * @return a CellUnderAttack list tied to a given player name.
     */
    public static List<CellUnderAttack> getCellsUnderAttack(String playerName) {
        return FlagWar.getCellsUnderAttackByPlayer(playerName);
    }

    /**
     * Retrieves the number of active flags a Player has in play.
     * @param player A {@link Player}
     * @return The number of flags a Player has in play.
     */
    public static int getNumActiveFlags(Player player) {
        return FlagWar.getNumActiveFlags(player.getName());
    }

    /**
     * Gets the {@link CellUnderAttack} from a given {@link Cell}.
     * @param cell The cell to extract from.
     * @return The extracted CellUnderAttack object.
     */
    public static CellUnderAttack getAttackData(Cell cell) {
        return FlagWar.getAttackData(cell);
    }

    /**
     * Get a timestamp for when a {@link Town} was last flagged.
     * @param town The town to check.
     * @return the previous timestamp for when the town was flagged, in milliseconds.
     */
    public static long getFlaggedTimestamp(Town town) {
        return FlagWar.lastFlagged(town);
    }

    /**
     * Gets the time between when a flag and it's beacon shifts colors.
     * @return a {@link Long} value of the total time between a flag's color shift.
     */
    public long getColorShiftTime() {
        return FlagWarConfig.getTimeBetweenFlagColorChange();
    }

}
