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
import gamma.value.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class rgbFunction extends ArgInfoFunction
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    @SuppressWarnings("null")
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        if (code.size() < 3 || code.size() > 4) {
            throw new ExecutionException("rgb() function requires 3 or four parameters");
        }
        double red =   (Double)code.get(0);
        double green = (Double)code.get(1);
        double blue =  (Double)code.get(2);
        double alpha = 255.0;
        if (code.size() > 3) {
            alpha = (Double)code.get(3);
        }

        return Color.toColorDouble(red, green, blue, alpha);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
