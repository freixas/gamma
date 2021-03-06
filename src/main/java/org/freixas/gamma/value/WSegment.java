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

import org.freixas.gamma.math.Relativity;

/**
 * This is a worldline segment definition structure. A WSegment stores the
 * information provided by a script writer to define a segment. A WorldlineSegment,
 * uses this information to create the actual segment.
 *
 * @author Antonio Freixas
 */
public class WSegment implements ExecutionImmutable
{
    private final double v;
    private final double a;
    private final WorldlineSegment.LimitType type;
    private final double delta;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a worldline segment based on information from the parser.This worldline is incompletely defined.
     * It is only fully defined when it is added to a worldline.
     *
     * @param v The initial velocity. If none was given, use NaN.
     * @param a The acceleration. If none was given, use 0.
     * @param type The limit type.
     * @param delta The limit delta. If no limit was given use NaN.
     */
    public WSegment(double v, double a, WorldlineSegment.LimitType type, double delta)
    {
        this.v = v;
        this.a = a;
        this.type = type;
        this.delta = delta;
    }

    // **********************************************************************
    // *
    // * Getter
    // *
    // **********************************************************************

    /**
     * Get the initial velocity of the segment. This could be NaN.
     *
     * @return The initial velocity of the segment.
     */
    public double getV()
    {
        return v;
    }

    /**
     * Get the segment's acceleration.
     *
     * @return The segment's acceleration.
     */
    public double getA()
    {
        return a;
    }

    /**
     * Get the limit type, which tells us how to calculate the end of the
     * segment.
     *
     * @return The limit type.
     */
    public WorldlineSegment.LimitType getType()
    {
        return type;
    }

    /**
     * Typically, the limit is a delta value: a certain amount of time, tau, or
     * distance, but it can be an absolute value, such as a specific velocity.
     *
     * @return The limit value.
     */
    public double getLimit()
    {
        return delta;
    }

    // **********************************************************************
    // *
    // * Drawing Frame Support
    // *
    // **********************************************************************

    /**
     * Create a new version of this WSegment that is relative to the given
     * frame rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new segment.
     */
    public WSegment relativeTo(Frame prime)
    {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        double v = this.v;

        if (!Double.isNaN(v)) {
            v = Relativity.vPrime(v, prime.getV());
        }

        @SuppressWarnings("LocalVariableHidesMemberVariable")
        double delta = this.delta;

        if (type != WorldlineSegment.LimitType.NONE && !Double.isNaN(delta)) {
            if (type == WorldlineSegment.LimitType.D) {
                delta = Relativity.lengthContraction(delta, prime.getV());
            }
            else if (type == WorldlineSegment.LimitType.T) {
                delta = Relativity.timeDilation(delta, prime.getV());
            }
        }

        return new WSegment(v, a, type, delta);
    }
}
