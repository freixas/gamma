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

import gamma.execution.DiagramEngine;
import gamma.execution.ExecutionException;

/**
 *
 * @author Antonio Freixas
 */
public class RangeVariable extends DisplayVariable
{
    private final DisplayVariable.Type type;
    private final double initialValue;
    private final double minValue;
    private final double maxValue;
    private final String label;
    private double currentValue;

    private final DiagramEngine diagramEngine;

    public RangeVariable(DiagramEngine diagramEngine, double initialValue, double minValue, double maxValue, String label)
    {
        if (initialValue < minValue || initialValue > maxValue) {
            throw new ExecutionException("The initial value for a display variable must lie within its range.");
        }
        this.type = DisplayVariable.Type.RANGE;
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

    @Override
    public DisplayVariable.Type getType()
    {
        return type;
    }

    public double getInitialValue()
    {
        return initialValue;
    }

    public double getMinValue()
    {
        return minValue;
    }

    public double getMaxValue()
    {
        return maxValue;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public double getCurrentValue()
    {
        return this.currentValue;
    }

    @Override
    public void setCurrentValue(double value)
    {
        if (value != this.currentValue) {
            this.currentValue = value;
            diagramEngine.updateForDisplayVariable(false);
        }
    }

}
