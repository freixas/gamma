/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021  by Antonio Freixas
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
package gamma.preferences;

import gamma.Gamma;
import java.io.File;
import java.util.prefs.Preferences;

/**
 *
 * @author Antonio Freixas
 */
public class PreferenceManager
{
    static private final Preferences userPreferences = Preferences.userNodeForPackage(Gamma.class);
    static private final File BASE_SCRIPT_DIRECTORY = new File(Gamma.USER_DATA_HOME, "Gamma/Scripts");
    static private final File BASE_IMAGE_DIRECTORY = new File(Gamma.USER_DATA_HOME, "Gamma/Images");
    static private final File BASE_VIDEO_DIRECTORY = new File(Gamma.USER_DATA_HOME, "Gamma/Videos");

    static public final boolean getDisplayGreetingMessage()
    {
        return userPreferences.getBoolean("DISPLAY_GREETING", true);
    }

    static public final void setDisplayGreetingMessage(boolean displayGreetingMessage)
    {
        userPreferences.putBoolean("DISPLAY_GREETING", displayGreetingMessage);
    }

    static public final File getDefaultScriptDirectory()
    {
        String defaultScriptDirectory = userPreferences.get("DEFAULT_SCRIPT_DIRECTORY", BASE_SCRIPT_DIRECTORY.toString());
        if (defaultScriptDirectory == null) return null;
        return new File(defaultScriptDirectory);
    }

    static public final void setDefaultScriptDirectory(File defaultScriptDirectory)
    {
        userPreferences.put("DEFAULT_SCRIPT_DIRECTORY", defaultScriptDirectory.toString());
    }

    static public final File getDefaultImageDirectory()
    {
        String defaultImageDirectory = userPreferences.get("DEFAULT_IMAGE_DIRECTORY", BASE_IMAGE_DIRECTORY.toString());
        if (defaultImageDirectory == null) return null;
        return new File(defaultImageDirectory);
    }

    static public final void setDefaultImageDirectory(File defaultImageDirectory)
    {
        userPreferences.put("DEFAULT_IMAGE_DIRECTORY", defaultImageDirectory.toString());
    }

    static public final File getDefaultVideoDirectory()
    {
        String defaultVideoDirectory = userPreferences.get("DEFAULT_VIDEO_DIRECTORY", BASE_VIDEO_DIRECTORY.toString());
        if (defaultVideoDirectory == null) return null;
        return new File(defaultVideoDirectory);
    }

    static public final void setDefaultVideoDirectory(File defaultVideoDirectory)
    {
        userPreferences.put("DEFAULT_VIDEO_DIRECTORY", defaultVideoDirectory.toString());
    }

    static public final String getEditorCommand()
    {
        return userPreferences.get("EDITOR_COMMAND", null);
    }

    static public final void setEditorCommand(String editorCommand)
    {
        userPreferences.put("EDITOR_COMMAND", editorCommand);
    }

    static public final File getDefaultStylesheet()
    {
        String defaultStylesheet = userPreferences.get("DEFAULT_STYLESHEET", null);
        if (defaultStylesheet == null) return null;
        return new File(defaultStylesheet);
    }

    static public final void setDefaultStylesheet(File defaultStylesheet)
    {
        userPreferences.put("DEFAULT_STYLESHEET", defaultStylesheet.toString());
    }

    // Other preferences to be added:
    //
    // * Export image format
    // * Export video format
    // * Settings for each image/video format
    // *

}
