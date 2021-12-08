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
import gamma.value.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class PathHCode extends HCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.COORDINATE);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        // The number of coordinates is the data size minus the path points
        // count
        
        double numOfCoordinates = data.size() - 1;

        ArrayList<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < numOfCoordinates; i++) {
            coords.add((Coordinate)data.get(i));
        }

        data.clear();

        data.add(new Path(coords));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
