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
package org.freixas.gamma.math;

import org.freixas.gamma.value.Coordinate;

/**
 *
 * @author Antonio Freixas
 */
public final class Relativity
{
    // We cache the last v and gamma values so as to speed up
    // the gamma calculation

    private static double v = Double.NaN;
    private static double gamma;

    // TERMS:
    //   x = proper distance
    //   t = proper time
    //   v = velocity (valid for either frame)
    //   xPrime = distance in moving frame
    //   tPrime = time in moving frame
    //   gamma is a shorthand for 1 / sqrt(1 - v^2)
    //
    // NOTES:
    //   At time t = 0, t' = 0, x = x' = 0
    //   Velocity v is constant
    //   Velocities are in fractions of light speed, so the speed of light is 1
    //   If t is in seconds, x is in light seconds, etc.

    private Relativity() {}

    /**
     * Given (x, t), calculate x' given an inertial frame F' at velocity v. This
     * is the Lorentz transformation.
     *
     * @param x The x value.
     * @param t The t value
     * @param v The velocity.
     *
     * @return x'.
     */
    static public double xPrime(double x, double t, double v) {
	return (x - v * t) * Relativity.gamma(v);

    }

    /**
     * Given (x, t), calculate t' given an inertial frame F' at velocity v. This
     * is the Lorentz transformation.
     *
     * @param x The x value.
     * @param t The t value
     * @param v The velocity.
     *
     * @return t'.
     */
    static public double tPrime(double x, double t, double v) {
	return (t - v * x) * Relativity.gamma(v);
    }

    /**
     * Given (x', t'), calculate x given an inertial frame F' at velocity v.
     * This is the inverse Lorentz transformation.
     *
     * @param xPrime The x value.
     * @param tPrime The t value
     * @param v The velocity.
     *
     * @return x.
     */
    static public double x(double xPrime, double tPrime, double v) {
	return (xPrime + v * tPrime) * Relativity.gamma(v);
    }

    /**
     * Given (x', t'), calculate t given an inertial frame F' at velocity v.
     * This is the inverse Lorentz transformation.
     *
     * @param xPrime The x value.
     * @param tPrime The t value
     * @param v The velocity.
     *
     * @return t.
     */
    static public double t(double xPrime, double tPrime, double v) {
	return (tPrime + v * xPrime) * Relativity.gamma(v);
    }

    /**
     * Given (x, t), calculate (x', t') given an inertial frame F' at velocity
     * v. This is the Lorentz transformation.
     *
     * @param rest The (x, t) coordinate.
     * @param v The velocity.
     *
     * @return (x', t')
     */
    static public Coordinate toPrimeFrame(Coordinate rest, double v)
    {
        return toPrimeFrame(rest.x, rest.t, v);
    }

    /**
     * Given (x, t), calculate (x', t') given an inertial frame F' at velocity
     * v.This is the Lorentz transformation.
     *
     * @param x The x coordinate.
     * @param t The t coordiante.
     * @param v The velocity.
     *
     * @return (x', t')
     */
    static public Coordinate toPrimeFrame(double x, double t, double v)
    {
        return new Coordinate(xPrime(x, t, v), tPrime(x, t, v));
    }

    /**
     * Given (x', t'), calculate (x, t) given an inertial frame F' at velocity
     * v.This is the Lorentz transformation.
     *
     * @param prime The (x', t') coordinate.
     * @param v The velocity.
     *
     * @return (x, t)
     */
    static public Coordinate toRestFrame(Coordinate prime, double v)
    {
        return toRestFrame(prime.x, prime.t, v);
    }

    /**
     * Given (x', t'), calculate (x, t) given an inertial frame F' at velocity
     * v.This is the Lorentz transformation.
     *
     * @param xP The x' coordinate
     * @param tP The t' coordinate.
     * @param v The velocity.
     *
     * @return (x, t)
     */
    static public Coordinate toRestFrame(double xP, double tP, double v)
    {
        return new Coordinate(x(xP, tP, v), t(xP, tP, v));
    }

    /**
     * Given an inertial frame F' at velocity v, calculate gamma, 1 / sqrt(1 - v<sup>2</sup>).
     *
     * @param v The velocity.
     *
     * @return Gamma.
     */
    static public double gamma(double v) {
	if (Double.isNaN(Relativity.v) || Relativity.v != v) {
	    Relativity.v = v;
	    Relativity.gamma = 1 / Math.sqrt(1 - v*v);
	}
	return Relativity.gamma;
    }

