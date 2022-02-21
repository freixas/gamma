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

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.execution.hcode.HCode;
import org.freixas.gamma.execution.hcode.Jump;
import org.freixas.gamma.execution.hcode.Label;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The HCodeProgram receives the h-code produced by the parser and transforms
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

        // Convert labels to program locations. We start by finding all the
        // labels. We scan backwards in case we have multiple labels in
        // a sequence. All of them should point to the first non-label item
        // that follows

        HashMap<Integer, Integer> labels = new HashMap<>();
        int lastNonLabel = codes.size();

        for (int i = codes.size() - 1; i > -1; i--) {
            Object code = codes.get(i);
            if (code instanceof Label label) {
                labels.put(label.getId(), lastNonLabel);
            }
            else {
                lastNonLabel = i;
            }
        }

        // Now that we know where every label's location is, let's find all the
        // Jump instructions and convert their label to a location

        for (Object code : codes) {
            if (code instanceof Jump jump) {
                Integer id = jump.getId();
                if (!labels.containsKey(id)) {
                    throw new ProgrammingException("HCodeProgram: Label '" + jump.getId() + "' not found");
                }
                int location = labels.get(id);
                jump.setJumpLocation(location);
            }
        }
    }

    /**
     * Reset the program for a new execution. This is called even for the first
     * execution.
     *
     * @return The program.
     */
    public LinkedList<Object> initialize()
    {
        data = new LinkedList<>();
        return program;
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
     * @param hCode The h-code whose data we want.
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
