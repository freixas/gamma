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
package org.freixas.gamma.file;

import org.freixas.gamma.MainWindow;
import org.freixas.gamma.execution.*;
import org.freixas.gamma.parser.Parser;
import java.io.File;
import java.util.ArrayList;

/**
 * This handler is run when a script is parsed successfully.
 *
 * @author Antonio Freixas
 */
public class ScriptParseCompleteHandler implements Runnable
{
    private final MainWindow window;
    private final Parser parser;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create the handler.
     *
     * @param window The window associated with the parsed script.
     * @param parser The parser used to parse the script.
     */
    public ScriptParseCompleteHandler(MainWindow window, Parser parser)
    {
        this.window = window;
        this.parser = parser;
    }

    // **********************************************************************
    // *
    // * Run
    // *
    // **********************************************************************

    @Override
    public void run()
    {
        // If we arrive here, the script URL should be a file

        URLFile script = parser.getScriptURL();

        // The parser may detect some dependent files, such as stylesheets or
        // include files. Any changes to those should  also cause the script to
        // reload, so we need to tell the main window about the changes

        ArrayList<URLFile> dependentFiles = parser.getDependentFiles();
        window.setScript(script, dependentFiles, false);

        // The parser has finished. Start up the diagram engine.

        DiagramEngine dEngine =
            new DiagramEngine(
                window, parser.getHCodes(),
                parser.isAnimated(),
                parser.getSetStatement(), parser.getStylesheet());
        dEngine.execute();
    }

}
