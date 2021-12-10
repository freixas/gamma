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
package gamma.value;

import gamma.execution.SymbolTable;

/**
 * This class provides indirect access to a value in the symbol table.
 * <p>
 * This class is marked ExecutionImmutable because it is only created during
 * execution, not during parsing. If we do any code optimization, this may no
 * longer be true.
 *
 * @author Antonio Freixas
 */
public class SymbolTableAddress extends Address implements ExecutionImmutable
{
    private final SymbolTable table;
    private final String name;

    public SymbolTableAddress(SymbolTable table, String name)
    {
        this.table = table;
        this.name = name;
    }

    /**
     * Get the name of the symbol for which this object is an address.
     *
     * @return The name of the symbol for which this object is an address.
     */
    public String getName()
    {
        return name;
    }

    @Override
    public boolean exists()
    {
        return table.contains(name);
    }

    @Override
    public Object getValue()
    {
        return table.get(name);
    }

    @Override
    public void setValue(Object value)
    {
        table.put(name, value);
    }
}
