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
public class toXAngleFunction extends ArgInfoFunction
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(1, argTypes);
    }

    @Override
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        Object arg1 = code.getFirst();

        return switch (arg1) {
            case null ->
                throw new ExecutionException("The toXAngle() function's value is null ");
            case Double dbl ->
                Relativity.vToXAngle(dbl);
            case Observer observer ->
                Relativity.vToXAngle(new Frame(observer).getV());
            case Frame frame ->
                Relativity.vToXAngle(frame.getV());
            default ->
                throw new ExecutionException("toXAngle requires a velocity, frame, or observer");
        };

    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }


}
