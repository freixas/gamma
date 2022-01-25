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

import gamma.execution.ScriptPrintDialog;
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
import gamma.preferences.PreferencesManager;
import gamma.value.ChoiceVariable;
import gamma.value.ToggleVariable;
import gamma.value.DisplayVariable;
import gamma.value.RangeVariable;
import java.io.IOException;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

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
    private File script;
    private final File[] directoryDefaults;
    private final MainWindowController controller;

    private MenuItem fileMenuExportDiagram;
    private MenuItem fileMenuExportVideo;
    private MenuItem fileMenuPrint;
    private MenuItem fileMenuClose = null;
    private MenuItem fileMenuPreferences;

    private boolean hasDisplayControls;

    private VBox top;
    private SplitPane controlsSplitter;
    private VBox diagramParent;
    private ScrollPane scrollPane;
    private VBox displayControlArea;

    private Screen screen;

    private FileWatcher watcher = null;
    private Thread watcherThread = null;

    private DiagramEngine diagramEngine = null;
    private Canvas canvas;

    private static boolean displayGreetingsDialog = PreferencesManager.getDisplayGreetingMessage();
    private ScriptPrintDialog scriptPrintDialog = null;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a main window.
     *
     * @param ID The ID assigned to this window.
     * @param script The associated script file (may be null).
     * @throws Exception
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MainWindow(int ID, File script, File[] directoryDefaults) throws Exception
    {
        // We can't fully deal with the file until the main window is
        // instantiated.

        this.ID = ID;
        this.script = null;
        this.directoryDefaults = directoryDefaults;

        // The script file must be an absolute file or else it gets messed
        // up when it's turned into an absolute path in the FileWatcher

        final File absoluteScript = script!= null ? script.getAbsoluteFile() : null;

        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("resources/MainWindow.fxml"));
        Parent root = loader.load();
        controller = (MainWindowController)loader.getController();
        controller.setMainWindow(this);
        setScene(new Scene(root));

        // The FXML file has display controls by default

        hasDisplayControls = true;

        // Handle tasks that can only be performed on instantiated window.

        setOnShown((WindowEvent t) -> {
            locateUIElements();
            setCloseState(Gamma.getWindowCount() > 1);
            setScript(absoluteScript, new ArrayList<>());
            if (displayGreetingsDialog) {
                try {
                    GreetingsDialog greetings = new GreetingsDialog(this);
                    greetings.show();
                    displayGreetingsDialog = false;
                }
                catch (Exception e) { }
            }
        });

        this.setOnCloseRequest((WindowEvent t) -> {
            Gamma.closeWindow((MainWindow)t.getSource());
        });

        // Add icons

        getIcons().addAll(
            new Image(getClass().getResourceAsStream("/gamma/resources/gamma-icon-16x16.png")),
            new Image(getClass().getResourceAsStream("/gamma/resources/gamma-icon-24x24.png")),
            new Image(getClass().getResourceAsStream("/gamma/resources/gamma-icon-32x32.png")),
            new Image(getClass().getResourceAsStream("/gamma/resources/gamma-icon-48x48.png")),
            new Image(getClass().getResourceAsStream("/gamma/resources/gamma-icon-256x256.png"))
        );

        show();
    }

    // **********************************************************************
    // *
    // * Getters / Settings
    // *
    // **********************************************************************

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
     * Get the script file associated with this main window. The file can be
     * null.
     *
     * @return the script file associated with this main window.
     */
    public File getScript()
    {
        return script;
    }

    /**
     * Get the current default directories being used for various file dialogs.
     *
     * @return The current default directories being used for various file dialogs.
     */

    public File[] getDirectoryDefaults()
    {
        return directoryDefaults;
    }

    public Screen getScreen()
    {
        ObservableList<Screen> screens = Screen.getScreensForRectangle(getX(), getY(), 1.0, 1.0);
        if (screens.size() < 1) {
            if (screen != null) return screen;
            screen = Screen.getPrimary();
            return screen;
        }
        screen = screens.get(0);
        return screen;
    }

    /**
     * Set the script file associated with this main window. The file can be
     * null.
     * <p>
     *
     * Certain menu items are enabled or disabled depending on whether the script
     * is null. The title bar is updated to display the file name. If the script
     * is not null, the associated script is run.
     *
     * @param script The script file associated with this window.
     */
    public void setScript(File script, ArrayList<File> dependentFiles)
    {
        boolean disable = script == null;

        // Enable/disable various File Menu entries

        fileMenuExportDiagram.setDisable(disable);
        fileMenuExportVideo.setDisable(disable);
        fileMenuPrint.setDisable(disable);

        if (script != null) {

            // Update the title bar

            setTitle("Gamma - " + script.getName());

            // Determine if we must create a new watcher

            if (watcherThread == null || !watcher.hasSameFiles(script, dependentFiles)) {

                // Stop any existing watcher

                if (watcherThread != null) {
                    watcher.stopThread();
                }

                // The final parameter tells the file watcher whether we have
                // a new script file (requiring an immediate parse/display) or
                // whether we are just updating the dependent files

                watcher = new FileWatcher(script, dependentFiles, this, !script.equals(this.script));
                watcherThread = new Thread(watcher);
                watcherThread.start();
                if (diagramEngine != null) {
                    diagramEngine.close();
                    diagramEngine = null;
                }
            }
        }

        // Did we change the file? If so, try to open the editor on it

        if (script != null && !script.equals(this.script)) {
            String editorCommand = PreferencesManager.getEditorCommand();
            if (editorCommand.length() > 0) {
                editorCommand = editorCommand.replace("$F$", script.toString());
                try {
                    Runtime.getRuntime().exec(editorCommand);
                }
                catch (IOException e) {
                    showTextAreaAlert(
                        Alert.AlertType.ERROR, "Editor Command Error", "Editor Command Error",
                        "Error when trying to execute this editor command:\n\n" + editorCommand +"\n\n" +
                        "Error is:\n\n" +
                        e.getLocalizedMessage(),
                        true);
                }
            }
        }

        this.script = script;
    }

    /**
     * Get the current DiagramEngine associated with this window, if any.
     *
     * @return The current diagram engine, or null if none.
     */
    public DiagramEngine getDiagramEngine()
    {
        return diagramEngine;
    }

    /**
     * Associate a DiagramEngine with this window. If a prior association
     * exists, close it down.
     *
     * @param diagramEngine The diagram engine to set.
     */
    public void setDiagramEngine(DiagramEngine diagramEngine)
    {
        if (this.diagramEngine != null) this.diagramEngine.close();
        this.diagramEngine = diagramEngine;

        // Clean up any display variables we may have added

        displayControlArea.getChildren().clear();

        // Disable display controls by default

        enableDisplayControls(false);
    }

    /**
     * Return the canvas. The canvas is the area in which diagrams are drawn.
     *
     * @return The canvas
     */
    public Canvas getCanvas()
    {
        return canvas;
    }

    /**
     * Get the default directory to use for various file dialogs.
     *
     * If we already have a default for the file type, use it. If we don't,
     * but we have a script file and the request is for scripts, use the
     * script's parent directory. Otherwise, use a global default.
     *
     * @param type The type of file whose default directory we want.
     *
     * @return The directory to use for various file dialogs.
     */
    public File getDefaultDirectory(Gamma.FileType type)
    {
        if (directoryDefaults[type.getValue()] == null) {

            // If we want the default directory to use for a script and we have
            // an associated file, use it's parent directory

            if (type == Gamma.FileType.SCRIPT && script != null) {
                directoryDefaults[type.getValue()] = script.getParentFile();
            }

            // Otherwise, ask for the global default

            else {
                directoryDefaults[type.getValue()] = PreferencesManager.getDefaultDirectory(type);
            }
        }
        return directoryDefaults[type.getValue()];
    }

    // **********************************************************************
    // *
    // * GUI Management
    // *
    // **********************************************************************

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

        // The top of the window tree

        top = (VBox)getScene().lookup("#top");

        // We start with a SplitPane that can contain the diagram area and the
        // display controls. We don't need it unless some display controls
        // are added

        controlsSplitter = (SplitPane)getScene().lookup("#controlsSplitter");

        // We use a parent for our drawing area (a Canvas) because Canvas
        // doesn't resize. The parent will resize according to its parent
        // containers rules and we can use it to resize the canvas (when we
        // don't have a fixed diagram size).

        diagramParent = (VBox)getScene().lookup("#diagramParent");
        canvas = (Canvas)getScene().lookup("#diagramArea");

        // The scroll pane provides scrolling support for all the display
        // controls and appears in the right side pane in the SplitPane

        scrollPane = (ScrollPane)getScene().lookup("#scrollPane");
