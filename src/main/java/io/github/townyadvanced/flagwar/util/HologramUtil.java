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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
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

    private HologramUtil() {
        // Masking "public" constructor.
    }

    /**
     * Ignition source for a hologram.
     * Retrieves hologram settings via {@link FlagWarConfig}, then will attempt to draw a hologram.
     * @param name Hologram's name - Typically, the Cell String from CellUnderAttack.
     * @param location Hologram's location
     * @param lifeTime (Remaining) duration of the flag's timer.
     */
    public static void drawHologram(final String name,
                                    final Location location,
                                    final Duration lifeTime) {
        List<Map.Entry<String, String>> settings = FlagWarConfig.getHologramSettings();

        // DecentHolograms
        Plugin decentHolograms = FlagWar.getInstance().getServer().getPluginManager()
            .getPlugin("DecentHolograms");
        if (decentHolograms != null && decentHolograms.isEnabled()) {
            drawDecentHolograms(name, location, settings, lifeTime);
        } else {
            Messaging.debug("Tried to draw a hologram (%s), but no supported hologram plugins loaded.", name);
        }
    }

    /**
     * Draw Hologram using DecentHolograms ({@link DHAPI}).
     *
     * @param name Name of the hologram (typically, the Cell String)
     * @param location Location to initially spawn the hologram.
     * @param settings Map of 'holograms.lines' from the config.
     * @param lifeTime (Remaining) Duration of flag's timer.
     */
    @ApiStatus.Experimental
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    private static void drawDecentHolograms(final String name,
                                           final Location location,
                                           final List<Map.Entry<String, String>> settings,
                                           final Duration lifeTime) {

        if (DHAPI.getHologram(name) != null) {
            Messaging.debug("Attempted to draw a pre-existing hologram at: %s", name);
            Messaging.debug("This should be destroyed and re-created, or fetched and updated.");
            return;
        }
        // Create Invisible
        Hologram hologram = DHAPI.createHologram(name, location.add(0.5, 0.82 + (0.35 * settings.size()), 0.5), false);
        hologram.setDefaultVisibleState(false);

        // Add Lines
        for (Map.Entry<String, String> holoSetting : settings) {
            String type = holoSetting.getKey();
            String data = holoSetting.getValue();

            switch (type) {
                case "item" -> {
                    Material material = Material.matchMaterial(data);
                    if (material != null) {
                        DHAPI.addHologramLine(hologram, material);
                    }
                }
                case "text" -> DHAPI.addHologramLine(hologram, data);
                case "timer" -> DHAPI.addHologramLine(hologram, FormatUtil.time(lifeTime, data));
                default -> DHAPI.addHologramLine(hologram, "");
            }
        }

        //Set Visible
        hologram.setDefaultVisibleState(true);
    }

    /**
     * Destroys a given hologram by name.
     * @param name Hologram name (Cell String)
     */
    public static void destroyHologram(final String name) {
        Plugin decentHolograms = FlagWar.getInstance().getServer().getPluginManager()
            .getPlugin("DecentHolograms");
        if (decentHolograms != null && decentHolograms.isEnabled()) {
            Hologram hologram = DHAPI.getHologram(name);
            if (hologram != null) {
                hologram.destroy();
            }
        } else {
            Messaging.debug("Tried to destroy a hologram (%s), but no supported plugins are in use.", name);
        }
    }

    /**
     * Set's the Hologram's timer line text - which is assumed to be line index 2.
     * @param name Hologram name (Cell String)
     * @param lifeTime Flag duration
     */
    public static void updateHologramTimer(final String name, final Duration lifeTime) {
        Plugin decentHolograms = FlagWar.getInstance().getServer().getPluginManager()
            .getPlugin("DecentHolograms");
        if (decentHolograms != null && decentHolograms.isEnabled()) {
            Hologram hologram = DHAPI.getHologram(name);

            if (hologram != null) {
                DHAPI.setHologramLine(hologram, FlagWarConfig.getHologramTimerLineIndex(), FormatUtil.time(lifeTime, FlagWarConfig.getTimerText()));
            }
        } else {
            Messaging.debug("Tried to update a hologram's timer (%s), but no supported plugins are in use.", name);
        }
    }
}
