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

import gamma.execution.hcode.HCode;
import gamma.MainWindow;
import gamma.ProgrammingException;
import gamma.execution.*;
import gamma.execution.lcode.Command;
import gamma.parser.Parser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

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
        // The parser has finished. Start up the diagrame engine.

        DiagramEngine dEngine =
            new DiagramEngine(window, parser.getHCodes(), parser.isAnimated());
        dEngine.execute();

//        // The parser's output is safe for use
//        // The parser object will not be re-used by any other thread.
//
//        debugHCode();
//
//        // Begin exection
//
//        HCodeEngine hEngine;
//        LCodeEngine lEngine;
//        ArrayList<Command> commands;
//        try {
//            hEngine = new HCodeEngine(parser.getHCodes());
//            hEngine.execute();
//            lEngine = hEngine.getLCodeEngine();
//            commands = lEngine.getCommands();
//        }
//        catch (ProgrammingException e) {
//            showAlert(AlertType.ERROR, "Programming Error", e.getMessage());
//        }
//        catch (Exception e) {
//            showAlert(AlertType.ERROR, "Runtime Error", e.getMessage());
//        }
//
//        Alert alert = new Alert(AlertType.INFORMATION);
//        alert.setTitle("Parser Completion");
//        alert.setContentText("Parsing completed");
//        alert.showAndWait();
    }

}
