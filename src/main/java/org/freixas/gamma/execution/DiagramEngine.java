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
package org.freixas.gamma.execution;

import javafx.scene.control.Alert;
import org.freixas.gamma.GammaRuntimeException;
import org.freixas.gamma.MainWindow;
import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.css.value.Stylesheet;
import org.freixas.gamma.execution.hcode.SetStatement;

import java.util.LinkedList;

/**
 * The Diagram Engine controls the overall execution of a user's program,
 * including animation.
 * <p>
 * The diagram engine starts with a h-code program produced by the parser. The
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
    private final SetStatement setStatement;
    private final Stylesheet stylesheet;

    private AnimationEngine animationEngine;
    private HCodeEngine hCodeEngine;

    private boolean isClosed;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a new diagram engine.
     *
     * @param window The associated window in which the diagram will be displayed.
     * @param hCodes The h-codes to run.
     * @param isAnimated True if this is an animated diagram.
     * @param setStatement The set statement to use.
     * @param stylesheet The stylesheet to use.
     */
    public DiagramEngine(MainWindow window, LinkedList<Object> hCodes, boolean isAnimated, SetStatement setStatement, Stylesheet stylesheet)
    {
        this.window = window;
        this.program = new HCodeProgram(hCodes);
        this.isAnimated = isAnimated;
        this.setStatement = setStatement;
        this.stylesheet = stylesheet;

        this.animationEngine = null;
        this.hCodeEngine = null;

        this.isClosed = false;

        // This call shuts down any diagram engine currently running in this
        // window

        window.setDiagramEngine(this);
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    public LinkedList<Object> getHCodes()
    {
        return program.getHCodes();
    }

    public boolean isAnimated()
    {
        return isAnimated;
    }

    public SetStatement getSetStatement()
    {
        return setStatement;
    }

    public Stylesheet getStylesheet()
    {
        return stylesheet;
    }

    // **********************************************************************
    // *
    // * Control Methods
    // *
    // **********************************************************************



    public void execute() throws ExecutionException, ProgrammingException
    {
        if (isClosed) return;
        window.clearScriptPrintDialog();

        try {
           // Execute animated scripts

            if (isAnimated) {
                animationEngine = new AnimationEngine(window, setStatement, stylesheet, program);
                animationEngine.execute();
            }

            // Execute non-animated scripts

            else {
                hCodeEngine = new HCodeEngine(window, setStatement, stylesheet, program);
                hCodeEngine.execute();
             }
        }
        catch (Throwable e) {
            handleException(e);
        }
    }

    /**
     * This is called when a display variable is changed.If the script is
     * animated, we let the animation engine handle this. Otherwise, we redraw.
     *
     * @param restart True if the animation should be restarted when a display
     * variable changes.
     */
    public void updateForDisplayVariable(boolean restart)
    {
        if (isClosed) return;

        try {
            if (isAnimated) {
                animationEngine.updateForDisplayVariable(restart);
            }
            else {
                hCodeEngine.execute();
            }
        }
        catch (Throwable e) {
            handleException(e);
        }
    }

    /**
     * This handler provides common handling for all exceptions that occur
     * while running a program. During animation, errors can occur in separate
     * threads, so try-catch won't handle everything.
     *
     * @param e The exception.
     */
    public void handleException(Throwable e)
    {
        close();

        // e.printStackTrace();
        if (e instanceof GammaRuntimeException gammaRuntimeException) {
            window.showRuntimeException(gammaRuntimeException);
        }
        else {
            window.showTextAreaAlert(Alert.AlertType.ERROR, "Error", "Error", e.getLocalizedMessage(), true);
        }
    }

    public void close()
    {
        isClosed = true;
        if (isAnimated) {
            if (animationEngine != null) animationEngine.close();
            animationEngine = null;
        }
        else {
            if (hCodeEngine != null) hCodeEngine.close();
            hCodeEngine = null;
        }
        window.enableDisplayControls(false);
    }

    // **********************************************************
    // *
    // * Debug methods
    // *
    // **********************************************************

//    @SuppressWarnings("null")
//    private void debugHCode(LinkedList<Object> hCodes)
//    {
//        try {
//            File logDir  = new File("D:/users/tony/Documents/Gamma/Logs");
//            logDir.mkdirs();
//
//            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
//            File log = new File(logDir, timestamp +" Log.csv");
//            log.createNewFile();
//
//            FileOutputStream out = new FileOutputStream(log);
//            try (PrintWriter writer = new PrintWriter(out)) {
//                ListIterator<Object> iter = hCodes.listIterator();
//                writer.write("Type, Value\n");
//
//                while (iter.hasNext()) {
//                    Object obj = iter.next();
//                    String type;
//                    String value;
//
//                    if (obj instanceof ArgInfoHCode) {
//                        type = "HCODE";
//                        value = obj.getClass().getName();
//                    }
//                    else {
//                        type = "VALUE";
//                        value = valueToString(obj);
//                        if (value.equals("")) {
//                            value = obj.getClass().getName();
//                        }
//                    }
//
//                    writer.write(
//                        type + "," +
//                        value + "\n");
//                }
//            }
//        }
//        catch (IOException e) {
//
//        }
//    }

//    private void debugTokens(ArrayList<Token> tokens)
//    {
//        try {
//            File logDir  = new File("D:/users/tony/Documents/Gamma/Logs");
//            logDir.mkdirs();
//
//            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
//            File log = new File(logDir, timestamp +" Log.csv");
//            log.createNewFile();
//            FileOutputStream out = new FileOutputStream(log);
//            try (PrintWriter writer = new PrintWriter(out)) {
//                ListIterator<Token> iter = tokens.listIterator();
//                writer.write("Line Number,Char Number,Type,Value\n");
//
//                while (iter.hasNext()) {
//                    Token token = iter.next();
//
//                    String value = valueToString(token.getValue());
//
//                    writer.write(
//                            token.getLineNumber() + "," +
//                        token.getCharNumber() + "," +
//                        token.getType() + "," +
//                        value + "\n");
//                }
//            }
//        }
//        catch (IOException e) {
//
//        }
//    }

//    private String valueToString(Object value)
//    {
//        if (value instanceof Character character) {
//            return quoteForCsv(character.toString());
//        }
//        else if (value instanceof Double double1) {
//            return double1.toString();
//        }
//        else if (value instanceof String string) {
//            return(quoteForCsv(string));
//        }
//        else if (value instanceof Integer integer1) {
//            return integer1.toString();
//        }
//        else {
//            return "";
//        }
//    }
//
//    private String quoteForCsv(String str)
//    {
//        if (str.matches(".*[,\r\n\"].*")) {
//            str = str.replaceAll("\"", "\"\"");
//            str = "\"" + str + "\"";
//        }
//        return str;
//    }

}
