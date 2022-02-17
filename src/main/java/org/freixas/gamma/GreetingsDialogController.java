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
package org.freixas.gamma;

import org.freixas.gamma.preferences.PreferencesManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * FXML Controller class for the Greetings dialog.
 *
 * @author Antonio Freixas
 */
public final class GreetingsDialogController implements Initializable
{
    @FXML
    private GridPane buttonBar;
    @FXML
    private Button closeButton;
    @FXML
    private CheckBox displayOption;
    @FXML
    private WebView html;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // Monitor the display toggle

        displayOption.selectedProperty().addListener(
            (Object, oldValue, newValue) -> PreferencesManager.setDisplayGreetingMessage(!newValue));

        // Load the greetings message

        WebEngine engine = html.getEngine();
        URL greetingsURL = getClass().getResource("/greetings.html");
        if (greetingsURL != null) {
            engine.load(greetingsURL.toString());
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closeButton.getScene().getWindow().hide();
    }

}
