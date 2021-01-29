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

package io.github.townyadvanced.flagwar.util;

import com.palmergames.bukkit.towny.TownySettings;
import java.util.regex.Pattern;

/**
 * @author dumptruckman
 */
public class TimeTools {

    private TimeTools() {
        super();
    }

    /**
     * This will parse a time string such as 2d30m to an equivalent amount of
     * seconds.
     *
     * @param dhms The time string
     * @return The amount of seconds
     */
    public static long dhmsToSeconds(String dhms) {

        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        int days = 0;

        if (dhms.contains("d")) {
            days = Integer.parseInt(dhms.split("d")[0].replace(" ", ""));
            if (dhms.contains("h") || dhms.contains("m") || dhms.contains("s")) {
                dhms = dhms.split("d")[1];
            }
        }
        if (dhms.contains("h")) {
            hours = Integer.parseInt(dhms.split("h")[0].replace(" ", ""));
            if (dhms.contains("m") || dhms.contains("s")) {
                dhms = dhms.split("h")[1];
            }
        }
        if (dhms.contains("m")) {
            minutes = Integer.parseInt(dhms.split("m")[0].replace(" ", ""));
            if (dhms.contains("s")) {
                dhms = dhms.split("m")[1];
            }
        }
        if (dhms.contains("s")) {
            seconds = Integer.parseInt(dhms.split("s")[0].replace(" ", ""));
        }
        return (days * 86400L) + (hours * 3600L) + (minutes * 60L) + (long)seconds;
    }

    public static long getMillis(String dhms) {
        return getSeconds(dhms) * 1000;
    }

    public static long getSeconds(String dhms) {

        if (Pattern.matches(".*[a-zA-Z].*", dhms)) {
            return (TimeTools.dhmsToSeconds(dhms));
        }
        return Long.parseLong(dhms);
    }

    public static long getTicks(String dhms) {
        return convertToTicks(getSeconds(dhms));
    }

    /**
     * Converts Seconds to Ticks
     *
     * @param t - Unix time
     * @return ticks
     */
    public static long convertToTicks(long t) {
        return t * 20;
    }

    /**
     * Converts Seconds to 'Short' Ticks
     *
     * These ticks are only relevant to the 'Short' Timer Task
     *
     * Rounds half up
     *
     * @param timeSeconds number of seconds to convert.
     * @return ticks
     */
    public static int convertToShortTicks(double timeSeconds) {
        //TODO: TownySettings >> FlagWarSettings
        //Equivalent to: getSeconds(ConfigNodes.PLUGIN_SHORT_INTERVAL) - or - '20s' (default)
        return (int)((timeSeconds / TownySettings.getShortInterval()) + 0.5);
    }

    public static int getHours(long milliSeconds) {
        return (int) ((milliSeconds /1000) / 60) /60;
    }
}
