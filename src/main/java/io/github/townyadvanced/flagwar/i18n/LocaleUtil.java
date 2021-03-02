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

import java.net.URL;
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
        Locale defaultLocale = new Locale("en","US");
        String lRegex = "[a-zA-Z]{2,8}";
        String cRegexA = lRegex + "_[a-zA-Z]{2}";
        String cRegexB = lRegex + "_[0-9]{3}";
        String vRegexA = "[_-][0-9][0-9a-zA-Z]{3}";
        String vRegexB = "[_-][0-9a-zA-Z]{5,8}";
        String language;
        String country;
        String variant;
        Locale locale;

        if (localeString.isEmpty() || !fileInJar(localeString)){
            locale = defaultLocale;
            logger.severe("Locale is undefined or is not in FlagWar. Defaulting!");
        } else {
            if (localeString.matches(cRegexA+vRegexA) || localeString.matches(cRegexA+vRegexB)
                || localeString.matches(cRegexB+vRegexA) || localeString.matches(cRegexB+vRegexB)) {

                //Locale w/Variant
                language = localeString.substring(0, localeString.indexOf("_"));
                country = localeString.substring(localeString.indexOf("_"));
                if (country.contains("_")) {
                    variant = country.substring(country.indexOf("_"));
                    country = country.substring(0, country.indexOf("_"));
                } else if (country.contains("-")) {
                    variant = country.substring(country.indexOf("-"));
                    country = country.substring(0, country.indexOf("-"));
                } else {
                    variant = "";
                }
                locale = new Locale(language, country, variant);
            }else if (localeString.matches(cRegexA) || localeString.matches(cRegexB)) {
                // Locale w/Region
                language = localeString.substring(0, localeString.indexOf("_"));
                country = localeString.substring(localeString.indexOf("_"));
                locale = new Locale(language, country);
            }else if (localeString.matches(lRegex)) {
                locale = new Locale (localeString);
            } else {
                logger.severe("Defaulting because something went wrong while assigning the locale!");
                locale = defaultLocale;
            }
        }

        setLocale(locale);
        ResourceBundle msg = ResourceBundle.getBundle("Translation", getLocale());
        setMessages(msg);

        String usingLocale = String.format("Using locale: %s - %s", getMessages().getString("locale"), getMessages().getString("locale-version"));
        logger.info(usingLocale);
    }

    private static boolean fileInJar(String localeString) {
        String localeFile = String.format("/Translation_%s.properties", localeString);
        URL u = FlagWar.class.getResource(localeFile);
        return u != null;
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
