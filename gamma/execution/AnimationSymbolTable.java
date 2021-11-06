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

import gamma.value.AnimationVariable;

/**
 * Animation variables are stored in this table and in the regular Symbol
 * table. The are placed in the regular symbol table just to make sure they
 * aren't assigned to twice. When retrieving a symbol, if the symbol is present
 * in this table, it's value will depend on the frame number.
 *
 * @author Antonio Freixas
 */
public class AnimationSymbolTable extends SymbolTable
{
    public AnimationSymbolTable(HCodeEngine engine)
    {
        super(engine);
    }

    @Override
    public Object get(String symbol)
    {
        AnimationVariable var = (AnimationVariable)directGet(symbol);
        if (var == null) return null;

        // TO DO
        // When the animation engine is written, this needs to get a value
        // based on the frame number

        return var.getInitialValue();
    }

    @Override
    public void put(String symbol, Object value)
    {
        // If the symbol is already here, then it means this is not the initial
        // frame of the animation

        if (contains(symbol)) return;

        super.put(symbol, value);

    }

}
