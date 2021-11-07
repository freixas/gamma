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
package gamma.execution.lcode;

import gamma.execution.ExecutionException;
import gamma.value.Coordinate;
import gamma.value.Frame;
import java.awt.Toolkit;

/**
 *
 * @author Antonio Freixas
 */
public class DisplayStruct extends Struct
{
    static Coordinate originDefault = new Coordinate(0.0, 0.0);

    public int width = Struct.INT_NOT_SET;
    public int height = -Struct.INT_NOT_SET;
    public Coordinate origin = originDefault;
    public double scale = 1.0;
    public double units = 1.0;

    public DisplayStruct()
    {
    }

    public void widthRangeCheck()
    {
        if (width < 1 || width >= Toolkit.getDefaultToolkit().getScreenSize().width) {
            throw new ExecutionException("Display width is out of range");
        }
    }

    public void heightRangeCheck()
    {
        if (height < 1 || height >= Toolkit.getDefaultToolkit().getScreenSize().height) {
            throw new ExecutionException("Display height is out of range");
        }
    }

    public void scaleRangeCheck()
    {
        if (scale <= 0.0) {
            throw new ExecutionException("Display scale is out of range");
        }
    }

    public void unitsRangeCheck()
    {
        if (units <= 0.0) {
            throw new ExecutionException("Display units are out of range");
        }
    }

    @Override
    public void relativeTo(Frame prime)
    {
        // Do nothing
    }

}