    /**
     * Given gamma, return the corresponding velocity. There are actually two
     * answers, differing only in sign. The sign of the returned value will be
     * the same as the sign of gamma.
     *
     * @param gamma The gamma value.
     *
     * @return The corresponding positive velocity.
     */
    static public double gammaToV(double gamma)
    {
        if (gamma == 0) return Double.POSITIVE_INFINITY;
        return Math.sqrt(gamma * gamma - 1) / gamma;
    }

    /**
     * Given a time tau in inertial frame F' for an observer moving at velocity
     * v, calculate the matching time t, assuming this observer's tau 0 occurs
     * when t is 0.
     *
     * @param tau The time in frame F', the moving frame.
     * @param v The velocity of the moving observer
     *
     * @return
     */
    static public double tauToT(double tau, double v) {
	return tau * Relativity.gamma(v);
    }

    /**
     * Given a time t in inertial frame F, calculate the matching time tau for
     * an observer moving at velocity v, assuming this observer's tau 0 occurs
     * when t is 0.
     *
     * @param t The time in frame F, the rest frame.
     * @param v The velocity of the moving observer.
     *
     * @return Tau for the moving observer.
     */
    static public double tToTau(double t, double v) {
	return t / Relativity.gamma(v);
    }

    /**
     * Given a proper length measured in frame F' at velocity v, calculate
     * the contracted length in frame F.
     *
     * @param length The proper length (in F').
     * @param v The relative velocity.
     *
     * @return The contracted length (in F).
     */
    static public double lengthContraction(double length, double v) {
	return length / Relativity.gamma(v);
    }

    /**
     * Given a contracted length measured in frame F, calculate the proper
     * length in frame F' moving at velocity v.
     *
     * @param length The contracted length (in F).
     * @param v The relative velocity.
     *
     * @return The proper length (in F').
     */
    static public double invLengthContraction(double length, double v) {
	return length * Relativity.gamma(v);
    }

    /**
     * Given a proper duration measured in frame F' at velocity v, calculate the
     * dilated duration in frame F.
     *
     * @param duration The proper duration (in F').
     * @param v The relative velocity.
     *
     * @return The dilated duration (in F).
     */
    static public double timeDilation(double duration, double v) {
	return duration * Relativity.gamma(v);
    }

    /**
     * Given a dilated duration measured in frame F, calculate the dilated
     * duration in frame F' moving at velocity v.
     *
     * @param duration The dilated duration (in F).
     * @param v The relative velocity.
     *
     * @return The proper duration (in F').
     */
    static public double invTimeDilation(double duration, double v) {
	return duration / Relativity.gamma(v);
    }

    /**
     * Given a velocity v1 relative to frame F, calculate the corresponding
     * velocity relative to frame F' moving at velocity v.
     *
     * @param v1 A velocity relative to frame F.
     * @param v The velocity of frame F'.
     *
     * @return The corresponding velocity in frame F'.
     */
    static public double vPrime(double v1, double v)
    {
        return (v1 - v) / (1 - (v1 * v));
    }

    /**
     * Given a velocity v1 relative to frame F' moving at velocity v, calculate
     * the corresponding velocity relative to frame F.
     *
     * @param v1 A velocity relative to frame F'.
     * @param v The velocity of frame F'.
     *
     * @return The corresponding velocity in frame F.
     */
    static public double v(double v1, double v)
    {
        return (v1 + v) / (1 + (v1 * v));
    }

    /**
     * Convert a velocity to an x axis angle in degrees. The velocity must be
     * between -1 and 1, exclusive. The output will be between -45 and 45,
     * exclusive.
     *
     * @param v The velocity as a percentage of the speed of light.
     *
     * @return The x axis angle in degrees.
     */
    public static double vToXAngle(double v)
    {
        return Math.toDegrees(Math.atan(v));
    }

    /**
     * Convert an x axis angle in degrees to a velocity. The angle must be
     * between -45 and 45, exclusive. The output will be between -1 and 1,
     * exclusive.
     *
     * @param angle The x axis angle in degrees.
     *
     * @return The velocity.
     */
    public static double angleXToV(double angle)
    {
        return Math.tan(Math.toRadians(angle));
    }

