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

import gamma.ProgrammingException;
import gamma.execution.hcode.HCode;
import gamma.execution.lcode.Color;
import gamma.execution.lcode.Command;
import gamma.execution.lcode.Struct;
import gamma.execution.lcode.StyleStruct;
import gamma.value.Coordinate;
import gamma.value.Frame;
import gamma.value.Observer;
import gamma.value.Style;
import gamma.value.WInitializer;
import gamma.value.WSegment;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class HCodeEngine
{
    private static final Frame defFrame = new Frame(new Observer(new WInitializer(new Coordinate(0.0, 0.0), 0.0, 0.0), new ArrayList<>()), Frame.AtType.TAU, 0);
    private final LinkedList<Object> codes;

    private final SymbolTable table;
    private final AnimationSymbolTable animationTable;
    private StyleStruct styleDefaults;
    private final LCodeEngine lCodeEngine;

    private File file;
    private int lineNumber;

    public HCodeEngine(LinkedList<Object> codes, LCodeEngine lCodeEngine)
    {
        this.codes = codes;

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

    public LinkedList<Object> getCodes()
    {
        return codes;
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
        try {
            int codePtr = 0;

            while (codes.size() > codePtr) {

                // If we have an hCode, execute it. We continue scanning from
                // the same position

                if (codes.get(codePtr) instanceof HCode) {
                    codePtr = executeOneInstruction(codePtr);
                }
                else {
                    codePtr++;
                }
            }

            if (codes.size() > 0) {
                throw new ExecutionException("Execution ended but the code stack is not empty");
            }
        }
        catch (ExecutionException e) {
            throwExecutionException(e.getMessage());
        }
        catch (ProgrammingException e) {
            throwProgrammingException(e.getMessage());
        }
    }

    private int executeOneInstruction(int codePtr)
    {
        HCode hCode = (HCode)codes.get(codePtr);

        // Check the arguments

        ArgInfo argInfo = hCode.getArgInfo();

        // Get the number of arguments

        int numOfArgs = argInfo.getNumberOfArgs();
        int n = 0;

        if (numOfArgs == -1) {
            if (codePtr > 0) {
                Object argN = codes.get(codePtr - 1);
                if (!(argN instanceof Integer)) {
                    throwExecutionException("Expected the number of hCode arguments");
                }
                numOfArgs = (Integer)argN;
                n = 1;
            }
            else {
                throwExecutionException("hCode arguments are missing");
            }
        }

        // Create a sublist of just the arguments

        int codeStart = codePtr - numOfArgs - n;
        if (codeStart < 0) {
            throwExecutionException("Invalid number of hCode arguments");
        }
        List<Object> code = codes.subList(codeStart, codeStart + numOfArgs);

        argInfo.checkTypes(code);

        // Now create a sublist of the arguments, the potential argument count,
        // and the instruction

        code = codes.subList(codeStart, codePtr + 1);

        // Execute the hCode

        hCode.execute(this, code);

        // This hCode returns either zero or one values. We need to start
        // checking for the next instruction at whatever we have at the
        // start of the chunk of code we just processed and removed

        return codeStart;
    }

    public void addCommand(Command command)
    {
        lCodeEngine.addCommand(command);
    }

    public void throwExecutionException(String message)
        throws ExecutionException
    {
        throw new ExecutionException(file.getName()+ ":" + lineNumber + ": " + message);
    }

    public void throwProgrammingException(String message)
        throws ProgrammingException
    {
        throw new ProgrammingException(file.getName()+ ":" + lineNumber + ": " + message);
    }

}
