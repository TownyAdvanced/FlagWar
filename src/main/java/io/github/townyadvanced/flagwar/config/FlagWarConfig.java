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

package io.github.townyadvanced.flagwar.config;

import com.palmergames.util.TimeTools;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class FlagWarConfig {

    private FlagWarConfig() {
        super();
    }

    /** Default timer materials. */
    private static final Material[] DEFAULT_TIMER_MATERIALS = new Material[] {
        Material.LIME_WOOL, Material.GREEN_WOOL, Material.BLUE_WOOL, Material.CYAN_WOOL,
        Material.LIGHT_BLUE_WOOL, Material.GRAY_WOOL, Material.WHITE_WOOL,
        Material.PINK_WOOL, Material.ORANGE_WOOL, Material.RED_WOOL };

    /** Base material, or the war flag's post. */
    private static Material flagBaseMaterial = null;
    /** Light-emitting {@link Material}, spawned on top of the TIMER_MATERIALS of a War Flag. */
    private static Material flagLightMaterial = null;
    /** Beacon wireframe {@link Material}, forming the borders of a beacon. */
    private static Material beaconWireFrameMaterial = null;
    /** {@link Plugin} instance, used internally. */
    private static final Plugin PLUGIN = FlagWar.getInstance();
    /** Holds an instance of FlagWar's logger. */
    private static final Logger LOGGER = PLUGIN.getLogger();

    /**
     * {@link Material} array used for representing the FlagWar Flag Timer, used in both beacons and tops of War Flags.
     */
    static final Material[] TIMER_MATERIALS = isUsingDefaultTimerBlocks()
        ? DEFAULT_TIMER_MATERIALS : getCustomTimerBlocks();

    /**
     * Checks if a {@link Material} should be affected by an operation.
     * @param material simple Material to check.
     * @return True if the matched Material is used in constructing a war flag's base or light, or the wireframe; Or, if
     * the material is one of the timer materials.
     */
    public static boolean isAffectedMaterial(final Material material) {
        return Arrays.asList(TIMER_MATERIALS).contains(material)
            || material == getFlagBaseMaterial()
            || material == getFlagLightMaterial()
            || material == getBeaconWireFrameMaterial();
    }

    /**
     * Returns a copy of the {@link Material} array making up the WarFlag's timer indicators.
     * @return a clone of the Material array.
     */
    public static Material[] getTimerBlocks() {
        return Arrays.copyOf(TIMER_MATERIALS, TIMER_MATERIALS.length);
    }

    /**
     * Returns a copy of the {@link Material} array making up the WarFlag's timer indicators.
     * @return a clone of the Material array.
     */
    public static Material[] getCustomTimerBlocks() {
        List<?> blocks = PLUGIN.getConfig().getList("timer_blocks.blocks", null);
        if (blocks != null) {
            int i = blocks.size();
            var materials = new Material[i];
            try {
                for (var j = 0; j < i; j++) {
                    materials[j] = Material.valueOf(String.valueOf(blocks.get(j)).toUpperCase());
                }
                return materials;
            } catch (IllegalArgumentException e) {
                LOGGER.severe("One or more timer blocks were invalid! Using default list.");
                LOGGER.severe(e.getMessage());
                return DEFAULT_TIMER_MATERIALS;
            }
        } else {
            LOGGER.severe("Timer blocks list was null! Using default list.");
            return DEFAULT_TIMER_MATERIALS;
        }
    }

    /**
     * Check if default timer blocks are being used, as set in the configuration file.
     * @return True if timer_blocks.use_default is set to true in the configuration file.
     */
    public static boolean isUsingDefaultTimerBlocks() {
        return PLUGIN.getConfig().getBoolean("timer_blocks.use_default");
    }

    /**
     * Check if attacks are allowed or not, as set in the configuration file.
     * @return True if rules.allow_attacks is set to true in the configuration file.
     */
    public static boolean isAllowingAttacks() {
        return PLUGIN.getConfig().getBoolean("rules.allow_attacks");
    }

    /**
     * Gets the time (seconds) for flag.waiting_time in the configuration file, and stores it as ticks.
     * If null, assume 30 seconds.
     * @return the time in ticks.
     */
    public static long getFlagWaitingTime() {
        var waitingTime = PLUGIN.getConfig().getString("flag.waiting_time");
        if (waitingTime == null) {
            waitingTime = "30s";
        }
        return TimeTools.convertToTicks(
            TimeTools.getSeconds(waitingTime));
    }

    /**
     * Check if extra "debug" messages should be written to the JUL logger on the WARN level.
     * <p>
     * (Lazy way to bypass Spigot's log4j settings.)
     * @return true if configured to show debug messages.
     */
    public static boolean isDebugging() {
        return PLUGIN.getConfig().getBoolean("extra.debug");
    }

    /**
     * Gets the time between iterations though the {@link #TIMER_MATERIALS}; a fraction of {@link #getFlagWaitingTime()}
     * over the length of the array.
     * @return the temporal difference between color changes, in ticks.
     */
    public static long getTimeBetweenFlagColorChange() {
        return getFlagWaitingTime() / getTimerBlocks().length;
    }

    /**
     * Check if the beacon should be drawn.
     * @return the result of beacon.draw, from the configuration file.
     */
    public static boolean isDrawingBeacon() {
        var beaconIsDrawn = PLUGIN.getConfig().getBoolean("beacon.draw");
        Messaging.debug("(Config) Should beacons be drawn: %s", new Object[] {beaconIsDrawn});
        return beaconIsDrawn;
    }

    /**
     * Gets the maximum amount of active flags a player can have in play.
     * @return the result of player_limits.max_active_flags_per_player, from the configuration file.
     */
    public static int getMaxActiveFlagsPerPerson() {
        return PLUGIN.getConfig().getInt("player_limits.max_active_flags_per_player");
    }

    /** @return the value stored in {@link #flagBaseMaterial}. */
    public static Material getFlagBaseMaterial() {
        return flagBaseMaterial;
    }

    /** @return the value stored in {@link #flagLightMaterial}. */
    public static Material getFlagLightMaterial() {
        return flagLightMaterial;
    }

    /** @return the value stored in {@link #beaconWireFrameMaterial}. */
    public static Material getBeaconWireFrameMaterial() {
        return beaconWireFrameMaterial;
    }

    /** @return the beacon radius as an integer, defined in the configuration file at the beacon.radius key. */
    public static int getBeaconRadius() {
        return PLUGIN.getConfig().getInt("beacon.radius");
    }

    /** @return the beacon size as an integer, calculated as '(r * 2) - 1'. */
    public static int getBeaconSize() {
        return getBeaconRadius() * 2 - 1;
    }

    /** @return the beacon's minimum y-value above the flag, as defined by the 'beacon.height_above_flag.min' key. */
    public static int getBeaconMinHeightAboveFlag() {
        return PLUGIN.getConfig().getInt("beacon.height_above_flag.min");
    }

    /** @return the value of 'rules.time_to_wait_after_flag' as a long (ticks). */
    public static long getTimeToWaitAfterFlagged() {
        return PLUGIN.getConfig().getLong("rules.time_to_wait_after_flagged");
    }

    /** @return the value of 'rules.prevent_interaction_while_flagged.town'. */
    public static boolean isFlaggedInteractionTown() {
        return PLUGIN.getConfig().getBoolean("rules.prevent_interaction_while_flagged.town");
    }

    /** @return the value of 'rules.prevent_interaction_while_flagged.nation'. */
    public static boolean isFlaggedInteractionNation() {
        return PLUGIN.getConfig().getBoolean("rules.prevent_interaction_while_flagged.nation");
    }

    /** @return the value of 'beacon.height_above_flag.max'. */
    public static int getBeaconMaxHeightAboveFlag() {
        return PLUGIN.getConfig().getInt("beacon.height_above_flag.max");
    }

    /**
     * Sets the {@link #flagBaseMaterial} to the supplied {@link Material}, effectively overriding the default flag.
     * @param flagBaseMat supplied Material to override the flagBaseMaterial with.
     */
    public static void setFlagBaseMaterial(final Material flagBaseMat) {
        FlagWarConfig.flagBaseMaterial = flagBaseMat;
    }

    /**
     * Sets the {@link #flagLightMaterial} to the supplied {@link Material}.
     * @param flagLightMat supplied Material to override the flagLightMaterial with.
     */
    public static void setFlagLightMaterial(final Material flagLightMat) {
        FlagWarConfig.flagLightMaterial = flagLightMat;
    }

    /**
     * Sets the {@link #beaconWireFrameMaterial} to the supplied {@link Material}.
     * @param beaconWireFrameMat supplied Material to override the beaconWireFrameMaterial with.
     */
    public static void setBeaconWireFrameMaterial(final Material beaconWireFrameMat) {
        FlagWarConfig.beaconWireFrameMaterial = beaconWireFrameMat;
    }

    /** @return the value of 'player_limits.min_online_in_town'.*/
    public static int getMinPlayersOnlineInTownForWar() {
        return PLUGIN.getConfig().getInt("player_limits.min_online_in_town");
    }

    /** @return the value of 'player_limits.min_online_in_nation'. */
    public static int getMinPlayersOnlineInNationForWar() {
        return PLUGIN.getConfig().getInt("player_limits.min_online_in_nation");
    }

    /** @return the value of 'economy.town_block_captured'. */
    public static double getWonTownBlockReward() {
        return PLUGIN.getConfig().getDouble("economy.town_block_captured");
    }

    /** @return the value of 'economy.home_block_captured'. */
    public static double getWonHomeBlockReward() {
        return (PLUGIN.getConfig().getDouble("economy.home_block_captured"));
    }

    /** @return the value of 'economy.war_flag_cost'. */
    public static double getCostToPlaceWarFlag() {

        return PLUGIN.getConfig().getDouble("economy.war_flag_cost");
    }

    /** @return the value of 'economy.attack_defended_reward'. */
    public static double getDefendedAttackReward() {
        return PLUGIN.getConfig().getDouble("economy.attack_defended_reward");
    }

    /** @return the value of 'rules.only_attack_borders'. */
    public static boolean isAttackingBordersOnly() {
        return PLUGIN.getConfig().getBoolean("rules.only_attack_borders");
    }

    /** @return the value of 'rules.flag_takes_ownership_of_town_blocks'. */
    public static boolean isFlaggedTownBlockTransferred() {
        return PLUGIN.getConfig().getBoolean("rules.flag_takes_ownership_of_town_blocks");
    }
}
