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
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;

/**
 * A concrete line is the main implementation of a line. It is usually infinite,
 * but can be capped off on one or both ends. If capped off on one end, the line
 * starts or ends on the point. If capped off on both ends, the line effectively
 * becomes a single point.
 *
 * @author Antonio Freixas
 */
public class ConcreteLine extends Line
{
    // **********************************************************************
    // *
    // * Nested Classes
    // *
    // **********************************************************************

    /**
     * Lines have starting and ending points, These define the bounds, but the
     * bounds might be lower-left and upper-right, or lower-right and upper-left.
     * Because of this, we can't use a normal Bounds object to store the line
     * bounds.
     *
     * @param min The coordinate with the smallest t value.
     * @param max The coordinate with the largest t value.
     */
    record UnsortedBounds(Coordinate min, Coordinate max)
    {
        @Override
        public String toString()
        {
            return "UnsortedBounds{" + "from " + min + " to " + max + '}';
        }

    }

    private final double angle;
    private final Coordinate coord;
    private final double slope;
    private final double mXOrigin;
    private final double t1MinusMX1;

    private final boolean isInfiniteMinus;
    private final boolean isInfinitePlus;
    private final UnsortedBounds unsortedBounds;
    private final Bounds bounds;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a line that goes through the origin of a frame.
     *
     * @param type The axis to use.
     * @param frame The frame.
     */
    public ConcreteLine(AxisType type, Frame frame)
    {
        this(type, frame.getV(), frame.getOrigin());
    }

    /**
     * Create a line parallel to a given axis of a frame and that crosses the
     * opposite axis at the given offset.
     *
     * @param type The axis type, AxisType.X or AxisType.T
     * @param frame The frame that defines the axes.
     * @param offset The offset at which to cross the opposite axis.
     */
    public ConcreteLine(AxisType type, Frame frame, double offset)
    {
        this(type, frame.getV(), type == AxisType.X ? frame.toRest(new Coordinate(0, offset)) : frame.toRest(new Coordinate(offset, 0)));
    }

    /**
     * Create a line whose angle is parallel to an axis for an inertial
     * frame based on a given velocity and which passes through a given point.
     *
     * @param type The axis type, AxisType.X or AxisType.T
     * @param v The velocity.
     * @param point The point through which the line crosses.
     */
    public ConcreteLine(AxisType type, double v, Coordinate point)
    {
        this((type == AxisType.X ? Relativity.vToXAngle(v) : Relativity.vToTAngle(v)), point);
    }

    /**
     * Create a line that goes through two points. Since lines are stored as an
     * angle and a point through which the line passed, the two points are used
     * to calculate the line's angle and the first point becomes the one through
     * which the line passes.
     *
     * @param coord1 One point.
     * @param coord2 The other point.
     */
    public ConcreteLine(Coordinate coord1, Coordinate coord2)
    {
        this(Math.toDegrees(Math.atan2(coord2.t - coord1.t, coord2.x - coord1.x)), coord1);
    }

    /**
     * Create a line at a given angle and that goes through a given coordinate.
     *
     * @param angle The angle of the line in degrees.
     * @param coord The point through which the line crosses.
     */
    public ConcreteLine(double angle, Coordinate coord)
    {
        this.angle = angle = Util.normalizeAngle90(angle);
        this.coord = new Coordinate(coord);

        // The equation for the line is t = m * (x - x1) + t1
        // where (x1, t1) is the point through which the line crosses
        // and m is the slope.
        //
        // The slope is m = tan(angle), where the angle is not PI/2 and
        // +infinity otherwise (it's actually +/- infinity).
        //
        // The formula can be simplified to t = mx + (t1 - mx1)


        // Vertical lines have slopes of +/- infinity, so we assign +infinity

        this.slope = angle == 90.0 ? Double.POSITIVE_INFINITY : Math.tan(Math.toRadians(angle));

        // Pre-calculate the slope times the x value (mx1). We'll use this in
        // intersection calculations

        this.mXOrigin = this.slope * this.coord.x;

        // Pre-calculate the constant (t1 - mx1)

        this.t1MinusMX1 = this.coord.t - this.mXOrigin;

        this.isInfiniteMinus = true;
        this.isInfinitePlus = true;
        this.unsortedBounds = getUnsortedBounds();
        this.bounds = new Bounds(unsortedBounds.min, unsortedBounds.max);
    }

