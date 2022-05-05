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
package org.freixas.gamma;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.freixas.gamma.execution.DiagramEngine;
import org.freixas.gamma.execution.ScriptPrintDialog;
import org.freixas.gamma.execution.SlideshowEngine;
import org.freixas.gamma.file.FileWatcher;
import org.freixas.gamma.file.URLFile;
import org.freixas.gamma.parser.ParseException;
import org.freixas.gamma.parser.Parser;
import org.freixas.gamma.preferences.PreferencesManager;
import org.freixas.gamma.value.ChoiceVariable;
import org.freixas.gamma.value.DisplayVariable;
import org.freixas.gamma.value.RangeVariable;
import org.freixas.gamma.value.ToggleVariable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * This is the main window of the Gamma application. Each window manages its own script,
 * so everything involved in running a script needs to eventually use a context associated
 * with this window.
 *
 * @author Antonio Freixas
 *
 */
@SuppressWarnings({ "FieldCanBeLocal", "unused" })
public final class MainWindow extends Stage
{
    // The toggle for whether to display the Greetings dialog or not.
    // This is global to all main windows -- only the first main window displayed
    // should display the Greetings dialog, and only if the setting is enabled

    static private boolean displayGreetingsDialog = PreferencesManager.getDisplayGreetingMessage();

    private final int ID;                       // The window's ID
    private URLFile script;                     // The associated script
    private final File[] directoryDefaults;     // The default dirs for each type of file

    // Various menu items

    private MenuItem fileMenuExportDiagram;
    private MenuItem fileMenuExportVideo;
    private MenuItem fileMenuPrint;
    private MenuItem fileMenuClose = null;

    // Various toolbar items

    private Button toolbarFileNew;
    private Button toolbarFileOpen;
    private Button toolbarFileOpenURL;
    private Button toolbarFileExportDiagram;
    private Button toolbarReload;
    private Button toolbarSlideshowStart;
    private Button toolbarSlideshowPrevious;
    private Button toolbarSlideshowPlayPause;
    private Button toolbarSlideshowNext;
    private Button toolbarSlideshowEnd;

    // Various containers/controls

    private VBox top;
    private SplitPane controlsSplitter;
    private VBox diagramParent;
    private ScrollPane scrollPane;
    private VBox displayControlArea;

    private ScriptPrintDialog scriptPrintDialog = null;

    private Screen screen;                      // The screen the window originated on

    private boolean hasDisplayControls;         // True if display controls exist

    // Used to monitor for changes in the associated script

    private FileWatcher watcher = null;
    private Thread watcherThread = null;

    private SlideshowEngine slideshowEngine;    // The associated slideshow engine
    private DiagramEngine diagramEngine;        // The diagram engine
    private Canvas canvas;                      // The diagram drawing area

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a main window.
     *
     * @param ID The ID assigned to this window.
     * @param name The name of the associated script file (may be null).
     * @param isURL True if the name should be treated as a URL.
     * @param directoryDefaults The default directories to use when opening or
     * saving files.
     *
     * @throws Exception For any exception.
     */

    public MainWindow(int ID, String name, boolean isURL, File[] directoryDefaults) throws Exception
    {
        // We can't fully deal with the script until the main window is
        // instantiated.

        this.ID = ID;
        this.script = null;
        this.directoryDefaults = directoryDefaults;

        this.slideshowEngine = null;
        this.diagramEngine = null;

        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        MainWindowController controller = loader.getController();
        controller.setMainWindow(this);
        setScene(new Scene(root));

        // The FXML file has display controls by default. This just means that we have
        // a side panel that comes from the FXML file

        hasDisplayControls = true;

        // Handle tasks that can only be performed on instantiated window.

        setOnShown((WindowEvent t) -> {
            locateUIElements();
            setCloseState(Gamma.getWindowCount() > 1);

            try {
                setScript(name, isURL);
            }
            catch (MalformedURLException e) {
                showTextAreaAlert(Alert.AlertType.ERROR, "Invalid URL", "Invalid URL", "The URL given for the script is invalid: '" + name +"'", true);
            }

            if (displayGreetingsDialog) {
                try {
                    GreetingsDialog greetings = new GreetingsDialog(this);
                    greetings.show();
                    displayGreetingsDialog = false;
                }
                catch (Exception ignored) { /* e.printStackTrace(); */ }
            }
        });

        this.setOnCloseRequest((WindowEvent t) -> Gamma.closeWindow((MainWindow)t.getSource()));

        // Add icons

        InputStream icon16 = getClass().getResourceAsStream("/gamma-icon-16x16.png");
        InputStream icon24 = getClass().getResourceAsStream("/gamma-icon-24x24.png");
        InputStream icon32 = getClass().getResourceAsStream("/gamma-icon-32x32.png");
        InputStream icon48 = getClass().getResourceAsStream("/gamma-icon-48x48.png");
        InputStream icon256 = getClass().getResourceAsStream("/gamma-icon-256x256.png");

        if (icon16 != null) getIcons().add(new Image(icon16));
        if (icon24 != null) getIcons().add(new Image(icon24));
        if (icon32 != null) getIcons().add(new Image(icon32));
        if (icon48 != null) getIcons().add(new Image(icon48));
        if (icon256 != null) getIcons().add(new Image(icon256));

        // Show the window

        show();
    }

