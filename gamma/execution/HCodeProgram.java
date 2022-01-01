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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The HCodeProgram receives the hCode produced by the parser and transforms
 * it into separate HCode and data stacks. A master copy of the data can be
 * saved if the program will be executed multiple times.
 * <p>
 * One of the main tasks of the HCodeProgram is to fetch the data corresponding
 * to a given HCode.
 *
 * @author Antonio Freixas
 */
public class HCodeProgram
{
    private final LinkedList<Object> program;
    private LinkedList<Object> data;

    public HCodeProgram(LinkedList<Object> codes)
    {
        this.program = codes;
    }

    /**
     * Reset the program for a new execution. This is called even for the first
     * execution.
     *
     * @return An iterator which traverses the program.
     */
    public Iterator<Object> initialize()
    {
        data = new LinkedList<>();
        return program.iterator();
    }

    public void pushData(Object obj)
    {
        data.add(obj);
    }

    /**
     * Get the set of data corresponding to the given HCode. This is not an
     * arbitrary HCode, but the next HCode in the current execution of the
     * program.
     *
     * @param hCode The HCode whose data we want.
     * 
     * @return A list of the matching data. While this list contains only the
     * data for the current HCode, changes to the returned data affect the
     * data stack. The data should be cleared when done and a result, if any,
     * should be pushed onto the list.
     */
    public List<Object> getData(HCode hCode)
    {
        int numOfArgs = hCode.getNumberOfArgs();

        // If the number of args is -1, get the number of args from the stack

        if (numOfArgs == -1) {
            Object obj = data.removeLast();         // Remove the count
            if (!(obj instanceof Integer)) {
                throw new ProgrammingException("HCodeProgram.getData(): Expected the number of hCode arguments");
            }
            numOfArgs = (Integer)obj;
        }

        // Create a sublist of just the arguments

        if (data.size() < numOfArgs) {
            throw new ProgrammingException("HCodeProgram.getData(): Too few elements on the data stack");
        }

        return data.subList(data.size() - numOfArgs, data.size());
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
