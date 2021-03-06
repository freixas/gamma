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
import org.freixas.gamma.math.Util;
import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;

/**
 * Create a bounding box.
 *
 * @author Antonio Freixas
 */
public class Bounds implements ExecutionMutable, Displayable
{
    public Coordinate min;
    public Coordinate max;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

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
        if (other != null) {
            // There's no need to sort as the other bounds will already
            // be sorted

            this.min = new Coordinate(other.min);
            this.max = new Coordinate(other.max);
        }
        else {
            throw new ProgrammingException("Bounds: Trying to copy a null object");
        }
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

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

    // **********************************************************************
    // *
    // * Modify
    // *
    // **********************************************************************

    /**
     * Set this bounding box to a new set of corners. The values given can be
     * for any two opposing corners. The corners are sorted so that the min corner
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
    @SuppressWarnings("unused")
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

    // **********************************************************************
    // *
    // * ExecutionMutable Support
    // *
    // **********************************************************************

    @Override
    public Bounds createCopy()
    {
        return new Bounds(this);
    }

    // **********************************************************************
    // *
    // * Out Codes
    // *
    // **********************************************************************

    /**
     * Return a code that identifies which area a point lies within.
     * See the Cohen - Sutherland algorithm.
     *
     * @param coord The point.
     * @return The out code.
     */
    public int computeOutCode(Coordinate coord)
    {
        return computeOutCode(coord.x, coord.t);
    }

    /**
     * Return a code that identifies which area a point lies within. See the
     * Cohen - Sutherland algorithm.
     *
     * @param x The x coordinate.
     * @param t The t coordinate
     *
     * @return The out code.
     */
    public int computeOutCode(double x, double t)
    {
	int code = 0x0;			// INSIDE

	if (Util.fuzzyLT(x, min.x)) {
	    code |= 0x01;		// LEFT
	}
	else if (Util.fuzzyGT(x, max.x)) {
	    code |= 0x02;		// RIGHT
	}

	if (Util.fuzzyLT(t, min.t)) {
	    code |= 0x04;		// BOTTOM
	}
	else if (Util.fuzzyGT(t, max.t)) {
	    code |= 0x08;		// TOP
	}

	return code;
    }

