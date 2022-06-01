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
import org.freixas.gamma.execution.*;
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
    // The ID to assign to the next FileWatcher we create

    static private int nextWatcherID = -1;

    // The toggle for whether to display the Greetings dialog or not.
    // This is global to all main windows -- only the first main window displayed
    // should display the Greetings dialog, and only if the setting is enabled

    static private boolean displayGreetingsDialog = PreferencesManager.getDisplayGreetingMessage();

    private final int ID;                       // The window's ID

    private URLFile mainScript;                 // The associated main script
    private Parser mainScriptParser;            // The parser used to parse the main script

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

    private int watcherID;
    private FileWatcher watcher;
    private Thread watcherThread;

    private Slideshow slideshow;                // The associated slideshow
    private SlideshowEngine slideshowEngine;    // The associated slideshow engine
    private Slideshow.Slide slide;              // The associated slide, if any
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
        this.mainScript = null;
        this.mainScriptParser = null;
        this.directoryDefaults = directoryDefaults;

        this.watcherID = -1;
        this.watcher = null;
        this.watcherThread = null;

        this.slideshow = null;
        this.slideshowEngine = null;
        this.slide = null;
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

            associateMainScript(name, isURL);

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

            if (type == Gamma.FileType.SCRIPT && mainScript != null && mainScript.isFile()) {
                directoryDefaults[type.getValue()] = mainScript.getFile().getParentFile();
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
        return mainScript;
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
    // * Manage Script Association
    // *
    // **********************************************************************

    /**
     * Associate a script file with this window. This is main script file,
     * meaning it can be a regular or slideshow script. This method should be
     * called when the user associates a script with a window using "File/Open"
     * or "File/Open URL".
     * <p>
     * If the given name contains "://" or if isURL is true, the name is treated
     * as a URL; otherwise, it is treated as a filename. If the name is null,
     * the main window is disassociated from any script file.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Any associated script is terminated and disassociated.
     *     <li>If the script is null, this method is done.
     *     <li>The script is read. If it can't be read, an error is reported and
     *         the script is not associated with this window. The user must again
     *         use "File/Open" or "File/Open URL".
     *     <li>The script is associated with the window.
     *     <li>The script is parsed.
     *     <li>If a syntax error occurs, an error is displayed. The user can
     *         again use "File/Open" or "File/Open URL" or, if the script is
     *         local, just save the script again.
     *     <li>If the script is on the local file system, a FileWatcher is
     *         started  to monitor for changes in the script or in any
     *         dependent file.
     *     <li>If the file is a regular script, it is executed.
     *     <li>If the file is a slideshow, all dependent scripts are parsed. If
     *         any of them have syntax errors, all syntax errors for all
     *         dependent scripts are displayed. The user can
     *         again use "File/Open" or "File/Open URL" or, if the script or any
     *         dependent script is local, just save it.
     * </ul>
     *
     * @param name The name of the script file.
     * @param isURL True if the name should be interpreted as a URL.
     */
    public void associateMainScript(@Nullable String name, boolean isURL)
    {
        // End anything we previously have running

        disassociateMainScript();

        // If the given name is null, we're done

        if (name == null) return;

        // Create a URLFile from the name

        URLFile script;
        try {
            script = new URLFile(name, isURL);
        }
        catch (MalformedURLException e) {
            showTextAreaAlert(Alert.AlertType.ERROR, "Invalid URL", "Invalid URL", e.getLocalizedMessage(), true);
            return;
        }

        // Read the contents

        String content;
        try {
            content = script.readString();
        }
        catch (IOException e) {
            showTextAreaAlert(Alert.AlertType.ERROR, "I/O Error", "I/O Error", new GammaIOException(e).getLocalizedMessage(), true);
            return;
        }

        // If we've reached this point, we can now associate this main script
        // file with this window

        mainScript = script;

        if (mainScript.isFile()) {

            // If this is a local file, we can also update the default directory for
            // script files to point to the directory in which the script exists

            setDefaultDirectory(Gamma.FileType.SCRIPT, mainScript.getFile());

            // If this is a local file and we have an editor command, try to
            // open the script in the editor

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

        // Parse the main script and execute it

        parseAndExecuteMainScript(content);
    }

    /**
     * Reload a modified associated main script file. This means that the file
     * (and any dependent files) are reloaded from local disk or from the web
     * and parsed again. Note that it is possible for a script to change from
     * slideshow to regular or vice versa.
     * <p>
     * Generally, this would be called when a FileWatcher notices that the
     * contents of the script file or of any dependent files have changed. Only
     * local files are monitored, not URLs.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Any associated script is terminated.
     *     <li>The script is read. If it can't be read, an error is reported and
     *         the script is disassociated from this window. The user must
     *         again use "File/Open" or "File/Open URL".
     *     <li>The script is parsed.
     *     <li>If the script is on the local file system and any dependent
     *         files have changed, the existing FileWatcher is terminated and
     *         a new one started.
     *     <li>If a syntax error occurs, an error is displayed. The user can
     *         again use "File/Open" or "File/Open URL" or, if the script is
     *         local, just save the script again.
     *     <li>If the file is a regular script, it is executed.
     *     <li>If the file is a slideshow, all dependent scripts are parsed. If
     *         any of them have syntax errors, all syntax errors for all
     *         dependent scripts are displayed. The user can again use
     *         "File/Open" or "File/Open URL" or, if the script or any
     *         dependent script is local, just save it.
     * </ul>
     */
    public void reloadModifiedMainScript()
    {
        // End anything we previously have running

        terminateAssociatedScript();

        // If we don't have an associated file, we're done

        if (mainScript == null) return;

        // Read the contents of the currently associated file. If we get an
        // error, we disassociate the file

        String content;
        try {
            content = mainScript.readString();
        }
        catch (IOException e) {
            disassociateMainScript();
            showTextAreaAlert(Alert.AlertType.ERROR, "I/O Error", "I/O Error", new GammaIOException(e).getLocalizedMessage(), true);
            return;
        }

        // Parse the main script and execute it

        parseAndExecuteMainScript(content);
    }

    /**
     * Reload the current script file from the whatever was last read and parsed.
     * This method is called when the user asks that the file be reloaded.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>If there is no associated script file, it does nothing.
     *     <li>If the script had a syntax error, it does nothing.
     *     <li>The associated script file is terminated.
     *     <li>If the main script is a slideshow, the current slide is restarted.
     *     <li>If the main script is a regular script, it is restarted.
     * </ul>
     */
    public void reloadScript()
    {
        // No associated file

        if (diagramEngine == null) return;

        // This counts as a user interaction

        userInteractionOccurred();

        // Close the current diagram engine and start a new one

        diagramEngine.close();
        if (slideshowEngine != null) {
            diagramEngine = new DiagramEngine(this, slideshowEngine.getCurrentSlide().getParser());
        }
        else {
            diagramEngine = new DiagramEngine(this, mainScriptParser);
        }
        diagramEngine.execute();
    }

    /**
     * Parse a main script file and execute it.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>The script is parsed.
     *     <li>If a syntax error occurs, an error is displayed. The user can
     *         again use "File/Open" or "File/Open URL" or, if the script is
     *         local, just save the script again.
     *     <li>If the script is on the local file system, a FileWatcher is
     *         started  to monitor for changes in the script or in any
     *         dependent file.
     *     <li>If the file is a regular script, it is executed.
     *     <li>If the file is a slideshow, all dependent scripts are parsed. If
     *         any of them have syntax errors, all syntax errors for all
     *         dependent scripts are displayed. The user can
     *         again use "File/Open" or "File/Open URL" or, if the script or any
     *         dependent script is local, just save it.
     * </ul>
     *
     * @param content The contents of the main script file.
     */
    private void parseAndExecuteMainScript(String content)
    {
        // Make sure the title is correct

        setTitle("Gamma - " + mainScript.getName());

        // Parse the main script.

        ParseException parseException = null;
        mainScriptParser = new Parser(mainScript, content);
        try {
            mainScriptParser.parse();
        }
        catch (ParseException e) {
            parseException = e;
        }

        // Reuse an existing FileWatcher or start a new one

        setupFileWatcher();

        // If we have a syntax error, display it, and we're done

        if (parseException != null) {
            showParseException(parseException);
            return;
        }

        // The script file is valid. Enable various file menu and toolbar
        // entries

        fileMenuExportDiagram.setDisable(false);
        fileMenuExportVideo.setDisable(false);
        fileMenuPrint.setDisable(false);

        toolbarFileExportDiagram.setDisable(false);
        toolbarReload.setDisable(false);

        // We need to either fire off a DiagramEngine or a SlideshowEngine

        if (mainScriptParser.isSlideshow()) {
            slideshow = mainScriptParser.getSlideshow();
            slideshowEngine = new SlideshowEngine(this, slideshow);
            slideshowEngine.execute();
        }

        else {
            diagramEngine = new DiagramEngine(this, mainScriptParser);
            diagramEngine.execute();
        }
    }

    /**
     * Execute a slide in a slideshow.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Any running slide is terminated.
     *     <li>The new slide is executed.
     * </ul>
     *
     * @param slide The slide to execute.
     */
    public void runSlide(Slideshow.Slide slide) throws ExecutionException, ProgrammingException
    {
        if (diagramEngine != null) diagramEngine.close();
        diagramEngine = null;

        // Add the slide name from the display

        setTitle("Gamma - " + mainScript.getName() + " - " + slide.getUrlFile().getName());

        diagramEngine = new DiagramEngine(this, slide.getParser());
        diagramEngine.execute();
    }

    /**
     * Terminate the associated script and disassociate it from this window.
     */
    private void disassociateMainScript()
    {
        // Stop monitoring

        stopFileWatcher();

        // Terminate any running engines

        terminateAssociatedScript();

        // Set the title back to default

        setTitle("Gamma");

        mainScript = null;
        mainScriptParser = null;
    }

    /**
     * Terminate the associated script. This may be permanent, or we may want to
     * restart the script.
     */
    private void terminateAssociatedScript()
    {
        // Close a slideshow if one exists

        if (slideshow != null) {

            if (slideshowEngine != null) slideshowEngine.close();

            slideshow = null;
            slideshowEngine = null;
            slide = null;

            // Clear the slide name from the display

            setTitle("Gamma - " + mainScript.getName());
        }

        // Close a diagram engine if one is running. It is possible to have
        // both a slideshow and diagram engine running

        if (diagramEngine != null)  {
            diagramEngine.close();
            diagramEngine = null;
        }

        // Disable various file menu and toolbar entries

        fileMenuExportDiagram.setDisable(true);
        fileMenuExportVideo.setDisable(true);
        fileMenuPrint.setDisable(true);

        toolbarFileExportDiagram.setDisable(true);
        toolbarReload.setDisable(true);
    }

    /**
     * This is called when a non-animated diagram is fully drawn or when the
     * last frame of an animated diagram is fully drawn.
     */
    public void diagramCompleted()
    {
        if (slideshowEngine != null) {
            slideshowEngine.slideDone();
        }
    }

    /**
     * This is called whenever a user interaction occurs:
     * <ul>
     *     <li>The user zooms or pans.
     *     <li>The user reloads the slide.
     *     <li>The user manipulates a GUI control.
     * </ul>
     * If we are running a slideshow, this information is passed on to the
     * slideshow engine; otherwise, it is ignored.
     */
    public void userInteractionOccurred()
    {
        if (slideshowEngine == null) return;
        slideshowEngine.userInteraction();
    }

    // **********************************************************************
    // *
    // * Manage the FileWatcher
    // *
    // **********************************************************************

    /**
     * Start a new FileWatcher.
     * <p>
     * A FileWatcher is never started if the main script file is not a local
     * file.
     */
    private void startNewFileWatcher()
    {
        // Terminate any existing watcher

        stopFileWatcher();

        // Start a new one, but only if we have a local file

        if (mainScript != null && mainScript.isFile()) {

            // Get the dependent files if we have any

            ArrayList<URLFile> dependentFiles = null;
            if (mainScriptParser != null) dependentFiles = mainScriptParser.getDependentFiles();

            // Create the watcher

            watcherID = nextWatcherID++;
            watcher = new FileWatcher(nextWatcherID, mainScript.getFile(), dependentFiles, this);
            watcherThread = new Thread(watcher);
            watcherThread.start();
        }
    }

    /**
     * Re-use an existing FileWatcher, if one is running; otherwise, start a new
     * one. This method is called when we already have a FileWatcher for the
     * main script file, but the file has been recompiled and might have new
     * dependent files.
     * <p>
     * A FileWatcher is never re-used if the main script file is not a local
     * file.
     * <p>
     * A new FileWatcher is started if
     * <ul>
     *     <li>No FileWatcher exists, or
     *     <li>The current file watcher is monitoring a different set of files
     *     than what we need.
     * </ul>
     */
    public void setupFileWatcher()
    {
        // Check to see if we can re-use the existing FileWatcher. To re-use a
        // FileWatcher, one must exist and be monitoring the exact same files
        // as what we now need to monitor

        ArrayList<URLFile> dependentFiles = null;
        if (mainScriptParser != null) dependentFiles = mainScriptParser.getDependentFiles();

        if (watcher == null || !watcher.hasSameFiles(mainScript.getFile(), dependentFiles)) {
            startNewFileWatcher();
        }
    }

    public void stopFileWatcher()
    {
        if (watcherThread != null) {
            watcher.stopThread();
            watcher = null;
            watcherID = -1;
            watcherThread = null;
        }
    }

    /**
     * If an exception occurs in a FileWatcher, we report it here, but only if
     * it is a FileWatcher that we are currently using.
     *
     * @param ID The ID of the watcher that generated the exception.
     * @param e The exception we need to report.
     */
    public void fileWatcherException(int ID, Exception e)
    {
        if (ID == watcherID) {
            showTextAreaAlert(Alert.AlertType.ERROR, "Error", "Error", e.getLocalizedMessage(), true);
        }
    }

    /**
     * If a FileWatcher stops, we need to remove any references we have to it. If
     * this is not a FileWatcher that we are currently using, we ignore it.
     *
     * @param ID The ID of the watcher that stopped.
     */
    public void fileWatcherDone(int ID)
    {
        if (ID == watcherID) {
            watcher = null;
            watcherID = -1;
            watcherThread = null;
        }
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

        final MainWindow window = this;

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
                    window.userInteractionOccurred();

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
                    window.userInteractionOccurred();

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
                (value, oldValue, newValue) -> {
                    window.userInteractionOccurred();
                    bool.setBooleanCurrentValue(newValue);
                });
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
                (value, oldValue, newValue) -> {
                    window.userInteractionOccurred();
                    choice.setIntCurrentValue(newValue.intValue() + 1);
                });
        }

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPrefWidth(200);
        displayControlArea.getChildren().add(separator);
    }

    /**
     * Remove all the display controls that might have been added.
     */
    public void removeAllDisplayControls()
    {
        displayControlArea.getChildren().clear();
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
        stopFileWatcher();
        mainScript = null;

        if (slideshowEngine != null) slideshowEngine.close();
        slideshowEngine = null;
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