    /**
     * Create a new line based on another line. The new line can be capped
     * off differently from the original line.
     *
     * @param other The line used to determine the infinite line.
     * @param isInfiniteMinus If true, don't cap off the start.
     * @param isInfinitePlus  If true, don't cap off the end.
     */
    public ConcreteLine(ConcreteLine other, boolean isInfiniteMinus, boolean isInfinitePlus)
    {
	this.angle = other.angle;
	this.coord = other.coord;
	this.slope = other.slope;
	this.mXOrigin = other.mXOrigin;
	this.t1MinusMX1 = other.t1MinusMX1;

	this.isInfiniteMinus = isInfiniteMinus;
	this.isInfinitePlus = isInfinitePlus;
        this.unsortedBounds = getUnsortedBounds();
        this.bounds = new Bounds(unsortedBounds.min, unsortedBounds.max);
    }

    /**
     * Create a new line offset from an existing line.
     *
     * @param other The line on which to base the new line.
     * @param offset The offset to use. The offset is subtracted so that a
     * line going through (x, t) will go through (x - offset.x, t - offset.t).
     */

    public ConcreteLine(ConcreteLine other, Coordinate offset)
    {
	this.angle = other.angle;
        Coordinate offsetCoord = new Coordinate(other.coord);
        offsetCoord.subtract(offset);
	this.coord = offsetCoord;
	this.slope = other.slope;
        this.mXOrigin = this.slope * this.coord.x;
        this.t1MinusMX1 = this.coord.t - this.mXOrigin;

	this.isInfiniteMinus = other.isInfiniteMinus;
	this.isInfinitePlus = other.isInfinitePlus;
        this.unsortedBounds = getUnsortedBounds();
        this.bounds = new Bounds(unsortedBounds.min, unsortedBounds.max);
    }

    /**
     * Copy constructor.
     *
     * @param other The other line to copy.
     */
    public ConcreteLine(ConcreteLine other)
    {
        this.angle = other.angle;
        this.coord = other.coord;
        this.slope = other.slope;
        this.mXOrigin = other.mXOrigin;
        this.t1MinusMX1 = other.t1MinusMX1;

        this.isInfiniteMinus = other.isInfiniteMinus;
        this.isInfinitePlus = other.isInfinitePlus;
        this.unsortedBounds = other.unsortedBounds;
        this.bounds = other.bounds;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the point through which the line crosses.
     *
     * @return The point through which the line crosses.
     */
    @Override
    public Coordinate getCoordinate()
    {
        return new Coordinate(coord);
    }

    /**
     * Get the angle of the line in degrees.
     *
     * @return The angle of the line in degrees.
     */
    @Override
    public double getAngle()
    {
        return angle;
    }

    /**
     * Get the slope of a line. If the line is vertical, return +infinity.
     *
     * @return The slope of the line.
     */
    @Override
    public double getSlope()
    {
        return this.slope;
    }

    /**
     * Get the line's offset. Given the standard formula for a line,
     * t = mx + k, where m is the slope, k is the offset.
     *
     * @return The line's offset.
     */
    @Override
    public double getConstantOffset()
    {
        return this.t1MinusMX1;
    }

    /**
     * If true, the line starts at some infinite distance. If not true, the
     * line starts at the point.
     *
     * @return True if the line starts at some infinite distance.
     */
    @Override
    public boolean isInfiniteMinus()
    {
        return isInfiniteMinus;
    }

    /**
     * If true, the line ends at some infinite distance. If not true, the
     * line ends at the point.
     *
     * @return True if the line ends at some infinite distance.
     */
    @Override
    public boolean isInfinitePlus()
    {
        return isInfinitePlus;
    }

    /**
     * Get the bounds of a line. For an infinite line, the bounds go from
     * negative to positive infinity. A line can be marked as finite on one or
     * bother ends. The finite end is the point through which the line
     * traverses.
     * <p>
     * This is used mainly by worldlines where the initial segment or the final
     * segment has zero acceleration.
     *
     * @return The bounding box.
     */
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
        return new ConcreteLine(this, offset);
    }

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    /**
     * Create a new version of this line that is relative to the given frame
     * rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new line.
     */
    @Override
    public ConcreteLine relativeTo(Frame prime)
    {
        return
            new ConcreteLine(
                new ConcreteLine(Relativity.toPrimeAngle(angle, prime.getV()), prime.toFrame(coord)),
                isInfiniteMinus, isInfinitePlus);
    }

