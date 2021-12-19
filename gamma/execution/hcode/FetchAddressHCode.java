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
import gamma.execution.HCodeEngine;
import gamma.value.SymbolTableAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class FetchAddressHCode extends ArgInfoHCode
{
    private static final ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.STRING);
        argInfo = new ArgInfo(1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        String symbol = (String)data.get(0);
        data.clear();

        // Create an address. We don't worry whether this points to an existing
        //symbol until we need to assign to it (or find a property in it)

        data.add(new SymbolTableAddress(engine.getSymbolTable(), symbol));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
