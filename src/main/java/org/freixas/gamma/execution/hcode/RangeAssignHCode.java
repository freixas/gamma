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
import org.freixas.gamma.value.RangeVariable;
import org.freixas.gamma.value.SymbolTableAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class RangeAssignHCode extends ArgInfoHCode
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ADDRESS);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(5, argTypes, 0);
    }


    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        if (data.get(1) == null) throw new ExecutionException("The initial value is null");
        if (data.get(2) == null) throw new ExecutionException("The minimum value is null");
        if (data.get(3) == null) throw new ExecutionException("The maximum value is null");
        if (data.get(4) == null) throw new ExecutionException("The label is null");

        Address address =     (Address)data.get(0);
        double initialValue = (Double) data.get(1);
        double minValue =     (Double) data.get(2);
        double maxValue =     (Double) data.get(3);
        Object object =                data.get(4);
        data.clear();

        // Make sure this is a symbol table address

        if (!(address instanceof SymbolTableAddress)) {
            throw new ExecutionException("A display variable cannot be assigned to a field");
        }

        // Check to see if this animation variable has already been defined.

        if (address.exists()) {
            throw new ExecutionException("This display variable has already been defined");
        }

        // Convert the label object to a string

        String label = engine.toDisplayableString(object);

        // Create an entry in the regular symbol table just so we know if it
        // is redefined within the same execution

        String name = ((SymbolTableAddress)address).getName();
        engine.getSymbolTable().directPut(name, initialValue);

        // Set the value in the dynamic symbol table

        RangeVariable var =
            new RangeVariable(
                engine.getMainWindow().getDiagramEngine(),
                initialValue, minValue, maxValue, label);
        engine.getDynamicSymbolTable().put(name, var);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
