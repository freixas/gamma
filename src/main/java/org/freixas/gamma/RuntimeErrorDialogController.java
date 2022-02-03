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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author Antonio Freixas
 */
public class RuntimeErrorDialogController {

    @FXML
    private GridPane buttonBar;

    @FXML
    private Button closeButton;

    @FXML
    private WebView html;

    @FXML
    private Label headerTitle;

    @FXML
    private Label headerSubtitle;


    @FXML
    void handleClose(ActionEvent event) {
        closeButton.getScene().getWindow().hide();
    }

    public void setForProgrammingError()
    {
        headerTitle.setText("Programming Error");
        headerSubtitle.setText("This error should not have occurred:");
    }

    public void setHTML(String htmlContent)
    {
        WebEngine engine = html.getEngine();
        engine.loadContent(htmlContent);
    }

}
