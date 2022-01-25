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

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;

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
    private final ConcreteLine line;
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
        if (line == null) throw new ExecutionException("setBounds() has a null line");
        if (bounds == null) throw new ExecutionException("setBounds() has a null bounding box");

        if (line instanceof BoundedLine boundedLine) {
            this.line = boundedLine.line;
        }
        else if (line instanceof ConcreteLine concreteLine) {
            this.line = concreteLine;
        }
        else {
            throw new ProgrammingException("BoundedLine: line is not a bounded or concrete line");
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
     * Create a new line offset from an existing line.
     *
     * @param other The line on which to base the new line.
     * @param offset The offset to use. The offset is subtracted so that a
     * line going through (x, t) will go through (x - offset.x, t - offset.t).
     */

    public BoundedLine(BoundedLine other, Coordinate offset)
    {
        this.line = new ConcreteLine(other.line, offset);
        this.originalBounds = new Bounds(
            other.bounds.min.subtract(offset),
            other.bounds.max.subtract(offset));
        segment = this.line.infiniteIntersect(this.originalBounds);
        if (segment != null) {
            this.bounds = segment.getBounds();
        }
        else {
            this.bounds = null;
        }
    }

    /**
     * This is a special constructor used by the relativeTo() method.
     *
     * @param line The line to attach to.
     * @param bounds The bounding box to attach.
     * @param segment The bounded segment.
     */
    private BoundedLine(ConcreteLine line, Bounds bounds, CurveSegment segment)
    {
        this.line = line;
        this.originalBounds = new Bounds(bounds);
        this.segment = segment;
        if (segment != null) {
            this.bounds = segment.getBounds();
        }
        else {
            this.bounds = null;
        }
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

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

    // **********************************************************************
    // *
    // * Offset support
    // *
    // **********************************************************************

    /**
     * Create a new line offset by the given coordinate.
     *
     * @param offset The coordinate to offset by. A point (x, t) on the line
     * becomes (x - offset.x, t - offset.t) in the new line.
     *
     * @return A new offset line.
     */
    @Override
    public Line offsetLine(Coordinate offset)
    {
        return new BoundedLine(this, offset);
    }

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    @Override
    public BoundedLine relativeTo(Frame prime)
    {
        // Convert the original line to the new frame, although I don't think
        // it gets used (the relativeTo() call is just before the execution of
        // the l-code, while the original line might only be used in the h-code

        ConcreteLine newLine = line.relativeTo(prime);

        CurveSegment newSegment;

        if (segment == null) {
            newSegment = null;
        }

        // If the CurveSegment is a ConcreteLine, we need to make it
        // relative to the drawing frame

        else if (segment instanceof ConcreteLine concreteLine) {
            newSegment = concreteLine.relativeTo(prime);
        }

        // If the CurveSegment is a LineSegment, make it relative to the
        // drawing frame

        else if (segment instanceof LineSegment boundedSegment) {
            newSegment = boundedSegment.relativeTo(prime);
        }

        else {
            throw new ProgrammingException("BoundedLine.relativeTo(): Unexpected CurveSegment type");
        }

        return new BoundedLine(newLine, originalBounds, newSegment);
    }

    // **********************************************************************
    // *
    // * Intersections
    // *
    // **********************************************************************

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

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

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
