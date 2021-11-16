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

import gamma.value.Bounds;
import gamma.value.Coordinate;
import gamma.value.Frame;
import gamma.value.Line;

/**
 *
 * @author Antonio Freixas
 */
public class LineStruct extends Struct
{

    static Coordinate min = new Coordinate(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    static Coordinate max = new Coordinate(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    public Line line;
    public boolean lineSet = false;
    public Coordinate clipBL = min;
    public Coordinate clipTR = max;

    public Bounds bounds;

    public LineStruct()
    {
    }

    @Override
    public void finalizeValues()
    {
        bounds = new Bounds(clipBL, clipTR);
    }

    @Override
    public void relativeTo(Frame prime)
    {
        line = line.relativeTo(prime);
        clipBL = prime.toFrame(clipBL);
        clipTR = prime.toFrame(clipTR);
    }
}
