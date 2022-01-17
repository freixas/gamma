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

import gamma.GammaRuntimeException;
import gamma.MainWindow;
import gamma.ProgrammingException;
import gamma.css.value.Stylesheet;
import gamma.execution.hcode.FunctionExecutor;
import gamma.execution.hcode.HCodeExecutor;
import gamma.execution.hcode.HCode;
import gamma.execution.hcode.ArgInfoHCode;
import gamma.execution.hcode.GenericHCode;
import gamma.execution.hcode.Label;
import gamma.execution.hcode.SetStatement;
import gamma.execution.lcode.Command;
import gamma.math.Util;
import gamma.value.ConcreteObserver;
import gamma.value.Coordinate;
import gamma.value.Displayable;
import gamma.value.Frame;
import gamma.value.WInitializer;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The HCodeEngine controls the execution of a specific HCodeProgram. The
 * program is executed once for a non-animated diagram and multiple times for
 * an animated diagram.
 *
 * @author Antonio Freixas
 */
public class HCodeEngine
{
    private static final Frame defFrame = new Frame(new ConcreteObserver(new WInitializer(new Coordinate(0.0, 0.0), 0.0, 0.0), new ArrayList<>()), Frame.AtType.TAU, 0);

    private final MainWindow window;
    private final SetStatement setStatement;
    private final Stylesheet stylesheet;
    private final HCodeProgram program;
    private int programCounter;

    private final DynamicSymbolTable dynamicTable;
    private int precision;
    private final SetStatement.PrecisionType precisionType;

    SymbolTable table;
    private LCodeEngine lCodeEngine;
    private final HCodeExecutor hCodeExecutor;
    private final FunctionExecutor functionExecutor;

    private File file;
    private int lineNumber;

    public HCodeEngine(MainWindow window, SetStatement setStatement, Stylesheet stylesheet, HCodeProgram program)
    {
        this.window = window;
        this.setStatement = setStatement;

        // Get the stylesheet so that it contains
        //
        // - The fatory default stylesheet
        // - The user's default stylesheet, if any
        // - The stylesheet given

        this.stylesheet = stylesheet;
        if (Stylesheet.USER_STYLESHEET != null) this.stylesheet.prefixStylesheet(Stylesheet.USER_STYLESHEET);
        this.stylesheet.prefixStylesheet(Stylesheet.DEFAULT_STYLESHEET);
        stylesheet.setCacheEnabled(true);

        this.program = program;
        this.dynamicTable = new DynamicSymbolTable(this);
        this.lCodeEngine = null;

        this.hCodeExecutor = new HCodeExecutor(this);
        this.functionExecutor = new FunctionExecutor();
        precisionType = SetStatement.PrecisionType.DISPLAY;
        setPrecision(precisionType);
    }

    private void initializeSymbolTable(SymbolTable table)
    {
        // Add pre-defined variables

        // Infinity values

        table.put("inf", Double.POSITIVE_INFINITY);
        table.protect("inf");

        // Null

        table.put("null", null);
        table.protect("null");

        // Add booleans

        table.put("true", 1.0);
        table.put("false", 0.0);

        table.protect("true");
        table.protect("false");

        // Add the default frame

        table.put("defFrame", new Frame(defFrame));
        table.protect("defFrame");
    }

    public MainWindow getMainWindow()
    {
        return window;
    }

    public SetStatement getSetStatement()
    {
        return setStatement;
    }

    public final void setPrecision(SetStatement.PrecisionType type)
    {
        if (type == SetStatement.PrecisionType.DISPLAY) {
            precision = setStatement.getDisplayPrecision();
        }
        else {
            precision = setStatement.getPrintPrecision();
        }
    }

    public Stylesheet getStylesheet()
    {
        return stylesheet;
    }

    public HCodeProgram getProgram()
    {
        return program;
    }

    public SymbolTable getSymbolTable()
    {
        return table;
    }

    public DynamicSymbolTable getDynamicSymbolTable()
    {
        return dynamicTable;
    }

    public HCodeExecutor getHCodeExecutor()
    {
        return hCodeExecutor;
    }

