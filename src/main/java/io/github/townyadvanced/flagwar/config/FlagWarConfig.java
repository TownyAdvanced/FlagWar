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

import io.github.townyadvanced.flagwar.FlagWar;
import com.palmergames.util.TimeTools;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public final class FlagWarConfig {

    private FlagWarConfig() {
        super();
    }

    /**
     * {@link Material} array used for representing the FlagWar Flag Timer, used in both beacons and tops of War Flags.
     */
    static final Material[] TIMER_MATERIALS = new Material[] {
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

    /**
     * Checks if a {@link Material} should be affected by an operation.
     * @param material simple Material to check.
     * @return True if the matched Material is used in constructing a war flag's base or light, or the wireframe; Or, if
     * the material is wool.
     */
    public static boolean isAffectedMaterial(final Material material) {
        return Tag.WOOL.isTagged(material)
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
        return PLUGIN.getConfig().getBoolean("beacon.draw");
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

    /** @return the beacon's minimum y-value above the flag, as defined by the 'beacon.height_above_flag_min' key. */
    public static int getBeaconMinHeightAboveFlag() {
        return PLUGIN.getConfig().getInt("beacon.height_above_flag_min");
    }

    /** @return the value of 'rules.get_time_to_wait_after_flag' as a long (ticks). */
    public static long getTimeToWaitAfterFlagged() {
        return PLUGIN.getConfig().getLong("rules.get_time_to_wait_after_flagged");
    }

    /** @return the value of 'rules.prevent_interaction_while_flagged.town'. */
    public static boolean isFlaggedInteractionTown() {
        return PLUGIN.getConfig().getBoolean("rules.prevent_interaction_while_flagged.town");
    }

    /** @return the value of 'rules.prevent_interaction_while_flagged.nation'. */
    public static boolean isFlaggedInteractionNation() {
        return PLUGIN.getConfig().getBoolean("rules.prevent_interaction_while_flagged.nation");
    }

    /** @return the value of 'beacon.height_above_flag_max'. */
    public static int getBeaconMaxHeightAboveFlag() {
        return PLUGIN.getConfig().getInt("beacon.height_above_flag_max");
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
