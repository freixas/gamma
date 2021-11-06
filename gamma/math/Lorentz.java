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
public final class Lorentz
{
    private static double v = Double.NaN;
    private static double g;
    
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
    
    private Lorentz() {}
    
    static public double xPrime(double x, double t, double v) {
	return (x - v * t) * Lorentz.gamma(v);

    }
    static public double tPrime(double x, double t, double v) {
	return (t - v * x) * Lorentz.gamma(v);
    }

    static public double x(double xPrime, double tPrime, double v) {
	return (xPrime + v * tPrime) * Lorentz.gamma(v);
    }

    static public double t(double xPrime, double tPrime, double v) {
	return (tPrime + v * xPrime) * Lorentz.gamma(v);
    }
    
    static public Coordinate toPrimeFrame(Coordinate rest, double v)
    {
        return new Coordinate(xPrime(rest.x, rest.t, v), tPrime(rest.x, rest.t, v));
    }

    static public Coordinate toRestFrame(Coordinate prime, double v)
    {
        return new Coordinate(x(prime.x, prime.t, v), t(prime.x, prime.t, v));
    }

    static public double gamma(double v) {
	if (Double.isNaN(Lorentz.v) || Lorentz.v != v) {
	    Lorentz.v = v;
	    Lorentz.g = 1 / Math.sqrt(1 - v*v);
	}
	return Lorentz.g;
    }

    static public double tauToT(double tau, double v) {
	return tau * Lorentz.gamma(v);
    }

    static public double tToTau(double t, double v) {
	return t / Lorentz.gamma(v);
    }

    static public double lengthContraction(double x, double v) {
	return x / Lorentz.gamma(v);
    }

    static public double invLengthContraction(double x, double v) {
	return x * Lorentz.gamma(v);
    }

}
