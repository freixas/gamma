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

import org.freixas.gamma.parser.TokenContext;

import java.io.IOException;

/**
 * The RuntimeError dialog displays script runtime errors in a nice
 * dialog that shows the location of the error along with some context.
 *
 * @author Antonio Freixas
 */
public final class RuntimeErrorDialog extends ScriptErrorDialog
{
    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create an RuntimeError dialog.
     *
     * @param window The parent window.
     * @throws IOException If the FXML file fails to load.
     */
    public RuntimeErrorDialog(MainWindow window) throws IOException
    {
        super(window, "/RuntimeErrorDialog.fxml", "Runtime Error");
    }

    // **********************************************************************
    // *
    // * Show
    // *
    // **********************************************************************

    /**
     * Display the RuntimeError dialog.
     *
     * @param e The GammaRuntime exception with all the error information.
     */
    public void displayError(GammaRuntimeException e)
    {
        String message = e.getLocalizedMessage();
        TokenContext context = e.getTokenContext();

        // We have two types of runtime exceptions: User problems
        // (Execution exceptions) and programming errors (Programming exceptions).
        // Set the appropriate type.

        if (e.getType() == GammaRuntimeException.Type.PROGRAMMING) {
            ((RuntimeErrorDialogController)getController()).setForProgrammingError();
        }

        String html;

        // For some errors, we might not have a context

        if (context != null) {
            String code = context.getCode();

            // For runtime errors, we only know the starting line for the
            // statement in which the error occurred

            int prevLineEnd = code.lastIndexOf("\n", context.getCharStart());
            String linesOK1 = prevLineEnd > -1 ? getLinesBeforeError(code, prevLineEnd) : null;

            int nextLineStart = code.indexOf("\n", context.getCharEnd());
            String linesOK2 = nextLineStart > -1 ? getLinesAfterError(code, nextLineStart) : null;

            int curLineStart = prevLineEnd > -1 ? prevLineEnd + 1 : 0;
            int curLineEnd = nextLineStart > - 1 ? nextLineStart : code.length();

            String lineError = code.substring(curLineStart, curLineEnd);

            // Create the message

            html =
                HTML_PREFIX +
                "<p>" +
                (context.getURLFile() != null ? "File <span class='file'>" + context.getURLFile().getPath() + "</span><br/>" : "") +
                "Line number: <span class='lineNumber'>" + context.getLineNumber() + "</span><br/>" +
                "Character number: <span class='charNumber'>" + context.getCharNumber() + "</span><br/>" +
                "</p>" +
                "<div class='program'><p>" +
                (linesOK1 != null ? "<span class='ok'>" + linesOK1 + "</span>" : "") +
                "<span class='error'>" + lineError + "</span>" +
                (linesOK2 != null ? "<span class='ok'>" + linesOK2 + "</span>" : "") +
                "</p></div>" +
                "<p>Error: <span class='msg'>" + message + "</span></p>" +
                (e.getType() == GammaRuntimeException.Type.PROGRAMMING ?
                "<p>You should not have received this error. Please report it to gamma@freixas.org.</p>" : "") +
                HTML_SUFFIX;
        }

        else {
            html =
                HTML_PREFIX +
                "<p>Error: <span class='msg'>" + message + "</span></p>" +
                (e.getType() == GammaRuntimeException.Type.PROGRAMMING ?
                "<p>You should not have received this error. Please report it to gamma@freixas.org.</p>" : "") +
                HTML_SUFFIX;
        }

        // Display it

        ((RuntimeErrorDialogController)getController()).setHTML(html);
        showAndWait();
    }

}
