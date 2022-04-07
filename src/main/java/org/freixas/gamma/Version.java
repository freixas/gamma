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
    static public int MAJOR_VERSION = 1;
    static public int MINOR_VERSION = 0;
    static public int BUILD_NUMBER = 0;
    static public String VERSION_QUALIFIER = "development";

    static Pattern versionPattern = Pattern.compile("^(\\d*)\\.(\\d*)\\.(\\d*)(-(.+))?$");

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
                    String versionQualifier = VERSION_QUALIFIER == null || VERSION_QUALIFIER.length() == 0 ?
                        "" : "-" + VERSION_QUALIFIER;

                    VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + BUILD_NUMBER + versionQualifier;
                }
            }
            else {
                VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + BUILD_NUMBER + "-" + VERSION_QUALIFIER;
            }
        }

        catch (IOException ignored) {
            VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + MINOR_VERSION +  "-" + VERSION_QUALIFIER;
        }
    }

    /**
     * Return true if the given version string is valid.
     *
     * @param versionString The version string to check.
     *
     * @return True if the given version string is valid.
     */
    static public boolean isValidVersion(String versionString)
    {
        Matcher matcher = versionPattern.matcher(versionString);
        return (matcher.matches());
    }

    /**
     * Return true if the given version is valid and if Gamma's version is
     * greater than or equal to the given version. If this is a development
     * version of Gamma, this method always returns true.
     * <p>
     * The version qualifier is ignored in the comparison.
     * <p>
     * Use the isValidMethod() function to differentiate between whether the
     * version is invalid or is valid but greater than the current version.
     *
     * @param versionString The version we are comparing Gamma's version to.
     *
     * @return True if the given version string is valid and is less than or
     * equal to the current version of Gamma.
     */
    static public boolean GE(String versionString)
    {
        if (VERSION_QUALIFIER.equals("development")) return true;

        Matcher matcher = versionPattern.matcher(versionString);
        if (matcher.matches()) {
            int majorVersion = Integer.parseInt(matcher.group(1));
            int minorVersion = Integer.parseInt(matcher.group(2));
            int buildNumber = Integer.parseInt(matcher.group(3));

            if (majorVersion < MAJOR_VERSION) return true;
            if (majorVersion == MAJOR_VERSION) {
                if (minorVersion < MINOR_VERSION) return true;
                if (minorVersion == MINOR_VERSION) {
                    return buildNumber <= BUILD_NUMBER;
                }
            }
        }

        return false;
    }

}
