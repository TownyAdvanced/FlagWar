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

package io.github.townyadvanced.flagwar.command;

import com.palmergames.bukkit.util.Colors;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.exceptions.FlagWarException;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlagWarCommand implements CommandExecutor {

    private static FlagWar plugin;
    private static String flagWarVersion;

    private boolean isConsole;
    private Player player;
    private CommandSender sender;
    private static final Logger LOGGER = FlagWar.getPlugin().getLogger();

    public FlagWarCommand(FlagWar instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender commandSender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args) {

        flagWarVersion = Colors.Green + "Flag War version: " + Colors.LightGreen + plugin.getDescription().getVersion();

        this.sender = commandSender;

        if (sender instanceof Player)
            isConsole = false;
        else
            isConsole = true;

        try {
            parseFlagWarCommand(sender, args);
        } catch (FlagWarException fwe) {
            LOGGER.severe(fwe.getMessage());
            fwe.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean parseFlagWarCommand(CommandSender sender, String [] args) throws FlagWarException {
        if (!isConsole && !player.hasPermission("flagwar.command.flagwar")) {
            throw new FlagWarException("message");
        }

        if (isConsole) {
            sender.sendMessage("");
        }

        StringBuilder message = new StringBuilder();
        message.append(flagWarVersion);
        message.append("/flagwaradmin");


        sender.sendMessage(message.toString());
        return true;
    }
}
