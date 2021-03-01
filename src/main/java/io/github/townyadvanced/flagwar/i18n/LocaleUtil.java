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

import io.github.townyadvanced.flagwar.FlagWar;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class LocaleUtil {
    private static Locale currentLocale;
    private static ResourceBundle messages;

    private LocaleUtil() {
        throw new IllegalStateException("Utility Class");
    }

    public static void setUpLocale(String localeString){

        Logger logger = FlagWar.getInstance().getLogger();

        String country;
        String language;
        Locale locale;

        if (!localeString.isEmpty() && localeString.contains("_")) {
            language = localeString.substring(0, localeString.indexOf("_")).toLowerCase();
            country = localeString.substring(localeString.indexOf("_") + 1).toUpperCase();
            if (country.contains("_")) {
                String variant = country.substring(country.indexOf("_") + 1);
                country = country.substring(0, country.indexOf("_"));
                if (country.contains("_")) {
                    logger.severe("Too many underscores for a valid locale! Defaulting.");
                    locale = new Locale("en","US");
                } else {
                    locale = new Locale(language, country, variant);
                }
            } else {
                locale = new Locale(language, country);
            }
        } else if (!localeString.isEmpty()) {
            //TODO - Implement Custom Language Loading
            logger.severe("Unsupported locale specified: You can contribute a locale via PR at https://github.com/TownyAdvanced/FlagWar/. Defaulting.");
            locale = new Locale("en","US");
        } else {
            locale = new Locale("en","US");
            logger.warning("No specified locale! Defaulting.");
        }

        setLocale(locale);
        ResourceBundle msg = ResourceBundle.getBundle("Translation", getLocale());
        setMessages(msg);

        String usingLocale = String.format("Using locale: %s - %s", getMessages().getString("locale"), getMessages().getString("locale-version"));
        logger.info(usingLocale);
    }

    public static Locale getLocale() {
        return currentLocale;
    }
    private static void setLocale(Locale locale) {
        currentLocale = locale;
    }
    public static ResourceBundle getMessages() {
        return messages;
    }
    private static void setMessages(ResourceBundle resourceBundle) {
        messages = resourceBundle;
    }
}
