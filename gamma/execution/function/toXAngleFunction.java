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
package gamma.execution.function;

import gamma.execution.ArgInfo;
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import gamma.math.Relativity;
import gamma.value.Frame;
import gamma.value.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class toXAngleFunction extends Function
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(1, argTypes);
    }

    @Override
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        Object arg1 = code.get(0);
        if (arg1 instanceof Double dbl) {
            return Relativity.vToXAngle(dbl);
        }
        else if (arg1 instanceof Observer observer) {
            return Relativity.vToXAngle(new Frame(observer).getV());
        }
        else if (arg1 instanceof Frame frame) {
            return Relativity.vToXAngle(frame.getV());
        }
        else {
            throw new ExecutionException("toXAngle requires a velocity or a frame");
        }
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }


}
