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
package org.freixas.gamma.execution.hcode;

import org.freixas.gamma.math.Util;

/**
 *
 * @author Antonio Freixas
 */
public class SetStatement
{
    public enum PrecisionType {
        DISPLAY, PRINT
    }

    public static final double DEFAULT_UNITS = 1.0;
    public static final double DEFAULT_DISPLAY_PRECISION = 12.0;
    public static final double DEFAULT_PRINT_PRECISION = 12.0;

    private double units = DEFAULT_UNITS;
    private int displayPrecision = Util.toInt(DEFAULT_DISPLAY_PRECISION);
    private int printPrecision = Util.toInt(DEFAULT_PRINT_PRECISION);

    public SetStatement()
    {
        this(DEFAULT_UNITS, DEFAULT_DISPLAY_PRECISION, DEFAULT_PRINT_PRECISION);
    }

    public SetStatement(double units, double displayPrecision, double printPrecision)
    {
        this.units = DEFAULT_UNITS;
    }

    public void set(double units, double displayPrecision, double printPrecision)
    {
        this.units = units;
        this.displayPrecision = Util.toInt(displayPrecision);
        this.printPrecision = Util.toInt(printPrecision);
    }

    public double getUnits()
    {
        return units;
    }

    public void setUnits(double units)
    {
        this.units = units;
    }

    public int getDisplayPrecision()
    {
        return displayPrecision;
    }

    public void setDisplayPrecision(double displayPrecision)
    {
        this.displayPrecision = Util.toInt(displayPrecision);
    }

    public int getPrintPrecision()
    {
        return printPrecision;
    }

    public void setPrintPrecision(double printPrecision)
    {
        this.printPrecision = Util.toInt(printPrecision);
    }

}

