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

import org.freixas.gamma.parser.ParseException;
import org.freixas.gamma.parser.TokenContext;

import java.io.IOException;

/**
 * The SyntaxError dialog displays script syntax errors in a nice
 * dialog that shows the location of the error along with some context.
 *
 * @author Antonio Freixas
 */
public final class SyntaxErrorDialog extends ScriptErrorDialog
{
    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create an SyntaxError dialog.
     *
     * @param window The parent window.
     * @throws IOException If the FXML file fails to load.
     */
    public SyntaxErrorDialog(MainWindow window) throws IOException
    {
        super(window, "/SyntaxErrorDialog.fxml", "Syntax Error");
    }

    // **********************************************************************
    // *
    // * Show
    // *
    // **********************************************************************

    /**
     * Display the SyntaxError  dialog.
     *
     * @param e The Parse exception with all the error information.
     */
    public void displayError(ParseException e)
    {
        TokenContext context = e.getToken().getContext();
        String code = context.getCode();
        String message = e.getLocalizedMessage();

        String linesOK1 = getLinesBeforeError(context.getCode(), context.getCharStart() - 1);
        String error = getError(context);
        String linesUnknown = getLinesAfterError(context.getCode(), context.getCharEnd() + 1);

        // Create the message

        String html =
            HTML_PREFIX +
            "<p>" +
            (context.getURLFile() != null ? "File <span class='file'>" + context.getURLFile().getPath() + "</span><br/>" : "") +
            "Line number: <span class='lineNumber'>" + context.getLineNumber() + "</span><br/>" +
            "</p>" +
            "<div class='program'><p>" +
            "<span class='ok'>" + linesOK1 + "</span>" +
            "<span class='error'>" + error + "</span>" +
            "<span class='unknown'>" + linesUnknown + "</span>" +
            "</p></div>" +
            "<p>Error: <span class='msg'>" + message + "</span></p>" +
            HTML_SUFFIX;

        // Display it

        ((SyntaxErrorDialogController)getController()).setHTML(html);
        showAndWait();
    }

    /**
     * Get the error token, formatted properly for HTML.
     *
     * @param context The token context.
     *
     * @return The error token as a String.
     */
    private String getError(TokenContext context)
    {
        return context.getCode().substring(context.getCharStart(), context.getCharEnd() + 1).replace("\n", "<br/>");
    }

}
