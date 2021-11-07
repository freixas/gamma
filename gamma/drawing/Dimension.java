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
package gamma.drawing;

/**
 * A Dimension is a box width a width and a height.
 *
 * @author Antonio Freixas
 */
public class Dimension
{
    public double width;
    public double height;

    public Dimension(double width, double height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Copy constructor.
     *
     * @param other The dimension to copy.
     */
    public Dimension(Dimension other)
    {
        this.width = other.width;
        this.height = other.height;
    }

    /**
     * Set this coordinate equal to another coordinate.
     *
     * @param width The width coordinate.
     * @param height The height coordinate
     */
    public void setTo(double width, double height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Set this coordinate equal to another coordinate.
     *
     * @param other The dimension to copy.
     */
    public void setTo(Dimension other)
    {
        this.width = other.width;
        this.height = other.height;
    }
}
