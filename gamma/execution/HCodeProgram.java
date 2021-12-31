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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import gamma.value.ExecutionMutableSupport;

/**
 * The HCodeProgram receives the hCode produced by the parser and transforms
 * it into separate HCode and data stacks. A master copy of the data can be
 * saved if the program will be executed multiple times.
 *  <p>
 * One of the main tasks of the HCodeProgram is to fetch the data corresponding
 * to a given HCode.
 *
 * @author Antonio Freixas
 */
public class HCodeProgram
{
    private final ArrayList<HCode> hCodes;
    private final LinkedList<Object> masterData;
    private LinkedList<Object> data;

    private int dataPtr;
    private int lastHCodeReturnCount;

    public HCodeProgram(LinkedList<Object> codes)
    {
        this.hCodes = new ArrayList<>();
        this.masterData = new LinkedList<>();

        ListIterator<Object> iter = codes.listIterator();
        int argOffset = 0;

        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof HCode hCode) {
                hCode.setArgOffset(argOffset);
                argOffset = 0;
                hCodes.add(hCode);
            }
            else {
                masterData.add(obj);
                argOffset++;
            }
        }
    }

    /**
     * Reset the program for a new execution. This is called even for the
     * first execution.
     *
     * @param copyData If true, the master data is copied and the copy is
     * used for execution. If false, the master data is used directly (and
     * consumed).
     *
     * @return An HCode iterator which traverses the HCode (not the data) stack.
     */
    public Iterator<HCode> reset(boolean copyData)
    {
        dataPtr = 0;
        lastHCodeReturnCount = 0;

        if (copyData) {
            ListIterator<Object> iter = masterData.listIterator();
            data = new LinkedList<>();
            while (iter.hasNext()) {
                Object obj = iter.next();
                data.add(ExecutionMutableSupport.copy(obj));
            }
        }
        else {
            data = masterData;
        }

        return hCodes.iterator();
    }

    /**
     * Get the set of data corresponding to the given HCode. This is not an
     * arbitrary HCode, but the next HCode in the current execution of the
     * program.
     *
     * @param hCode The HCode whose data we want.
     * @return A list of the matching data. While this list contains only the
     * data for the current HCode, changes to the returned data affect the
     * data stack. The data should be cleared when done and a result, if any,
     * should be pushed onto the list.
     */
    public List<Object> getData(HCode hCode)
    {
        int numOfArgs = hCode.getNumberOfArgs();
        int numOfRets = hCode.getNumberOfReturnedValues();
        int argOffset = hCode.getArgOffset();

        dataPtr += lastHCodeReturnCount;

        // If the number of args is -1, get the number of args from the stack

        if (numOfArgs == -1) {
            int lastArgPtr = dataPtr + argOffset - 1;
            if (lastArgPtr >= data.size()) {
                throw new ProgrammingException("CodeProgram.getData(): hCode arguments are missing");
            }
            Object obj = data.get(lastArgPtr);
            if (!(obj instanceof Integer)) {
                throw new ProgrammingException("HCodeProgram.getData(): Expected the number of hCode arguments");
            }

            // We add 1 to allow for the count

            numOfArgs = (Integer)obj + 1;
        }

        // Adjust the data ptr to point to the beginning of the arguments

        dataPtr += argOffset - numOfArgs;

        // Grab the count of values returned for next time

        lastHCodeReturnCount = numOfRets;

        // Create a sublist of just the arguments

        return data.subList(dataPtr, dataPtr + numOfArgs);
    }

    /**
     * Returns true if the data stack is empty, which should occur only after
     * the last HCode executes.
     *
     * @return True if the data stack is empty.
     */
    public boolean isDataEmpty()
    {
        return data.isEmpty();
    }

}
