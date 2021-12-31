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
package gamma.execution;

import gamma.value.DynamicVariable;

/**
 * Animation variables are stored in this table and in the regular Symbol
 * table. The are placed in the regular symbol table just to make sure they
 * aren't assigned to twice. When retrieving a symbol, if the symbol is present
 * in this table, it's value will depend on the frame number.
 *
 * @author Antonio Freixas
 */
public class DynamicSymbolTable extends SymbolTable
{
    public DynamicSymbolTable(HCodeEngine engine)
    {
        super(engine);
    }

    @Override
    public Object get(String symbol)
    {
        Object var = directGet(symbol);
        if (var == null) return null;
        return ((DynamicVariable)var).getCurrentValue();
    }

    public DynamicVariable getDynamicVariable(String symbol)
    {
        Object var = directGet(symbol);
        if (var == null) return null;
        return (DynamicVariable)var;
    }

    /**
     * Set the value of a symbol. If the symbol doesn't exist, create it.
     * <p>
     * The dynamic symbol table exist through multiple executions of a script.
     * The first time a dynamic symbol is created, it is placed in the dynamic
     * symbol table and is assigned the given value. In later executions, the
     * symbol if left alone. The symbol's value is changed by changing the
     * dynamic variable in some other manner.
     *
     * @param symbol The name of the symbol.
     * @param value The symbol's value.
     */
    @Override
    public void put(String symbol, Object value)
    {
        // If the symbol is already here, then it means this is not the initial
        // execution of the script

        if (contains(symbol)) return;

        super.put(symbol, value);
    }

}
