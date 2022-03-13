/*
 * Copyright (c) 2022 Antonio Freixas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freixas.gamma;

import javafx.scene.control.Alert;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * This class handles platform-dependent tasks.
 */
public class Platform
{
    static public final String osName = System.getProperty("os.name").toLowerCase();
    static public final boolean IS_WINDOWS = osName.contains("win");
    static public final boolean IS_MAC = osName.contains("mac");
    static public final boolean IS_LINUX = osName.contains("nix") || osName.contains("nux") || osName.contains("aix");

    /**
     * Display, in the user's default browser, an HTML file located in the Help folder.
     *
     * @param name The name of the HTML file to display.
     * @param mainWindow The parent window for error dialogs.
     */
    static public void browseHelp(String name, MainWindow mainWindow)
    {
        try {
            File helpFile = new File(Gamma.HELP_LOCATION.getAbsolutePath() + "/" + name);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(helpFile.toURI());
                    return;
                }
            }

            // We were unable to use Desktop, try open

            open(helpFile.toString(), mainWindow);
        }
        catch (Exception e) {
            mainWindow.showTextAreaAlert(
                Alert.AlertType.ERROR, "Help Error", "Help Error",
                "Error when trying to view help:\n\n" + e.getLocalizedMessage() + "\n\n" +
                    "Look in " + Gamma.HELP_LOCATION.getAbsolutePath() +
                    " for " + name +
                    " and open it in your browser.",
                true);
        }

    }

    /**
     * Open the given file using the default application used to open it.
     *
     * @param name The name of the file (preferably, a full path).
     * @param mainWindow The parent window for error dialogs.
     *
     * @throws IOException If the command fails.
     */
    static public void open(String name, MainWindow mainWindow) throws IOException
    {
        String openCommand = "";
        if (IS_WINDOWS) {
            openCommand = "start \"\" /b ";
        }
        else if (IS_MAC) {
            openCommand = "open ";
        }
        else if (IS_LINUX) {
            openCommand = "xdg-open ";
        }
        Runtime.getRuntime().exec(openCommand + '"' + name + '"');
    }

}