    // **********************************************************************
    // *
    // * Intersections
    // *
    // **********************************************************************

    /**
     * Intersect this line with another. If there are infinite matches,
     * return the earliest one. This could generate an infinite coordinate.
     *
     * @param other The other line to intersect with.
     *
     * @return The intersection or null if none.
     */
    @Override
    public Coordinate intersect(Line other)
    {
        if (other != null) {

            Bounds intersectionBounds = bounds.intersect(other.getBounds());
            if (intersectionBounds == null) return null;

            // Check for parallel lines

            if (Util.fuzzyEQ(this.slope, other.getSlope())) {

                // Both vertical and overlapping?

                if (Double.isInfinite(this.slope) && coord.fuzzyEQ(other.getCoordinate())) {
                    return new Coordinate(coord.x, intersectionBounds.min.t);
                }

                // Both horizontal and overlapping?

                else if (Util.fuzzyZero(this.slope) && coord.fuzzyEQ(other.getCoordinate())) {
                    return new Coordinate(intersectionBounds.min.x, coord.t);
                }

                // We can't come up with a coordinate for parallel lines that
                // aren't horizontal or vertical

                return null;
            }

            // The equation for the line is t = m * (x - x1) + t1
            // where (x1, t1) is the point through which the line crosses
            // and m is the slope.
            //
            // The slope is m = tan(angle), where the angle is not PI/2.
            //
            // If neither line is vertical, then the intersection is
            // m1 * (x - x1) + t1 = m2 * (x - x2) + t2
            //
            // Solving for x, we get
            //
            // (x = m1 * x1 - m2 * x2 - t1 + t2) / (m1 - m2)
            //
            // Then solve for t using the original equation

            double x;
            double t;
            double otherMXOrigin = other.getSlope() * other.getCoordinate().x;

            if (!Double.isInfinite(this.slope)) {
                if (!Double.isInfinite(other.getSlope())) {

                    // Neither line is vertical

                    x = (this.mXOrigin - otherMXOrigin - this.coord.t + other.getCoordinate().t) / (this.slope - other.getSlope());
                }
                else {

                    // The other line is vertical and this line is not

                    x = other.getCoordinate().x;
                }
                t = this.slope * (x - this.coord.x) + this.coord.t;
            }
            else {
                if (!Double.isInfinite(other.getSlope())) {

                    // This line is vertical and the other is not

                    x = this.coord.x;
                    t = other.getSlope() * (x - other.getCoordinate().x) + other.getCoordinate().t;
                }
                else {
                    return null;
                }
            }

            return new Coordinate(x, t);
        }
        return null;
    }

    /**
     * Intersect one line with another.
     *
     * @param line1 The first line.
     * @param line2 The second line
     *
     * @return The intersection or null if none.
     */
    static public Coordinate intersect(Line line1, Line line2)
    {
        if (line1 != null) {
            return line1.intersect(line2);
        }
        return null;
    }

    /**
     * Intersect this line with a bounding box. Return the line segment
     * that intersects or null if none.
     * <p>
     * The given bounding box cannot have corners at infinity.
     *
     * @param extBounds The bounding box.
     *
     * @return The line segment that intersects or null if none.
     */
    @Override
    public LineSegment intersect(Bounds extBounds)
    {
        if (Double.isInfinite(extBounds.min.x) ||
            Double.isInfinite(extBounds.min.t) ||
            Double.isInfinite(extBounds.max.x) ||
            Double.isInfinite(extBounds.max.t)) {
            throw new ProgrammingException("Line.intersect(): received a bounding box with an infinite coordinate");
        }

        // Intersect with our bounding box (which will have corners at
        // infinity

        extBounds = extBounds.intersect(getBounds());
        if (extBounds == null) return null;

        // Create lines for the edges of the bounding box. If the line is
        // not vertical, we use the left and right edges. If it is vertical,
        // we use the top and bottom edges.

        double boundsAngle = 90.0;

        if (Double.isInfinite(slope)) {
            boundsAngle = 0.0;
        }
        ConcreteLine line1 = new ConcreteLine(boundsAngle, extBounds.min);
        ConcreteLine line2 = new ConcreteLine(boundsAngle, extBounds.max);

        // Intersect the line with each edge. There will always be an
        // intersection

        LineSegment segment = new LineSegment(intersect(line1), intersect(line2));

        // Intersect the line segment with the extBounds

        segment =  segment.intersect(extBounds);

        // Make sure the line segment's first point is always earlier in time
        // than its second

        if (segment != null && segment.getPoint1().t > segment.getPoint2().t) {
            segment = new LineSegment(segment.getPoint2(), segment.getPoint1());
        }
        return segment;
    }

