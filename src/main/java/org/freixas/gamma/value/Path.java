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
package org.freixas.gamma.value;

import org.freixas.gamma.execution.HCodeEngine;

import java.util.ArrayList;

/**
 * This class represents a path object.
 *
 * @author Antonio Freixas
 */
public class Path implements ExecutionImmutable, Displayable
{
    private final ArrayList<Coordinate> coords;
    private final Bounds bounds;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a path.
     *
     * @param coords The coordinates that make up the path.
     */
    public Path(ArrayList<Coordinate> coords)
    {
        double minX = Double.POSITIVE_INFINITY;
        double minT = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxT = Double.NEGATIVE_INFINITY;

        this.coords = new ArrayList<>();
        for (Coordinate coord : coords) {
            if (coord.x < minX) minX = coord.x;
            if (coord.x > maxX) maxX = coord.x;
            if (coord.t < minT) minT = coord.t;
            if (coord.t > maxT) maxT = coord.t;

            this.coords.add(new Coordinate(coord));
        }

        this.bounds = new Bounds(minX, minT, maxX, maxT);
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

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

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    public Path relativeTo(Frame prime)
    {
        ArrayList<Coordinate> newCoords = new ArrayList<>();

        for (Coordinate coord : coords) {
            newCoords.add(prime.toFrame(coord));
        }
        return new Path(newCoords);
    }

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        StringBuilder str = new StringBuilder("[ Path\n");
        for (int i = 0; i < coords.size(); i++) {
            str.append(String.format("  %2d)", i + 1));
            str.append(coords.get(i).toDisplayableString(engine));
            str.append("\n");
        }
        str.append("]");
        return str.toString();
    }

}
