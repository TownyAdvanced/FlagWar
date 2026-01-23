/*
 * Copyright (c) 2026 TownyAdvanced
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

package io.github.townyadvanced.flagwar.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class HologramUtil {
    /** DecentHolograms HologramLine for the timer. */
    private static HologramLine timerLineDHAPI;

    private HologramUtil() {
        // Masking "public" constructor.
    }

    /**
     * Ignition source for a hologram.
     * Retrieves hologram settings via {@link FlagWarConfig}, then will attempt to draw a hologram.
     * @param hologramName Hologram's name - Typically, the Cell String from CellUnderAttack.
     * @param location Hologram's location
     * @param flagLifeTime (Remaining) duration of the flag's timer.
     */
    public static void drawHologram(final String hologramName, final Location location, final Duration flagLifeTime) {
        List<Map.Entry<String, String>> holoSettings = FlagWarConfig.getHologramSettings();

        // DecentHolograms
        Plugin decentHolograms = FlagWar.getInstance().getServer().getPluginManager()
            .getPlugin("DecentHolograms");
        if (decentHolograms != null && decentHolograms.isEnabled()) {
            drawDecentHolograms(hologramName, location, holoSettings, flagLifeTime);
        }
    }

    /**
     * Draw Hologram using DecentHolograms ({@link DHAPI})
     * <p>
     *     <b>Process:</b>
     *     <ol>
     *         <li>Create a DecentHolograms hologram using {@link DHAPI#createHologram(String, Location)}.
     *         Use the CellString as the hologram's name.</li>
     *         <li>Set Invisible</li>
     *         <li>Add Lines</li>
     *         <li>Set offset</li>
     *         <li>Set Visible</li>
     *     </ol>
     * </p>
     * @param hologramName Name of the hologram (typically, the Cell String)
     * @param location Location to initially spawn the hologram.
     * @param holoSettings Map of 'holograms.lines' from the config.
     * @param flagLifeTime (Remaining) Duration of flag's timer.
     */
    @ApiStatus.Experimental
    public static void drawDecentHolograms(final String hologramName,
                                           final Location location,
                                           final List<Map.Entry<String, String>> holoSettings,
                                           final Duration flagLifeTime) {

        // Create Invisible
        Hologram hologram = DHAPI.createHologram(hologramName, location, false);
        hologram.setDefaultVisibleState(false);

        // Add Lines
        for (Map.Entry<String, String> holoSetting : holoSettings) {
            var type = holoSetting.getKey();
            var data = holoSetting.getValue();

            switch (type) {
                case "item" -> {
                    Material material = Material.matchMaterial(data);
                    if (material != null) {
                        DHAPI.addHologramLine(hologram, material);
                    }
                }
                case "text" -> DHAPI.addHologramLine(hologram, data);
                case "timer" -> timerLineDHAPI = DHAPI.addHologramLine(hologram, FormatUtil.time(flagLifeTime, data));
                default -> DHAPI.addHologramLine(hologram, "");
            }
        }

        //Teleport
        final double hOffset = 0.5d;
        final double vOffset = 0.9d;
        final double textHeight = 0.23d;
        hologram.setLocation(location.add(hOffset, vOffset + (hologram.getPage(0).size() * textHeight), hOffset));

        //Set Visible
        hologram.setDefaultVisibleState(true);
    }

    /**
     * Sets the Hologram's timer line text using
     * {@link FormatUtil#time(Duration, String)} with the supplied Duration and
     * {@link FlagWarConfig#getTimerText()} as the parameters.
     * @param flagLifeTime (Remaining) Duration of the flag's timer.
     */
    public static void updateHologram(final Duration flagLifeTime) {
        String formatText = FormatUtil.time(flagLifeTime, FlagWarConfig.getTimerText());
        if (timerLineDHAPI != null) {
            timerLineDHAPI.setText(formatText);
        }
    }

    /** Destroys the hologram. */
    public static void destroyHologram() {
        if (timerLineDHAPI != null) {
            timerLineDHAPI.getParent().getParent().delete();
        }
    }
}
