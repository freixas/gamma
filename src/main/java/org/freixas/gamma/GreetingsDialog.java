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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class handles the Greetings dialog. This is displayed by default the first time
 * the user runs the program and until they disable it (it can be re-enabled in
 * the Preferences dialog).
 *
 * @author Antonio Freixas
 */
public final class GreetingsDialog extends Stage
{

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a Greetings dialog.
     *
     * @param window The parent window.
     * @throws IOException if there is a problem loading the associated FXML file.
     */
    public GreetingsDialog(MainWindow window) throws IOException
    {
        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/GreetingsDialog.fxml"));
        VBox root = loader.load();
        setScene(new Scene(root));

        initOwner(window);

        setResizable(true);
        setTitle("Welcome!");
    }

}
