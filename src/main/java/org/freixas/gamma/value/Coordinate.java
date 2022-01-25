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
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;
import javafx.geometry.Point2D;

/**
 *
 * @author Antonio Freixas
 */
public class Coordinate extends ObjectContainer implements ExecutionMutable, Displayable
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

    @Override
    public Object createCopy()
    {
        return new Coordinate(this);
    }

    /**
     * Add a coordinate to this one.
     *
     * @param other The other coordinate to add.
     *
     * @return This coordinate.
     */
    public Coordinate add(Coordinate other)
    {
        this.x += other.x;
        this.t += other.t;
        return this;
    }

    /**
     * Subtract a coordinate from this one.
     *
     * @param other The other coordinate to subtract.
     *
     * @return This coordinate.
     */
    public Coordinate subtract(Coordinate other)
    {
        this.x -= other.x;
        this.t -= other.t;
        return this;
    }

    @Override
    public Object getProperty(String name)
    {
        // The HCode that handles object properties will complain if an invalid
        // property name is used, so we don't need to re-check here

        switch (name) {
            case "x" -> { return x; }
            case "t" -> { return t; }
        }
        return null;
    }

    @Override
    public void setProperty(String name, Object value)
    {
        if (!(value instanceof Double)) {
            throw new ExecutionException("Coordinate properties 'x' and 't' must be floating point numbers");
        }

        // The HCode that handles object properties will complain if an invalid
        // property name is used, so we don't need to re-check here

        switch (name) {
            case "x" -> { x = (Double)value; }
            case "t" -> { t = (Double)value; }
        }
    }

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "(" + engine.toDisplayableString(x) + ", " + engine.toDisplayableString(t) + ")";
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

    public boolean fuzzyEQ(Coordinate other)
    {
        return Util.fuzzyEQ(this.x, other.x) && Util.fuzzyEQ(this.t, other.t);
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
        return Double.doubleToLongBits(this.t) == Double.doubleToLongBits(other.t);
    }

}
