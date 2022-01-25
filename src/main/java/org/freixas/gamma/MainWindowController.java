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

import gamma.file.ExportDiagramDialog;
import gamma.preferences.PreferencesDialog;
import java.io.File;
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
 * FXML Controller class
 *
 * The controller reacts to user events identified by the UI elements. Some
 * reactions can be handled here, but the majority of the work may have to be
 * performed by some other class.The MainWindow class acts as the tool that
 * connects the controller with any other classes it may require.
 *
 * @author Antonio Freixas
 */
public class MainWindowController implements Initializable
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

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @FXML
    private void fileMenuNew(ActionEvent event)
            throws Exception
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

    @FXML
    private void fileMenuOpen(ActionEvent event)
            throws Exception
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

    @FXML
    private void fileMenuExportDiagram(ActionEvent event) throws Exception
    {
        ExportDiagramDialog exportImageDialog = new ExportDiagramDialog(mainWindow);
        exportImageDialog.showDialog();
    }

    @FXML
    private void fileMenuExportVideo(ActionEvent event)
    {
        new Alert(Alert.AlertType.INFORMATION, "File Save Video called!").show();
    }

    @FXML
    private void fileMenuPrint(ActionEvent event)
    {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job.showPrintDialog(mainWindow)) {
            job.printPage(mainWindow.getCanvas());
        }
    }

    @FXML
    private void fileMenuClose(ActionEvent event)
    {
        Gamma.closeWindow(mainWindow);
    }

    @FXML
    private void fileMenuPreferences(ActionEvent event) throws Exception
    {
        PreferencesDialog preferencesDialog = new PreferencesDialog();
        preferencesDialog.show(mainWindow);
    }

    @FXML
    private void fileMenuExit(ActionEvent event)
    {
        Gamma.exit();
    }

    @FXML
    private void windowMenuNewWindow(ActionEvent event) throws Exception
    {
        Gamma.newMainWindow(null, mainWindow.getDirectoryDefaults());
    }

    @FXML
    private void helpMenuContents(ActionEvent event)
    {
        new Alert(Alert.AlertType.INFORMATION, "Help Menu Contents called!").show();
    }

    @FXML
    private void helpMenuAbout(ActionEvent event)
    {
        new Alert(Alert.AlertType.INFORMATION, "Help About called!").show();
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
