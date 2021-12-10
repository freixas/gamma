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
package gamma.value;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Antonio Freixas
 */
public class Path implements ExecutionImmutable
{
    private final ArrayList<Coordinate> coords;
    private final Bounds bounds;

    public Path(ArrayList<Coordinate> coords)
    {
        double minX = Double.POSITIVE_INFINITY;
        double minT = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxT = Double.NEGATIVE_INFINITY;

        this.coords = new ArrayList<>();
        for (int i = 0; i < coords.size(); i++) {
            Coordinate coord = coords.get(i);

            if (coord.x < minX) minX = coord.x;
            if (coord.x > maxX) maxX = coord.x;
            if (coord.t < minT) minT = coord.t;
            if (coord.t > maxT) maxT = coord.t;

            this.coords.add(new Coordinate(coord));
        }

        this.bounds = new Bounds(minX, minT, maxX, maxT);
    }

    public Coordinate get(int index)
    {
        return new Coordinate(coords.get(index));
    }

    public int size()
    {
        return coords.size();
    }

    public Bounds getBounds()
    {
        return new Bounds(bounds);
    }

    public Path relativeTo(Frame prime)
    {
        ArrayList<Coordinate> newCoords = new ArrayList<>();

        for (int i = 0; i < coords.size(); i++) {
            newCoords.add(prime.toFrame(coords.get(i)));
        }
        return new Path(newCoords);
    }

}
