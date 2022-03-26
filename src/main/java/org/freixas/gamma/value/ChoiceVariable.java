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

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.execution.DiagramEngine;
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.math.Util;

/**
 * Choice variables allow an end user to set the value of a variable using
 * a choice box.
 *
 * @author Antonio Freixas
 */
public class ChoiceVariable extends DisplayVariable
{
    private final int initialValue;
    private final String[] choices;
    private final String label;
    private final boolean restart;
    private int currentValue;

    private final DiagramEngine diagramEngine;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a new choice variable.
     *
     * @param diagramEngine The associated diagram engine.
     * @param initialValue The variable's initial value.
     * @param choices The choices presented to the end user.
     * @param label The label for the choice variable.
     * @param restart If true, restart an animation and recalculate any animation
     * variables.
     */
    public ChoiceVariable(DiagramEngine diagramEngine, double initialValue, String[] choices, String label, boolean restart)
    {
        this.initialValue = Util.toInt(initialValue);
        this.choices = choices;
        this.label = label;
        this.restart = restart;
        this.currentValue = this.initialValue;

        if (this.currentValue < 1 || this.currentValue > choices.length) {
            throw new ExecutionException("The choice's value does not match a choice");
        }

        this.diagramEngine = diagramEngine;
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
    @SuppressWarnings("unused")
    public int getInitialValue()
    {
        return initialValue;
    }

    /**
     * Get the list of choices.
     *
     * @return The list of choices.
     */
    public String[] getChoices()
    {
        return choices;
    }

    /**
     * Return true if a choice change should restart the animation.
     *
     * @return True if a choice change should restart the animation.
     */
    public boolean isRestart()
    {
        return restart;
    }

    /**
     * Get the current value as an integer.
     *
     * @return The current value as an integer.
     */
    public int getIntCurrentValue()
    {
        return currentValue;
    }

    /**
     * Set the current value using an integer. This will cause the diagram to be
     * redrawn.
     *
     * @param value the value to set.
     */
    public void setIntCurrentValue(int value)
    {
        if (value != currentValue) {
            if (value < 1 || value > choices.length) {
                throw new ProgrammingException("ChoiceDisplayVariable.setIntCurrentValue(): The choice's value does not match a choice");
            }
            currentValue = value;
            diagramEngine.updateForDisplayVariable(restart);
        }
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
        setIntCurrentValue(Util.toInt(value));
    }

    // **********************************************************************
    // *
    // * DynamicVariable Support
    // *
    // **********************************************************************

    @Override
    public double getCurrentValue()
    {
        return currentValue;
    }

}
