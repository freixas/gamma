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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.freixas.gamma.css.value.Stylesheet;
import org.freixas.gamma.execution.DiagramEngine;
import org.freixas.gamma.execution.hcode.SetStatement;
import org.freixas.gamma.file.ExportDiagramDialog;
import org.freixas.gamma.file.URLFile;
import org.freixas.gamma.preferences.PreferencesDialog;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller for the main window.
 *
 * @author Antonio Freixas
 */
public final class MainWindowController implements Initializable
{
    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem fileMenuNew;
    @FXML
    private MenuItem fileMenuOpen;
    @FXML
    private MenuItem fileMenuOpenURL;
    @FXML
    private MenuItem fileMenuExportDiagram;
    @FXML
    private MenuItem fileMenuExportVideo;
    @FXML
    private MenuItem fileMenuPrint;
    @FXML
    private MenuItem fileMenuClose;
    @FXML
    private MenuItem fileMenuPreferences;
    @FXML
    private MenuItem fileMenuExit;

    @FXML
    private Menu windowMenu;

    @FXML
    private MenuItem windowMenuNewWindow;

    @FXML
    private Menu helpMenu;
    @FXML
    private MenuItem helpSampleScripts;
    @FXML
    private MenuItem helpMenuContents;
    @FXML
    private MenuItem helpMenuAbout;

    @FXML
    private Button toolbarFileNew;
    @FXML
    private Button toolbarFileOpen;
    @FXML
    private Button toolbarFileOpenURL;

    @FXML
    private Button toolbarFileExportDiagram;

    @FXML
    private Button toolbarReload;

    @FXML
    private Button toolbarSlideshowStart;
    @FXML
    private Button toolbarSlideshowPlayPause;
    @FXML
    private Button toolbarSlideshowNext;
    @FXML
    private Button toolbarSlideshowPrevious;
    @FXML
    private Button toolbarSlideshowEnd;

