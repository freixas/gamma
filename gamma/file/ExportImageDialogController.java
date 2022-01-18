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
package gamma.file;

import gamma.preferences.PreferencesManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Antonio Freixas
 */
public class ExportImageDialogController implements Initializable
{

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ToggleGroup format;
    @FXML
    private RadioButton gif;
    @FXML
    private RadioButton jpg;
    @FXML
    private RadioButton png;
    @FXML
    private RadioButton tiff;
    @FXML
    private Slider compression;
    @FXML
    private CheckBox progressive;

    /**
     * Initializes the controller class.
     */

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        dialogPane.setExpandableContent(null);

        RadioButton[] radioButtons = { gif, jpg, png, tiff };
        int imageFormat = PreferencesManager.getImageFormat();
        radioButtons[imageFormat].setSelected(true);

        compression.setValue(PreferencesManager.getImageCompression());
        progressive.setSelected(PreferencesManager.getImageProgressive());

        setState(imageFormat == ExportImageDialog.ImageType.JPG.getValue());

        radioButtons[ExportImageDialog.ImageType.JPG.getValue()]
            .selectedProperty()
            .addListener((obj, oldValue, value) -> {
                setState(value);
            });
    }

    public void saveSettings()
    {
        RadioButton[] radioButtons = { gif, jpg, png, tiff };
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                PreferencesManager.setImageFormat(i);
                break;
            }
        }

        Double compressionValue = compression.getValue();
        PreferencesManager.setImageCompression(compressionValue.floatValue());

        PreferencesManager.setImageProgressive(progressive.isSelected());
    }

    private void setState(boolean enableJPG)
    {
        if (enableJPG) {
            compression.setDisable(false);
            progressive.setDisable(false);
        }
        else {
            compression.setDisable(true);
            progressive.setDisable(true);
        }
    }

}
