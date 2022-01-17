/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021-2022  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gamma.preferences;

import gamma.Gamma;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Antonio Freixas
 */
public class PreferencesDialogController implements Initializable
{
    @FXML
    private TextField scriptsDir;
    @FXML
    private TextField imagesDir;
    @FXML
    private TextField videosDir;
    @FXML
    private TextField stylesheetName;
    @FXML
    private TextField editorCommand;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private CheckBox greetings;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        dialogPane.setExpandableContent(null);

        greetings.setSelected(PreferencesManager.getDisplayGreetingMessage());

        ChangeListener<String> validateDir = (obj, oldText, text) -> {
            StringProperty prop = (StringProperty)obj;
            TextField field = (TextField)prop.getBean();
            File dir = new File(text);
            if (dir.isDirectory()) {
                field.setStyle("-fx-text-fill: black;");
            }
            else {
                field.setStyle("-fx-text-fill: red;");
            }
        };
        scriptsDir.textProperty().addListener(validateDir);
        imagesDir.textProperty().addListener(validateDir);
        videosDir.textProperty().addListener(validateDir);

        ChangeListener<String> validateFile = (obj, oldText, text) -> {
            StringProperty prop = (StringProperty)obj;
            TextField field = (TextField)prop.getBean();
            File dir = new File(text);
            if (dir.isFile()) {
                field.setStyle("-fx-text-fill: black;");
            }
            else {
                field.setStyle("-fx-text-fill: red;");
            }
        };
        stylesheetName.textProperty().addListener(validateFile);

        String file = PreferencesManager.getDefaultScriptsDirectory();
        scriptsDir.setText(file);
        file = PreferencesManager.getDefaultImagesDirectory();
        imagesDir.setText(file);
        file = PreferencesManager.getDefaultVideosDirectory();
        videosDir.setText(file);

        file = PreferencesManager.getDefaultStylesheet();
        stylesheetName.setText(file);

        String cmd = PreferencesManager.getEditorCommand();
        editorCommand.setText(cmd);
    }


    @FXML
    private void selectScriptsDirectory(ActionEvent event)
    {
        selectDirectory(scriptsDir, "Scripts");
    }


    @FXML
    private void selectImagesDirectory(ActionEvent event)
    {
        selectDirectory(imagesDir, "Images");
    }


    @FXML
    private void selectVideosDirectory(ActionEvent event)
    {
        selectDirectory(videosDir, "Videos");
    }


    @FXML
    private void selectStylesheetFile(ActionEvent event)
    {
        selectFile(stylesheetName, "Stylesheet");
    }

    private void selectDirectory(TextField field, String type)
    {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Default " + type + " Directory");

        File initDir = new File(field.getText());
        if (!initDir.isDirectory()) {
            if (initDir.isFile()) {
                initDir = initDir.getParentFile();
            }
            else {
                initDir = Gamma.USER_DATA_HOME;
            }
        }
        chooser.setInitialDirectory(initDir);

        File selectedFile;
        Window window = field.getScene().getWindow();
        selectedFile = chooser.showDialog(window);
        if (selectedFile != null && selectedFile.isDirectory()) {
            field.setText(selectedFile.toString());
        }
    }

    private void selectFile(TextField field, String type)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose " + type + " File");

        File initDir = new File(field.getText());
        if (!initDir.isDirectory()) {
            if (initDir.isFile()) {
                initDir = initDir.getParentFile();
            }
            else {
                initDir = Gamma.USER_DATA_HOME;
            }
        }
        chooser.setInitialDirectory(initDir);

        File selectedFile;
        Window window = field.getScene().getWindow();
        selectedFile = chooser.showOpenDialog(window);
        if (selectedFile != null && selectedFile.isFile()) {
            field.setText(selectedFile.toString());
        }
    }

    public void updatePreferences()
    {
        PreferencesManager.setDisplayGreetingMessage(greetings.isSelected());

        String scriptsDirName = scriptsDir.getText();
        PreferencesManager.setDefaultScriptsDirectory(scriptsDirName != null ? scriptsDirName : "");

        String imagesDirName = imagesDir.getText();
        PreferencesManager.setDefaultImagesDirectory(imagesDirName != null ? imagesDirName : "");

        String videosDirName = videosDir.getText();
        PreferencesManager.setDefaultVideosDirectory(videosDirName != null ? videosDirName : "");

        String name = stylesheetName.getText();
        PreferencesManager.setDefaultStylesheet(name != null ? name : null);

        String cmd = editorCommand.getText();
        PreferencesManager.setEditorCommand(cmd != null && cmd.length() > 0? cmd : "");
    }

}
