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

import gamma.math.Lorentz;
import gamma.math.OffsetAcceleration;

/**
 * A structure holding the x, v, t, tau, and d values for a worldline segment
 * endpoint. In some cases, the values may be +infinite or -infinity.
 *
 * @author Antonio Freixas
 */
public class HyperbolaEndpoint
{
    public double v;
    public double x;
    public double t;
    public double tau;
    public double d;

    HyperbolaEndpoint(double v, double x, double t, double tau, double d)
    {
        this.v = v;
        this.x = x;
        this.t = t;
        this.tau = tau;
        this.d = d;
    }

    HyperbolaEndpoint(HyperbolaEndpoint other)
    {
        this.v = other.v;
        this.x = other.x;
        this.t = other.t;
        this.tau = other.tau;
        this.d = other.d;
    }

    /**
     * Initialize all the endpoint values from the time value and the offset
     * acceleration curve.
     *
     * @param t The time.
     * @param curve The offset acceleration curve.
     */
    HyperbolaEndpoint(double t, OffsetAcceleration curve)
    {
        this.v = curve.tToV(t);
        this.x = curve.tToX(t);
        this.t = t;
        this.tau = curve.tToTau(t);
        this.d = curve.tToD(t);
    }

    @Override
    public String toString()
    {
        return "Velocity: " + v + "\n" +
               "X       : " + x + "\n" +
               "T       : " + t + "\n" +
               "Tau     : " + tau + "\n" +
               "Distance: " + d;
    }



 }