    private MainWindow mainWindow;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
    }

    /**
     *  File / New
     *  <p>
     *  Creates a new script file and associates it with the main window.
     *
     * @throws IOException If there is a problem with creating a new file.
     */
    @FXML
    private void fileMenuNew(ActionEvent ignoredEvent) throws IOException
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Create New Script File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("All Files", "*.*"));
        fileChooser.setInitialDirectory(mainWindow.getDefaultDirectory(Gamma.FileType.SCRIPT));

        File selectedFile;
        while ((selectedFile = fileChooser.showSaveDialog(mainWindow)) != null) {
            if (selectedFile.createNewFile()) {
                mainWindow.setScript(new URLFile(selectedFile), null, true);
                return;
            }
            new Alert(Alert.AlertType.ERROR, "Use File/Open to open an existing script.").showAndWait();
        }
    }

    /**
     * File / Open
     * <p>
     * Opens an existing script file and associates it with the main window.
     */
    @FXML
    private void fileMenuOpen(ActionEvent ignoredEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Existing Script File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("All Files", "*.*"));
        fileChooser.setInitialDirectory(mainWindow.getDefaultDirectory(Gamma.FileType.SCRIPT));

        File selectedFile = fileChooser.showOpenDialog(mainWindow);
        if (selectedFile != null) {
            mainWindow.setScript(new URLFile(selectedFile), null, true);
        }
    }

    /**
     * File / Open URL
     * <p>
     * Loads and parses content from a URL and associates it with the main window.
     */
    @FXML
    public void fileMenuOpenURL(ActionEvent ignoredEvent)
    {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            URL resource = getClass().getResource("/AlertDialog.css");
            if (resource != null) dialog.getDialogPane().getStylesheets().add(resource.toExternalForm());
            dialog.setTitle("Open a URL");
            dialog.setHeaderText("Enter a URL to open");
            dialog.setContentText("URL:");
            dialog.getEditor().setPrefColumnCount(40);
            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty() || result.get().length() < 1) return;

            String urlString = result.get();
            try {
                mainWindow.setScript(urlString, true);
                return;
            }
            catch (MalformedURLException e) {
                mainWindow.showTextAreaAlert(Alert.AlertType.ERROR, "Invalid URL", "Invalid URL", "The given URL '" + urlString + "' is invalid", true);
            }
        }
    }

    /**
     * File / Export Diagram
     * <p>
     * Export the diagram as an image.
     */
    @FXML
    private void fileMenuExportDiagram(ActionEvent ignoredEvent) throws Exception
    {
        ExportDiagramDialog exportImageDialog = new ExportDiagramDialog(mainWindow);
        exportImageDialog.showDialog();
    }

    /**
     * File / Export Video
     * <p>
     * Export the diagram as a video (not currently supported).
     */
    @FXML
    private void fileMenuExportVideo(ActionEvent ignoredEvent)
    {
        new Alert(Alert.AlertType.INFORMATION, "File Save Video called!").show();
    }

    /**
     * File / Print
     * <p>
     * Print the diagram (not currently supported).
     */
    @FXML
    private void fileMenuPrint(ActionEvent ignoredEvent)
    {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job.showPrintDialog(mainWindow)) {
            job.printPage(mainWindow.getCanvas());
        }
    }

    /**
     * File / Close
     * <p>
     * Close the main window.
     */
    @FXML
    private void fileMenuClose(ActionEvent ignoredEvent)
    {
        Gamma.closeWindow(mainWindow);
    }

    /**
     * File / Preferences
     * <p>
     * Display the Preferences dialog.
     */
    @FXML
    private void fileMenuPreferences() throws Exception
    {
        PreferencesDialog preferencesDialog = new PreferencesDialog();
        preferencesDialog.show(mainWindow);
    }

    /**
     * File / Exit
     * <p>
     * Exit the application.
     */
    @FXML
    private void fileMenuExit(ActionEvent ignoredEvent)
    {
        Gamma.exit();
    }

    /**
     * Window / New Window
     * <p>
     * Create a new main window.
     */
    @FXML
    private void windowMenuNewWindow(ActionEvent ignoredEvent) throws Exception
    {
        Gamma.newMainWindow(null, false, mainWindow.getDirectoryDefaults());
    }

    /**
     * Help / Sample Scripts
     * <p>
     * Display a File Open dialog on the sample scripts directory.
     */
    @FXML
    private void helpSampleScripts(ActionEvent ignoredEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open A Sample Script File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("All Files", "*.*"));
        fileChooser.setInitialDirectory(Gamma.SAMPLE_SCRIPTS_LOCATION);

        File selectedFile = fileChooser.showOpenDialog(mainWindow);
        if (selectedFile != null) {
            mainWindow.setScript(new URLFile(selectedFile), null, true);
        }

    }

    /**
     * Help / Quick Start
     * <p>
     * Display the quick start help content in a browser window.
     */
    @FXML
    private void helpQuickStart(ActionEvent ignoredEvent)
    {
        Platform.browseHelp("quick-start.html", mainWindow);
    }

    /**
     * Help / Content
     * <p>
     * Display the help content in a browser window.
     */
    @FXML
    private void helpMenuContents(ActionEvent ignoredEvent)
    {
        Platform.browseHelp("index.html", mainWindow);
    }

    /**
     * Help / About
     * <p>
     * Display the About dialog.
     */
    @FXML
    private void helpMenuAbout(ActionEvent ignoredEvent)
    {
        try {
            AboutDialog aboutDialog = new AboutDialog(mainWindow);
            aboutDialog.show();
        }
        catch (Exception ignored) {}
    }

    /**
     * Find and set the main window associated with this controller.
     *
     * @param window The MainWindow associated with this controller.
     */
    public void setMainWindow(MainWindow window)
    {
        mainWindow = window;
    }

    @FXML
    public void toolbarFileNew(ActionEvent ignoredEvent) throws IOException
    {
        fileMenuNew(ignoredEvent);
    }

    @FXML
    public void toolbarFileOpen(ActionEvent ignoredEvent)
    {
        fileMenuOpen(ignoredEvent);
    }

    @FXML
    public void toolbarFileOpenURL(ActionEvent ignoredEvent)
    {
        fileMenuOpenURL(ignoredEvent);
    }

    @FXML
    public void toolbarFileExportDiagram(ActionEvent ignoredEvent) throws Exception
    {
        fileMenuExportDiagram(ignoredEvent);
    }

    @FXML
    public void toolbarReload(ActionEvent ignoredEvent)
    {
        DiagramEngine engine = mainWindow.getDiagramEngine();
        if (engine == null) return;

        LinkedList<Object> hCodes = engine.getHCodes();
        boolean isAnimated = engine.isAnimated();
        SetStatement setStatement = engine.getSetStatement();
        Stylesheet stylesheet = engine.getStylesheet();

        engine = new DiagramEngine(mainWindow, hCodes, isAnimated, setStatement, stylesheet);
        engine.execute();
    }

    @FXML
    public void toolbarSlideshowStart(ActionEvent ignoredEvent)
    {
    }

    @FXML
    public void toolbarSlideshowPrevious(ActionEvent ignoredEvent)
    {
    }

    @FXML
    public void toolbarSlideshowPlayPause(ActionEvent ignoredEvent)
    {
    }

    @FXML
    public void toolbarSlideshowNext(ActionEvent ignoredEvent)
    {
    }

    @FXML
    public void toolbarSlideshowEnd(ActionEvent ignoredEvent)
    {
    }

}
