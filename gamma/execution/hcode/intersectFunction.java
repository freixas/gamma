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
package gamma.execution.hcode;

import gamma.execution.ArgInfo;
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import gamma.value.ConcreteLine;
import gamma.value.Coordinate;
import gamma.value.Line;
import gamma.value.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class intersectFunction extends ArgInfoFunction
{
    private static final ArgInfo argInfo;

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

        if (result == null) {
            throw new ExecutionException("intersect() function: No intersection found");
        }
        return result;

    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
