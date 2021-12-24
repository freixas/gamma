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

import gamma.ProgrammingException;
import gamma.execution.HCodeEngine;

/**
 * A line is defined by an angle (in degrees) and a point through which
 * the line crosses.
 * <p>
 * A line can be created using other methods, but all are converted to an
 * angle and a point.
 *
 * @author Antonio Freixas
 */
public class BoundedLine extends Line
{
    private ConcreteLine line;
    private final CurveSegment segment;
    private final Bounds originalBounds;
    private final Bounds bounds;

    /**
     * Attach a bounding box to a line. The bounded line references the
     * unbounded line. The bounding box does not.
     * <p>
     * The bounding box is used for only two purposes:
     * <ul>
     * <li>When intersecting two lines, the intersection must fall within the
     * bounding box os this BoundedLine (and possibly, also within the bounding
     * box of the other line).
     * <li>When drawing a line, only the portion within the bounding box is
     * drawn.
     * </ul>
     * The bounding box may have infinite ends.
     *
     * @param line The line to attach to.
     * @param bounds The bounding box to attach.
     */
    public BoundedLine(Line line, Bounds bounds)
    {
        if (line == null) {
            throw new ProgrammingException("BoundedLine: Trying to attach a bounding box to a null line");
        }

        if (line instanceof BoundedLine boundedLine) {
            this.line = boundedLine.line;
        }
        else if (line instanceof ConcreteLine concreteLine) {
            this.line = concreteLine;
        }

        // Save the original bounding box. We don't use it for anything
        // except for displaying it

        this.originalBounds = new Bounds(bounds);

        // We use the bounding box to bound the line as a curve segment
        // (line or line segment) and then we get the bounds of the segment

        segment = this.line.infiniteIntersect(bounds);
        if (segment != null) {
            this.bounds = segment.getBounds();
        }
        else {
            this.bounds = null;
        }
    }

    /**
     * Copy constructor.
     *
     * @param other The other line to copy.
     */
    public BoundedLine(BoundedLine other)
    {
        this.line = other.line;
        this.segment = other.segment;
        this.originalBounds = other.originalBounds;
        this.bounds = new Bounds(other.bounds);
    }

    /**
     * Return the ConcreteLine wrapped inside this BoundedLine.
     *
     * @return The ConcreteLine wrapped inside this BoundedLine.
     */
    public ConcreteLine getLine()
    {
        return line;
    }

    /**
     * Return the bounded line, which can be either a line or a line segment.
     *
     * @return The bounded line.
     */
    public CurveSegment getCurveSegment()
    {
        return segment;
    }

    @Override
    public Coordinate getCoordinate()
    {
        // Get the coordinate of the bounded line, not the original

        if (segment instanceof ConcreteLine concreteLine) {
            return concreteLine.getCoordinate();
        }

        // If we have a line segment, choose the first point

        return new Coordinate(((LineSegment)segment).getPoint1());
    }

    @Override
    public double getAngle()
    {
        return line.getAngle();
    }

    @Override
    public double getSlope()
    {
        return line.getSlope();
    }

    @Override
    public double getConstantOffset()
    {
        return line.getConstantOffset();
    }

    @Override
    public boolean isInfiniteMinus()
    {
        if (segment instanceof ConcreteLine concreteLine) {
            return concreteLine.isInfiniteMinus();
        }
        return false;       // Line segment
    }

    @Override
    public boolean isInfinitePlus()
    {
        if (segment instanceof ConcreteLine concreteLine) {
            return concreteLine.isInfinitePlus();
        }
        return false;       // Line segment
    }

    @Override
    public Bounds getBounds()
    {
        return new Bounds(bounds);
    }

    @Override
    public BoundedLine relativeTo(Frame prime)
    {
        //return new BoundedLine(line.relativeTo(prime));
        return null;
    }

    @Override
    public Coordinate intersect(Line other)
    {
        // This bounded line must fall within our bounding box

        if (segment == null) return null;

        // Intersect the two lines and see if they intersect

        Coordinate intersection = line.intersect(other);
        if (intersection == null) return null;

        // The intersection must fall within our bounding box

        if (bounds.inside(intersection)) return intersection;

        return null;
    }

    @Override
    public LineSegment intersect(Bounds bounds)
    {
        if (segment == null) return null;

        if (segment instanceof ConcreteLine concreteLine) {
            return concreteLine.intersect(bounds);
        }
        else if (segment instanceof LineSegment lineSegment) {
            return lineSegment.intersect(bounds);
        }
        return null;
    }

   @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Bounded Line " + line.toDisplayableString(engine) + " bounds " + originalBounds.toDisplayableString(engine) + " ]";
    }

    @Override
    public String toString()
    {
        return "BoundedLine{" +
               "\n  " + line +
               "\n  originalBounds=" + originalBounds +
               "\n  bounds=" + bounds +
               "\n}";
    }

}
