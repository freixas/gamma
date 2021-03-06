/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.value;

/**
 * Dynamic variables are variables whose values are set by Gamma in response to
 * a change in an animation frame or a change in a GUI control.
 * <p>
 * Dynanmic variables are defined by the script writer and are created the first
 * time the variable is encountered. Their values can survive the re-execution
 * of a script.
 *
 * @author Antonio Freixas
 */
abstract public class DynamicVariable implements ExecutionImmutable
{
    /**
     * Get the current value of this dynamic variable.
     *
     * @return The current value of this dynamic variable.
     */
    abstract public double getCurrentValue();

}
