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
import gamma.math.Util;
import gamma.value.Displayable;
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
        argTypes.add(ArgInfo.Type.ANY);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        Object arg1 = data.get(0);
        Object arg2 = data.get(1);
        data.clear();

        boolean isDouble1 = arg1 instanceof Double;
        boolean isDouble2 = arg2 instanceof Double;
        boolean isString1 = arg1 instanceof String;
        boolean isString2 = arg2 instanceof String;
        boolean isDisplayable1 = arg1 instanceof Displayable;
        boolean isDisplayable2 = arg2 instanceof Displayable;

        // Both value are doubles; add them

        if (isDouble1 && isDouble2) {
            data.add((Double)arg1 + (Double)arg2);
        }

        // At least one value is a String; convert the other value to a string
        // (if needed) and concatenate them

        else if (isString1 || isString2) {
            String string1;
            String string2;

            if (isDouble1) {
                string1 = engine.toDisplayableString((Double)arg1);
            }
            else if (isDisplayable1) {
                string1 = ((Displayable)arg1).toDisplayableString(engine);
            }
            else {
                string1 = (String)arg1;
            }

            if (isDouble2) {
                string2 = engine.toDisplayableString((Double)arg2);
            }
            else if (isDisplayable2) {
                string2 = ((Displayable)arg2).toDisplayableString(engine);
            }
            else {
                string2 = (String)arg2;
            }

            data.add(string1 + string2);
        }

        // Invalid combination

        else {
            throw new ExecutionException("Invalid types for '+' operator");
        }
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }
}
