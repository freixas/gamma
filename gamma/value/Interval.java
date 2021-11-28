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

/**
 * Create an interval.
 *
 * @author Antonio Freixas
 */
public class Interval
{
    public double minT;
    public double maxT;

    public Interval(double t1, double t2)
    {
        minT = Math.min(t1, t2);
        maxT = Math.max(t1, t2);
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

        this.minT = other.minT;
        this.maxT = other.maxT;
    }

    /**
     * Get the interval delta.
     *
     * @return The interval delta.
     */
    public double getDelta()
    {
        return maxT - minT;
    }

 }
