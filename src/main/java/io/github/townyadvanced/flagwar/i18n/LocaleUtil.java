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

    public static void setUpLocale(String localeString){

        Logger logger = FlagWar.getInstance().getLogger();

        String country;
        String language;
        Locale locale;

        if (!localeString.isEmpty() && localeString.contains("_")) {
            language = localeString.substring(0,localeString.indexOf("_")-1).toLowerCase();
            country = localeString.substring(localeString.indexOf("_")+1).toUpperCase();
            String logMessage = String.format("Translation read as: %s_%s", language, country);
            logger.info(logMessage);
            if (country.contains("_")) {
                String variant = country.substring(country.indexOf("_") + 1);
                country = country.substring(0, country.indexOf("_") - 1);
                locale = new Locale(language, country, variant);
            } else {
                locale = new Locale(language,country);
            }
        } else {
            locale = new Locale("en","US","POSIX");
            logger.warning("Defaulting FlagWar localization to en_US_POSIX.");
        }

        ResourceBundle msg = ResourceBundle.getBundle("Translations", currentLocale);

        setLocale(locale);
        setMessages(msg);

        logger.info(messages.getString("test-message"));
    }



    public Locale getLocale() {
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
