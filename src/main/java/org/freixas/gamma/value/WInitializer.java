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

import org.freixas.gamma.execution.ExecutionException;

/**
 * This is a worldline initializer definition structure. It is used to
 * initialize a worldline.
 * <p>
 * The initializer contains three elements.
 * <ul>
 *     <li>A coordinate defining the origin of the worldline. All user-defined
 *     segments start at the origin and continue forward in time.
 *     <li>The tau value at the origin. It need not be zero. The origin is the
 *     origin of the segments and not necessarily the 0 tau point.
 *     <li>The distance value at the origin. Again, it can be anything.
 * </ul>
 *
 * @author Antonio Freixas
 */
public class WInitializer implements ExecutionImmutable
{
    private final Coordinate origin;
    private final double tau;
    private final double d;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Create a worldline initializer.
     *
     * @param origin The origin of the worldline.
     * @param tau The tau value at the origin.
     * @param d The distance value at the origin.
     */
    public WInitializer(Coordinate origin, Double tau, Double d)
    {
        if (origin == null) {
            throw new ExecutionException("The worldline's origin is null");
        }
        if (tau == null) {
            throw new ExecutionException("The worldline's initial tau is null");
        }
        if (d == null) {
            throw new ExecutionException("The worldline's initial distance is null");
        }
        this.origin = origin;
        this.tau = tau;
        this.d = d;
    }

    /**
     *  Create a worldline initializer.
     *
     * @param origin The origin of the worldline.
     * @param tau The tau value at the origin.
     * @param d The distance value at the origin.
     */
    public WInitializer(Coordinate origin, double tau, double d)
    {
        if (origin == null) {
            throw new ExecutionException("The worldline's origin is null");
        }
        this.origin = origin;
        this.tau = tau;
        this.d = d;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the origin.
     *
     * @return The origin.
     */
    public Coordinate getOrigin()
    {
        return new Coordinate(origin);
    }

    /**
     * Get tau at the origin.
     *
     * @return Tau at the origin.
     */
    public double getTau()
    {
        return tau;
    }

    /**
     * Get the distance at the origin.
     *
     * @return The distance at the origin.
     */
    public double getD()
    {
        return d;
    }

    // **********************************************************************
    // *
    // * Drawing Frame Support
    // *
    // **********************************************************************

    /**
     * Create a new version of this WInitializer that is relative to the
     * given frame rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new initializer.
     */
    public WInitializer relativeTo(Frame prime)
    {
        return new WInitializer(
            prime.toFrame(origin),
            tau,
            d);
    }

}
