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
import org.freixas.gamma.math.Relativity;

/**
 * This is a worldline initializer definition structure. It is used to
 * initialize a worldline.
 *
 * @author Antonio Freixas
 */
public class WInitializer implements ExecutionImmutable
{
    private final Coordinate origin;
    private final double tau;
    private final double d;

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
    
    public WInitializer(Coordinate origin, double tau, double d)
    {
        if (origin == null) {
            throw new ExecutionException("The worldline's origin is null");
        }
        this.origin = origin;
        this.tau = tau;
        this.d = d;
    }

    public Coordinate getOrigin()
    {
        return new Coordinate(origin);
    }

    public double getTau()
    {
        return tau;
    }

    public double getD()
    {
        return d;
    }

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
