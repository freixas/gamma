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

import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;

/**
 * Create an interval.
 *
 * @author Antonio Freixas
 */
public class Interval implements ExecutionImmutable, Displayable
{
    public final double minX;
    public final double maxX;
    public final double minT;
    public final double maxT;

    public final boolean hasXRange;
    public final boolean hasTRange;

    public Interval(double x1, double x2, double t1, double t2)
    {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minT = Math.min(t1, t2);
        maxT = Math.max(t1, t2);

        boolean isInfiniteX = Double.isInfinite(minX) && Double.isInfinite(maxX);
        boolean isInfiniteT = Double.isInfinite(minT) && Double.isInfinite(maxT);

        if (isInfiniteX && minX == maxX) {
            throw new ExecutionException("Invalid x range in interval");
        }
        if (isInfiniteT && minT == maxT) {
            throw new ExecutionException("Invalid t range in interval");
        }

        hasXRange = !isInfiniteX;
        hasTRange = !isInfiniteT;

        if (!hasXRange && !hasTRange) {
            throw new ExecutionException("Invalid interval: all ranges are infinite");
        }
    }

    /**
     * Copy constructor.
     *
     * @param other The other interval to copy.
     */
    public Interval(Interval other)
    {
        // There's no need to sort as the other bounds will already
        // be sorted

        this.minX = other.minX;
        this.maxX = other.maxX;
        this.minT = other.minT;
        this.maxT = other.maxT;

        this.hasXRange = other.hasXRange;
        this.hasTRange = other.hasTRange;
    }

    /**
     * Get the X interval delta.
     *
     * @return The interval delta.
     */
    public double getXDelta()
    {
        return maxX - minX;
    }

     /**
     * Get the T interval delta.
     *
     * @return The interval delta.
     */
    public double getTDelta()
    {
        return maxT - minT;
    }

   @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Interval " +
               "X min " + engine.toDisplayableString(minX) + " max " + engine.toDisplayableString(maxX) + " " +
               "T min " + engine.toDisplayableString(minT) + " max " + engine.toDisplayableString(maxT) + " " +
               " ]";
    }

 }
