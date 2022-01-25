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
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.value.Frame;
import org.freixas.gamma.value.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class gammaFunction extends ArgInfoFunction
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
        if (arg1 == null) {
            throw new ExecutionException("The gamma() function's value is null");
        }
        else if (arg1 instanceof Double dbl) {
            return Relativity.gamma(dbl);
        }
        else if (arg1 instanceof Observer observer) {
            return Relativity.gamma(new Frame(observer).getV());
        }
        else if (arg1 instanceof Frame frame) {
            return Relativity.gamma(frame.getV());
        }
        else {
            throw new ExecutionException("gamma requires a velocity or a frame");
        }
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }


}