    /**
     * Intersect this line with a bounding box. Return the curve segment
     * that intersects or null if none. This segment will be either a line or a
     * line segment.
     * <p>
     * The given bounding box can have corners at infinity.
     *
     * @param extBounds The bounding box.

     * @return The curve segment that intersects or null if none.
     */
    public CurveSegment infiniteIntersect(Bounds extBounds)
    {
        // Intersect the line's bounding box with the given bounds. We will
        // use the intersecting bounding box for further calculations. If there
        // is no intersection, we're done.

        extBounds = bounds.intersect(extBounds);
        if (extBounds == null) return null;

        // Non-infinite bounding box: use the standard intersect() method

        if (!Double.isInfinite(extBounds.min.x) &&
            !Double.isInfinite(extBounds.min.t) &&
            !Double.isInfinite(extBounds.max.x) &&
            !Double.isInfinite(extBounds.max.t)) {
            return intersect(extBounds);
        }

        // If the intersected bounding box is the same as the line's bounding
        // box, we return the line

        if (extBounds.min.x == bounds.min.x &&
            extBounds.min.t == bounds.min.t &&
            extBounds.max.x == bounds.max.x &&
            extBounds.max.t == bounds.max.t) {
            return new ConcreteLine(this);
        }

        // Is the line vertical?

        if (Util.fuzzyEQ(angle, 90)) {
            if (Double.isFinite(extBounds.min.t) && Double.isFinite(extBounds.max.t)) {
                return new LineSegment(coord.x, extBounds.min.t, coord.x, extBounds.max.t);
            }
            double t;
            if (Double.isFinite(extBounds.min.t)) {
                t = extBounds.min.t;
            }
            else if (Double.isFinite(extBounds.max.t)) {
                t = extBounds.max.t;
            }
            else {
                t = coord.t;
            }
            ConcreteLine line = new ConcreteLine(angle, new Coordinate(coord.x, t));
            return new ConcreteLine(line, Double.isInfinite(extBounds.min.t), Double.isInfinite(extBounds.max.t));
        }

        // Is the line horizontal?

        if (Util.fuzzyZero(angle)) {
            if (Double.isFinite(extBounds.min.x) && Double.isFinite(extBounds.max.x)) {
                return new LineSegment(extBounds.min.x, coord.t, extBounds.max.x, coord.t);
            }
            double x;
            if (Double.isFinite(extBounds.min.x)) {
                x = extBounds.min.x;
            }
            else if (Double.isFinite(extBounds.max.x)) {
                x = extBounds.max.x;
            }
            else {
                x = coord.x;
            }
            ConcreteLine line = new ConcreteLine(angle, new Coordinate(x, coord.t));
            return new ConcreteLine(line, Double.isInfinite(extBounds.min.x), Double.isInfinite(extBounds.max.x));
        }

        Coordinate point1 = new Coordinate(unsortedBounds.min);
        Coordinate point2 = new Coordinate(unsortedBounds.max);

	int outcode0 = extBounds.computeOutCode(point1);
	int outcode1 = extBounds.computeOutCode(point2);

	boolean accept = false;

	while (true) {

	    // Bitwise OR is 0: both points inside clip; trivially
	    // accept and exit loop

	    if ((outcode0 | outcode1) == 0) {
		accept = true;
		break;
	    }

	    // Bitwise AND is not 0: both points share an outside zone
	    // (LEFT, RIGHT, TOP, or BOTTOM), so both must be outside
	    // window; exit loop (accept is false)

	    else if ((outcode0 & outcode1) != 0) {
		break;
	    }

	    // Failed both tests, so calculate the line segment to clip
	    // from an outside point to an intersection with clip edge

	    else {
		// At least one endpoint is outside the clip
		// rectangle; pick it.

		int outcodeOut = outcode0 != 0 ? outcode0 : outcode1;

                Coordinate point;

		if ((outcodeOut & 0x08) != 0) { 	// Point is above the clip window
                    ConcreteLine line = new ConcreteLine(0, new Coordinate(0, extBounds.max.t));
                    point = this.intersect(line);
		}
		else if ((outcodeOut & 0x04) != 0) { // Point is below the clip window
                    ConcreteLine line = new ConcreteLine(0, new Coordinate(0, extBounds.min.t));
                    point = this.intersect(line);
		}
		else if ((outcodeOut & 0x02) != 0) {  // Point is to the right of clip window
                    ConcreteLine line = new ConcreteLine(90, new Coordinate(extBounds.max.x, 0));
                    point = this.intersect(line);
		}
		else if ((outcodeOut & 0x01) != 0) {   // Point is to the left of clip window
                    ConcreteLine line = new ConcreteLine(90, new Coordinate(extBounds.min.x, 0));
                    point = this.intersect(line);
		}
                else {
                    throw new ProgrammingException("Line.infiniteIntersect(): Unexpected point position");
                }

		// Now we move outside point to intersection point to clip
		// and get ready for next pass.

		if (outcodeOut == outcode0) {
                    point1 = point;
		    outcode0 = extBounds.computeOutCode(point1);
		}
		else {
		    point2 = point;
		    outcode1 = extBounds.computeOutCode(point2);
		}
	    }
	}

	if (accept) {

            // If both ends are finite, we return a LineSegment.

            if (Double.isFinite(point1.x) && Double.isFinite(point1.t) &&
                Double.isFinite(point2.x) && Double.isFinite(point2.t)) {
                return new LineSegment(point1, point2);
            }

            // We have a line  with one finite end and one infinite end. Base a
            // new line on the finite end

            Coordinate point;
            Coordinate infPoint;
            if (Double.isFinite(point1.x) && Double.isFinite(point1.t)) {
                point = point1;
                infPoint = point2;
            }
            else {
                point = point2;
                infPoint = point1;
            }
            ConcreteLine newLine = new ConcreteLine(angle, point);

            // The infinite end will be the same as the current line

            return new ConcreteLine(newLine, infPoint.t == Double.NEGATIVE_INFINITY, infPoint.t == Double.POSITIVE_INFINITY);
        }
	return null;
    }

