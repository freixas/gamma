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

import gamma.execution.HCodeEngine;
import gamma.math.Relativity;

/**
 * Create an interval.
 *
 * @author Antonio Freixas
 */
public class Interval implements ExecutionImmutable, Displayable
{
    public enum Type {
        T, TAU, D
    }

    private final Type type;
    private final double min;
    private final double max;

    public Interval(Type type, double min, double max)
    {
        this.type = type;
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    /**
     * Copy constructor.
     *
     * @param other The other interval to copy.
     */
    public Interval(Interval other)
    {
        this.type = other.type;
        this.min = other.min;
        this.max = other.max;
    }

    /**
     * Get the interval type.
     *
     * @return The interval delta.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Get the interval's minimum value.
     *
     * @return The interval's minimum value.
     */
    public double getMin()
    {
        return min;
    }

    /**
     * Get the interval's maximum value.
     *
     * @return The interval's maximum value.
     */
    public double getMax()
    {
        return max;
    }

    /**
     * Get the interval delta.
     *
     * @return The interval delta.
     */
    public double getDelta()
    {
        return max - min;
    }

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    /**
     * Create a new version of this interval that is relative to the given
     * frame rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new interval.
     */
    public IntervalObserver relativeTo(Frame prime)
    {
        // TAU and D aren't affected by the frame

        if (type == Type.T) {
            ???l
        }

    }

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

   @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Interval " +
               "min " + engine.toDisplayableString(min) + " max " + engine.toDisplayableString(max) +
               " ]";
    }

 }
