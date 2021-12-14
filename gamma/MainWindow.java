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

import gamma.execution.DiagramEngine;
import java.io.File;
import java.util.ListIterator;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import gamma.file.FileWatcher;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

/**
 * This class manages the view / controller / model for one main window. It
 * creates the view (from an FXML file) and the controller. It also creates and
 * manages any model classes associated with the view / controller.
 *
 * @author Antonio Freixas
 *
 */
public final class MainWindow extends Stage
{

    private final int ID;
    private File file;
    private final MainWindowController controller;

    private MenuItem fileMenuExportDiagram;
    private MenuItem fileMenuExportVideo;
    private MenuItem fileMenuPrint;
    private MenuItem fileMenuClose = null;

    private FileWatcher watcher = null;
    private Thread watcherThread = null;

    private DiagramEngine diagramEngine = null;
//    private LCodeEngine lCodeEngine = null;
    private Canvas canvas;

    private PrintDialog printDialog = null;
    private TextArea printDialogTextArea = null;

    /**
     * Create a main window.
     *
     * @param ID The ID assigned to this window.
     * @param file The associated script file (may be null).
     * @throws Exception
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MainWindow(int ID, File file)
            throws Exception
    {
        // We can't fully deal with the file until the main window is
        // instantiated.

        this.ID = ID;
        this.file = file;

        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("resources/MainWindow.fxml"));
        Parent root = loader.load();
        controller = (MainWindowController) loader.getController();
        controller.setMainWindow(this);
        setScene(new Scene(root));

        // Handle tasks that can only be performed on instantiated window.

        setOnShown((WindowEvent t) -> {
            locateUIElements();
            setCloseState(Gamma.getWindowCount() > 1);
            setFile(file);
        });

        this.setOnCloseRequest((WindowEvent t) -> {
            Gamma.closeWindow((MainWindow)t.getSource());
        });

        show();
    }

    /**
     * Set the file associated with this main window. The file can be null.
     * <p>
     *
     * Certain menu items are enabled or disabled depending on whether the file
     * is null. The title bar is updated to display the file name. If the file
     * is not null, the associated script file is run.
     *
     * @param file The script file associated with this window.
     */
    public void setFile(File file)
    {
        this.file = file;
        boolean disable = file == null;

        // Enable/disable various File Menu entries

        fileMenuExportDiagram.setDisable(disable);
        fileMenuExportVideo.setDisable(disable);
        fileMenuPrint.setDisable(disable);

        if (file != null) {

            // Update the title bar

            setTitle("Gamma - " + file.getName());

            watcher = new FileWatcher(file, this);
            watcherThread = new Thread(watcher);
            watcherThread.start();
        }
    }

    /**
     * Get the file associated with this main window. The file can be null.
     *
     * @return the file associated with this main window.
     */
    public File getFile()
    {
        return file;
    }

    @Override
    public void close()
    {
        if (watcherThread != null) {
            watcher.stopThread();
            watcher = null;
            watcherThread = null;
        }
        file = null;

        if (diagramEngine != null) diagramEngine.close();
        super.close();
    }

    /**
     * Set the enable/disable state of the File/Close menu.
     *
     * @param enable If true, enable the File/Close menu; otherwise, disable it.
     */
    public void setCloseState(boolean enable)
    {
        fileMenuClose.setDisable(!enable);
    }

    /**
     * Get the ID assigned to this window.
     *
     * The ID is assigned by the application and is an arbitrary integer used to
     * quickly identify a specific window.
     *
     * @return The window ID.
     */
    public int getID()
    {
        return ID;
    }

    /**
     * Set up access to various user interface elements, particularly menu
     * items, which are not searchable with the Scene's lookup() method.
     */
    private void locateUIElements()
    {
        MenuBar menuBar = (MenuBar)getScene().lookup("#menuBar");
        ObservableList<Menu> menus = menuBar.getMenus();
        ListIterator<Menu> iterMenuBar = menus.listIterator();

        while (iterMenuBar.hasNext()) {
            Menu menu = iterMenuBar.next();

            if (menu.getId().equals("fileMenu")) {
                ObservableList<MenuItem> menuItems = menu.getItems();
                ListIterator<MenuItem> iterFileMenu = menuItems.listIterator();

                while (iterFileMenu.hasNext()) {
                    MenuItem menuItem = iterFileMenu.next();
                    String menuId = menuItem.getId();

                    if (menuId != null) {
                        switch (menuId) {
                            case "fileMenuExportDiagram" ->
                                fileMenuExportDiagram = menuItem;
                            case "fileMenuExportVideo" ->
                                fileMenuExportVideo = menuItem;
                            case "fileMenuPrint" ->
                                fileMenuPrint = menuItem;
                            case "fileMenuClose" ->
                                fileMenuClose = menuItem;
                            default -> {
                            }
                        }
                    }
                }
            }
        }

        canvas = (Canvas)getScene().lookup("#diagramArea");
        canvas.setFocusTraversable(true);
    }

    /**
     * Get the directory to use for File/New, File/Open, and File/Save.
     *
     * If this window is not associated with any file, get the default script
     * location from the Application. If the window is associated with a file,
     * then use that file's parent directory.
     *
     * @return The directory to use for File/New, File/Open, and File/Save.
     */
    public File getFileDirectory()
    {
        if (file == null) {
            return Gamma.getDefaultDirectory();
        }
        return file.getParentFile();

    }

//    /**
//     * Set the current LCodeEngine. If a prior engine exists, close it
//     * down.
//     *
//     * @param lCodeEngine The lcode engine to set.
//     */
//    public void setLCodeEngine(LCodeEngine lCodeEngine)
//    {
//        if (this.lCodeEngine != null) this.lCodeEngine.close();
//        this.lCodeEngine = lCodeEngine;
//    }
//
     /**
     * Get the current DiagramEngine, if any.
     *
     * @return The current diagram engine, or null if none.
     */
    public DiagramEngine getDiagramEngine()
    {
        return diagramEngine;
    }

     /**
     * Set the current DiagramEngine. If a prior engine exists, close it
     * down.
     *
     * @param diagramEngine The diagram engine to set.
     */
    public void setDiagramEngine(DiagramEngine diagramEngine)
    {
        if (this.diagramEngine != null) this.diagramEngine.close();
        this.diagramEngine = diagramEngine;
    }

   /**
     * Return the canvas.
     *
     * @return The canvas
     */
    public Canvas getCanvas()
    {
        return canvas;
    }

    /**
     * Show an Alert with a text area for the body.
     *
     * @param type The type of Alert.
     * @param title The Alert's title.
     * @param header The Alert's header text.
     * @param content The text to place in the text area
     * @param block If true, block until the user dismisses the dialog
     */
    public void showTextAreaAlert(Alert.AlertType type, String title,
                                  String header, String content, boolean block)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);

        TextArea area = new TextArea(content);
        area.setWrapText(true);
        area.setEditable(false);

        alert.getDialogPane().setContent(area);
        alert.getDialogPane().setHeaderText(header);
        alert.setResizable(true);

        if (block) {
            alert.showAndWait();
        }
        else {
            alert.show();
        }
    }

    /**
     * Add text to the HCode's print dialog.
     *
     * @param str The text to add. A newline is added to the string.
     */
    public void print(String str)
    {
        if (printDialog == null) printDialog = new PrintDialog(this);
        printDialog.appendText(str + "\n");
        printDialog.show();
    }

    public void clearPrintDialog()
    {
        if (printDialog != null) printDialog.clear();
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 73 * hash + this.ID;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MainWindow other = (MainWindow) obj;
        return this.ID == other.ID;
    }

}
