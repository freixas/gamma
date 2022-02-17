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

import java.awt.Desktop;

import org.freixas.gamma.file.ExportDiagramDialog;
import org.freixas.gamma.preferences.PreferencesDialog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

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
    private MenuItem fileMenuNew;
    @FXML
    private MenuItem fileMenuOpen;
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
    private MenuItem windowMenuNewWindow;
    @FXML
    private MenuItem helpMenuContents;
    @FXML
    private MenuItem helpMenuAbout;
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu windowMenu;
    @FXML
    private Menu helpMenu;

    private MainWindow mainWindow;
    @FXML
    private MenuItem helpSampleScripts;
    @FXML
    private MenuItem helpQuickStart;

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
                mainWindow.setScript(selectedFile, null);
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
            mainWindow.setScript(selectedFile, null);
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
        Gamma.newMainWindow(null, mainWindow.getDirectoryDefaults());
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
            mainWindow.setScript(selectedFile, null);
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
        try {
            File helpFile = new File(Gamma.HELP_LOCATION.getAbsolutePath() + "/quick-start.html");
            Desktop.getDesktop().browse(helpFile.toURI());
        }
        catch (IOException e) {
            mainWindow.showTextAreaAlert(
                Alert.AlertType.ERROR, "Help Error", "Help Error",
                "Error when trying to view help:\n\n" + e.getLocalizedMessage() + "\n\n" +
                    "Look in the installation folder for help/quick-start.html and open it in your browser.",
                true);
        }
    }

    /**
     * Help / Content
     * <p>
     * Display the help content in a browser window.
     */
    @FXML
    private void helpMenuContents(ActionEvent ignoredEvent)
    {
        try {
            File helpFile = new File(Gamma.HELP_LOCATION.getAbsolutePath() + "/index.html");
            Desktop.getDesktop().browse(helpFile.toURI());
        }
        catch (IOException e) {
            mainWindow.showTextAreaAlert(
                Alert.AlertType.ERROR, "Help Error", "Help Error",
                "Error when trying to view help:\n\n" + e.getLocalizedMessage() + "\n\n" +
                    "Look in the installation folder for help/index.html and open it in your browser.",
                true);
        }
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

}
