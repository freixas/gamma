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

import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;

/**
 * Create a bounding box.
 *
 * @author Antonio Freixas
 */
public class Bounds
{
    public Coordinate min;
    public Coordinate max;

    public Bounds(double x1, double t1, double x2, double t2)
    {
        min = new Coordinate(Math.min(x1, x2), Math.min(t1, t2));
        max = new Coordinate(Math.max(x1, x2), Math.max(t1, t2));
    }

    /**
     * Creating a bounding box. The values given can be for any two
     * opposing corners. The corners are sort so that the min corner is bottom
     * left and the max corner is upper right.
     *
     * @param a The first corner.
     * @param b The second corner.
     */
    public Bounds(Coordinate a, Coordinate b)
    {
        this(a.x, a.t, b.x, b.t);
    }

    /**
     * Copy constructor.
     *
     * @param other The other bounds to copy.
     */
    public Bounds(Bounds other)
    {
        // There's no need to sort as the other bounds will already
        // be sorted

        this.min.x = other.min.x;
        this.min.t = other.min.t;
        this.max.x = other.max.x;
        this.max.t = other.max.t;
    }

    /**
     * Set this bounding box to a new set of corners. The values given can be
     * for any two opposing corners. The corners are sort so that the min corner
     * is bottom left and the max corner is upper right.
     *
     * @param x1 The x coordinate of the first corner.
     * @param t1 The t coordinate of the first corner.
     * @param x2 The x coordinate of the second corner.
     * @param t2 The t coordinate of the second corner.
     */
    public void setTo(double x1, double t1, double x2, double t2)
    {
        min.setTo(Math.min(x1, x2), Math.min(t1, t2));
        max.setTo(Math.max(x1, x2), Math.max(t1, t2));
    }

    /**
     * Set this bounding box to a new set of corners. The values given can be
     * for any two opposing corners. The corners are sort so that the min corner
     * is bottom left and the max corner is upper right.
     *
     * @param a The first corner.
     * @param b The second corner.
     */
    public void setTo(Coordinate a, Coordinate b)
    {
        setTo(a.x, a.t, b.x, b.t);
    }

    /**
     * Set this bounding box to match another.
     *
     * @param other The other bounds to copy.
     */
    public void setTo(Bounds other)
    {
        // There's no need to sort as the other bounds will already
        // be sorted

        min.setTo(other.min);
        max.setTo(other.max);
    }

    /**
     * Get the coordinate for the bottom left corner of the bounds.
     *
     * @return The coordinate for the bottom left corner of the bounds.
     */
    public Coordinate getMin()
    {
        return min;
    }

    /**
     * Get the coordinate for the upper right corner of the bounds.
     *
     * @return The coordinate for the upper right corner of the bounds.
     */
    public Coordinate getMax()
    {
        return max;
    }

    /**
     * Get the width of the bounds.
     *
     * @return The width of the bounds.
     */
    public double getWidth()
    {
        return max.x - min.x;
    }

    /**
     * Get the height of the bounds.
     *
     * @return The height of the bounds.
     */
    public double getHeight()
    {
        return max.t - min.t;
    }

    /**
     * Returns true if the given point is inside this bounding box.
     *
     * @param c The point.
     *
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
     *
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
     *
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

    /**
     * Returns the bounding box for the intersection of the given bounding box
     * with this one.
     *
     * @param other The bounding box to use for the intersection.
     *
     * @return The bounding box for the intersection or null if none.
     */
    public Bounds intersect(Bounds other)
    {
        if (!intersects(other)) return null;
        return new Bounds(
            Math.max(min.x, other.min.x),
            Math.max(min.t, other.min.t),
            Math.min(max.x, other.max.x),
            Math.min(max.t, other.max.t));

    }

    /**
     * Transform the bounding box to get a new bounding box. The new
     * box is the bounding box of the transformed box.
     *
     * @param transform The transform to apply.
     *
     * @return The transformed bounds.
     */
    public Bounds transform(Affine transform)
    {
        Point2D p1 = transform.transform(min.x, min.t);
        Point2D p2 = transform.transform(min.x, max.t);
        Point2D p3 = transform.transform(max.x, min.t);
        Point2D p4 = transform.transform(max.x, max.t);


        return new Bounds(
            Math.min(Math.min(p1.getX(), p2.getX()), Math.min(p3.getX(), p4.getX())),
            Math.min(Math.min(p1.getY(), p2.getY()), Math.min(p3.getY(), p4.getY())),
            Math.max(Math.max(p1.getX(), p2.getX()), Math.max(p3.getX(), p4.getX())),
            Math.max(Math.max(p1.getY(), p2.getY()), Math.max(p3.getY(), p4.getY())));
    }
}
