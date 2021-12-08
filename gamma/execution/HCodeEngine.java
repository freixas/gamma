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
import gamma.ProgrammingException;
import gamma.execution.hcode.HCode;
import gamma.value.Color;
import gamma.execution.lcode.Command;
import gamma.execution.lcode.Struct;
import gamma.execution.lcode.StyleStruct;
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

    private final HCodeProgram program;
    private final SymbolTable table;
    private final AnimationSymbolTable animationTable;
    private StyleStruct styleDefaults;
    private final LCodeEngine lCodeEngine;

    private File file;
    private int lineNumber;

    public HCodeEngine(HCodeProgram program, LCodeEngine lCodeEngine)
    {
        this.program = program;

        this.table = new SymbolTable(this);
        this.animationTable = new AnimationSymbolTable(this);
        this.lineNumber = 0;
        this.styleDefaults = new StyleStruct();
        this.lCodeEngine = lCodeEngine;

        initializeSymbolTable();
    }

    private void initializeSymbolTable()
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
        Struct.initializeStruct(styleDefaults, "style", style);
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
        return this.lCodeEngine;
    }

    public void execute()
    {
        Iterator<HCode> iter = program.reset();

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
        catch (Throwable e) {
            throwGammaException(e);
        }
    }

    public void addCommand(Command command)
    {
        lCodeEngine.addCommand(command);
    }

    public void throwGammaException(Throwable e)
        throws GammaRuntimeException
    {
        if (e instanceof GammaRuntimeException) {
            throw new GammaRuntimeException(file.getName() + ":" + lineNumber + ": " + e.getLocalizedMessage(), e);
        }
        else {
            throw new GammaRuntimeException(file.getName() + ":" + lineNumber + ": " + e.getLocalizedMessage(), e);
        }
    }

}
