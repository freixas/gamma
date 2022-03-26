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
package org.freixas.gamma.value;

import org.freixas.gamma.math.OffsetAcceleration;

/**
 * A structure holding the x, v, t, tau, and d values for a worldline segment
 * endpoint. In some cases, the values may be +infinite or -infinity.
 *
 * @author Antonio Freixas
 */
public class WorldlineEndpoint implements ExecutionImmutable
{
    public final double v;
    public final double x;
    public final double t;
    public final double tau;
    public final double d;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Create a worldline endpoint.
     *
     * @param v The velocity at this point
     * @param x The x coordinate relative to the rest frame.
     * @param t The t coordinate relative to the rest frame.
     * @param tau The tau value at this point.
     * @param d The distance value at this point.
     */
    WorldlineEndpoint(double v, double x, double t, double tau, double d)
    {
        this.v = v;
        this.x = x;
        this.t = t;
        this.tau = tau;
        this.d = d;
    }

    /**
     * Copy constructor.
     *
     * @param other The worldline endpoint to copy.
     */
    WorldlineEndpoint(WorldlineEndpoint other)
    {
        this.v = other.v;
        this.x = other.x;
        this.t = other.t;
        this.tau = other.tau;
        this.d = other.d;
    }

    /**
     * Initialize all the endpoint values from the time value and an offset
     * acceleration curve.
     *
     * @param t The time.
     * @param curve The offset acceleration curve.
     */
    WorldlineEndpoint(double t, OffsetAcceleration curve)
    {
        this.v = curve.tToV(t);
        this.x = curve.tToX(t);
        this.t = t;
        this.tau = curve.tToTau(t);
        this.d = curve.tToD(t);
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

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
