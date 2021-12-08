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

import gamma.math.Relativity;
import gamma.math.Util;

/**
 * A line is defined by an angle (in degrees) and a point through which
 * the line crosses.
 * <p>
 * A line can be created using other methods, but all are converted to an
 * angle and a point.
 *
 * @author Antonio Freixas
 */
public class Line extends CurveSegment
{
    public enum AxisType
    {
        X, T
    };

    private double angle;
    private Coordinate coord;
    private double slope;
    private double mXOrigin;
    private double t1MinusMX1;

    private boolean isInfiniteMinus;
    private boolean isInfinitePlus;

    /**
     * Create a line from a Frame axis that goes through the origin of the
     * frame.
     *
     * @param type The axis to use.
     * @param frame The frame.
     */
    public Line(AxisType type, Frame frame)
    {
        this(type, frame.getV(), frame.getOrigin());
    }

    /**
     * Create a line parallel to a given axis and that crosses the opposite
     * axis at the given offset.
     *
     * @param type The axis type, AxisType.X or AxisType.T
     * @param frame The frame that defines the axes.
     * @param offset The offset at which to cross the opposite axis.
     */
    public Line(AxisType type, Frame frame, double offset)
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
    public Line(AxisType type, double v, Coordinate point)
    {
        if (type == AxisType.X) {
            initialize(Relativity.vToXAngle(v), point);
        }
        else {
            initialize(Relativity.vToTAngle(v), point);
        }
    }

    /**
     * Create a line at a given angle and that goes through a given coordinate.
     *
     * @param angle The angle of the line in degrees.
     * @param coord The point through which the line crosses.
     */
    public Line(double angle, Coordinate coord)
    {
        initialize(Util.normalizeAngle90(angle), coord);
    }

    /**
     * Create a line that goes through two points.
     *
     * @param coord1
     * @param coord2
     */
    public Line(Coordinate coord1, Coordinate coord2)
    {
        double rad = Math.atan2(coord2.t - coord1.t, coord2.x - coord1.x);
        double degrees = Math.toDegrees(rad);
        degrees = Util.normalizeAngle90(degrees);
        initialize(degrees, new Coordinate(coord1));
    }

    /**
     * Initialize the line.
     *
     * @param angle The line's angle, in degrees, with 0 being parallel to the x
     * axis. The value should be normalized so it is between 90 (inclusive)
     * and -90 (exclusive).
     * @param coord The coordinate through which the line goes.
     */
    private void initialize(double angle, Coordinate coord)
    {
        this.angle = angle;
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
    }

    /**
     * Get the point through which the line crosses.
     *
     * @return The point through which the line crosses.
     */
    public Coordinate getCoordinate()
    {
        return new Coordinate(coord);
    }

    /**
     * Get the angle of the line in degrees.
     *
     * @return The angle of the line in degrees.
     */
    public double getAngle()
    {
        return angle;
    }

    /**
     * Get the slope of a line. If the line is vertical, return +infinity.
     *
     * @return The slope of the line.
     */
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
    public double getConstantOffset()
    {
        return this.t1MinusMX1;
    }

    /**
     * Create a new version of this line that is relative to the given frame
     * rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new line.
     */
    public Line relativeTo(Frame prime)
    {
        return new Line(Relativity.toPrimeAngle(angle, prime.getV()), prime.toFrame(coord));
    }

    /**
     * Intersect this line with another.
     *
     * @param other The other line to intersect with.
     *
     * @return The intersection or null if none.
     */
    public Coordinate intersect(Line other)
    {
        if (other != null) {

            // Check for parallel lines
            // This assumes we've normalized the angles

            if (this.slope == other.slope) return null;

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

            if (!Double.isInfinite(this.slope)) {
                if (!Double.isInfinite(other.slope)) {

                    // Neither line is vertical

                    x = (this.mXOrigin - other.mXOrigin - this.coord.t + other.coord.t) / (this.slope - other.slope);
                }
                else {

                    // The other line is vertical and this line is not

                    x = other.coord.x;
                }
                t = this.slope * (x - this.coord.x) + this.coord.t;
            }
            else {
                if (!Double.isInfinite(other.slope)) {

                    // This line is vertical and the other is not

                    x = this.coord.x;
                    t = other.slope * (x - other.coord.x) + other.coord.t;
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
     * @param bounds The bounding box.
     *
     * @return The line segment that intersects or null if none.
     */
    public LineSegment intersect(Bounds bounds)
    {
        // Intersect with our bounding box (which will have corners at
        // infinity

        bounds = bounds.intersect(getBounds());
        if (bounds == null) return null;

        // Create lines for the edges of the bounding box. If the line is
        // not vertical, we use the left and right edges. If it is vertical,
        // we use the top and bottom edges.

        double boundsAngle = 90.0;

        if (Double.isInfinite(slope)) {
            boundsAngle = 0.0;
        }
        Line line1 = new Line(boundsAngle, bounds.min);
        Line line2 = new Line(boundsAngle, bounds.max);

        // Intersect the line with each edge. There will always be an
        // intersection

        LineSegment segment = new LineSegment(intersect(line1), intersect(line2));

        // Intersect the line segment with the bounds

        return segment.intersect(bounds);
    }

    public boolean isIsIfiniteMinus()
    {
        return isInfiniteMinus;
    }

    public void setIsInfiniteMinus(boolean isInfiniteMinus)
    {
        this.isInfiniteMinus = isInfiniteMinus;
    }

    public boolean isIsInfinitePlus()
    {
        return isInfinitePlus;
    }

    public void setIsInfinitePlus(boolean isInfinitePlus)
    {
        this.isInfinitePlus = isInfinitePlus;
    }

    @Override
    public Bounds getBounds()
    {
        double minX, minT, maxX, maxT;
        if (angle >= 0) {
            minX = Double.NEGATIVE_INFINITY; minT = Double.NEGATIVE_INFINITY; maxX = Double.POSITIVE_INFINITY; maxT = Double.POSITIVE_INFINITY;
        }
        else {
            minX = Double.POSITIVE_INFINITY; minT = Double.NEGATIVE_INFINITY; maxX = Double.NEGATIVE_INFINITY; maxT = Double.POSITIVE_INFINITY;
        }

        if (!isInfiniteMinus) { minX = coord.x; minT = coord.t; }
        if (!isInfinitePlus) { maxX = coord.x; maxT = coord.t; }

        return new Bounds(minX, minT, maxX, maxT);
    }

    @Override
    public String toString()
    {
        return "Line{" +
               "\n  angle=" + angle + ", coord=" + coord +
               "\n isInfiniteMinus=" + isInfiniteMinus + ", isInfinitePlus=" + isInfinitePlus +
               "\n}";
    }


}