    public FunctionExecutor getFunctionExecutor()
    {
        return functionExecutor;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    public static Frame getDefFrame()
    {
        return new Frame(defFrame);
    }

    public LCodeEngine getLCodeEngine()
    {
        return lCodeEngine;
    }

    public void print(String str)
    {
        window.print(str);
    }

    public void execute()
    {
        // System.err.println("\n\n*******************\nNew execution\n*******************\n");
        lineNumber = 0;

        // Create an LCodeEngine only the first time

        boolean firstExecution = false;
        if (lCodeEngine == null) {
            lCodeEngine = new LCodeEngine(window);
            firstExecution = true;
        }
        else {
            lCodeEngine.removeAllCommands();
        }

        table = new SymbolTable(this);
        initializeSymbolTable(table);

        LinkedList<Object> code = program.initialize();
        programCounter = 0;

        try {
            while (programCounter < code.size()) {
//                System.err.print(programCounter + ": ");
                Object obj = code.get(programCounter);
                if (!(obj instanceof HCode) && !(obj instanceof Label)) {
//                    System.err.print("PUSH " + toDisplayableString(obj));
                    program.pushData(obj);
                }
                else if (obj instanceof HCode hCode) {
                    // Individual HCode using ArgInfo method

                    if (hCode instanceof ArgInfoHCode argInfoHCode) {
//                        System.err.print("HCODE " + hCode.getClass().getName());
//                        if (hCode instanceof Jump jump) {
//                            System.err.print(" " + jump.getJumpLocation());
//                        }

                        // Check the arguments

                        ArgInfo argInfo = argInfoHCode.getArgInfo();

                        // Get the number of arguments

                        List<Object> data = program.getData(argInfoHCode);

                        // Execute the hCode

                        argInfo.checkTypes(data);
                        argInfoHCode.execute(this, data);
                    }

                    // Generic HCode method

                    else if (hCode instanceof GenericHCode genericHCode) {
//                        System.err.print("HCODE " + genericHCode.getType());
                        genericHCode.execute(this);
                    }
//                    else if (obj instanceof Label label) {
//                        System.err.print("LABEL " + label.getId());
//                    }
                }
//                System.err.println();
                programCounter++;
            }
            if (!program.isDataEmpty()) {
                throw new ProgrammingException("HCodeEngine.execute(): Execution ended but the data stack is not empty");
            }
        }
        catch (Throwable e) {
            throwGammaException(e);
        }

        // We only enable stylesheet caching on the first execution. If the user
        // writes an aninmated script where the style property changes
        // with each execution, we might put a lot of stuff into the cache that
        // will never be used

        stylesheet.setCacheEnabled(false);

        // Execute the lCodes

        // Set up the lcode engine for this set of lcodes.
        // This handles the initial drawing and sets up observers to
        // handle redraws

        if (firstExecution) {
            lCodeEngine.setup();
        }
        else {
            lCodeEngine.setUpDrawingFrame();
            lCodeEngine.execute();
        }
    }

    public void goTo(int location)
    {
        // The program counter will be incremented after executing a jump
        // instruction, so we decrement the given location by one

        programCounter = location - 1;
    }

    public void close()
    {
        if (lCodeEngine != null) lCodeEngine.close();
    }

    public void addCommand(Command command)
    {
        lCodeEngine.addCommand(command);
    }

    public String toDisplayableString(Object obj)
    {
        if (obj instanceof String str) {
            return str;
        }
        else if (obj instanceof Double dbl) {
            return Util.toString(dbl, precision);
        }
        else if (obj instanceof Displayable displayable) {
            return displayable.toDisplayableString(this);
        }
        else {
            throw new ProgrammingException("HCodeEngine.toDisplayableString(): Couldn't convert object to string");
        }
    }

    public void throwGammaException(Throwable e)
        throws GammaRuntimeException
    {
        String msg = e.getLocalizedMessage();
        if (msg == null) msg = e.getClass().getCanonicalName();
            throw new GammaRuntimeException(file.getName() + ":" + lineNumber + ": " + msg, e);
    }

}
