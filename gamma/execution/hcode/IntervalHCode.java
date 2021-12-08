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
import gamma.value.Bounds;
import gamma.value.Interval;
import java.util.ArrayList;
import java.util.List;

/**
 * Create an interval object. These are implemented as Bounds.
 * <p>
 * Arg 1 is one end of the x interval.<br>
 * Arg 2 is the other end of the x interval.<br>
 * Arg 3 is one end of the t interval.<br>
 * Arg 4 is the other end of the t interval.<br>
 *
 * @author Antonio Freixas
 */
public class IntervalHCode extends HCode
{
    private static final ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        double t1 = (Double)data.get(0);
        double t2 = (Double)data.get(1);

        data.clear();

        data.add(new Interval(t1, t2));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }
}
