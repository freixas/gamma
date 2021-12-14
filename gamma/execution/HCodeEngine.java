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
import gamma.execution.hcode.HCode;
import gamma.execution.hcode.SetStatement;
import gamma.value.Color;
import gamma.execution.lcode.Command;
import gamma.execution.lcode.Struct;
import gamma.execution.lcode.StyleStruct;
import gamma.math.Util;
import gamma.value.Coordinate;
import gamma.value.Frame;
import gamma.value.Observer;
import gamma.value.Style;
import gamma.value.WInitializer;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class HCodeEngine
{
    private static final Frame defFrame = new Frame(new Observer(new WInitializer(new Coordinate(0.0, 0.0), 0.0, 0.0), new ArrayList<>()), Frame.AtType.TAU, 0);

    private MainWindow window;
    private final SetStatement setStatement;
    private final HCodeProgram program;

    private final AnimationSymbolTable animationTable;
    private int precision;
    private SetStatement.PrecisionType precisionType;

    SymbolTable table;
    private LCodeEngine lCodeEngine;
    private StyleStruct styleDefaults;

    private File file;
    private int lineNumber;

    public HCodeEngine(MainWindow window, SetStatement setStatement, HCodeProgram program)
    {
        this.window = window;
        this.setStatement = setStatement;
        this.program = program;
        this.animationTable = new AnimationSymbolTable(this);
        this.lCodeEngine = null;
        precisionType = SetStatement.PrecisionType.DISPLAY;
        setPrecision(precisionType);
    }

    private void initializeSymbolTable(SymbolTable table)
    {
        // Add pre-defined variables

        // Infinity values

        table.put("inf", Double.POSITIVE_INFINITY);
        // Not sure if -inf will work on its own

        table.protect("inf");

        // Add colors

        table.put("red",   Color.red);
        table.put("green", Color.green);
        table.put("blue",  Color.blue);

        table.put("yellow",  Color.yellow);
        table.put("magenta", Color.magenta);
        table.put("cyan",    Color.cyan);

        table.put("black", Color.black);
        table.put("gray",  Color.gray);
        table.put("white", Color.white);

        // Add booleans

        table.put("true", 1.0);
        table.put("false", 0.0);

        table.protect("true");
        table.protect("false");

        // Add the default frame

        table.put("defFrame", new Frame(defFrame));
        table.protect("defFrame");
    }

    public SetStatement getSetStatement()
    {
        return setStatement;
    }

    public void setPrecision(SetStatement.PrecisionType type)
    {
        if (type == SetStatement.PrecisionType.DISPLAY) {
            precision = setStatement.getDisplayPrecision();
        }
        else {
            precision = setStatement.getPrintPrecision();
        }
    }

    public String toDisplayableString(Double d)
    {
        return Util.toString(d, precision);
    }

    public HCodeProgram getProgram()
    {
        return program;
    }

    public SymbolTable getSymbolTable()
    {
        return table;
    }

    public AnimationSymbolTable getAnimationSymbolTable()
    {
        return animationTable;
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

    public void setStyleDefaults(Style style)
    {
        styleDefaults = new StyleStruct();
        Struct.initializeStruct(this, styleDefaults, "style", style);
    }

    public StyleStruct getStyleDefaults()
    {
        return styleDefaults;
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

    public void execute(boolean copyData)
    {
        lineNumber = 0;
        styleDefaults = new StyleStruct();

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

        Iterator<HCode> iter = program.reset(copyData);

        try {
            while (iter.hasNext()) {
                HCode hCode = iter.next();

                // Check the arguments

                ArgInfo argInfo = hCode.getArgInfo();

                // Get the number of arguments

                List<Object> data = program.getData(hCode);
                argInfo.checkTypes(data);

                // Execute the hCode

                hCode.execute(this, data);
            }

            if (!program.isDataEmpty()) {
                throw new ProgrammingException("HCodeEngine.execute(): Execution ended but the data stack is not empty");
            }
        }
        catch (ProgrammingException | ExecutionException e) {
            throw e;
        }
        catch (Throwable e) {
            throwGammaException(e);
        }

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

    public void close()
    {
        if (lCodeEngine != null) lCodeEngine.close();
    }

    public void addCommand(Command command)
    {
        lCodeEngine.addCommand(command);
    }

    public void throwGammaException(Throwable e)
        throws GammaRuntimeException
    {
            throw new GammaRuntimeException(file.getName() + ":" + lineNumber + ": " + e.getLocalizedMessage(), e);
    }

}
