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
package org.freixas.gamma.preferences;

import org.freixas.gamma.Gamma;
import org.freixas.gamma.file.ExportDiagramDialog;
import java.io.File;
import java.util.prefs.Preferences;

/**
 *
 * @author Antonio Freixas
 */
public class PreferencesManager
{
    static private final Preferences userPreferences = Preferences.userNodeForPackage(Gamma.class);
    static private final String BASE_SCRIPTS_DIRECTORY = new File(Gamma.USER_DATA_HOME, "Gamma/Scripts").toString();
    static private final String BASE_IMAGES_DIRECTORY = new File(Gamma.USER_DATA_HOME, "Gamma/Images").toString();
    static private final String BASE_VIDEOS_DIRECTORY = new File(Gamma.USER_DATA_HOME, "Gamma/Videos").toString();

    static private final String WINDOWS_DEFAULT_EDITOR_COMMAND = "notepad \"$F$\"";
    static private final String MAC_DEFAULT_EDITOR_COMMAND = "open -t \"$F$\"";
    static private final String LINUX_DEFAULT_EDITOR_COMMAND = "open \"$F$\"";

    static private final int IMAGE_FORMAT = ExportDiagramDialog.ImageType.PNG.getValue();
    static private final float IMAGE_COMPRESSION = (float)95.0;
    static private final boolean IMAGE_PROGRESSIVE = false;
    static private final int IMAGE_PPI = 96;

    // **********************************************************************
    // *
    // * Direct Preferences Access
    // *
    // **********************************************************************

    // DISPLAY GREETING MESSAGE

    static public boolean getDisplayGreetingMessage()
    {
        return userPreferences.getBoolean("DISPLAY_GREETING", true);
    }

    static public void setDisplayGreetingMessage(boolean displayGreetingMessage)
    {
        userPreferences.putBoolean("DISPLAY_GREETING", displayGreetingMessage);
    }

    // DEFAULT SCRIPTS DIRECTORY

    static public String getDefaultScriptsDirectory()
    {
        return userPreferences.get("DEFAULT_SCRIPT_DIRECTORY", BASE_SCRIPTS_DIRECTORY);
    }

    static public void setDefaultScriptsDirectory(String defaultScriptsDirectory)
    {
        userPreferences.put("DEFAULT_SCRIPT_DIRECTORY", defaultScriptsDirectory != null ? defaultScriptsDirectory : "");
    }

    // DEFAULT IMAGES DIRECTORY

    static public String getDefaultImagesDirectory()
    {
        return userPreferences.get("DEFAULT_IMAGE_DIRECTORY", BASE_IMAGES_DIRECTORY);
    }

    static public void setDefaultImagesDirectory(String defaultImagesDirectory)
    {
        userPreferences.put("DEFAULT_IMAGE_DIRECTORY", defaultImagesDirectory != null ? defaultImagesDirectory : "");
    }

    // DEFAULT VIDEOS DIRECTORY

    static public String getDefaultVideosDirectory()
    {
        return userPreferences.get("DEFAULT_VIDEO_DIRECTORY", BASE_VIDEOS_DIRECTORY);
    }

    static public void setDefaultVideosDirectory(String defaultVideosDirectory)
    {
        userPreferences.put("DEFAULT_VIDEO_DIRECTORY", defaultVideosDirectory != null ? defaultVideosDirectory : "");
    }

    // DEFAULT STYLESHEET

    static public String getDefaultStylesheet()
    {
        return userPreferences.get("DEFAULT_STYLESHEET", "");
    }

    static public void setDefaultStylesheet(String defaultStylesheet)
    {

        userPreferences.put("DEFAULT_STYLESHEET", defaultStylesheet != null ? defaultStylesheet : "");
    }

    // EDITOR COMMAND

    static public String getEditorCommand()
    {
        String defaultEditorCommand = "";
        if (Gamma.IS_WINDOWS) { defaultEditorCommand = WINDOWS_DEFAULT_EDITOR_COMMAND; }
        else if (Gamma.IS_MAC) { defaultEditorCommand = MAC_DEFAULT_EDITOR_COMMAND; }
        else if (Gamma.IS_LINUX) { defaultEditorCommand = LINUX_DEFAULT_EDITOR_COMMAND; }

        return userPreferences.get("EDITOR_COMMAND", defaultEditorCommand);
    }