//        scrollPane.managedProperty().bind(scrollPane.visibleProperty());
//        scrollPane.setVisible(false);

        // The display control area holds all the display controls

        displayControlArea = (VBox)getScene().lookup("#displayControlArea");

        // If we don't do this, we can't capture keyboard events in the Canvas

        canvas.setFocusTraversable(true);

        // We start out with no display controls, so we need to remove the split
        // pane from the diagram container and reparent the diagram area

        enableDisplayControls(false);
    }

    /**
     * Set up the GUI for plain windows or windows with display controls.
     *
     * @param enable True if this window will have display controls.
     */
    private void enableDisplayControls(boolean enable)
    {
        if (enable && !hasDisplayControls) {
            top.getChildren().add(1, controlsSplitter);
            controlsSplitter.getItems().addAll(diagramParent, scrollPane);
            controlsSplitter.setDividerPositions(.9, .1);
            VBox.setVgrow(diagramParent, Priority.ALWAYS);
            hasDisplayControls = true;
        }

        if (!enable && hasDisplayControls) {
            controlsSplitter.getItems().clear();
            top.getChildren().remove(controlsSplitter);
            top.getChildren().add(1, diagramParent);
            VBox.setVgrow(diagramParent, Priority.ALWAYS);
            hasDisplayControls = false;
        }
    }

    /**
     * Add display controls to this window.
     *
     * @param var The display variable to use to create the display control
     * component.
     */
    public void addDisplayControl(DisplayVariable var) {
        enableDisplayControls(true);

        if (var instanceof RangeVariable range) {
            Label label = new Label(range.getLabel());
            label.setPadding(new Insets(10.0));
            displayControlArea.getChildren().add(label);

            Slider slider = new Slider(range.getMinValue(), range.getMaxValue(), range.getInitialValue());
            slider.setShowTickMarks​(true);
            slider.setShowTickLabels(true);

            double delta = range.getMaxValue() - range.getMinValue();
            slider.setMajorTickUnit(delta / 10.0);
            slider.setMinorTickCount(5);
            slider.setBlockIncrement(delta / 100.0);
            slider.setPrefWidth(200);
            slider.setMaxWidth(Double.MAX_VALUE);
            displayControlArea.getChildren().add(slider);

            slider.valueProperty().addListener(
                (value, oldValue, newValue) -> range.setCurrentValue((Double)newValue));

        }
        else if (var instanceof ToggleVariable bool) {
            CheckBox button = new CheckBox(bool.getLabel());
            button.setAllowIndeterminate(false);
            button.setSelected(bool.getBooleanCurrentValue());
            button.setPadding(new Insets(10.0));
            button.setPrefWidth(200);
            button.setMaxWidth(Double.MAX_VALUE);
            displayControlArea.getChildren().add(button);

            button.selectedProperty().addListener(
                (value, oldValue, newValue) -> bool.setBooleanCurrentValue(newValue));
        }
        else if (var instanceof ChoiceVariable choice) {
            Label label = new Label(choice.getLabel());
            label.setPadding(new Insets(10.0));
            displayControlArea.getChildren().add(label);

            String[] choices = choice.getChoices();
            ChoiceBox<String> box = new ChoiceBox<>(FXCollections.observableArrayList(choices));
            box.getSelectionModel().select(choice.getIntCurrentValue() - 1);
            box.setPrefWidth(200);
            box.setMaxWidth(Double.MAX_VALUE);
            displayControlArea.getChildren().add(box);

            box.getSelectionModel().selectedIndexProperty().addListener(
                (value, oldValue, newValue) -> choice.setIntCurrentValue(newValue.intValue() + 1));
        }

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPrefWidth(200);
        displayControlArea.getChildren().add(separator);
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
    public void scriptPrint(String str)
    {
        if (scriptPrintDialog == null) {
            try {
                scriptPrintDialog = new ScriptPrintDialog(this);
            }
            catch (Exception e) {
                showTextAreaAlert(Alert.AlertType.ERROR, "Error", "Error", "Failed to open a Script Print dialog: " + e.getLocalizedMessage(), true);
            }
        }
        scriptPrintDialog.appendText(str + "\n");
        scriptPrintDialog.show();
    }

    public void clearScriptPrintDialog()
    {
        if (scriptPrintDialog != null) scriptPrintDialog.clear();
    }

    // **********************************************************************
    // *
    // * Manage the window
    // *
    // **********************************************************************

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
     * Close this window and delete it.
     */
    @Override
    public void close()
    {
        if (watcherThread != null) {
            watcher.stopThread();
            watcher = null;
            watcherThread = null;
        }
        script = null;

        if (diagramEngine != null) diagramEngine.close();
        super.close();
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

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
