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

package io.github.townyadvanced.flagwar.util;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Messaging {
    /** Sets the logger to the Bukkit-provided logger. */
    private static final Logger LOGGER = FlagWar.getInstance().getLogger();

    private Messaging() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Sends a simple {@link String} to a given {@link Player}.
     * @param recipient Player receiving message.
     * @param str A simple String.
     */
    public static void send(@NotNull final Player recipient, @NotNull final String str) {
        recipient.sendMessage(str);
    }

    /**
     * Send a debugMessage (FW_DEBUG: [{@link String}]) over the {@link Logger} via {@link Level#WARNING}.
     * <p>
     * Must have the extra.debug config node set to true for the message to be sent.
     * @param debugMessage Simple String to pass to the logger.
     */
    public static void debug(@NotNull final String debugMessage) {
        if (FlagWarConfig.isDebugging()) {
            LOGGER.log(Level.WARNING, () -> String.format("FW_DEBUG: %s", debugMessage));
        }
    }

    /**
     * Send a debugMessage (FW_DEBUG: [{@link String}]) over the WARN channel, passing the supplied baseMessage and
     * arguments to {@link String#format(String, Object...)}.
     *
     * @param baseMessage A String, compatible with {@link java.util.Formatter#format(String, Object...)}.
     * @param args Arguments to pass to the String for formatting.
     */
    public static void debug(@NotNull final String baseMessage, @NotNull final Object[] args) {
        debug((String.format(baseMessage, args)));
    }
}
