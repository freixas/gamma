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

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 *
 * @author Antonio Freixas
 */
public class PrintDialog extends Dialog<String>
{
    private final TextArea textArea;

    public PrintDialog(MainWindow window)
    {
        super();
        setTitle("Print Output");

        setResizable(true);
        initStyle(StageStyle.DECORATED);
        initModality(Modality.NONE);
        initOwner(window);

        textArea = new TextArea();
        textArea.setWrapText(false);
        textArea.setEditable(false);

        DialogPane pane = getDialogPane();
        pane.setHeaderText("Print Output");
        pane.setContent(textArea);

        ButtonType printButtonType = new ButtonType("Print");
        pane.getButtonTypes().addAll(printButtonType, ButtonType.CLOSE);
    }

    public void appendText(String str)
    {
        textArea.appendText(str);
    }

    public void clear()
    {
        textArea.clear();
    }

}
