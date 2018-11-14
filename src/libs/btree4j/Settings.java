/*
 * Copyright (c) 2006-2018 Makoto Yui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libs.btree4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);

    public static final String XBIRD_VERSION = "1.0";
    private static final String PROPERTY_FILE_NAME = "btree4j.properties";
    public static final boolean isLoggingEnabled = true;

    //--------------------------------------------
    // Shared variables

    private static final Properties properties;
    static {
        properties = new Properties();
        final String userDir = System.getProperty("user.dir");
        System.out.println(userDir);
        try {
            // put default settings.
            // InputStream is = Settings.class.getResourceAsStream(PROPERTY_FILE_NAME);
            // properties.load(is);
            // put user specific settings.            
            File propFile = new File(userDir, PROPERTY_FILE_NAME);
            if (propFile.exists()) {
                properties.load(new FileInputStream(propFile));
                LOG.info("Loaded btree4j.properties in: " + propFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Exception caused while loading user provided properties file.", e);
        }
    }

    private Settings() {} // prevent instantiation

    public static Properties getProperties() {
        return properties;
    }

    /**
     * Gets the configuration value from the key.
     */
    public static String get(final String key) {
        return properties.getProperty(key);
    }

    public static String get(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getThroughSystemProperty(final String key) {
        final String v = System.getProperty(key);
        return (v == null) ? properties.getProperty(key) : v;
    }

    /**
     * Puts configuration value.
     */
    public static void put(final String key, final String value) {
        properties.put(key, value);
    }

    /**
     * Overloads the specified properties.
     */
    public static void putAll(Properties props) {
        properties.putAll(props);
    }

}
