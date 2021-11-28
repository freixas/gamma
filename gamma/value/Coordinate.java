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
import javafx.geometry.Point2D;

/**
 *
 * @author Antonio Freixas
 */
public class Coordinate extends ObjectContainer
{
    static private String[] propertyNames = { "x", "t" };

    public double x;
    public double t;

    public Coordinate(double x, double t)
    {
        super(propertyNames);
        this.x = x;
        this.t = t;
    }

    /**
     * Copy constructor.
     *
     * @param other The coordinate to copy.
     */
    public Coordinate(Coordinate other)
    {
        this(other.x, other.t);
    }

    public Coordinate(Point2D point)
    {
        this(point.getX(), point.getY());
    }

    /**
     * Set this coordinate equal to another coordinate.
     *
     * @param x The x coordinate.
     * @param t The t coordinate
     */
    public void setTo(double x, double t)
    {
        this.x = x;
        this.t = t;
    }

    /**
     * Set this coordinate equal to another coordinate.
     *
     * @param other The coordinate to copy.
     */
    public void setTo(Coordinate other)
    {
        this.x = other.x;
        this.t = other.t;
    }

    /**
     * Add a coordinate to this one.
     *
     * @param other The other coordinate to add.
     */
    public void add(Coordinate other)
    {
        this.x += other.x;
        this.t += other.t;
    }

    /**
     * Subtract a coordinate from this one.
     *
     * @param other The other coordinate to subtract.
     */
    public void subtract(Coordinate other)
    {
        this.x -= other.x;
        this.t -= other.t;
    }

    @Override
    public Object getProperty(String name)
    {
        switch (name) {
            case "x" -> { return this.x; }
            case "t" -> { return this.t; }
            default -> { return null; }
        }
    }

    @Override
    public void setProperty(String name, Object value)
    {
        switch (name) {

            case "x" -> { this.x = (Double)value; }
            case "t" -> { this.t = (Double)value; }
            default -> { /* Do nothing */ }
        }
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + t + ')';
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 13 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 13 * hash + (int)(Double.doubleToLongBits(this.t) ^ (Double.doubleToLongBits(this.t) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinate other = (Coordinate)obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        return Double.doubleToLongBits(this.t) == Double.doubleToLongBits(other.t);
    }

}