    /**
     * Convert a velocity to a t axis angle in degrees. The velocity must be
     * between -1 and 1, exclusive. The output will be between -45and
     * -90, exclusive, or between 45, exclusive,  and 90, inclusive.
     * exclusive. A velocity of 0 returns an angle of 90, never -90.
     *
     * @param v The velocity as a percentage of the speed of light.
     *
     * @return The t axis angle in degrees.
     */
    public static double vToTAngle(double v)
    {
        double angle = Math.toDegrees(Math.atan(v));
        if (angle >= 0) {
            return 90 - angle;
        }
        else {
            return -90 - angle;
        }
    }

    /**
     * Convert a t axis angle in degrees to a velocity. The angle must be
     * between 45, exclusive, and 90, inclusive, or between -45 and -90,
     * exclusive. The output will be between -1 and 1, exclusive.
     *
     * @param angle The t axis angle in degrees.
     *
     * @return The velocity.
     */
    public static double angleTToV(double angle)
    {
        angle = Math.toRadians(angle);
        if (angle >= 0) {
            angle = (Math.PI / 2) - angle;
        }
        else {
            angle = (-Math.PI / 2) - angle;
        }
        return Math.tan(angle);
    }

    /**
     * Convert an angle (in degrees) in the rest frame to the equivalent angle
     * in the prime frame. Any angle is acceptable.
     *
     * @param angle The angle in degrees in the rest frame.
     * @param v The velocity of the prime frame.
     *
     * @return The angle in degrees in the prime frame in the range -180,
     * exclusive, to 180, inclusive.
     */
    public static double toPrimeAngle(double angle, double v)
    {
        double angle180 = Util.normalizeAngle180(angle);

        if (Math.abs(angle180) == 45 || Math.abs(angle180) == 135) {
            return angle;
        }

        // Tangent is a periodic function, with its main interval being from
        // -90 to 90. If the angle. We'll normalize the angle to the range -180
        // to 180. If the result falls outside the -90 to 90 range, we'll need
        // to invert the final result

        boolean invert = Math.abs(angle180) > 90;

        double angle90 = Util.normalizeAngle90(angle);
        double v1 = Math.abs(angle90) < 45 ? angleXToV(angle90) : angleTToV(angle90);
        double v2 = vPrime(v1, v);
        double vAngle = Math.abs(angle90) < 45 ? vToXAngle(v2) : vToTAngle(v2);

        if ((v2 > 0 && vAngle > 45) || (v2 < 0 && vAngle < -45)) invert = !invert;

        System.out.println("\n(angle = " + angle + " v = " + v + ") angle180 = " + angle180 + " angle90 = " + angle90 + " v1 = " + v1 + " v2 = " + v2 + " vAngle = " + vAngle);

        if (invert) vAngle = Util.normalizeAngle180(vAngle + 180);
        vAngle = Util.normalizeAngle180(vAngle);

        System.out.println("Final vAngle = " + vAngle);

        return vAngle;

//        // The angles associated with velocity are in the range -90 to 90. The
//        // angles we receive can be anything. Normalize the angle to be between
//        // -180 and 180. Also normalize to be within -90 to 90.
//
//        angle = Util.normalizeAngle180(angle);
//        double angle90 = Util.normalizeAngle90(angle);
//
//        // Check whether the angle is the angle of light: +/- 45 degrees
//
//        if (Math.abs(angle) == 45 || Math.abs(angle) == 135) {
//            return angle;
//        }
//
//        // X angle
//
//        else if (angle90 > -45 && angle90 < 45) {
//            double v1 = angleXToV(angle90);
//            double v2 = vPrime(v1, v);
//            double vAngle = vToXAngle(v2);
//            return vAngle;
//            // return Util.normalizeAngle180(vAngle + 180);
//        }
//
//        // T angle
//
//        else {
//            double v1 = angleTToV(angle90);
//            double v2 = vPrime(v1, v);
//            double vAngle = vToTAngle(v2);
//            if ((v1 <= 0 && v2 <= 0) || (v1 >= 0 && v2 >= 0)) return vAngle;
//            return Util.normalizeAngle180(vAngle + 180);
//        }
    }

}
