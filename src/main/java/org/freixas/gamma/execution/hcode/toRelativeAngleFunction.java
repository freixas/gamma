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
public class toRelativeAngleFunction extends ArgInfoFunction
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        double angle = (Double)code.get(0);
        Object arg1 = code.get(1);

        if (Double.isInfinite(angle)) throw new ExecutionException("In toRelativeAngle(), the angle can't be infinite");
        if (arg1 == null) throw new ExecutionException("In toRelativeAngle(), the velocity can't be null");

        double v;
        if (arg1 instanceof Double dbl) {
            v = (Double)arg1;
        }
        else if (arg1 instanceof Observer observer) {
            v = new Frame(observer).getV();
        }
        else if (arg1 instanceof Frame frame) {
            v = frame.getV();
        }
        else {
            throw new ExecutionException("toRelativeAngle()'s second parameter must be a velocity, frame, or observer");
        }

        if (Math.abs(v) >= 1.0) throw new ExecutionException("In toRelativeAngle(), the velocity must be between -1 and 1, exclusive");

        return Relativity.toPrimeAngle(angle, v);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
