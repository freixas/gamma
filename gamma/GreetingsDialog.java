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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Antonio Freixas
 */
public class GreetingsDialog extends Stage
{
    private final MainWindow window;
    private final VBox root;
    private final GreetingsDialogController controller;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    public GreetingsDialog(MainWindow window) throws Exception
    {
        this.window = window;

        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/gamma/resources/GreetingsDialog.fxml"));
        root = loader.load();
        controller = (GreetingsDialogController)loader.getController();
        setScene(new Scene(root));

        initOwner(window);

        setResizable(true);
        setTitle("Script Print");
    }

}