    static public void setEditorCommand(String editorCommand)
    {
        userPreferences.put("EDITOR_COMMAND", editorCommand != null ? editorCommand : "");
    }

    // EXPORT IMAGE FORMAT

    static public int getImageFormat()
    {
        return userPreferences.getInt("IMAGE_FORMAT", IMAGE_FORMAT);
    }

    static public void setImageFormat(int imageFormat)
    {
        userPreferences.putInt("IMAGE_FORMAT", imageFormat);
    }

    // EXPORT IMAGE COMPRESSION

    static public float getImageCompression()
    {
        return userPreferences.getFloat("IMAGE_COMPRESSION", IMAGE_COMPRESSION);
    }

    static public void setImageCompression(float imageCompression)
    {
        userPreferences.putFloat("IMAGE_COMPRESSION", imageCompression);
    }

    // EXPORT IMAGE PROGRESSIVE

    static public boolean getImageProgressive()
    {
        return userPreferences.getBoolean("IMAGE_PROGRESSIVE", IMAGE_PROGRESSIVE);
    }

    static public void setImageProgressive(boolean isProgressive)
    {
        userPreferences.putBoolean("IMAGE_PROGRESSIVE", isProgressive);
    }

    // IMAGE DPI

    static public int getImagePPI()
    {
        return userPreferences.getInt("IMAGE_PPI", IMAGE_PPI);
    }

    static public void setImagePPI(int dpi)
    {
        userPreferences.putInt("IMAGE_PPI", dpi);
    }

    // Other preferences to be added:
    //
    // * Export video format
    // * Settings for each image/video format
    // *

    // **********************************************************************
    // *
    // * Related Methods
    // *
    // **********************************************************************

    /**
     * Get the default directory to use for file dialogs for various types
     * of files. This method is only called by MainWindows that don't have a
     * default of their own. MainWindow defaults will always be directories that
     * have been accessed during the lifetime of this program. The directories
     * that this method returns may never have been accessed and will be created
     * if they don't exist.
     *
     * @param type The type of file.
     */
    static public File getDefaultDirectory(Gamma.FileType type)
    {
        String directoryName;
        String defaultName;

        // Get the default from the preferences system

        switch (type) {
            case SCRIPT -> {
                directoryName = PreferencesManager.getDefaultScriptsDirectory();
                defaultName = "/Scripts";
            }
            case IMAGE -> {
                directoryName = PreferencesManager.getDefaultImagesDirectory();
                defaultName = "/Images";
            }
            case VIDEO -> {
                directoryName = PreferencesManager.getDefaultVideosDirectory();
                defaultName = "/Videos";
            }
            default -> {
                directoryName = "";
                defaultName = "";
            }
        }

        File defaultDirectory;

        if (directoryName.length() > 0) {
            defaultDirectory = new File(directoryName);

            // isDirectory() implies that the file exists AND is a directory

            if (defaultDirectory.isDirectory()) {
                return defaultDirectory;
            }
            // Does it exist?

            if (!defaultDirectory.exists()) {
                 if (defaultDirectory.mkdirs() && defaultDirectory.exists()) {
                    return defaultDirectory;
                }
            }
        }

        // We reach here  only if the default directory was undefined or if it
        // was a file or if it didn't exist and we were unable to create it.
        // The fallback is to user USER_DATA_HOME/Gamma/(Scripts|Images|Videos)

        defaultDirectory = new File(Gamma.USER_DATA_HOME, "Gamma" + defaultName);
        if (defaultDirectory.isDirectory()) {
            return defaultDirectory;
        }

        if (!defaultDirectory.exists()) {
            if (defaultDirectory.mkdirs() && defaultDirectory.exists()) {
                return defaultDirectory;
            }
        }

        // One more try: If USER_DATA_HOME doesn't exist, try
        // user.home/Gamma/(Scripts|Images|Videos)

        defaultDirectory = new File(System.getProperty("user.home"), "Gamma" + defaultName);
        if (defaultDirectory.isDirectory()) {
            return defaultDirectory;
        }
        if (!defaultDirectory.exists()) {
            if (defaultDirectory.mkdirs() && defaultDirectory.exists()) {
                return defaultDirectory;
            }
        }
        // Final option: user.home

        return new File(System.getProperty("user.home"));
    }

}
