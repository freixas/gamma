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
 * A line is defined by an angle (in radians) and a point through which
 * the line crosses.
 * <p>
 * A line can be created using other methods, but all are converted to an
 * angle and a point.
 *
 * @author Antonio Freixas
 */
public class Line
{
    public enum AxisType
    {
        X, T
    };

    private double angle;
    private Coordinate coord;
    private double slope;
    private double mXOrigin;
    private double offset;

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
        if (type == AxisType.X) {
            initialize(Util.vToXAngle(frame.getV()), frame.toRest(new Coordinate(0, offset)));
        }
        else {
            initialize(Util.vToTAngle(frame.getV()), frame.toRest(new Coordinate(offset, 0)));
        }
    }

    /**
     * Create a line whose angle is parallel to a t axis for an inertial
     * frame based on a given velocity and which passes through a given point.
     *
     * @param type The axis type, AxisType.X or AxisType.T
     * @param v The velocity.
     * @param point The point through which the line crosses.
     */
    public Line(AxisType type, double v, Coordinate point)
    {
        initialize(Util.vToTAngle(v), point);
    }

    /**
     * Create a line at a given angle and that goes through a given coordinate.
     *
     * @param angle The angle of the line in degrees.
     * @param coord The point through which the line crosses.
     */
    public Line(double angle, Coordinate coord)
    {
        initialize(Math.toRadians(Util.normalizeAngle180(angle)), coord);
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
        if (rad == Math.PI) angle = 0;
        if (rad < 0) angle = Math.PI - angle;
        initialize(rad, new Coordinate(coord1));
    }

    /**
     * Initialize the line.
     *
     * @param angle The line's angle, in radians, with 0 being parallel to the x
     * axis. The value should be normalized so it is between 0 (inclusive) and
     * PI (exclusive)
     * @param coord
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

        this.slope = angle == Math.PI / 2 ? Double.POSITIVE_INFINITY : Math.tan(angle);


        // Pre-calculate the slope times the x value (mx1). We'll use this in
        // intersection calculations

        this.mXOrigin = this.slope * this.coord.x;

        // Pre-calculate the constant (t1 - mx1)

        this.offset = this.coord.t - this.mXOrigin;
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
     * Get the angle of the line in radians.
     *
     * @return The angle of the line in radians.
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
    public double getOffset()
    {
        return this.offset;
    }

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
            if (!Double.isInfinite(this.slope)) {
                if (!Double.isInfinite(other.slope)) {

                    // Neither line is vertical

                    x = (this.mXOrigin - other.mXOrigin - this.coord.t + other.coord.t) / (this.slope - other.slope);
                }
                else {

                    // The other line is vertical and this line is not

                    x = other.coord.x;
                }
            }
            else {

                // This line is vertical and the other is not

                x = this.coord.x;
            }

            double t = this.slope * (x - this.coord.x) + this.coord.t;
            return new Coordinate(x, t);
        }
        return null;
    }

    static public Coordinate intersect(Line line1, Line line2)
    {
        if (line1 != null) {
            return line1.intersect(line2);
        }
        return null;
    }

}
