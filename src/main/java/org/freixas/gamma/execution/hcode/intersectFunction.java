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
package org.freixas.gamma.execution.hcode;

import org.freixas.gamma.execution.ArgInfo;
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.value.ConcreteLine;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Line;
import org.freixas.gamma.value.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class intersectFunction extends ArgInfoFunction
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.LINE_OR_OBSERVER);
        argTypes.add(ArgInfo.Type.LINE_OR_OBSERVER);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    @SuppressWarnings("null")
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        Object arg1 = code.get(0);
        Object arg2 = code.get(1);

        if (arg1 == null || arg2 == null) {
            throw new ExecutionException("Can't calculate an intersection for a null value");
        }

        Coordinate result;

        // Both are lines

        if (arg1 instanceof Line && arg2 instanceof Line) {
            result = ConcreteLine.intersect((Line)arg1, (Line)arg2);
        }

        // The first is a line, so the second must be an observer

        else if (arg1 instanceof Line line) {
            Observer observer = (Observer)arg2;
            result = observer.intersect(line);
        }

        // The first is an observer and the second is an observer

        else if (arg2 instanceof Observer observer2) {
            Observer observer1 = (Observer)arg1;
            result = observer1.intersect(observer2);
        }

        // The first is an observer and the second is a line

        else {
            Observer observer = (Observer)arg1;
            result = observer.intersect((Line)arg2);
        }

        return result;

    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
