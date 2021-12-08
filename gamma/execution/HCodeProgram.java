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
import gamma.value.ExecutionImmutable;
import gamma.value.ExecutionMutable;

/**
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

        ListIterator iter = codes.listIterator();
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

    public Iterator<HCode> reset(boolean copyData)
    {
        dataPtr = 0;
        lastHCodeReturnCount = 0;

        if (copyData) {
            ListIterator<Object> iter = masterData.listIterator();
            data = new LinkedList<>();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof String ||
                    obj instanceof Integer ||
                    obj instanceof Double ||
                    obj instanceof ExecutionImmutable) {
                    data.add(obj);
                }
                else if (obj instanceof ExecutionMutable mutObj) {
                    data.add(mutObj.createCopy());
                }
            }
        }
        else {
            data = masterData;
        }

        return (Iterator<HCode>)hCodes.iterator();
    }

    public List<Object> getData(HCode hCode)
    {
        ArgInfo argInfo = hCode.getArgInfo();
        int numOfArgs = argInfo.getNumberOfArgs();

        dataPtr += lastHCodeReturnCount;

        // If the number of args is -1, get the number of args from the stack

        if (numOfArgs == -1) {
            int lastArgPtr = dataPtr + hCode.getArgOffset()- 1;
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

        dataPtr += hCode.getArgOffset() - numOfArgs;

        // Grab the count of values returned for next time

        lastHCodeReturnCount = argInfo.getNumberOfReturnedValues();

        // Create a sublist of just the arguments

        return data.subList(dataPtr, dataPtr + numOfArgs);
    }

    public boolean isDataEmpty()
    {
        return data.isEmpty();
    }

}
