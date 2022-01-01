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
import gamma.math.Util;

/**
 *
 * @author Antonio Freixas
 */
public class BooleanDisplayVariable extends DisplayVariable
{
    private final DisplayVariable.Type type;
    private final boolean initialValue;
    private final String label;
    private boolean currentValue;

    private final DiagramEngine diagramEngine;

    public BooleanDisplayVariable(DiagramEngine diagramEngine, double initialValue, String label)
    {
        this.type = DisplayVariable.Type.BOOLEAN;
        this.initialValue = !Util.fuzzyZero(initialValue);
        this.label = label;
        this.currentValue = this.initialValue;

        this.diagramEngine = diagramEngine;
    }

    @Override
    public DisplayVariable.Type getType()
    {
        return type;
    }

    public boolean getInitialValue()
    {
        return initialValue;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void setCurrentValue(double value)
    {
        setBooleanCurrentValue(!Util.fuzzyZero(value));
    }

    @Override
    public double getCurrentValue()
    {
        return currentValue ? 1.0 : 0.0;
    }

    public boolean getBooleanCurrentValue()
    {
        return currentValue;
    }

    public void setBooleanCurrentValue(boolean value)
    {
        if (value != currentValue) {
            currentValue = value;
            diagramEngine.updateForDisplayVariable();
        }
    }

}