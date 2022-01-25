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

/**
 *
 * @author Antonio Freixas
 */
public class AnimationVariable extends DynamicVariable implements ExecutionImmutable
{
    private final double initialValue;
    private final double finalValue;
    private final double stepSize;
    private double currentValue;

    public AnimationVariable(double initialValue, double finalValue,
                             double stepSize)
    {
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.stepSize = stepSize;
        this.currentValue = initialValue;
    }

    public double getInitialValue()
    {
        return initialValue;
    }

    public double getFinalValue()
    {
        return finalValue;
    }

    public double getStepSize()
    {
        return stepSize;
    }

    public void setCurrentValue(int frame)
    {
        currentValue = initialValue + ((frame - 1) * stepSize);
    }

    @Override
    public double getCurrentValue()
    {
        return this.currentValue;
    }

}
