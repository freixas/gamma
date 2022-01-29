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

import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.parser.TokenContext;

/**
 *
 * @author Antonio Freixas
 */
public class RuntimeErrorDialog extends ScriptErrorDialog
{

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    public RuntimeErrorDialog(MainWindow window) throws Exception
    {
        super(window, "/RuntimeErrorDialog.fxml", "Runtime Error");
    }

    public void displayError(GammaRuntimeException e)
    {
        String message = e.getLocalizedMessage();
        TokenContext context = e.getTokenContext();
        String code = context.getCode();

        int prevLineEnd = code.lastIndexOf("\n", context.getCharStart());
        String linesOK1 = prevLineEnd > -1 ? getLinesBeforeError(code, prevLineEnd) : null;

        int nextLineStart = code.indexOf("\n", context.getCharEnd());
        String linesOK2 = nextLineStart > -1 ? getLinesAfterError(code, nextLineStart) : null;

        int curLineStart = prevLineEnd > -1 ? prevLineEnd + 1 : 0;
        int curLineEnd = nextLineStart > - 1 ? nextLineStart : code.length();

        String lineError = code.substring(curLineStart, curLineEnd);

        String html =
            HTML_PREFIX +
            "<p>" +
            (context.getFile() != null ? "File <span class='file'>" + context.getFile().getPath() + "</span><br/>" : "") +
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

        ((RuntimeErrorDialogController)getController()).setHTML(html);
        showAndWait();
    }

}
