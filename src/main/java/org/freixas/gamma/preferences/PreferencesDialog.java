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
package org.freixas.gamma.preferences;

import org.freixas.gamma.MainWindow;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;

/**
 *
 * @author Antonio Freixas
 */
public class PreferencesDialog extends Dialog<ButtonType>
{
    private final DialogPane dialogPane;
    private final PreferencesDialogController controller;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a preferences dialog.
     */
    public PreferencesDialog() throws Exception
    {
        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/PreferencesDialog.fxml"));
        dialogPane = loader.load();
        controller = (PreferencesDialogController)loader.getController();
        setDialogPane(dialogPane);
    }

    public void show(MainWindow window)
    {
        initOwner(window);
        initModality(Modality.APPLICATION_MODAL);

        setResizable(true);
        setTitle("Set Preferences");

        showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> controller.updatePreferences());
    }

}