    // **********************************************************************
    // *
    // * Getters / Setters
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
    @SuppressWarnings("unused")
    public int getID()
    {
        return ID;
    }

    /**
     * Get all current default directories being used for various file dialogs.
     *
     * @return The current default directories being used for various file dialogs.
     */

    public File[] getDirectoryDefaults()
    {
        return directoryDefaults;
    }

    /**
     * Get the default directory to use for file dialogs for a specific type
     * of file
     * <p>
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
            // an associated file, use its parent directory

            if (type == Gamma.FileType.SCRIPT && script != null && script.isFile()) {
                directoryDefaults[type.getValue()] = script.getFile().getParentFile();
            }

            // Otherwise, ask for the global default

            else {
                directoryDefaults[type.getValue()] = PreferencesManager.getDefaultDirectory(type);
            }
        }
        return directoryDefaults[type.getValue()];
    }

    /**
     * Set the default directory to use for file dialogs for a specific type of
     * file.
     *
     * @param type The type of file whose default directory we want.
     * @param dir The default directory to use.
     */
    public void setDefaultDirectory(Gamma.FileType type, File dir)
    {
        if (dir == null) return;
        if (!dir.exists()) return;
        if (!dir.isDirectory()) dir = dir.getParentFile();

        directoryDefaults[type.getValue()] = dir;
    }

    public SlideshowEngine getSlideShowEngine()
    {
        return slideshowEngine;
    }

    public void setSlideshowEngine(SlideshowEngine slideshowEngine)
    {
        this.slideshowEngine = slideshowEngine;
    }

    /**
     * Get the script associated with this main window. The script can be
     * null if there is no associated script file or URL.
     *
     * @return The script associated with this main window.
     */
    public URLFile getScript()
    {
        return script;
    }

    /**
     * Set the script file associated with this main window by providing a name.
     * The name can be the name of a URL or a file, or it can be null. URLs are
     * identified either because the name contains "://" or because isURL is
     * true.
     *
     * @param name The name of the script file.
     * @param isURL True if the name should be interpreted as a URL.
     *
     * @throws MalformedURLException If the URL is not properly formed.
     */
    public void setScript(String name, boolean isURL) throws MalformedURLException
    {
        URLFile script = name == null ? null : new URLFile(name, isURL);
        setScript(script, new ArrayList<>(), true);
    }

