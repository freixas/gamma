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
 * This class is used to store information about an animation variable.
 *
 * @author Antonio Freixas
 */
public class AnimationVariable extends DynamicVariable implements ExecutionImmutable
{
    private final double initialValue;
    private final double finalValue;
    private final double stepSize;
    private double currentValue;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create an animation variable.
     *
     * @param initialValue The variable's initial value.
     * @param finalValue The variable's final value. This can be NaN if there
     * is no limit.
     * @param stepSize The step size by which the animation variable should
     * change on each frame.
     */
    public AnimationVariable(double initialValue, double finalValue,
                             double stepSize)
    {
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.stepSize = stepSize;
        this.currentValue = initialValue;
    }

    // **********************************************************************
    // *
    // * Getters/Setters
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
     * Get the final value. This could be NaN.
     *
     * @return The final value.
     */
    public double getFinalValue()
    {
        return finalValue;
    }

    /**
     * Get the step size value.
     *
     * @return The step size value.
     */
    public double getStepSize()
    {
        return stepSize;
    }

    @Override
    public double getCurrentValue()
    {
        return this.currentValue;
    }

    /**
     * Set the current value based on the frame number of the animation.
     *
     * @param frame The frame number.
     */
    public void setCurrentValue(int frame)
    {
        currentValue = initialValue + ((frame - 1) * stepSize);
    }

}
