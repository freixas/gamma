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
import java.util.ArrayList;
import java.util.List;

/**
 * Find the sum of two numbers.
 * <p>
 * Arg 1 is the first number.<br>
 * Arg 2 is the second number.
 *
 * @author Antonio Freixas
 */
public class AddHCode extends HCode
{
    private static final ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE_OR_STRING);
        argTypes.add(ArgInfo.Type.DOUBLE_OR_STRING);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> code)
    {
        Object arg1 = code.get(0);
        Object arg2 = code.get(1);
        code.clear();

        boolean isDouble1 = arg1 instanceof Double;
        boolean isDouble2 = arg2 instanceof Double;
        boolean isString1 = arg1 instanceof String;
        boolean isString2 = arg2 instanceof String;

        if (isDouble1 && isDouble2) {
            code.add((Double)arg1 + (Double)arg2);
        }
        else if (isDouble1 && isString2) {
            code.add((Double)arg1 + (String)arg2);
        }
        else if (isString1 && isDouble2) {
            code.add((String)arg1 + (Double)arg2);
        }
        else {
            code.add((String)arg1 + (String)arg2);
        }
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }
}
