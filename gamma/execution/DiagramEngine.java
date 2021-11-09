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
package gamma.execution;

import gamma.MainWindow;
import gamma.ProgrammingException;
import gamma.execution.hcode.HCode;
import gamma.parser.Token;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import javafx.scene.control.Alert;

/**
 * The Diagram Engine controls the overall execution of a user's program,
 * including animation.
 * <p>
 * The diagram engine starts with an hcode program produced by the parser. The
 * diagram engine's job finishes when a non-animated diagram is drawn or when
 * an animation ends or is terminated by the user.
 *
 * @author Antonio Freixas
 */
public class DiagramEngine
{
    private final MainWindow window;
    private final LinkedList<Object> hCodes;
    private final boolean isAnimated;
    private LCodeEngine lCodeEngine = null;

    public DiagramEngine(MainWindow window, LinkedList<Object> hCodes, boolean isAnimated)
    {
        this.window = window;
        this.hCodes = hCodes;
        this.isAnimated = isAnimated;
    }

    public void execute() throws ExecutionException, ProgrammingException
    {
        if (isAnimated) {
            // We need to take the hCodes and send them to the hCodeEngine to
            // be optimized. The ouput should be a symbol table, an animation
            // table, a hCodes of lCodes and a hCodes of optimized hCodes.

            // We should then turn this over to the animation engine to generate
            // the frames
        }
        else {

            // Execute the hCodes to generate lCodes

            try {
                lCodeEngine = new LCodeEngine(window);
                HCodeEngine hCodeEngine = new HCodeEngine(hCodes, lCodeEngine);
                hCodeEngine.execute();

                // Set up the lcode engine for this set of lcodes.
                // This handles the initial drawing and sets up observers to
                // handle redraws

                lCodeEngine.setup();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
                window.showTextAreaAlert(Alert.AlertType.ERROR, "Runtime Errors", "Runtime Errors", e.getMessage(), true);
            }
            catch (Exception e) {
                e.printStackTrace();
                window.showTextAreaAlert(Alert.AlertType.ERROR, "Error", "Error", e.getMessage(), true);
            }
        }
    }

    // **********************************************************
    // *
    // * Debug methods
    // *
    // **********************************************************

    @SuppressWarnings("null")
    private void debugHCode(LinkedList<Object> hCodes)
    {
        try {
            File logDir  = new File("D:/users/tony/Documents/Gamma/Logs");
            logDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            File log = new File(logDir, timestamp +" Log.csv");
            log.createNewFile();

            FileOutputStream out = new FileOutputStream(log);
            try (PrintWriter writer = new PrintWriter(out)) {
                ListIterator<Object> iter = hCodes.listIterator();
                writer.write("Type, Value\n");

                while (iter.hasNext()) {
                    Object obj = iter.next();
                    String type;
                    String value;

                    if (obj instanceof HCode) {
                        type = "HCODE";
                        value = obj.getClass().getName();
                    }
                    else {
                        type = "VALUE";
                        value = valueToString(obj);
                        if (value.equals("")) {
                            value = obj.getClass().getName();
                        }
                    }

                    writer.write(
                        type + "," +
                        value + "\n");
                }
            }
        }
        catch (IOException e) {

        }
    }

    private void debugTokens(ArrayList<Token> tokens)
    {
        try {
            File logDir  = new File("D:/users/tony/Documents/Gamma/Logs");
            logDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            File log = new File(logDir, timestamp +" Log.csv");
            log.createNewFile();
            FileOutputStream out = new FileOutputStream(log);
            try (PrintWriter writer = new PrintWriter(out)) {
                ListIterator<Token> iter = tokens.listIterator();
                writer.write("Line Number,Char Number,Type,Value\n");

                while (iter.hasNext()) {
                    Token token = iter.next();

                    String value = valueToString(token.getValue());

                    writer.write(
                            token.getLineNumber() + "," +
                        token.getCharNumber() + "," +
                        token.getType() + "," +
                        value + "\n");
                }
            }
        }
        catch (IOException e) {

        }
    }

    private String valueToString(Object value)
    {
        if (value instanceof Character character) {
            return quoteForCsv(character.toString());
        }
        else if (value instanceof Double double1) {
            return double1.toString();
        }
        else if (value instanceof String string) {
            return(quoteForCsv(string));
        }
        else if (value instanceof Integer integer1) {
            return integer1.toString();
        }
        else {
            return "";
        }
    }

    private String quoteForCsv(String str)
    {
        if (str.matches(".*[,\r\n\"].*")) {
            str = str.replaceAll("\"", "\"\"");
            str = "\"" + str + "\"";
        }
        return str;
    }


}
