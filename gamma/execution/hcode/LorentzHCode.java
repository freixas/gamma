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
import gamma.value.Coordinate;
import gamma.value.Frame;
import gamma.value.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class LorentzHCode extends HCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.COORDINATE);
        argTypes.add(ArgInfo.Type.OBSERVER_OR_FRAME);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    @SuppressWarnings("null")
    public void execute(HCodeEngine engine, List<Object> code)
    {
        Coordinate coord = (Coordinate)code.get(0);
        Object arg2 =                  code.get(1);
        code.clear();

        Frame frame;
        if (arg2 instanceof Observer observer) {
            frame = new Frame(observer);
        }
        else {
            frame = (Frame)arg2;
        }

        code.add(frame.toFrame(coord));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
