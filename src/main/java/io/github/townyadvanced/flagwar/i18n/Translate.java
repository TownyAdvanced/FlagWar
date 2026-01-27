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

package io.github.townyadvanced.flagwar.i18n;

import com.palmergames.bukkit.util.Colors;

/**
 * Helper class for abstracting away locale utilities.
 */
public final class Translate {

    private Translate() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Takes a Translation key and variable arguments (Varargs), and returns the formatted String.
     * The key is run through {@link LocaleUtil#getMessages()} and {@link java.util.ResourceBundle#getString(String)}.
     * @param translationKey The translation key, as it appears in a Translation_locale.properties ResourceBundle.
     * @param args The {@link String#format(String, Object...)} arguments to use, in place of {@link Object}s.
     * @return A translated String, with parsed arguments.
     */
    public static String from(final String translationKey, final Object... args) {
        return Colors.translateColorCodes(String.format(LocaleUtil.getMessages().getString(translationKey), args));
    }

    /**
     * Takes a Translation key and runs it through {@link LocaleUtil#getMessages()} and
     * {@link java.util.ResourceBundle#getString(String)}.
     * @param translationKey The translation key, as it appears in a Translation_locale.properties ResourceBundle.
     * @return A translated String, with formatting applied (necessary for some strings with line-breaks)
     */
    public static String from(final String translationKey) {
        // Redundant call to format() is intentional
        return Colors.translateColorCodes(String.format(LocaleUtil.getMessages().getString(translationKey)));
    }

    /**
     * Runs {@link #from(String, Object...)}, then prefixes the message.
     * @param translationKey A translation key, as it appears in a Translation_locale.properties ResourceBundle.
     * @param args The {@link String#format(String, Object...)} arguments to use, in place of {@link Object}s.
     * @return A prefixed message.
     */
    public static String fromPrefixed(final String translationKey, final Object... args) {
        return from("message-prefix", from(translationKey, args));
    }

    /**
     * Runs {@link #from(String)}, then prefixes the message.
     * @param translationKey A translation key, as it appears in a Translation_locale.properties ResourceBundle.
     * @return A prefixed message.
     */
    public static String fromPrefixed(final String translationKey) {
        return from("message-prefix", from(translationKey));
    }
}
