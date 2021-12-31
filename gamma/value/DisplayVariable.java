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

/**
 * Display variables are used to allow the user to control the diagram
 * script variables through a GUI.
 *
 * @author Antonio Freixas
 */
abstract public class DisplayVariable extends DynamicVariable
{
    public enum Type
    {
        RANGE, BOOLEAN
    }

    /**
     * Get the type of this display variable.
     *
     * @return The type of this display variable.
     */
    abstract public Type getType();

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

}