     /**
     * Set the script file associated with this main window. The file can be
     * null.
     * <p>
     * Certain menu items are enabled or disabled depending on whether the script
     * is null. The title bar is updated to display the file name. If the script
     * is not null, setting the associated script is run.
     *
     * @param script The script file associated with this window.
     * @param dependentFiles The files referenced by the script.
     * @param open True if the file was just opened
     */
    public void setScript(@Nullable URLFile script, ArrayList<URLFile> dependentFiles, boolean open)
    {
        try {
            boolean isNewScript;
            boolean disable = script == null;

            // Enable/disable various File Menu entries

            fileMenuExportDiagram.setDisable(disable);
            fileMenuExportVideo.setDisable(disable);
            fileMenuPrint.setDisable(disable);

            toolbarFileExportDiagram.setDisable(disable);
            toolbarReload.setDisable(disable);

            // Update the title bar

            setTitle("Gamma - " + (script != null ? script.toString() : ""));

            // In case we ever decide to support disassociating a window from
            // a script

            if (script == null) {
                setTitle("Gamma");

                if (watcherThread != null) {
                    watcher.stopThread();
                }

            }

            // The URLFile is a file

            else if (script.isFile()) {

                // Decide if we have a new script file
                // If the file was just opened, it's always a new file, even if
                // the user re-opens the same file

                File scriptFile = script.getFile();

                isNewScript = open || !script.equals(this.script);

                // Determine if we must create a new watcher. It's possible to call setScript()
                // with the same set of files

                if (watcherThread == null || isNewScript || !watcher.hasSameFiles(scriptFile, dependentFiles)) {

                    // Stop any existing watcher

                    if (watcherThread != null) {
                        watcher.stopThread();
                    }

                    // The final parameter tells the file watcher whether we have
                    // a new script file (requiring an immediate parse/display) or
                    // whether we are just updating the dependent files

                    watcher = new FileWatcher(scriptFile, dependentFiles, this, isNewScript);
                    watcherThread = new Thread(watcher);
                    watcherThread.start();
                    if (diagramEngine != null) {
                        diagramEngine.close();
                        diagramEngine = null;
                    }
                }

                // Save the new file and update the default directory for scripts

                this.script = script;
                setDefaultDirectory(Gamma.FileType.SCRIPT, scriptFile);

                // Is this a new script? If so, try to open the editor on it

                if (isNewScript) {

                    // Check to make sure the file exists

                    if (script.getFile().isFile()) {
                        String editorCommand = PreferencesManager.getEditorCommand();
                        if (editorCommand.length() > 0) {
                            editorCommand = editorCommand.replace("$F$", script.toString());
                            try {
                                Platform.cmd(editorCommand);
                            }
                            catch (Exception e) {
                                showTextAreaAlert(
                                    Alert.AlertType.ERROR,
                                    "Editor Command Error", "Editor Command Error",
                                    "Error when trying to execute this editor command:\n\n" + editorCommand + "\n\n" +
                                        "The reported error is:\n\n" +
                                        e.getLocalizedMessage(),
                                    true);
                            }
                        }
                    }
                }
            }

            // The URLFile is a URL

            else {

                // Load the URL contents

                String urlContents = script.readString();

                // Set the associated script

                this.script = script;

                // Parse it

                Parser parser = new Parser(this.script, urlContents);
                parser.parse();

                // Start up the diagram engine.

                DiagramEngine dEngine =
                    new DiagramEngine(
                        this, parser.getHCodes(),
                        parser.isAnimated(),
                        parser.getSetStatement(), parser.getStylesheet());
                dEngine.execute();
            }
        }
        catch (ParseException e) {
            showParseException(e);
        }
        catch (IOException e) {
            showTextAreaAlert(Alert.AlertType.ERROR,"URL Read Error", "URL Read Error", "Couldn't read " + script, true);
        }
     }

    /**
     * Get the screen on which this window was created.
     *
     * @return The screen on which this window was created.
     */
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
     * Get the canvas. The canvas is the area in which diagrams are drawn.
     *
     * @return The canvas
     */
    public Canvas getCanvas()
    {
        return canvas;
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

        for (Menu menu : menus) {
            if (menu.getId().equals("fileMenu")) {
                ObservableList<MenuItem> menuItems = menu.getItems();

                for (MenuItem menuItem : menuItems) {
                    String menuId = menuItem.getId();

                    if (menuId != null) {
                        switch (menuId) {
                            case "fileMenuExportDiagram" -> fileMenuExportDiagram = menuItem;
                            case "fileMenuExportVideo" -> fileMenuExportVideo = menuItem;
                            case "fileMenuPrint" -> fileMenuPrint = menuItem;
                            case "fileMenuClose" -> fileMenuClose = menuItem;
                            default -> {
                            }
                        }
                    }
                }
            }
        }

        // Toolbar items

        toolbarFileNew = (Button)getScene().lookup("#toolbar-file-new");
        toolbarFileOpen = (Button)getScene().lookup("#toolbar-file-open");
        toolbarFileOpenURL = (Button)getScene().lookup("#toolbar-file-open-url");
        toolbarFileExportDiagram = (Button)getScene().lookup("#toolbar-file-export-diagram");
        toolbarReload = (Button)getScene().lookup("#toolbar-reload");
        toolbarSlideshowStart = (Button)getScene().lookup("#toolbar-slideshow-start");
        toolbarSlideshowPrevious = (Button)getScene().lookup("#toolbar-slideshow-previous");
        toolbarSlideshowPlayPause = (Button)getScene().lookup("#toolbar-slideshow-play-pause");
        toolbarSlideshowNext = (Button)getScene().lookup("#toolbar-slideshow-next");
        toolbarSlideshowEnd = (Button)getScene().lookup("#toolbar-slideshow-end");

        // The top of the window tree

        top = (VBox)getScene().lookup("#top");

        // We start with a SplitPane that can contain the diagram area and the
        // display controls. We don't need it unless some display controls
        // are added

        controlsSplitter = (SplitPane)getScene().lookup("#controlsSplitter");

        // We use a parent for our drawing area (a Canvas) because Canvas
        // doesn't resize. The parent will resize according to its parent
        // containers rules, and we can use it to resize the canvas (when we
        // don't have a fixed diagram size).

        diagramParent = (VBox)getScene().lookup("#diagramParent");
        canvas = (Canvas)getScene().lookup("#diagramArea");

        // The scroll pane provides scrolling support for all the display
        // controls and appears on the right side pane in the SplitPane

        scrollPane = (ScrollPane)getScene().lookup("#scrollPane");

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
    public void enableDisplayControls(boolean enable)
    {
        if (enable && !hasDisplayControls) {
            top.getChildren().add(2, controlsSplitter);
            controlsSplitter.getItems().addAll(diagramParent, scrollPane);
            controlsSplitter.setDividerPositions(.9, .1);
            VBox.setVgrow(diagramParent, Priority.ALWAYS);
            hasDisplayControls = true;
            top.layout();
        }

        if (!enable && hasDisplayControls) {
            controlsSplitter.getItems().clear();
            top.getChildren().remove(controlsSplitter);
            top.getChildren().add(2, diagramParent);
            VBox.setVgrow(diagramParent, Priority.ALWAYS);
            hasDisplayControls = false;
            top.layout();
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
            slider.setShowTickMarks(true);
            slider.setShowTickLabels(true);

            double delta = range.getMaxValue() - range.getMinValue();
            slider.setMajorTickUnit(delta / 10.0);
            slider.setMinorTickCount(5);
            slider.setBlockIncrement(delta / 100.0);
            slider.setPrefWidth(200);
            slider.setMaxWidth(Double.MAX_VALUE);
            displayControlArea.getChildren().add(slider);

            slider.valueProperty().addListener(
                (value, oldValue, newValue) -> {
                    Point2D p = slider.localToScene(0.0, 0.0);
                    Tooltip tooltip = slider.getTooltip();
                    if (tooltip == null) {
                        tooltip = new Tooltip();
                        slider.setTooltip(tooltip);
                    }
                    tooltip.setText(Double.toString((Double)newValue));
                    Bounds bounds = slider.lookup(".thumb").getBoundsInParent();
                    Scene scene = slider.getScene();
                    tooltip.show(slider,
                                 p.getX() + scene.getX() + scene.getWindow().getX() + bounds.getCenterX(),
                                 p.getY() + scene.getY() + scene.getWindow().getY() + bounds.getHeight());
                    range.setCurrentValue((Double)newValue);
                });

            slider.focusedProperty().addListener(
                (value, oldValue, newValue) -> {
                    Tooltip tooltip = slider.getTooltip();
                    if (tooltip != null) tooltip.hide();
                });
            slider.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    double value = range.getInitialValue();
                    slider.setValue(value);
                }
            });

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
     * Display syntax errors.
     *
     * @param e A ParseException.
     */
    public void showParseException(ParseException e)
    {
        try {
            SyntaxErrorDialog dialog = new SyntaxErrorDialog(this);
            dialog.displayError(e);
        }

        // If our SyntaxError dialog fails, use a plain Alert dialog

        catch (Exception e2) {
            // e2.printStackTrace();
            showTextAreaAlert(Alert.AlertType.ERROR, "Syntax Error", "Syntax Error", e.getLocalizedMessage(), true);
        }
    }

