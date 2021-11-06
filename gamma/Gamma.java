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
import java.util.ListIterator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

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

    private static int windowID = 1;
    private static final ArrayList<MainWindow> windowList = new ArrayList<MainWindow>();
    private static File defaultDirectory = null;

    /**
     * Launch the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
            throws Exception
    {
        newMainWindow(null);
        primaryStage.close();
    }

    /**
     * Create a new main window.
     *
     * @param file The script file associated with the window (can be null).
     * @throws java.lang.Exception
     */
    public static void newMainWindow(File file)
            throws Exception
    {
        // Go through all the existing windows and set the Close button's state
        // appropriately
        
        ListIterator<MainWindow> iter = windowList.listIterator();
        while (iter.hasNext()) {
            MainWindow w = iter.next();
            w.setCloseState(windowList.size() + 1 > 1);
        }

        MainWindow window = new MainWindow(windowID, file);
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

    /**
     * Get the default directory to use for file dialogs when we don't have any
     * script file chosen.
     *
     * For Windows, the default location is %USERHOME%\Documents\Gamma and this
     * is created if it doesn't exist. If it can't be created, it is
     * %USERHOME%\Documents.
     *
     * For all other platforms, it is the user's home directory / Gamma, which
     * is created. If it can't be created, it is the user's home directory.
     *
     * @return
     */
    static File getDefaultDirectory()
    {
        if (defaultDirectory == null) {
            File userHome = new File(System.getProperty("user.home"));
            File documents = new File(userHome, "Documents");
            if (documents.exists()) {
                File gamma = new File(documents, "Gamma");
                gamma.mkdir();
                if (gamma.exists()) {
                    defaultDirectory = gamma;
                }
                else {
                    defaultDirectory = documents;
                }
            }
            else {
                File gamma = new File(userHome, "Gamma");
                gamma.mkdir();
                if (gamma.exists()) {
                    defaultDirectory = gamma;
                }
                else {
                    defaultDirectory = userHome;
                }
            }
        }
        return defaultDirectory;

    }
    
    static private void quickAlert(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Concurrency Problem");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
