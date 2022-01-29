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

/**
 *
 * @author Antonio Freixas
 */
public class ScriptErrorDialog extends Stage
{

    public static final String HTML_PREFIX =
        "<!DOCTYPE html>" +
        "<html>" +
        "<head>" +
        "<meta charset=\"UTF-8\">" +
        "<title>Syntax Error</title>" +
        "<style>" +
        "body {" +
        "font: .7em Verdana, Geneva, sans-serif;" +
        "}" +
        ".program { padding-left: 2em; white-space: pre; border-left: solid 5px #B88; }" +
        ".file, .lineNumber, .charNumber { font-weight: bold; }" +
        " .ok {" +
        "color: black;" +
        "}" +
        " .error {" +
        "color: #B00;" +
        "}" +
        " .unknown {" +
        "color: #AAA;" +
        "}" +
        " .msg {" +
        "font-style: italic;" +
        "}" +
        "</style>" +
        "</head>" +
        "<body>";

    public static final String HTML_SUFFIX =
        "</body>" +
        "</html>";

    private final Object controller;


    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    public ScriptErrorDialog(MainWindow window, String resourceName, String  title) throws Exception
    {
        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(resourceName));
        VBox root = loader.load();
        controller = loader.getController();
        setScene(new Scene(root));

        initOwner(window);

        setResizable(true);
        setTitle(title);
    }

    public Object getController()
    {
        return controller;
    }

    /**
     * Get two full lines before the line containing the error (if that many
     * lines are available).
     *
     * @param code The script code.
     * @param end The character position for the character before the error text.
     *
     * @return The text that precedes the error.
     */
    protected String getLinesBeforeError(String code, int end)
    {
        if (end < 0) return "";

        int lineCount = 0;
        int start = end;

        while (start > -1) {
            char c = code.charAt(start);
            if (c == '\n') {
                lineCount++;
                if (lineCount > 2) {
                    return code.substring(start + 1, end + 1).replace("\n", "<br/>");
                }
            }
            start--;
        }
        return code.substring(0, end + 1).replace("\n", "<br/>");
    }

    /**
     * Get two full lines after the line containing the error (if that many
     * lines are available).
     *
     * @param code The script code.
     * @param start The character position for the character after the error text.
     *
     * @return The text that follows the error.
     */
    protected String getLinesAfterError(String code, int start)
    {
        int end = start;

        int lineCount = 0;

        while (end < code.length() - 1) {
            char c = code.charAt(end);
            if (c == '\n') {
                lineCount++;
                if (lineCount > 3) {
                    return code.substring(start, end).replace("\n", "<br/>");
                }
            }
            end++;
        }
        return code.substring(start, code.length()).replace("\n", "<br/>");

    }



}
