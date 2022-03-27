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
package org.freixas.gamma.execution.hcode;

/**
 *
 * @author Antonio Freixas
 */
public abstract class ExecutorContext
{
    /**
     * Get the number of arguments for this h-code. If the argument count
     * is on the data stack, return -1.
     *
     * @return The number of arguments for this h-code.
     */
    abstract public int getNumberOfArgs();

    /**
     * Get the number of values returned by this h-code. The number will be
     * either 0 or 1.
     *
     * @return The number of values returned by this h-code.
     */
    abstract public int getNumberOfReturnedValues();

}
