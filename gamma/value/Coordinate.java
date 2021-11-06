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
     * Copy constructor. This makes a deep copy of the Coordinate object.
     *
     * @param other The coordinate to copy.
     */
    public Coordinate(Coordinate c)
    {
        this(c.x, c.t);
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
}
