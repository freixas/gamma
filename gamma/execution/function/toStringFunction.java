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
import gamma.execution.HCodeEngine;
import gamma.math.Util;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class toStringFunction extends Function
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        double d =   (Double)           code.get(0);
        int digits = Util.toInt((Double)code.get(1));

        if (digits < 0) {
            engine.throwExecutionException("Invalid number of digits in float to string conversion");
        }
        return String.format("%f." + digits, d);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
