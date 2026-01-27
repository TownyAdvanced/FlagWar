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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Handles loading of configuration files.
 */
public class ConfigLoader {

    /** {@link Plugin} instance used for basic operations, as well as associating the appropriate {@link #logger}.*/
    private final Plugin plugin;
    /** JUL Logger associated with the {@link Plugin} instance. */
    private final Logger logger;

    /**
     * Constructs the ConfigLoader, linking the {@link Plugin} instance and {@link Logger} variables.
     * @param bukkitPlugin the Plugin to link to for both it's instance and logger.
     */
    public ConfigLoader(final Plugin bukkitPlugin) {
        this.plugin = bukkitPlugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Intended to load the configuration. If the configuration version number is below the required version for safe
     * operation, attempt {@link #regenerateConfiguration(File, File)}
     * @param minConfigVer Minimum configuration version required. If not met, will attempt to back up and regenerate
     *                     configuration.
     * @throws IOException Thrown if for any reason it cannot read the config file, or create the backup file.
     * @throws InvalidConfigurationException Thrown if the configuration file is invalid.
     */
    public void loadConfig(final double minConfigVer) throws IOException, InvalidConfigurationException {
        plugin.saveDefaultConfig();
        var configFile = new File("plugins/FlagWar/config.yml");
        plugin.getConfig().load(configFile);

        if (plugin.getConfig().getDouble("config_version") < minConfigVer) {
            var backupFile = new File("plugins/FlagWar/config.old.yml");
            if (backupFile.createNewFile()) {
                logger.warning("Created new backup location: Flagwar/config.old.yml");
            }
            regenerateConfiguration(configFile, backupFile);
        }

        plugin.getConfig().load(configFile);
    }

    /**
     * Attempts to regenerate the configuration by first running {@link #backupConfig(File, File)} then generating a
     * fresh config.
     *
     * @param configFile File on disk responsible for storing configuration.
     * @param backupFile File on disk responsible for storing old configuration.
     * @throws IOException Thrown if there are issues writing either the backupFile or the configFile.
     */
    public void regenerateConfiguration(final File configFile, final File backupFile) throws IOException {
        if (backupConfig(configFile, backupFile)) {
            Files.delete(configFile.toPath());
            plugin.saveDefaultConfig();
        } else {
            plugin.onDisable();
        }
    }

    /**
     * Attempts to clone the sourceFile's contents to the targetFile. Both files must exist to work.
     *
     * @param sourceFile The source File, or the current configuration.
     * @param targetFile The target File to write to, or the backup. Writable file must pre-exist on disk.
     * @return True if a backup was successfully written to. False is returned if the TargetFile does not exist.
     * @throws IOException Thrown if the backup file exists, but is not writable - or if the source file does not exist
     * or is not readable.
     */
    public boolean backupConfig(final File sourceFile, final File targetFile) throws IOException {
        logger.warning("Attempting to back up the configuration.");
        if (targetFile.exists()) {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), REPLACE_EXISTING);
            logger.warning("Configuration Backup Successful. Old Backup Replaced.");
            return true;
        } else {
            logger.severe("ABORTING: Unable to back up configuration! Please back up manually.");
            return false;
        }
    }
}