    /**
     * Display runtime errors.
     *
     * @param e A ParseException.
     */
    public void showRuntimeException(GammaRuntimeException e)
    {
        try {
            RuntimeErrorDialog dialog = new RuntimeErrorDialog(this);
            dialog.displayError(e);
        }

        // If our RuntimeError dialog fails, use a plain Alert dialog

        catch (Exception e2) {
            // e2.printStackTrace();
            showTextAreaAlert(Alert.AlertType.ERROR, "Runtime Error", "Runtime Error", e.getLocalizedMessage(), true);
        }
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
        URL resource = getClass().getResource("/AlertDialog.css");
        if (resource != null) alert.getDialogPane().getStylesheets().add(resource.toExternalForm());
        alert.setTitle(title);

        String[] lines = content.split("\\R");
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(line.length(), maxLength);
        }

        Label label = new Label(content);
        label.getStyleClass().add("alertLabel");
        label.setWrapText(true);

        alert.getDialogPane().setContent(label);
        alert.getDialogPane().setHeaderText(header);
        alert.getDialogPane().setPrefWidth(400);
        alert.setResizable(true);

        alert.setOnShown(e -> {
            Font font = label.getFont();
            Bounds bounds = org.freixas.gamma.drawing.Label.getTextBounds(content, font);
            double width = Math.max(400, Math.min(bounds.getWidth() + 40, 800));
            alert.getDialogPane().setMinWidth(width);
        });
        if (block) {
            alert.showAndWait();
        }
        else {
            alert.show();
        }
    }

    /**
     * Add text to the h-code's Print dialog text area.
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
                showTextAreaAlert(
                        Alert.AlertType.ERROR, "Error", "Error",
                        "Failed to open a Script Print dialog: " + e.getLocalizedMessage(), true);
            }
        }
        scriptPrintDialog.appendText(str + "\n");
        scriptPrintDialog.show();
    }

    /**
     * Clear the h-code's Print dialog text area.
     */

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
        if (fileMenuClose != null) fileMenuClose.setDisable(!enable);
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
        diagramEngine = null;
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
