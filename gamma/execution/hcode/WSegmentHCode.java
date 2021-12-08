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
import gamma.value.WSegment;
import gamma.value.WorldlineSegment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class WSegmentHCode extends HCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.LIMIT_TYPE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(4, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        double v =     (Double)data.get(0);
        double a =     (Double)data.get(1);
        WorldlineSegment.LimitType limitType = (WorldlineSegment.LimitType)data.get(2);
        double delta = (Double)data.get(3);
        data.clear();

        data.add(new WSegment(v, a, limitType, delta));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
