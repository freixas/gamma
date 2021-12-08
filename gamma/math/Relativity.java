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
     * @param v Thevelocity.
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
     * Convert a velocity to a t axis angle in degrees.
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
     * Convert a t axis angle in degrees to a velocity.
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
     * Convert a velocity to an x axis angle in degrees.
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
     * Convert an x axis angle in degrees to a velocity.
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
     * Convert an angle (in degrees) in the rest frame to the equivalent angle
     * in the prime frame.
     *
     * @param angle The angle in degrees in the rest frame.
     * @param v The velocity of the prime frame.
     *
     * @return The angle in degrees in the prime frame.
     */
    public static double toPrimeAngle(double angle, double v)
    {
        // Check whether the angle is the angle of light: +/- 45 degrees

        if (angle == 45 || angle == -45) {
            return angle;
        }

        // X angle

        else if (angle > -45 && angle < 45) {
            double v1 = angleXToV(angle);
            double v2 = vPrime(v1, v);
            return vToXAngle(v2);
        }

        // T angle

        else {
            double v1 = angleTToV(angle);
            double v2 = vPrime(v1, v);
            return vToTAngle(v2);
        }
    }

}
