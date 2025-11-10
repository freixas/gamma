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
        File helpFile = new File(Gamma.HELP_LOCATION.getAbsolutePath() + "/" + name);
        try {
            if (Desktop.isDesktopSupported() && !System.getenv().containsKey("WSL_DISTRO_NAME")) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(helpFile.toURI());
                    return;
                }
            }

            // If Desktop is not supported or we're running Linux on Windows
            // using WSL, try using open

            open(helpFile.toString());
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
     *
     * @throws IOException If the command fails.
     */
    static public void open(String name) throws IOException
    {
        ProcessBuilder builder = null;
        if (IS_WINDOWS) {
            builder = new ProcessBuilder("start", "\"\"", "/b", name);
         }
        else if (IS_MAC) {
            builder = new ProcessBuilder("open", name);
        }
        else if (IS_LINUX) {
            builder = new ProcessBuilder("xdg-open", name);
        }
        if (builder != null) {
            builder
                .redirectOutput(ProcessBuilder.Redirect.to(new File("/dev/null")))
                .redirectError(ProcessBuilder.Redirect.to(new File("/dev/null")))
                .start();
        }
    }

    /**
     * Run a DOS or shell command.
     *
     * @param cmd The name of the command to execute.
     *
     * @throws IOException If the command fails.
     */
    static public void cmd(String cmd) throws IOException
    {
        String cmd1 = "bash";
        String cmd2 = "-c";

        if (IS_WINDOWS) {
            cmd1 = "cmd.exe";
            cmd2 = "/c";
        }

        ProcessBuilder builder = new ProcessBuilder(cmd1, cmd2, cmd);
        builder.start();

    }

}
