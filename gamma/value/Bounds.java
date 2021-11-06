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

/**
 * Create a bounding box.
 *
 * @author Antonio Freixas
 */
public class Bounds
{
    public Coordinate min;
    public Coordinate max;

    public Bounds(Coordinate a, Coordinate b)
    {
        this(a.x, a.t, b.x, b.t);
    }

    public Bounds(double x1, double t1, double x2, double t2)
    {
        min = new Coordinate(Math.min(x1, x2), Math.min(t1, t2));
        max = new Coordinate(Math.max(x1, x2), Math.max(t1, t2));
    }

    public Coordinate getMin()
    {
        return min;
    }

    public Coordinate getMax()
    {
        return max;
    }

    /**
     * Returns true if the given point is inside this bounding box.
     *
     * @param c The point.
     * @return True if the given point is inside this bounding box.
     */
    public boolean inside(Coordinate c)
    {
        return inside(c.x, c.t);
    }

    /**
     * Returns true if the given point is inside this bounding box.
     *
     * @param x The point's x coordinate.
     * @param t The point's t coordinate.
     * @return True if the given point is inside this bounding box.
     */
    public boolean inside(double x, double t)
    {
        return x >= min.x && x <= max.x && t >= min.t && t <= max.t;
    }

    /**
     * Returns true if the given bounding box intersects with this one.
     *
     * @param other The bounding box to check for intersection.
     * @return True if they intersect.
     */
    public boolean intersects(Bounds other)
    {
        return !(
            max.x < other.min.x ||
            min.x > other.max.x ||
            max.t < other.min.t ||
            min.t > other.max.t
        );
    }
}
