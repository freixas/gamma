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
 * Display variables are used to allow the user to control the diagram
 * script variables through a GUI.
 *
 * @author Antonio Freixas
 */
abstract public class DisplayVariable extends DynamicVariable
{
    /**
     * This allows the script writer to specify the order in which dynamic
     * variables are presented to the end user
     */
    private int displayOrder = -1;

    /**
     * Set the current value of this dynamic variable.
     *
     * @param value The value to set.
     */
    abstract public void setCurrentValue(double value);

    /**
     * Get the display variable's label.
     *
     * @return The display variable's label.
     */
    abstract public String getLabel();

    /**
     * Get the order in which this display variable should be displayed,
     * starting with 0 for the first variable to display.
     *
     * @return The display order.
     */
     public int getDisplayOrder()
     {
         return displayOrder;
     }

    /**
     * Set the order in which this display variable should be displayed,
     * starting with 0 for the first variable to display.
     *
     * @param displayOrder The display order.
     */
    public void setDisplayOrder(int displayOrder)
    {
        this.displayOrder = displayOrder;
    }

}
