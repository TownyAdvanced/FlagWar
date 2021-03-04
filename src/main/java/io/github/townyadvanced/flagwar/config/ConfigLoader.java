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

public class ConfigLoader {

    private final Plugin plugin;
    private final Logger logger;

    public ConfigLoader(Plugin plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadConfig() throws IOException, InvalidConfigurationException {
        double needConfVer = 1.0;

        plugin.saveDefaultConfig();
        File configFile = new File("plugins/FlagWar/config.yml");
        plugin.getConfig().load(configFile);

        if (plugin.getConfig().getDouble("config_version") < needConfVer) {
            File backupFile = new File("plugins/FlagWar/config.old.yml");
            if (backupFile.createNewFile())
                logger.warning("Created new backup location: Flagwar/config.old.yml");
            regenerateConfiguration(configFile, backupFile);
        }

        plugin.getConfig().load(configFile);
    }

    public void regenerateConfiguration(File configFile, File backupFile) throws IOException {
        if(!backupConfig(configFile, backupFile))
            plugin.onDisable();

        Files.delete(configFile.toPath());
        plugin.saveDefaultConfig();
    }

    public boolean backupConfig(File sourceFile, File targetFile) throws IOException {
        logger.warning("Attempting to back up the configuration.");
        if (targetFile.exists()){
            Files.copy(sourceFile.toPath(), targetFile.toPath(), REPLACE_EXISTING);
            logger.warning("Configuration Backup Successful. Old Backup Replaced.");
            return true;
        }
        else {
            logger.severe("ABORTING: Unable to back up configuration! Please back up manually.");
            return false;
        }
    }
}
