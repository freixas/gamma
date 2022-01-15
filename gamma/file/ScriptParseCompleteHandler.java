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
package gamma.file;

import gamma.MainWindow;
import gamma.execution.*;
import gamma.parser.Parser;
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

    public ScriptParseCompleteHandler(MainWindow window, Parser parser)
    {
        this.window = window;
        this.parser = parser;
    }

    @Override
    public void run()
    {
        // The parser may detect some dependent files, such as stylesheets or
        // include files. Any changes to those should  also cause the script to
        // reload, so we need to tell the main window about the changes

        File file = parser.getFile();
        ArrayList<File> dependentFiles = parser.getDependentFiles();
        window.setFile(file, dependentFiles);

        // The parser has finished. Start up the diagrame engine.

        DiagramEngine dEngine =
            new DiagramEngine(
                window, parser.getHCodes(),
                parser.isAnimated(), parser.hasDisplayVariables(),
                parser.getSetStatement(), parser.getStylesheet());
        dEngine.execute();
    }

}
