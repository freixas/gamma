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
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Frame;
import org.freixas.gamma.value.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class toRelativeVFunction extends ArgInfoFunction
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public Object execute(HCodeEngine engine, List<Object> code)
    {
        if (code.size() < 2 || code.size() > 3) {
            throw new ExecutionException("toRelativeV() requires 2 or 3 arguments");
        }

        // The first argument is the relative velocity we want to map
        // If there are two arguments, this velocity is relative to the rest frame

        double v = (double)code.get(0);
        if (Math.abs(v) >= 1) {
            throw new ExecutionException("The velocity must be between -1 and 1, exclusive");
        }

        // The second argument is the frame to which we want to map the
        // velocity above

        Object arg1 = code.get(1);
        double relativeDestFrameV;
        if (arg1 instanceof Double dbl) {
            relativeDestFrameV = dbl;
        }
        else if (arg1 instanceof Observer obs) {
            relativeDestFrameV = new Frame(obs).getV();
        }
        else if (arg1 instanceof Frame frame) {
            relativeDestFrameV = frame.getV();
        }
        else {
            throw new ExecutionException("toRelativeV()'s second argument must be a float, frame, or observer");
        }


        // The final argument is optional. If given, it is the frame to which
        // the velocity in the first frame is given. If omitted, this frame is
        // the rest frame

        Object arg2;
        double relativeSourceFrameV = 0;
        if (code.size() > 2) {
            arg2 = code.get(2);
            if (arg2 instanceof Double dbl) {
                relativeSourceFrameV = dbl;
            }
            else if (arg2 instanceof Observer obs) {
                relativeSourceFrameV = new Frame(obs).getV();
            }
            else if (arg2 instanceof Frame frame) {
                relativeSourceFrameV = frame.getV();
            }
            else {
                throw new ExecutionException("toRelativeV()'s second argument must be a float, frame, or observer");
            }
        }

        // If the source frame is not 0, we need to convert the velocity to
        // the rest frame

        if (!Util.fuzzyZero(relativeSourceFrameV)) {
            v = Relativity.v(v, relativeSourceFrameV);
        }

        // Now convert it to be relative to the destination frame

        return Relativity.vPrime(v, relativeDestFrameV);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }


}
