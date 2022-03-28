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

import org.freixas.gamma.GammaRuntimeException;
import org.freixas.gamma.MainWindow;
import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.css.value.Stylesheet;
import org.freixas.gamma.execution.hcode.FunctionExecutor;
import org.freixas.gamma.execution.hcode.HCodeExecutor;
import org.freixas.gamma.execution.hcode.HCode;
import org.freixas.gamma.execution.hcode.ArgInfoHCode;
import org.freixas.gamma.execution.hcode.GenericHCode;
import org.freixas.gamma.execution.hcode.Label;
import org.freixas.gamma.execution.hcode.SetStatement;
import org.freixas.gamma.execution.lcode.Command;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.ConcreteObserver;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Displayable;
import org.freixas.gamma.value.Frame;
import org.freixas.gamma.value.WInitializer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.freixas.gamma.parser.TokenContext;

/**
 * The HCodeEngine controls the execution of a specific HCodeProgram. The
 * program is executed once for a non-animated diagram and multiple times for
 * an animated diagram.
 *
 * @author Antonio Freixas
 */
public class HCodeEngine
{
    static private final Frame defFrame =
        new Frame(new ConcreteObserver(
            new WInitializer(
                new Coordinate(0.0, 0.0), 0.0, 0.0),
            new ArrayList<>()),
            Frame.AtType.TAU,
            0);

    private final MainWindow window;
    private final SetStatement setStatement;
    private final Stylesheet stylesheet;
    private final HCodeProgram program;
    private int programCounter;

    private final StaticSymbolTable staticSymbolTable;
    private final DynamicSymbolTable dynamicTable;
    private int precision;

    SymbolTable table;
    private LCodeEngine lCodeEngine;
    private final HCodeExecutor hCodeExecutor;
    private final FunctionExecutor functionExecutor;

    TokenContext tokenContext;

    private final boolean isClosed;

    public HCodeEngine(MainWindow window, SetStatement setStatement, Stylesheet stylesheet, HCodeProgram program)
    {
        this.window = window;
        this.setStatement = setStatement;
        this.isClosed = false;

        // Get the stylesheet so that it contains
        //
        // - The factory default stylesheet
        // - The user's default stylesheet, if any
        // - The stylesheet given

        this.stylesheet = stylesheet;
        if (Stylesheet.USER_STYLESHEET != null) this.stylesheet.prefixStylesheet(Stylesheet.USER_STYLESHEET);
        this.stylesheet.prefixStylesheet(Stylesheet.DEFAULT_STYLESHEET);
        stylesheet.setCacheEnabled(true);

        this.program = program;

        this.staticSymbolTable = new StaticSymbolTable(this);
        this.dynamicTable = new DynamicSymbolTable(this);

        this.lCodeEngine = null;

        this.hCodeExecutor = new HCodeExecutor(this);
        this.functionExecutor = new FunctionExecutor();
        setPrecision(SetStatement.PrecisionType.DISPLAY);
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

    public StaticSymbolTable getStaticSymbolTable()
    {
        return staticSymbolTable;
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

    public TokenContext getTokenContext()
    {
        return tokenContext;
    }

    public void setTokenContext(TokenContext tokenContext)
    {
        this.tokenContext = tokenContext;
    }

    static public Frame getDefFrame()
    {
        return new Frame(defFrame);
    }

    public LCodeEngine getLCodeEngine()
    {
        return lCodeEngine;
    }

    public void print(String str)
    {
        window.scriptPrint(str);
    }

    public void execute()
    {
        if (isClosed) return;

        // System.err.println("\n\n*******************\nNew execution\n*******************\n");
        tokenContext = new TokenContext(null, "", 0, 0, 0, 0);

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

                        // Execute the h-code

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
        // writes an animated script where the style property changes
        // with each execution, we might put a lot of stuff into the cache that
        // will never be used

        stylesheet.setCacheEnabled(false);

        // Execute the lCodes

        // Set up the l-code engine for this set of l-codes.
        // This handles the initial drawing and sets up observers to
        // handle redraws

        if (firstExecution) {
            dynamicTable.addDisplayControls(window);
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
        lCodeEngine = null;
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

    public void throwGammaException(Throwable e) throws GammaRuntimeException
    {
        // We might get a GammaRuntimeException

        if (e instanceof GammaRuntimeException gammaException) {
            throw gammaException;
        }

        String msg = e.getLocalizedMessage();
        if (msg == null) msg = e.getClass().getCanonicalName();

        GammaRuntimeException.Type type = GammaRuntimeException.Type.OTHER;
        if (e instanceof ExecutionException) {
            type = GammaRuntimeException.Type.EXECUTION;
        }
        else if (e instanceof ProgrammingException) {
            type = GammaRuntimeException.Type.PROGRAMMING;
        }

        throw new GammaRuntimeException(type, tokenContext, msg, e);
    }

}
