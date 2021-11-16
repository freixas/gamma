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
public class Path
{
    private final ArrayList<Coordinate> coords;

    public Path(ArrayList<Coordinate> coords)
    {
        this.coords = new ArrayList<>();
        this.coords.addAll(coords);
    }

    public Coordinate get(int index)
    {
        return new Coordinate(coords.get(index));
    }

    public int size()
    {
        return coords.size();
    }

    public void relativeTo(Frame prime)
    {
        for (int i = 0; i < coords.size(); i++) {
            Coordinate coord = coords.get(i);
            coord = prime.toFrame(coord);
            coords.set(i, coord);
        }
    }
}
