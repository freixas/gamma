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

/**
 *
 * @author Antonio Freixas
 */
public final class Util
{
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
     * The range is inclusive of the start and exclusive of the end.
     *
     * @param angle The angle to normalize in degrees.
     * @return The normalized angle in degrees.
     */
    static public double normalizeAngle90(double angle)
    {
        angle = angle + 90;
        return angle - (Math.floor(angle / 180.0) * 180.0) - 90;
    }

    static public double asinh(double x)
    {
        return Math.log(x + Math.sqrt(x*x + 1.0));
    }

    static public double acosh(double x)
    {
        return Math.log(x + Math.sqrt(x*x - 1.0));
    }

    static public double atanh(double x)
    {
        return Math.log((x + 1.0) / (x - 1.0)) / 2;
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
        return sign == 0 ? 1: sign;
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

    /**
     * Given a value and delta, find the smallest string representation of the
     * value that distinguishes the value from the value + delta.Scientific
     * notation is allowed.
     *
     * @param value The value whose string representation we want.
     * @param delta The delta value between the given value and the next
     *
     * @return The smallest string representation of the value.
     */
    static public String doubleToMinSizeString(double value, double delta)
    {
        int numOfSigDigits =
           (int)(Math.abs(Math.floor(Math.log10(Math.abs(value))) - Math.floor(Math.log10(Math.abs(delta)))) + 1.0);

        String value1 = String.format("%" + numOfSigDigits + "f", value);
        String value2 = String.format("%" + numOfSigDigits + "e", value);
        return value1.length() < value2.length() ? value1 : value2;
    }

}
