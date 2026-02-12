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

import com.palmergames.bukkit.util.Colors;
import com.palmergames.util.TimeTools;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NonNls;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Transitional class holding values from the configuration file, as well as defining some defaults.
 */
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
    /** Set of EditableMaterials which can be built/destroyed in attacked cells. */
    private static Set<Material> editableMaterialsInWarZone = null;
    /** Index to know which hologram line contains the timer string. */
    private static int hologramTimerLineIndex;
    /** {@link Plugin} instance, used internally. */
    private static final Plugin PLUGIN = FlagWar.getInstance();
    /** Holds an instance of FlagWar's logger. */
    private static final Logger LOGGER = PLUGIN.getLogger();

    /**
     * {@link Material} array used for representing the FlagWar Flag Timer, used in both beacons and tops of War Flags.
     */
    static final Material[] TIMER_MATERIALS = isUsingDefaultTimerBlocks()
        ? DEFAULT_TIMER_MATERIALS : getCustomTimerBlocks();

    /** Holds the result of {@link #isHologramConfigured()}. */
    private static boolean isHologramEnabled = isHologramConfigured();

    /**
     * Holds the hologram settings. First checks {@link #isHologramEnabled()}, and if true retrieves the
     * hologram settings via {@link #getHologramConfig()}.
     */
    static final List<Map.Entry<String, String>> HOLOGRAM_SETTINGS = isHologramEnabled()
        ? getHologramConfig() : null;

    /** Holds whether a valid hologram timer line is supplied in the config. */
    private static boolean hasTimerLine;

    /** Holds the text of the hologram timer line, if it exists. */
    private static String timerText;

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
     * Query the accuracy level for coordinate-related broadcasts.
     * @return Output of rules.flag_broadcast_accuracy.
     */
    public static String getBroadcastAccuracy() {
        return PLUGIN.getConfig().getString("rules.flag_broadcast_accuracy", "towny");
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
                return DEFAULT_TIMER_MATERIALS.clone();
            }
        } else {
            LOGGER.severe("Timer blocks list was null! Using default list.");
            return DEFAULT_TIMER_MATERIALS.clone();
        }
    }

    /**
     * @return The configuration value for the depth, below a world's sea level, a flag may be placed.
     */
    public static int getDepthAllowance() {
        return PLUGIN.getConfig().getInt("rules.flag_depth_allowance");
    }

    /**
     * Sets the editableMaterialsInWarZone.
     */
    @NonNls
    public static void setEditableMaterials() {
        Set<Material> allowedMaterials = new HashSet<>();
        String editableMaterials = PLUGIN.getConfig().getString("warzone.editable_materials");
        if (editableMaterials == null || editableMaterials.isEmpty()) {
            editableMaterialsInWarZone = allowedMaterials;
            return;
        }
        String[] matArray = editableMaterials.split(",");
        List<String> list = Arrays.stream(matArray).toList();
        for (String material : list) {
            if (material.equals("*")) {
                allowedMaterials.addAll(Arrays.asList(Material.values()));
            } else if (material.startsWith("-")) {
                allowedMaterials.remove(Material.matchMaterial(material));
            } else {
                allowedMaterials.add(Material.matchMaterial(material));
            }
        }
        editableMaterialsInWarZone = allowedMaterials;
    }

    /**
     * Check if default timer blocks are being used, as set in the configuration file.
     * @return True if timer_blocks.use_default is set to true in the configuration file.
     */
    public static boolean isUsingDefaultTimerBlocks() {
        return PLUGIN.getConfig().getBoolean("timer_blocks.use_default");
    }

    /**
     * Check if holograms are enabled in the config, and if a provider is present.
     * If holograms are enabled, but a provider is missing, log an error and return false.
     * @return True if both conditions are true.
     */
    private static boolean isHologramConfigured() {
        if (PLUGIN.getConfig().getBoolean("holograms.enabled")) {
            if (PLUGIN.getServer().getPluginManager().isPluginEnabled("DecentHolograms")) {
                return true;
            } else {
                LOGGER.severe("Could not find a hologram provider. Holograms will be disabled.");
                return false;
            }
        } else {
            return false;
        }
    }

    /** @return {@link #isHologramEnabled}, the cached result of {@link #isHologramConfigured()}. */
    public static boolean isHologramEnabled() {
        return isHologramEnabled;
    }

    /**
     * Retrieve the {@link ConfigurationSection} for the hologram lines. Then, instantiate an {@link ArrayList} for the
     * hologram settings. If the hologram lines section is empty, log the error, and disable holograms. Store the
     * maximum and minimum indexes. If there are indexes less than zero, ignore those indexes and log the error. Then,
     * for each supplied hologram line, pass the data through {@link Colors#translateColorCodes(String)} to parse any
     * color codes, validate the type and data, and add the line to the hologram settings. If type or data are invalid,
     * log the error, and set the line to an empty one. Finally, return the parsed hologram settings as a {@link List}
     * of {@link Map.Entry}'s containing the type and data of each line.
     * @return Hologram settings {@link List}.
     */
    public static List<Map.Entry<String, String>> getHologramConfig() {
        ConfigurationSection holoLines = PLUGIN.getConfig().getConfigurationSection("holograms.lines");
        List<Map.Entry<String, String>> holoSettings = new ArrayList<>();
        if (holoLines == null || holoLines.getKeys(false).isEmpty()) {
            LOGGER.severe("Hologram line settings not found!");
            LOGGER.severe("Disabling holograms.");
            isHologramEnabled = false;
            return Collections.emptyList();
        }
        int maxIdx = holoLines.getKeys(false)
            .stream().mapToInt(Integer::valueOf).max().orElse(-1);
        int minIdx = holoLines.getKeys(false)
            .stream().mapToInt(Integer::valueOf).min().orElse(-1);
        if (minIdx < 0) {
            LOGGER.severe("Hologram line indexes cannot be less than zero!");
            LOGGER.severe("Ignoring invalid lines.");
        }
        populateHolo(holoLines, holoSettings, maxIdx);
        return holoSettings;
    }

    private static void populateHolo(final ConfigurationSection hLines,
                                     final List<Map.Entry<String, String>> holoSettings, final int maxIndex) {
        for (int index = 0; index < maxIndex + 1; index++) {
            String lineType = hLines.getString(index + ".type", "empty");
            String data = Colors.translateColorCodes(hLines.getString(index + ".data", "null"));
            switch (lineType.toLowerCase()) {
                case "item" -> addHoloItem(index, data, holoSettings);
                case "text" -> addHoloText(index, data, holoSettings);
                case "timer" -> {
                    addHoloTimer(index, data, holoSettings);
                    hologramTimerLineIndex = index;
                }
                case "empty" -> {
                    final var nEmpty = index;
                    LOGGER.severe(() -> String.format("Missing hologram line type for line %s!", nEmpty));
                    setEmpty(holoSettings, index);
                }
                default -> {
                    final var nDef = index;
                    LOGGER.severe(() -> String.format("Invalid hologram line type %s for line %s!", lineType, nDef));
                    setEmpty(holoSettings, index);
                }
            }
        }
    }

    @NonNls
    private static void addHoloItem(final int index, final String data,
                                    final List<Map.Entry<String, String>> holoSettings) {
        if (Material.matchMaterial(data) == null) {
            LOGGER.severe(() -> String.format("Invalid hologram material %s for line %s!", data, index));
            setEmpty(holoSettings, index);
        } else {
            holoSettings.add(new AbstractMap.SimpleEntry<>("item", data));
        }
    }

    @NonNls
    private static void addHoloText(final int index, final String data,
                                    final List<Map.Entry<String, String>> holoSettings) {
        if (data.equals("null")) {
            LOGGER.severe(() -> String.format("Missing hologram text for line %s!", index));
            setEmpty(holoSettings, index);
        } else {
            holoSettings.add(new AbstractMap.SimpleEntry<>("text", data));
        }
    }

    @NonNls
    private static void addHoloTimer(final int index, final String data,
                                     final List<Map.Entry<String, String>> holoSettings) {
        if (!data.contains("%")) {
            LOGGER.severe(() -> String.format("Missing time placeholder for hologram line %s!", index));
            setEmpty(holoSettings, index);
        } else {
            if (!hasTimerLine) {
                hasTimerLine = true;
                timerText = data;
                holoSettings.add(new AbstractMap.SimpleEntry<>("timer", data));
            } else {
                LOGGER.severe(() -> String.format("Duplicate timer for hologram line %s!", index));
                setEmpty(holoSettings, index);
            }
        }
    }


    /**
     * Helper function for {@link #getHologramConfig()}. If the supplied line is the first line (i == 0), log that it
     * will simply be ignored. Otherwise, log that the line will be set to empty, and add it to the hologram settings
     * list.
     * @param list The hologram settings list.
     * @param i The hologram line index being parsed, used for error logging.
     */
    private static void setEmpty(final List<Map.Entry<String, String>> list, final int i) {
        if (i != 0) {
            LOGGER.severe("Setting to empty line.");
            list.add(new AbstractMap.SimpleEntry<>("empty", "null"));
        } else {
            LOGGER.severe("It's the first line! Ignoring.");
        }
    }

    /** @return {@link #HOLOGRAM_SETTINGS}, defined by {@link #getHologramConfig()}. */
    public static List<Map.Entry<String, String>> getHologramSettings() {
        return List.copyOf(HOLOGRAM_SETTINGS);
    }

    /** @return The hologram {@link #timerText}, if it exists, defined in {@link #getHologramConfig()}. */
    public static String getTimerText() {
        return timerText;
    }

    /** @return TRUE if a timer line is defined in {@link #getHologramConfig()}. */
    public static boolean hasTimerLine() {
        return hasTimerLine;
    }

    /**
     * Check if attacks are allowed or not, as set in the configuration file.
     * @return True if rules.allow_attacks is set to true in the configuration file.
     */
    public static boolean isAllowingAttacks() {
        return PLUGIN.getConfig().getBoolean("rules.allow_attacks");
    }

    /**
     * Gets the {@link Duration} for the <code>flag.waiting_time</code> in the configuration file.
     * If null, 30 seconds is the assumed default duration.
     * @return A Duration.
     */
    public static Duration getFlagLifeTime() {
        var lifeTime = PLUGIN.getConfig().getString("flag.waiting_time");
        if (lifeTime == null) {
            lifeTime = "30s";
        }
        return Duration.ofSeconds(TimeTools.getSeconds(lifeTime));
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
     * Get the {@link Duration} of each timer-material phase in a war flag's lifecycle.
     * @return The Flag Life-time, divided by the length of {@link #getTimerBlocks()}.
     */
    public static Duration getFlagPhasesDuration() {
        return getFlagLifeTime().dividedBy(getTimerBlocks().length);
    }

    /**
     * Check if the beacon should be drawn.
     * @return the result of "beacon.draw", from the configuration file.
     */
    public static boolean isDrawingBeacon() {
        var beaconIsDrawn = PLUGIN.getConfig().getBoolean("beacon.draw");
        Messaging.debug("(Config) Should beacons be drawn: %s", beaconIsDrawn);
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

    /** @return the beacon radius as an integer, defined in the configuration file at the "beacon.radius" key. */
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

    /**
     * Calculates the {@link Duration} from the value (in ms) from <code>rules.prevented_interaction_cooldown</code>.
     * @return The calculated Duration.
     */
    public static Duration getFlaggedInteractCooldown() {
        String timeString = PLUGIN.getConfig().getString("rules.prevented_interaction_cooldown");
        final long defValue = 600000;
        return Duration.ofMillis(timeString != null ? TimeTools.getMillis(timeString) : defValue);
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

    /** @return the value of 'player_limits.min_online_in_town_to_attack'.*/
    public static int getMinAttackingPlayersOnlineInTownForWar() {
        return PLUGIN.getConfig().getInt("player_limits.min_online_in_town_to_attack");
    }

    /** @return the value of 'player_limits.min_online_in_nation_to_attack'. */
    public static int getMinAttackingPlayersOnlineInNationForWar() {
        return PLUGIN.getConfig().getInt("player_limits.min_online_in_nation_to_attack");
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

    /** @return the value of 'rules.limit_attacks_based_on_neighbouring_plots'. */
    public static boolean isAttackingLimitedByNeighbouringPlots() {
        return PLUGIN.getConfig().getBoolean("rules.limit_attacks_based_on_neighbouring_plots");
    }

    /** @return the value of 'rules.only_attack_borders'. */
    public static int numberOfNeighbouringPlotsToPreventAttack() {
        return PLUGIN.getConfig().getInt("rules.neighbouring_plots_required_to_prevent_attack");
    }

    /** @return the value of 'rules.flag_takes_ownership_of_town_blocks'. */
    public static boolean isFlaggedTownBlockTransferred() {
        return PLUGIN.getConfig().getBoolean("rules.flag_takes_ownership_of_town_blocks");
    }

    /** @return the value of 'rules.flag_unclaims_townblocks'. */
    public static boolean isFlaggedTownBlockUnclaimed() {
        return PLUGIN.getConfig().getBoolean("rules.flag_unclaims_townblocks");
    }

    /** @return the value of 'warzone.explosions'. */
    public static boolean isAllowingExplosionsInWarZone() {
        return PLUGIN.getConfig().getBoolean("warzone.explosions");
    }

    /** @return the value of 'warzone.explosions_break_blocks'. */
    public static boolean isAllowingExplosionsToBreakBlocksInWarZone() {
        return PLUGIN.getConfig().getBoolean("warzone.explosions_break_blocks");
    }

    /** @return the value of 'warzone.fire'. */
    public static boolean isAllowingFireInWarZone() {
        return PLUGIN.getConfig().getBoolean("warzone.fire");
    }

    /** @return the value of 'warzone.switch'. */
    public static boolean isAllowingSwitchInWarZone() {
        return PLUGIN.getConfig().getBoolean("warzone.switch");
    }

    /** @return the value of 'warzone.item_use'. */
    public static boolean isAllowingItemUseInWarZone() {
        return PLUGIN.getConfig().getBoolean("warzone.item_use");
    }

    /**
     * @return whether this is a material which can be built or destroyed in an attacked Cell.
     * @param material the {@link Material}.
     */
    public static boolean isEditableMaterialInWarZone(final Material material) {
        return editableMaterialsInWarZone.contains(material);
    }

    /**
     * @return true when the flag is protected from the editable materials.
     */
    public static boolean isFlagAreaProtectedFromEditableMaterials() {
        return PLUGIN.getConfig().getInt("warzone.protected_area_surrounding_flag") > 0;
    }

    /**
     * @return the size of the protection area surrounding the flag.
     */
    public static int getFlagAreaProtectedSize() {
        return PLUGIN.getConfig().getInt("warzone.protected_area_surrounding_flag");
    }

    /**
     * @return the height of the protection area surrounding the flag.
     */
    public static int getFlagAreaProtectedHeight() {
        return PLUGIN.getConfig().getInt("warzone.protected_area_above_flag");
    }

    /** @return whether nations are allowed to toggle neutral.*/
    public static boolean isDeclaringNeutralAllowed() {
        return PLUGIN.getConfig().getBoolean("rules.nations_can_toggle_neutral");
    }

    /** @return the line of the hologram which contains the timer.*/
    public static int getHologramTimerLineIndex() {
        return hologramTimerLineIndex;
    }
}