    // **********************************************************************
    // *
    // * Inside/Outside
    // *
    // **********************************************************************

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
        return computeOutCode(x, t) == 0x0;
    }

    /**
     * Return true if a line segment is completely inside this bounding box.
     *
     * @param segment The line segment.
     * @return True if a line segment is completely inside the clip.
     */
    @SuppressWarnings("unused")
    public boolean completelyInsideClip(LineSegment segment)
    {
	int outCode0 = computeOutCode(segment.getPoint1());
	int outCode1 = computeOutCode(segment.getPoint2());
	return (outCode0 | outCode1) == 0;
    }

    /**
     * Return true if a line segment is completely outside this bounding box.
     *
     * @param segment The line segment.
     * @return True if a line segment is completely outside the clip.
     */

    @SuppressWarnings("unused")
    public boolean completelyOutsideClip(LineSegment segment)
    {
	int outCode0 = computeOutCode(segment.getPoint1());
	int outCode1 = computeOutCode(segment.getPoint2());
	return (outCode0 & outCode1) != 0;
    }

    // **********************************************************************
    // *
    // * Intersections
    // *
    // **********************************************************************

    /**
     * Returns true if the given bounding box intersects with this one.
     *
     * @param other The bounding box to check for intersection.
     *
     * @return True if they intersect.
     */
    public boolean intersects(Bounds other)
    {
        if (other == null) return false;
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
        // If the min and max values are the same and infinite, we have a bounding
        // box that will never bound anything, so we'll return null instead

        if (min.x == max.x && Double.isInfinite(min.x)) return null;
        if (min.t == max.t && Double.isInfinite(min.t)) return null;

        if (other.min.x == other.max.x && Double.isInfinite(other.min.x)) return null;
        if (other.min.t == other.max.t && Double.isInfinite(other.min.t)) return null;

        // Check for intersection

        if (!intersects(other)) return null;

        // Create the intersecting bounds

        return new Bounds(
            Math.max(min.x, other.min.x),
            Math.max(min.t, other.min.t),
            Math.min(max.x, other.max.x),
            Math.min(max.t, other.max.t));
    }
    /**
     * Intersect a line segment with this bounding box. This method returns
     * a line segment that lies completely within the bounds or null if there
     * is no intersection.
     *
     * @param segment The segment to intersect.
     * @return The clipped line segment or null if there is no intersection.
     */

    public LineSegment intersect(LineSegment segment)
    {
        // Copy the line segment

        segment = new LineSegment(segment);

        int outCode0 = computeOutCode(segment.getPoint1());
        int outCode1 = computeOutCode(segment.getPoint2());
        boolean accept = false;

        double slopeXT = Double.NaN;
        double slopeTX = Double.NaN;

        while (true) {

            // Bitwise OR is 0: both points inside clip; trivially
            // accept and exit loop

            if ((outCode0 | outCode1) == 0) {
                accept = true;
                break;
            }

            // Bitwise AND is not 0: both points share an outside zone
            // (LEFT, RIGHT, TOP, or BOTTOM), so both must be outside
            // window; exit loop (accept if false)

            else if ((outCode0 & outCode1) != 0) {
                break;
            }

            // Failed both tests, so calculate the line segment to clip
            // from an outside point to an intersection with clip edge

            else {
                double x = 0.0;
                double t = 0.0;

                // At least one endpoint is outside the clip
                // rectangle; pick it.

                int outCodeOut = outCode0 != 0 ? outCode0 : outCode1;

                // Now find the intersection point; use formulas:
                //
                //   slope = (t1 - t0) / (x1 - x0)
                //   x = x0 + (1 / slope) * (tm - t0), where tm is tMin or tMax
                //   t = t0 + slope       * (xm - x0), where xm is xMin or xMax
                //
                // No need to worry about divide-by-zero because, in
                // each case, the outCode bit being tested guarantees
                // the denominator is non-zero

                // Calculate the slopes if not yet set

                if (Double.isNaN(slopeXT)) {
                    if (Util.fuzzyEQ(segment.getPoint2().t, segment.getPoint1().t)) {
                        slopeXT = Double.POSITIVE_INFINITY;
                    }
                    else {
                        slopeXT = (segment.getPoint2().x - segment.getPoint1().x) / (segment.getPoint2().t - segment.getPoint1().t);
                    }
                    if (Util.fuzzyEQ(segment.getPoint2().x, segment.getPoint1().x)) {
                        slopeTX = Double.POSITIVE_INFINITY;
                    }
                    else {
                        slopeTX = (segment.getPoint2().t - segment.getPoint1().t) / (segment.getPoint2().x - segment.getPoint1().x);
                    }
                }

                if ((outCodeOut & 0x08) != 0) {    // Point is above the clip window
                    t = max.t;
                    x = segment.getPoint1().x + slopeXT * (t - segment.getPoint1().t);
                }
                else if ((outCodeOut & 0x04) != 0) { // Point is below the clip window
                    t = min.t;
                    x = segment.getPoint1().x + slopeXT * (t - segment.getPoint1().t);
                }
                else if ((outCodeOut & 0x02) != 0) {  // Point is to the right of clip window
                    x = max.x;
                    t = segment.getPoint1().t + slopeTX * (x - segment.getPoint1().x);
                }
                else if ((outCodeOut & 0x01) != 0) {   // Point is to the left of clip window
                    x = min.x;
                    t = segment.getPoint1().t + slopeTX * (x - segment.getPoint1().x);
                }

                // Now we move outside point to intersection point to clip
                // and get ready for next pass.

                if (outCodeOut == outCode0) {
                    segment.getPoint1().setTo(x, t);
                    outCode0 = computeOutCode(segment.getPoint1());
                }
                else {
                    segment.getPoint2().setTo(x, t);
                    outCode1 = computeOutCode(segment.getPoint2());
                }
            }
	}

	if (accept) return segment;
	return null;
    }

    // **********************************************************************
    // *
    // * Transformations
    // *
    // **********************************************************************

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

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Bounds from " + min.toDisplayableString(engine) + " to " + max.toDisplayableString(engine) + " ]";
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        return "Bounds{" + "from " + min + " to " + max + '}';
    }

}
