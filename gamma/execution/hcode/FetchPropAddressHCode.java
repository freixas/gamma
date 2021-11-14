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
import gamma.value.ObjectContainer;
import gamma.value.ObjectPropertyAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class FetchPropAddressHCode extends HCode
{
    private static final ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ADDRESS);
        argTypes.add(ArgInfo.Type.STRING);
        argInfo = new ArgInfo(2, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> code)
    {
        Address address = (Address)code.get(0);
        String propName = (String) code.get(1);
        code.clear();

        // Get the value pointed at by the address
        // This is either a symbol in a symbol table or a property in an object.
        // It must exist.

        if (!address.exists()) {
            throw new ExecutionException("Invalid address");
        }
        Object value = address.getValue();

        // Make sure the value has the given property

        if (!(value instanceof ObjectContainer) ||
            !((ObjectContainer)value).hasProperty(propName)) {
            throw new ExecutionException("'" + propName + "' is not a valid property");
        }

        // Convert this value into an address to this property

        code.add(new ObjectPropertyAddress((ObjectContainer)value, propName));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
