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

import org.freixas.gamma.execution.DiagramEngine;
import org.freixas.gamma.execution.ExecutionException;

/**
 * Range variables allow an end user to set the value of a variable using
 * a slider.
 *
 * @author Antonio Freixas
 */
public class RangeVariable extends DisplayVariable
{
    private final double initialValue;
    private final double minValue;
    private final double maxValue;
    private final String label;
    private double currentValue;

    private final DiagramEngine diagramEngine;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a range variable. The minimum value of the range should always be
     * less than the maximum value. "minValue" and "maxValue" are sorted to
     * ensure this.
     *
     * @param diagramEngine The associated diagram engine.
     * @param initialValue The initial value of the variable.
     * @param minValue The minimum value of the range.
     * @param maxValue The maximum value of the range.
     * @param label The label for the range variable.
     */
    public RangeVariable(DiagramEngine diagramEngine, double initialValue, double minValue, double maxValue, String label)
    {
        if (initialValue < minValue || initialValue > maxValue) {
            throw new ExecutionException("The initial value for a display variable must lie within its range.");
        }
        this.initialValue = initialValue;

        // Sort the min/max values

        if (minValue > maxValue) {
            double temp = minValue;
            minValue = maxValue;
            maxValue = temp;
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.label = label;
        this.currentValue = initialValue;

        this.diagramEngine = diagramEngine;
    }

    // **********************************************************************
    // *
    // * Getter/Setter
    // *
    // **********************************************************************

    /**
     * Get the initial value.
     *
     * @return The initial value.
     */
    public double getInitialValue()
    {
        return initialValue;
    }

    /**
     * Get the minimum value of the range.
     *
     * @return The minimum value of the range.
     */
    public double getMinValue()
    {
        return minValue;
    }

    /**
     * Get the maximum value of the range.
     *
     * @return The maximum value of the range.
     */
    public double getMaxValue()
    {
        return maxValue;
    }

    // **********************************************************************
    // *
    // * DisplayVariable Support
    // *
    // **********************************************************************

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void setCurrentValue(double value)
    {
        if (value != this.currentValue) {
            this.currentValue = value;
            diagramEngine.updateForDisplayVariable(false);
        }
    }

    // **********************************************************************
    // *
    // * DynamicVariable Support
    // *
    // **********************************************************************

    @Override
    public double getCurrentValue()
    {
        return this.currentValue;
    }

}
