/*
 * Copyright (C) 2021 Antonio Freixas
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
package gamma;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import gamma.cli.CommandLine;
import gamma.cli.CommandLineParser;
import gamma.cli.DefaultParser;
import gamma.cli.HelpFormatter;
import gamma.cli.Options;
import gamma.cli.ParseException;
import gamma.css.value.StyleException;
import gamma.css.value.Stylesheet;
import gamma.preferences.PreferenceManager;
import java.io.IOException;
import javax.swing.JFileChooser;

/**
 * The main application class.
 *
 * Anything functionality that is associated with the application and not
 * associated with just one window is included here. This includes the
 * functionality for creating a new main window.
 *
 * @author Antonio Freixas
 */
public class Gamma extends Application
{
    public enum FileType  {
        SCRIPT(0), IMAGE(1), VIDEO(2);

        private final int value;
        FileType(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    static public final File USER_DATA_HOME = new JFileChooser().getFileSystemView().getDefaultDirectory();

    private static int windowID = 1;
    private static final ArrayList<MainWindow> windowList = new ArrayList<>();
    private static Stylesheet systemStylesheet = null;

    // **********************************************************************
    // *
    // * Main
    // *
    // **********************************************************************

    /**
     * Launch the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    // **********************************************************************
    // *
    // * JavaFX Start
    // *
    // **********************************************************************

    @Override
    public void start(Stage primaryStage)
            throws Exception
    {
        List<String> list = getParameters().getRaw();
        String[] args = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            args[i] = list.get(i);
        }

        CommandLineParser parser = new DefaultParser();

        // create the Options

        Options options = new Options();
        options.addOption("h", "help", false, "displays this help message");
        options.addOption("v", "version", false, "displays the version number");
        options.addOption("s", "stylesheet", true, "path to default stylesheet");

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Gamma [options] [script-files ...]\n", options);
                System.exit(0);
            }

            else if (line.hasOption("version")) {
                String version = this.getClass().getPackage().getImplementationVersion();
                if (version == null) {
                    System.out.println("Development version");
                }
                else {
                    System.out.println("Version " + this.getClass().getPackage().getImplementationVersion());
                }
                System.exit(0);
            }

            File cssFile;
            if (line.hasOption("stylesheet")) {
                cssFile = new File(line.getOptionValue("stylesheet"));
            }
            else {
                cssFile = PreferenceManager.getDefaultStylesheet();
            }

            if (cssFile != null) {
                try {
                    systemStylesheet = Stylesheet.createStylesheet(cssFile);
                }
                catch (IOException | gamma.parser.ParseException | StyleException e)
                {
                    System.err.println(e.getLocalizedMessage());
                }
            }

            File[] defaultDirectories = { null, null, null };

            String[] filenames = line.getArgs();
            if (filenames.length > 0) {
                for (String filename : filenames) {
                    File file = new File(filename);
                    if (!file.exists()) {
                        System.err.println("File '" + filename + "' does not exist.");
                    }
                    else if (!file.isFile()) {
                        System.err.println("'" + filename + "' is not a file.");
                    }
                    else {
                        newMainWindow(file, defaultDirectories);
                    }
                }
            }
            else {
                newMainWindow(null, defaultDirectories);
            }
        }
        catch (ParseException exp) {
            System.err.println("Command line error:" + exp.getMessage());
        }

        primaryStage.close();
    }

    // **********************************************************************
    // *
    // * Window Management
    // *
    // **********************************************************************

    /**
     * Create a new main window.
     *
     * @param file The script file associated with the window (can be null).
     * @param defaultDirectories The default directories to use for file dialogs
     * in the the new window, one for each type of file (SCRIPT, IMAGE, and
     * VIDEO).
     *
     * @throws java.lang.Exception
     */
    public static void newMainWindow(File file, File[] defaultDirectories)
            throws Exception
    {
        // Go through all the existing windows and set the Close button's state
        // appropriately

        ListIterator<MainWindow> iter = windowList.listIterator();
        while (iter.hasNext()) {
            MainWindow w = iter.next();
            w.setCloseState(windowList.size() + 1 > 1);
        }

        MainWindow window = new MainWindow(windowID, file, defaultDirectories);
        windowList.add(window);
        windowID++;
    }

    /**
     * Get the number of main windows in the application. Other types of
     * windows (e.g. a Help window) are not included in the count.
     *
     * @return The number of main windows in the application.
     */
    public static int getWindowCount()
    {
        if (!Platform.isFxApplicationThread()) Gamma.quickAlert("Not FX Thread");
        return windowList.size();
    }

    /**
     * Exit the application by closing all windows (letting the windows
     * release any resources) and then exiting the GUI.
     */
    public static void exit()
    {
        while (windowList.size() > 1) {
            closeWindow(windowList.get(0));
        }

        Platform.exit();

        // Unfortunately, some threads are blocked and will not know to stop,
        // so we have to resort to this to exit everything

        System.exit(0);
    }

    /**
     * Close a main window.
     * <p>
     * When a main window has verified that it is able to close, it should
     * call this method. This removes any references to the window instance
     * from this class and calls the window's close() method to actually
     * close it.
     * <p>
     * This is also called by Gamma.exit().
     *
     * @param window The window to close.
     */
    public static void closeWindow(MainWindow window)
    {
        // If we have just one window (which we are about to close), go through
        // exit() instead

        if (windowList.size() == 1) exit();

        // Otherwise close the window

        windowList.remove(window);
        window.close();

        // If we now have just one window left, disable its close button
        // It can still be closed through the window manager

        if (windowList.size() == 1) {
            MainWindow finalWindow = windowList.get(0);
            finalWindow.setCloseState(windowList.size() > 1);
        }
    }

    // **********************************************************************
    // *
    // * Global Options
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
    public static File getDefaultDirectory(FileType type)
    {
        File defaultDirectory;
        String defaultName;

        // Get the default from the preferences system

        switch (type) {
            case SCRIPT -> {
                defaultDirectory = PreferenceManager.getDefaultScriptDirectory();
                defaultName = "/Scripts";
            }
            case IMAGE -> {
                defaultDirectory = PreferenceManager.getDefaultImageDirectory();
                defaultName = "/Images";
            }
            case VIDEO -> {
                defaultDirectory = PreferenceManager.getDefaultVideoDirectory();
                defaultName = "/Videos";

            }
            default -> {
                defaultDirectory = null;
                defaultName = "";
            }
        }

        if (defaultDirectory != null) {

            // isDirectory() implies that the file exists AND is a directory

            if (defaultDirectory.isDirectory()) return defaultDirectory;

            // Does it exist?

            if (!defaultDirectory.exists()) {
                defaultDirectory.mkdir();
                if (defaultDirectory.exists()) return defaultDirectory;
            }
        }

        // We reach hear only if the default directory was undefined or if it
        // was a file or if it didn't exist and we were unable to create it.
        // The fallback is to user USER_DATA_HOME/Gamma/(Scripts|Images|Videos)

        defaultDirectory = new File(USER_DATA_HOME, "Gamma" + defaultName);
        if (defaultDirectory.isDirectory()) return defaultDirectory;

        if (!defaultDirectory.exists()) {
            defaultDirectory.mkdir();
            if (defaultDirectory.exists()) return defaultDirectory;
        }

        // One more try: If USER_DATA_HOME doesn't exist, try
        // user.home/Gamma/(Scripts|Images|Videos)

        defaultDirectory = new File(System.getProperty("user.home"), "Gamma" + defaultName);
        if (defaultDirectory.isDirectory()) return defaultDirectory;

        if (!defaultDirectory.exists()) {
            defaultDirectory.mkdir();
            if (defaultDirectory.exists()) return defaultDirectory;
        }

        // Final option: user.home

        return new File(System.getProperty("user.home"));
    }

    static private void quickAlert(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Concurrency Problem");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
