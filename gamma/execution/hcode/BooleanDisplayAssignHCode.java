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
package gamma.execution.hcode;

import gamma.execution.ArgInfo;
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import gamma.value.Address;
import gamma.value.BooleanDisplayVariable;
import gamma.value.Displayable;
import gamma.value.SymbolTableAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class BooleanDisplayAssignHCode extends ArgInfoHCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ADDRESS);
        argTypes.add(ArgInfo.Type.DOUBLE);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(3, argTypes, 0);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        Address address =     (Address)data.get(0);
        double initialValue = (Double) data.get(1);
        Object object =                data.get(2);
        data.clear();

        // Make sure this is a symbol table address

        if (!(address instanceof SymbolTableAddress)) {
            throw new ExecutionException("A display variable cannot be assigned to an object");
        }

        // Check to see if this animation variable has already been defined.

        if (address.exists()) {
            throw new ExecutionException("This display variable has already been defined");
        }

        // Convert the label object to a string

        String label = engine.toDisplayableString(object);

        // Set the value in the regular symbol table

        address.setValue(initialValue);

        // Set the value in the animation symbol table

        String name = ((SymbolTableAddress)address).getName();
        BooleanDisplayVariable var =
            new BooleanDisplayVariable(
                engine.getMainWindow().getDiagramEngine(),
                initialValue, label);
        engine.getDynamicSymbolTable().put(name, var);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
