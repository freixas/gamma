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
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.value.Address;
import org.freixas.gamma.value.ObjectPropertyAddress;
import org.freixas.gamma.value.SymbolTableAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Assign a value to a variable or a property.
 * <p>
 * Arg 1 is the address where the variable should be stored.
 * Arg 2 is the value.
 *
 * @author Antonio Freixas
 */
public class AssignHCode extends ArgInfoHCode
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ADDRESS);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(2, argTypes, 0);
    }

    @Override
    @SuppressWarnings("null")
    public void execute(HCodeEngine engine, List<Object> data)
    {

        Address address = (Address)data.get(0);
        Object value =    data.get(1);
        data.clear();

        // If it's a property value, it must exist and the type of the property
        // and the value must be the same.

        if (address instanceof ObjectPropertyAddress) {
            if (!address.exists() || !address.typeMatches(value)) {
                throw new ExecutionException("Variable does not exist or is of the wrong type");
            }
        }

        // Otherwise, this is a symbol table address. We need to make sure we
        // are not assigning to a dynamic variable

        else if (address.exists()) {
            String name = ((SymbolTableAddress)address).getName();
            if (engine.getDynamicSymbolTable().contains(name)) {
                throw new ExecutionException("You cannot change the value of dynamic variable '" + name + "'");
            }
        }

        // Set the symbol pointed to by the address to the given value

        address.setValue(value);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
