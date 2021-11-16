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
 * This is a line segment between two points.
 *
 * @author Antonio Freixas
 */
public class LineSegment
{
    public Coordinate point1;
    public Coordinate point2;

    public LineSegment(double x1, double t1, double x2, double t2)
    {
        this(new Coordinate(x1, t1), new Coordinate(x2, t2));
    }

    public LineSegment(Coordinate p1, Coordinate p2)
    {
        this.point1 = p1;
        this.point2 = p2;
    }

    public LineSegment(LineSegment other)
    {
        this.point1 = other.point1;
        this.point2 = other.point2;
    }

    public final Coordinate getPoint1()
    {
        return point1;
    }

    public final Coordinate getPoint2()
    {
        return point2;
    }

    public LineSegment intersect(Bounds bounds)
    {
        return bounds.intersect(this);
    }

}
