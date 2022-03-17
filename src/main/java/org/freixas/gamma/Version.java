/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021-2022  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.freixas.gamma;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get the version number associated with this instance of the application. If
 * we are not running from a jar file, the version is always "Development". If
 * we are running from a jar file, the version comes from the version property
 * in /META-INF/maven/org.freixas.gamma/gamma/pom.properties.
 * <p>
 * This class contains just one static variable, which is set by a static
 * initializer. Version.VERSION provides access to the version number as a
 * String.
 *
 * @author Antonio Freixas
 */
public class Version
{
    /**
     * Gamma's version number.
     */
    static public String VERSION;
    static public int MAJOR_VERSION = 0;
    static public int MINOR_VERSION = 0;
    static public int BUILD_NUMBER = 0;
    static public String VERSION_QUALIFIER = "development";

    static Pattern versionPattern = Pattern.compile("^(\\d*)\\.(\\d*)\\.(\\d*)(-(.*))$");

    static {
        InputStream resourceAsStream = Version.class.getResourceAsStream(
            "/META-INF/maven/org.freixas.gamma/gamma/pom.properties"
        );
        Properties properties = new Properties();
        try {
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
                VERSION = properties.getProperty("version");

                Matcher matcher = versionPattern.matcher(VERSION);
                if (matcher.matches()) {
                    MAJOR_VERSION = Integer.parseInt(matcher.group(1));
                    MINOR_VERSION = Integer.parseInt(matcher.group(2));
                    BUILD_NUMBER = Integer.parseInt(matcher.group(3));
                    VERSION_QUALIFIER = matcher.group(5);

                    VERSION = MAJOR_VERSION + "." + MINOR_VERSION + (VERSION_QUALIFIER.length() > 0 ? "-" + VERSION_QUALIFIER : "");
                }
            }
            else {
                VERSION = MAJOR_VERSION + "." + MINOR_VERSION + (VERSION_QUALIFIER.length() > 0 ? "-" + VERSION_QUALIFIER : "");
            }
        }

        catch (IOException ignored) {
            VERSION = MAJOR_VERSION + "." + MINOR_VERSION + (VERSION_QUALIFIER.length() > 0 ? "-" + VERSION_QUALIFIER : "");
        }
    }

}
