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

import org.freixas.gamma.execution.*;
import org.freixas.gamma.value.Address;
import org.freixas.gamma.value.AnimationVariable;
import org.freixas.gamma.value.ObjectPropertyAddress;
import org.freixas.gamma.value.SymbolTableAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * The STATIC_ASSIGN h-code is used to assign a value to a static variable.
 * If the variable does not exist, it is created. If it is not a static variable,
 * it is converted to a static variable.
 * <p>
 * The assigned variable cannot be a dynamic variable.
 * <p>
 * Arguments:
 * <ul>
 *     <li>ADDRESS
 *     <li>ANY
 * </ul>
 * <p>
 * No return value.
 *
 * @author Antonio Freixas
 */
public class StaticAssignHCode extends ArgInfoHCode
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ADDRESS);
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(2, argTypes, 0);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        SymbolTableAddress address = (SymbolTableAddress)data.get(0);
        Object value =               data.get(1);
        data.clear();

        String name = address.getName();
        if (address.exists()) {

            // A static variable can't also be a dynamic variable

            if (engine.getDynamicSymbolTable().contains(name)) {
                throw new ExecutionException("You cannot change the value of dynamic variable '" + name + "'");
            }

            SymbolTable symbolTable = engine.getSymbolTable();

            // If the static variable appears in the normal symbol table,
            // remove it. It doesn't matter if we lose its current value since
            // we're about to replace the value

            if (symbolTable.contains(name)) {
                symbolTable.remove(name);
            }
        }

        // Set the symbol pointed to by the address to the given value. Because
        // of the way the normal symbol table works, if it finds the variable in
        // the static symbol table, it stores the value there

        StaticSymbolTable staticSymbolTable = engine.getStaticSymbolTable();
        staticSymbolTable.put(name, value);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
