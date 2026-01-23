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

import io.github.townyadvanced.flagwar.config.FlagWarConfig;

import java.time.Duration;

public final class FormatUtil {

    private FormatUtil() {
        // Masking public constructor
    }

    /**
     * Function used to format a {@link Duration} according to the formatting defined in
     * {@link FlagWarConfig#getTimerText()}.
     * @param duration Seed Duration
     * @param formatString Formatting specification: should contain arguments corresponding with seconds, minutes,
     *                     and hours, respectively.
     * @return The formatted string.
     */
    public static String time(final Duration duration, final String formatString) {
        final int hoursInDay = 24;
        final long hours = duration.toHoursPart() + (duration.toDaysPart() * hoursInDay);
        final int minutes = duration.toMinutesPart();
        final int seconds = duration.toSecondsPart();
        return String.format(formatString, seconds, minutes, hours);
    }
}
