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
package gamma.math;

import gamma.value.Coordinate;

/**
 *
 * @author Antonio Freixas
 */
public final class Util
{
    public static final double EPSILON = 5.0E-12;

    private Util() {}

    // Normalizations to any range work like this:
    //
    // The range starts at "start" and ends at "end"
    //
    //  width       = end - start  ;
    //  offsetValue = value - start;   // value relative to 0
    //  result = (offsetValue - (floor(offsetValue / width) * width)) + start;
    //
    // The result falls within the range inclusive of the start and exclusive
    // of the end.

    /**
     * Normalize the angle to be in the range 0 - 360.
     * The range is inclusive of the start and exclusive of the end.
     *
     * @param angle The angle to normalize in degrees.
     * @return The normalized angle in degrees.
     */
    static public double normalizeAngle360(double angle)
    {
        return angle - (Math.floor(angle / 360.0) * 360.0);
    }

    /**
     * Normalize the angle to be in the range 0 - 180.
     * The range is inclusive of the start and exclusive of the end.
     *
     * @param angle The angle to normalize in degrees.
     * @return The normalized angle in degrees.
     */
    static public double normalizeAngle180(double angle)
    {
        return angle - (Math.floor(angle / 180.0) * 180.0);
    }

    /**
     * Normalize the angle to be in the range -90 - +90.
     * The range is exclusive of the start and inclusive of the end.
     *
     * @param angle The angle to normalize in degrees.
     * @return The normalized angle in degrees.
     */
    static public double normalizeAngle90(double angle)
    {
        angle = angle + 90;
        angle = angle - (Math.floor(angle / 180.0) * 180.0) - 90;
        return angle == -90.0 ? 90.0 : angle;
    }

    /**
     * Get the angle of a line segment (from +180 to -180)
     *
     * @param start The starting point.
     * @param end The ending point.
     *
     * @return The angle of the line segment in degrees in the range +180
     * (inclusive) to -180 (exclusive).
     */
    static public double getAngle(Coordinate start, Coordinate end)
    {
        return getAngle(start.x, start.t, end.x, end.t);
    }

    /**
     * Get the angle of a line segment (from +180 to -180)
     *
     * @param startX The starting point's x value.
     * @param startT The starting point's t value.
     * @param endX The ending point's x value.
     * @param endT The ending point's t value.
     *
     * @return The angle of the line segment in degrees in the range +180
     * (inclusive) to -180 (exclusive).
     */
    static public double getAngle(double startX, double startT, double endX, double endT)
    {
        double deltaX = endX - startX;
        double deltaT = endT - startT;
        double m = (deltaT) / (deltaX);

        if (Util.fuzzyZero(deltaX)) {
            if (deltaT > 0.0) return 90.0;
            return -90.0;
        }
        // The angle will be from +90 to -90

        double angle = Math.toDegrees(Math.atan(m));

        if (deltaX > 0.0) return angle;
        if (angle <= 0.0) return 180.0 + angle;
        return angle - 180.0;
    }

//    static public double asinh(double x)
//    {
//        return Math.log(x + Math.sqrt(x*x + 1.0));
//    }

    static public double asinh(double x)
    {
        final double sign;

        if (Double.doubleToRawLongBits(x) < 0) {
            x = Math.abs(x);
            sign = -1.0d;
        }
        else {
            sign = 1.0d;
        }
        return sign * Math.log(Math.sqrt(x * x + 1.0) + x);
    }

    static public double acosh(double x)
    {
        return Math.log(x + Math.sqrt(x*x - 1.0));
    }

//    static public double atanh(double x)
//    {
//        return Math.log((x + 1.0) / (x - 1.0)) / 2;
//    }
//
    /**
     * Calculates inverse hyperbolic tangent of a {@code double} value.
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is x zero with the same sign
     * as the argument.
     * <li>If the argument is +1, then the result is positive infinity.
     * <li>If the argument is -1, then the result is negative infinity.
     * <li>If the argument's absolute value is greater than 1, then the result
     * is NaN.
     * </ul>
     *
     * @param x The number for which to get the inverse hyperbolic tangent/
     * @return The inverse hyperbolic tangent.
     */
    static public double atanh(double x)
    {
        final double mult;

        // check the sign bit of the raw representation to handle -0

        if (Double.doubleToRawLongBits(x) < 0) {
            x = Math.abs(x);
            mult = -0.5d;
        } else {
            mult = 0.5d;
        }
        return mult * Math.log((1.0d + x) / (1.0d - x));
    }


    /**
     * Get the sign of a number
     *
     * @param d The number
     * @return -1 for negative numbers and +1 for anything else.
     */
    static public double sign(double d)
    {
        double sign = Math.signum(d);
        return sign == 0 ? 1 : sign;
    }

    static public boolean fuzzyZero(double d)
    {
        return Math.abs(d) < EPSILON;
    }

    static public boolean fuzzyEQ(double d1, double d2)
    {
        return Math.abs(d1 - d2) < EPSILON;
    }

    static public boolean fuzzyLT(double d1, double d2)
    {
        if (fuzzyEQ(d1, d2)) return false;
        return d1 + EPSILON < d2;
    }

    static public boolean fuzzyLE(double d1, double d2)
    {
        return d1 <= d2  + EPSILON;
    }

    static public boolean fuzzyGT(double d1, double d2)
    {
        if (fuzzyEQ(d1, d2)) return false;
         return d1 - EPSILON > d2;
    }

    static public boolean fuzzyGE(double d1, double d2)
    {
        return d1 >= d2  - EPSILON ;
    }

    static public int toInt(double d)
    {
        return (int) (-Math.signum(d) * Math.ceil(-Math.abs(d) - 0.5));
    }

    static public long toLong(double d)
    {
        return (long) (-Math.signum(d) * Math.ceil(-Math.abs(d) - 0.5));
    }

    static public boolean toBoolean(double d)
    {
        return d != 0.0;
    }

}
