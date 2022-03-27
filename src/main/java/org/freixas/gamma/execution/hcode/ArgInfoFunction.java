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

import org.freixas.gamma.execution.ArgInfo;
import org.freixas.gamma.execution.HCodeEngine;
import java.util.List;

/**
 * An ArgInfoFunction is an Function which uses an ArgInfo structure to provide
 * information about its parameters. This class must be sub-classed, with one
 * class per Function.
 * <p>
 * For Functions with simple requirements, use a GenericFunction.
 *
 * @author Antonio Freixas
 */
public abstract class ArgInfoFunction extends Function
{
    /**
     * Execute the function.
     *
     * @param engine The h-code engine.
     * @param code The function arguments.
     * @return The function's output.
     */
    abstract public Object execute(HCodeEngine engine, List<Object> code);

    @Override
    public int getNumberOfArgs()
    {
        return getArgInfo().getNumberOfArgs();
    }

    @Override
    public int getNumberOfReturnedValues()
    {
        return getArgInfo().getNumberOfReturnedValues();
    }

    /**
     * Return the number of arguments required. -1 means the size is on the
     * stack.
     *
     * @return The number of arguments required.
     */
    abstract public ArgInfo getArgInfo();

}
