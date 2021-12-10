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

import gamma.math.Util;

/**
 * This is a line segment between two points.
 * <p>
 * This is a mutable object.
 *
 * @author Antonio Freixas
 */
public class LineSegment extends CurveSegment implements ExecutionImmutable
{
    public final Coordinate point1;
    public final Coordinate point2;

    public LineSegment(double x1, double t1, double x2, double t2)
    {
        this(new Coordinate(x1, t1), new Coordinate(x2, t2));
    }

    public LineSegment(Coordinate p1, Coordinate p2)
    {
        this.point1 = new Coordinate(p1);
        this.point2 = new Coordinate(p2);
    }

    public LineSegment(LineSegment other)
    {
        this.point1 = new Coordinate(other.point1);
        this.point2 = new Coordinate(other.point2);
    }

    @Override
    public final Bounds getBounds()
    {
        // Since this object is mutable, we need to ensure that the bounds
        // actually reflect the current state of the object

        return new Bounds(point1.x, point1.t, point2.x, point2.t);
    }

    /**
     * Intersect this line segment with a bounding box. This method returns a
     * line segment that lies completely within the bounds or null if there is
     * no intersection.
     *
     * @param bounds
     *
     * @return The clipped line segment or null if there is no intersection.
     */
    public LineSegment intersect(Bounds bounds)
    {
        // Copy this line segment

	LineSegment segment = new LineSegment(this);

	int outcode0 = bounds.computeOutCode(segment.point1);
	int outcode1 = bounds.computeOutCode(segment.point2);
	boolean accept = false;

        double slopeXT = Double.NaN;
        double slopeTX = Double.NaN;

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
		double x = 0.0;
		double t = 0.0;

		// At least one endpoint is outside the clip
		// rectangle; pick it.

		int outcodeOut = outcode0 != 0 ? outcode0 : outcode1;

		// Now find the intersection point; use formulas:
                //
		//   slope = (t1 - t0) / (x1 - x0)
		//   x = x0 + (1 / slope) * (tm - t0), where tm is tmin or tmax
		//   t = t0 + slope       * (xm - x0), where xm is xmin or xmax
                //
		// No need to worry about divide-by-zero because, in
		// each case, the outcode bit being tested guarantees
		// the denominator is non-zero

                // Calculate the slopes if not yet set

                if (Double.isNaN(slopeXT)) {
                    if (Util.fuzzyEQ(segment.point2.t, segment.point1.t)) {
                        slopeXT = Double.POSITIVE_INFINITY;
                    }
                    else {
                        slopeXT = (segment.point2.x - segment.point1.x) / (segment.point2.t - segment.point1.t);
                    }
                   if (Util.fuzzyEQ(segment.point2.x, segment.point1.x)) {
                        slopeTX = Double.POSITIVE_INFINITY;
                    }
                    else {
                        slopeTX = (segment.point2.t - segment.point1.t) / (segment.point2.x - segment.point1.x);
                    }
                }

		if ((outcodeOut & 0x08) != 0) { 	// Point is above the clip window
		    t = bounds.max.t;
		    x = segment.point1.x + slopeXT * (t - segment.point1.t);
		}
		else if ((outcodeOut & 0x04) != 0) { // Point is below the clip window
		    t = bounds.min.t;
		    x = segment.point1.x + slopeXT * (t - segment.point1.t);
		}
		else if ((outcodeOut & 0x02) != 0) {  // Point is to the right of clip window
		    x = bounds.max.x;
		    t = segment.point1.t + slopeTX * (x - segment.point1.x);
		}
		else if ((outcodeOut & 0x01) != 0) {   // Point is to the left of clip window
		    x = bounds.min.x;
		    t = segment.point1.t + slopeTX * (x - segment.point1.x);
		}

		// Now we move outside point to intersection point to clip
		// and get ready for next pass.

		if (outcodeOut == outcode0) {
                    segment.point1.setTo(x, t);
		    outcode0 = bounds.computeOutCode(segment.point1);
		}
		else {
		    segment.point2.setTo(x, t);
		    outcode1 = bounds.computeOutCode(segment.point2);
		}
	    }
	}

	if (accept) return segment;
	return null;
    }

    /**
     * Get the angle of this line segment (from +180 to -180)
     *
     * @return The angle of the line segment in degrees in the range +180
     * (inclusive) to -180 (exclusive).
     */
    public double getAngle()
    {
        return Util.getAngle(point1, point2);
    }

    @Override
    public String toString()
    {
        return "LineSegment{" + " from " + point1 + " to " + point2 + '}';
    }



}
