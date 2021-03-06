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
import org.freixas.gamma.value.AnimationVariable;
import org.freixas.gamma.value.SymbolTableAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class AnimAssignHCode extends ArgInfoHCode
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ADDRESS);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(4, argTypes, 0);
    }


    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        if (data.get(1) == null) throw new ExecutionException("The initial value is null");
        if (data.get(2) == null) throw new ExecutionException("The final value is null");
        if (data.get(3) == null) throw new ExecutionException("The step size is null");

        Address address =     (Address)data.get(0);
        double initialValue = (Double) data.get(1);
        double finalValue =   (Double) data.get(2);
        double stepSize =     (Double) data.get(3);
        data.clear();

        // Check for a valid step size

        if (stepSize == 0) {
            throw new ExecutionException("The step size cannot be zero");
        }

        // Check that the final value matches the step size

        if (!Double.isNaN(finalValue)) {
            if ((stepSize < 0 && finalValue > initialValue) ||
                (stepSize > 0 && finalValue < initialValue)) {
                throw new ExecutionException("Invalid final value");
            }
        }

        // Make sure this is a symbol table address

        if (!(address instanceof SymbolTableAddress)) {
            throw new ExecutionException("An animation variable cannot be assigned to a field");
        }

        // Check to see if this animation variable has already been defined.

        if (address.exists()) {
            throw new ExecutionException("This animation variable has already been defined");
        }

        // Set the value in the regular symbol table

        String name = ((SymbolTableAddress)address).getName();
        engine.getSymbolTable().directPut(name, initialValue);

        // Set the value in the animation symbol table

        AnimationVariable var = new AnimationVariable(initialValue, finalValue, stepSize);
        engine.getDynamicSymbolTable().put(name, var);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
