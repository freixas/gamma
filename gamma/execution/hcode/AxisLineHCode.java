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
import gamma.execution.HCodeEngine;
import gamma.value.Frame;
import gamma.value.Line;
import gamma.value.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class AxisLineHCode extends ArgInfoHCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.AXIS_TYPE);
        argTypes.add(ArgInfo.Type.OBSERVER_OR_FRAME);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(3, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        Line.AxisType type = (Line.AxisType)data.get(0);
        Object arg2 =                       data.get(1);
        double offset =      (Double)       data.get(2);
        data.clear();

        Frame frame;
        if (arg2 instanceof Observer observer) {
            frame = new Frame(observer);
        }
        else {
            frame = (Frame)arg2;
        }

        data.add(new Line(type, frame, offset));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