    // **********************************************************************
    // *
    // * Private
    // *
    // **********************************************************************

    /**
     * A private method used to calculate the bounds of the line for
     * the constructors. These need to be unsorted.
     *
     * @return The bounds of the line.
     */
    private UnsortedBounds getUnsortedBounds()
    {
        double minX, minT, maxX, maxT;

        minT = Double.NEGATIVE_INFINITY;
        maxT = Double.POSITIVE_INFINITY;

        // Vertical line

        if (Util.fuzzyEQ(angle, 90.0)) {
            minX = maxX = coord.x;
        }

        // Horizontal line

        else if (Util.fuzzyZero(angle)) {
            minT = maxT = coord.t;
            minX = Double.NEGATIVE_INFINITY;
            maxX = Double.POSITIVE_INFINITY;
        }

        // Slope from 0 to 90 degrees (exclusive)

        else if (angle >= 0) {
            minX = Double.NEGATIVE_INFINITY;
            maxX = Double.POSITIVE_INFINITY;
        }

        // Slope from 0 to -90 degrees (exclusive)

        else {
            minX = Double.POSITIVE_INFINITY;
            maxX = Double.NEGATIVE_INFINITY;
        }

        if (!isInfiniteMinus) { minX = coord.x; minT = coord.t; }
        if (!isInfinitePlus) { maxX = coord.x; maxT = coord.t; }

        return new UnsortedBounds(new Coordinate(minX, minT), new Coordinate(maxX, maxT));
    }

    // **********************************************************************
    // *
    // * Display Support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Line " + engine.toDisplayableString(angle) + " degrees through " + coord.toDisplayableString(engine) + " ]";
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        return "Line{" +
               "\n  angle=" + angle + ", coord=" + coord +
               "\n  isInfiniteMinus=" + isInfiniteMinus + ", isInfinitePlus=" + isInfinitePlus +
               "\n  bounds=" + unsortedBounds +
               "\n}";
    }


}
