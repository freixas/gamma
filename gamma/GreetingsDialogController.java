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
package gamma;

import gamma.preferences.PreferencesManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.web.HTMLEditor;

/**
 * FXML Controller class
 *
 * @author Antonio Freixas
 */
public class GreetingsDialogController implements Initializable
{
    @FXML
    private GridPane buttonBar;
    @FXML
    private Button closeButton;
    @FXML
    private HTMLEditor html;
    @FXML
    private CheckBox displayOption;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try {
            @SuppressWarnings("CollectionsToArray")
            Node[] nodes = html.lookupAll(".tool-bar").toArray(new Node[0]);
            for (Node node : nodes) {
                node.setVisible(false);
                node.setManaged(false);
            }

            displayOption.selectedProperty().addListener(
                (Object, oldValue, newValue) -> {
                    PreferencesManager.setDisplayGreetingMessage(!newValue);
                });

            InputStream input = getClass().getResourceAsStream("/gamma/resources/greetings.html");
            String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            html.setHtmlText(text);
        }
        catch (IOException e) {
            html.setHtmlText(
                "<html><head></head><body contenteditable=\"false\">" +
                 "<p>There was a problem reading the welcome message: " + e.getLocalizedMessage() + "</p>" +
                "</body></html>");
        }
    }

    @FXML
    void handleClose(ActionEvent event) {
        closeButton.getScene().getWindow().hide();
    }

}
