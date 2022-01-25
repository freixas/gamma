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
 *
 * @author Antonio Freixas
 */
public class ChoiceVariable extends DisplayVariable
{
    private final DisplayVariable.Type type;
    private final int initialValue;
    private final String[] choices;
    private final String label;
    private final boolean restart;
    private int currentValue;

    private final DiagramEngine diagramEngine;

    public ChoiceVariable(DiagramEngine diagramEngine, double initialValue, String[] choices, String label, boolean restart)
    {
        this.type = DisplayVariable.Type.BOOLEAN;
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

    @Override
    public DisplayVariable.Type getType()
    {
        return type;
    }

    public int getInitialValue()
    {
        return initialValue;
    }

    public String[] getChoices()
    {
        return choices;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public boolean isRestart()
    {
        return restart;
    }

    @Override
    public void setCurrentValue(double value)
    {
        setIntCurrentValue(Util.toInt(value));
    }

    @Override
    public double getCurrentValue()
    {
        return currentValue;
    }

    public int getIntCurrentValue()
    {
        return currentValue;
    }

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

}
