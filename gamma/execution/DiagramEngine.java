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
import gamma.execution.hcode.ArgInfoHCode;
import gamma.execution.hcode.SetStatement;
import gamma.parser.Token;
import gamma.value.AnimationVariable;
import gamma.value.DisplayVariable;
import gamma.value.DynamicVariable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
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
    private final HCodeProgram program;
    private final boolean isAnimated;
    private final boolean hasDisplayVariables;
    private final SetStatement setStatement;

    private DynamicSymbolTable dynamicSymbolTable;
    private Set<String> symbolNames;

    private AnimationEngine animationEngine;
    private HCodeEngine hCodeEngine;

    @SuppressWarnings("LeakingThisInConstructor")
    public DiagramEngine(MainWindow window, LinkedList<Object> hCodes, boolean isAnimated, boolean hasDisplayVariables, SetStatement setStatement)
    {
        this.window = window;
        this.program = new HCodeProgram(hCodes);
        this.isAnimated = isAnimated;
        this.setStatement = setStatement;
        this.hasDisplayVariables = hasDisplayVariables;

        this.animationEngine = null;
        this.hCodeEngine = null;

        // This call shuts down any diagram engine currently running in this
        // window

        window.setDiagramEngine(this);
    }

    public void execute() throws ExecutionException, ProgrammingException
    {
        window.clearPrintDialog();

        try {
           // Execute animated scripts

            if (isAnimated) {
                animationEngine = new AnimationEngine(window, setStatement, program, hasDisplayVariables);
                animationEngine.execute();
            }

            // Execute non-animated scripts

            else {
                hCodeEngine = new HCodeEngine(window, setStatement, program);
                hCodeEngine.execute(hasDisplayVariables);
          
                // If we have dynamic variables, we need add the controls to the main window

                if (hasDisplayVariables) {
                    dynamicSymbolTable = hCodeEngine.getDynamicSymbolTable();
                    symbolNames = dynamicSymbolTable.getSymbolNames();

                    Iterator<String> iter = symbolNames.iterator();
                    while (iter.hasNext()) {
                        DynamicVariable dynamicVariable = dynamicSymbolTable.getDynamicVariable(iter.next());
                        if (dynamicVariable instanceof DisplayVariable var) {
                            window.addDisplayControl(var);
                        }
                    }
                }
             }
        }
        catch (Throwable e) {
            handleExeception(e);
        }
    }

    /**
     * This is called when a display variable is changed. If the script is
     * animated, we let the animation engine handle this. Otherwise, we
     * redraw.
     */
    public void updateForDisplayVariable()
    {
        if (isAnimated) {
            animationEngine.updateForDisplayVariable();
        }
        else {
            hCodeEngine.execute(true);
        }
    }

    /**
     * This handler provides common handling for all exceptions that occur
     * while running a program. During animation, errors can occur in separate
     * threads, so try-catch won't handle everything.
     *
     * @param e The exception.
     */
    public void handleExeception(Throwable e)
    {
        e.printStackTrace();
        if (e instanceof ExecutionException) {
            window.showTextAreaAlert(Alert.AlertType.ERROR, "Runtime Errors", "Runtime Errors", e.getLocalizedMessage(), true);
        }
        else if (e instanceof ProgrammingException) {
            window.showTextAreaAlert(Alert.AlertType.ERROR, "Internal Errors", "Internal Errors", e.getLocalizedMessage(), true);
        }
        else {
            window.showTextAreaAlert(Alert.AlertType.ERROR, "Error", "Error", e.getLocalizedMessage(), true);
        }
    }

    public void close()
    {
        if (isAnimated) {
            if (animationEngine != null) animationEngine.close();
        }
        else {
            if (hCodeEngine != null) hCodeEngine.close();
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

                    if (obj instanceof ArgInfoHCode) {
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
