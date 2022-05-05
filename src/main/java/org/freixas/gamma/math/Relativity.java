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

import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.value.Coordinate;

/**
 *
 * @author Antonio Freixas
 */
@SuppressWarnings("unused")
public final class Relativity
{
    static final double C_M_SEC = 299792458.0;
    static final double C = C_M_SEC;
    static final double YEAR_IN_SEC = 365.25 * 24 * 60 * 60;
    static final double SEC_IN_YEARS = 1 / YEAR_IN_SEC;

    // We cache the last v and gamma values to speed up
    // the gamma calculation

    static private double v = Double.NaN;
    static private double gamma;

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
     * @param t The t coordinate.
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
    static public double gamma(double v)
    {
        if (Double.isNaN(Relativity.v) || Relativity.v != v) {
            Relativity.v = v;
            Relativity.gamma = 1 / Math.sqrt(1 - v * v);
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
     * @return Time for the rest observer.
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
     * the contracted length in the rest frame.
     *
     * @param length The proper length (in F').
     * @param v The velocity of frame F' relative to the rest frame.
     *
     * @return The contracted length in the rest frame.
     */
    static public double lengthContraction(double length, double v) {
	return length / Relativity.gamma(v);
    }

    /**
     * Given a contracted length measured in the rest frame, calculate the proper
     * length in frame F' moving at velocity v.
     *
     * @param length The contracted length in the rest frame.
     * @param v The velocity of frame F' relative to the rest frame.
     *
     * @return The proper length in F'.
     */
    static public double invLengthContraction(double length, double v) {
	return length * Relativity.gamma(v);
    }

    /**
     * Given a proper duration measured in frame F' at velocity v, calculate the
     * dilated duration in the rest frame.
     *
     * @param duration The proper duration in F'.
     * @param v The velocity of frame F' relative to the rest frame.
     *
     * @return The dilated duration in the rest frame.
     */
    static public double timeDilation(double duration, double v) {
	    return duration * Relativity.gamma(v);
    }

    /**
     * Given a dilated duration measured in the rest frame, calculate the dilated
     * duration in frame F' moving at velocity v.
     *
     * @param duration The dilated duration in the rest frame.
     * @param v The velocity of frame F' relative to the rest frame.
     *
     * @return The proper duration in F'.
     */
    static public double invTimeDilation(double duration, double v) {
	    return duration / Relativity.gamma(v);
    }

    /**
     * Given a velocity v1 relative to the rest frame, calculate the corresponding
     * velocity relative to frame F' moving at velocity v.
     *
     * @param v A velocity relative to the rest frame.
     * @param frameV The velocity of frame F' also relative to the rest frame.
     *
     * @return The corresponding velocity in frame F'.
     */
    static public double vPrime(double v, double frameV)
    {
        return (v - frameV) / (1 - (v * frameV));
    }

    /**
     * Given a velocity v1 relative to frame F' moving at velocity v, calculate
     * the corresponding velocity relative to the rest frame.
     *
     * @param v A velocity relative to frame F'.
     * @param frameV The velocity of frame F' relative to the rest frame.
     *
     * @return The corresponding velocity in the rest frame.
     */
    static public double v(double v, double frameV)
    {
        return (v + frameV) / (1 + (v * frameV));
    }

    /**
     * Convert a velocity to an x-axis angle in degrees. The velocity must be
     * between -1 and 1, exclusive. The output will be between -45 and 45,
     * exclusive.
     *
     * @param v The velocity as a percentage of the speed of light.
     *
     * @return The x-axis angle in degrees.
     */
    static public double vToXAngle(double v)
    {
        return Math.toDegrees(Math.atan(v));
    }

    /**
     * Convert an x-axis angle in degrees to a velocity. The angle must be
     * between -45 and 45, exclusive. The output will be between -1 and 1,
     * exclusive.
     *
     * @param angle The x-axis angle in degrees.
     *
     * @return The velocity.
     */
    static public double angleXToV(double angle)
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
    static public double vToTAngle(double v)
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
    static public double angleTToV(double angle)
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
    static public double toPrimeAngle(double angle, double v)
    {
        // We accept any angle, but normalize the angle to within -180 to + 180

        double angle180 = Util.normalizeAngle180(angle);

        // If the angle is some version of 45/-45 degrees, return the same angle
        // we received

        if (Math.abs(angle180) == 45 || Math.abs(angle180) == 135) {
            return angle;
        }

        // Tangent is a periodic function, with its main interval being from
        // -90 to 90. When we normalize the angle to being within -90 to 90, we
        // lose half our range. We can recover it by inverting the final result

        double angle90 = Util.normalizeAngle90(angle);
        boolean invert = Math.abs(angle180) > 90;

        // Convert the angle to a velocity relative to whichever axis is
        // appropriate. Convert the velocity to the equivalent in the new
        // inertial frame. Convert the new velocity back to angle with respect
        // to the same axis we first used

        double v1 = Math.abs(angle90) < 45 ? angleXToV(angle90) : angleTToV(angle90);
        double v2 = vPrime(v1, v);
        double vAngle = Math.abs(angle90) < 45 ? vToXAngle(v2) : vToTAngle(v2);

        // If the angle was between converted using the t axis, then there is
        // a discontinuity if the velocity changes signs. The final angle might
        // come out as -89 when it should really be 91. If so, we need to invert
        // the result (possibly inverting an inversion)

        if (Math.abs(angle90) > 45 && Util.sign(v1) != Util.sign(v2)) invert = !invert;

        // Return the possibly inverted angle. If we don't invert, the value
        // will be within -90 to 90, so we don't need to normalize it

        if (invert) vAngle = Util.normalizeAngle180(vAngle + 180);
        return vAngle;
    }

    /**
     * Given the wavelength of a signal as observed by a source and the
     * wavelength of the same signal as observed by a receiver, calculate the
     * relative velocity between the source and receiver, as measured in the
     * co-moving frame of the receiver at the instant the signal is received.
     * <p>
     * The velocity is positive when the observers are moving away from each
     * other and negative when they are moving towards each other. In a Minkowski
     * spacetime diagram, this means that the sign of the velocity is correct if
     * the source is to the right of the receiver and should be inverted otherwise.
     * <p>
     * The units are not important as long as they are the same for both
     * wavelengths.
     *
     * @param sourceWavelength The wavelength of a signal as observed by a source.
     * @param receiverWavelength The wavelength of a signal as observed by a receiver.
     *
     * @return The velocity, as a percentage of light speed.
     */
    static public double dopplerWavelengthToV(double sourceWavelength, double receiverWavelength)
    {
        if (sourceWavelength <= 0) throw new ExecutionException("The source wavelength must be > 0");
        if (receiverWavelength <= 0) throw new ExecutionException("The received wavelength must be > 0");
        double s2 = sourceWavelength * sourceWavelength;
        double r2 = receiverWavelength * receiverWavelength;
        return (r2 - s2) / (s2 + r2);
    }

    /**
     * Given a frequency of a signal as observed by a source and the frequency of
     * the same signal as observed by a receiver, calculate the relative
     * velocity between the source and receiver, as measured in the co-moving frame
     * of the receiver at the instant the signal is received.
     * <p>
     * The velocity is positive when the observers are moving away from each
     * other and negative when they are moving towards each other. In a Minkowski
     * spacetime diagram, this means that the sign of the velocity is correct if
     * the source is to the right of the receiver and should be inverted otherwise.
     * <p>
     * The units are not important as long as they are the same for both
     * frequencies.
     *
     * @param sourceFrequency The frequency of a signal as observed by a source.
     * @param receiverFrequency The frequency of a signal as observed by a source.
     *
     * @return The velocity, as a percentage of light speed.
     */
    static public double dopplerFrequencyToV(double sourceFrequency, double receiverFrequency)
    {
        if (sourceFrequency <= 0) throw new ExecutionException("The source frequency must be > 0");
        if (receiverFrequency <= 0) throw new ExecutionException("The received frequency must be > 0");
        double s2 = sourceFrequency * sourceFrequency;
        double r2 = receiverFrequency * receiverFrequency;
        return (s2 - r2) / (s2 + r2);
    }

    /**
     * An electromagnetic signal is sent from a source to a receiver. Given the
     * relative velocity of the source to the receiver and given the wavelength
     * of the signal sent by the source, return the wavelength of the signal
     * received by the receiver.
     * <p>
     * The velocity should be positive when the observers are moving away from
     * each other and negative when they are moving towards each other. In a
     * Minkowski spacetime diagram, this means that the sign of the velocity
     * matches the actual velocity if the source is to the right of the receiver
     * and should be inverted otherwise.
     *
     * @param sourceWavelength The wavelength of the signal sent by the source.
     * @param v The relative velocity of the source to the receiver as measured
     * in the co-moving frame of the receiver at the instant the signal is
     * received.
     *
     * @return The wavelength of the signal received by the receiver,
     */
    static public double dopplerVToWavelength(double sourceWavelength, double v)
    {
        if (Math.abs(v) >= 1.0) throw new ExecutionException("The velocity must be between -1 and 1, exclusive");
        return sourceWavelength * Math.sqrt((1 + v) / (1 - v));
    }

    /**
     * An electromagnetic signal is sent from a source to a receiver. Given the
     * relative velocity of the source to the receiver and given the frequency
     * of the signal sent by the source, return the frequency of the signal
     * received by the receiver.
     * <p>
     * The velocity should be positive when the observers are moving away from
     * each other and negative when they are moving towards each other. In a
     * Minkowski spacetime diagram, this means that the sign of the velocity
     * matches the actual velocity if the source is to the right of the receiver
     * and should be inverted otherwise.
     *
     * @param sourceFrequency The frequency of the signal sent by the source.
     * @param v The relative velocity of the source to the receiver as measured
     * in the co-moving frame * of the receiver at the instant the signal is
     * received.
     *
     * @return The frequency of the signal received by the receiver.
     */
    static public double dopplerVToFrequency(double sourceFrequency, double v)
    {
        if (Math.abs(v) >= 1.0) throw new ExecutionException("The velocity must be between -1 and 1, exclusive");
        return sourceFrequency / Math.sqrt((1 + v) / (1 - v));
    }


}
