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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Antonio Freixas
 */
public class SymbolTable
{
    private final HCodeEngine engine;
    private final HashMap<String, Object> table;
    private final ArrayList<String> protectedSymbols;

    public SymbolTable(HCodeEngine engine)
    {
        this.engine = engine;
        this.table = new HashMap<>();
        this.protectedSymbols = new ArrayList<>();
    }

    /**
     * Returns true if the given name is in the symbol table.
     *
     * @param key The name of the symbol.
     * @return  True if the given name is in the symbol table.
     */
    public boolean contains(String key)
    {
        return table.containsKey(key);
    }

    /**
     * Get a symbol from the symbol table. Since this always returns a value,
     * use contains() to ensure the symbol is in the symbol table.
     * <p>
     * If the variable is an animation variable, we get it from the
     * animation symbol table instead.
     *
     * @param symbol The name of the symbol.
     * @return The symbol's value or null if it is not in the table.
     */
    public Object get(String symbol)
    {
        // If this is an animation variable, get it from the animation symbol
        // table

        AnimationSymbolTable animationTable = engine.getAnimationSymbolTable();
        Object value = animationTable.get(symbol);
        if (value == null) {
            value = table.get(symbol);
        }
        return value;
    }

    protected Object directGet(String symbol)
    {
        return table.get(symbol);
    }

    /**
     * Set the value of a symbol. If the symbol doesn't exist, create it.
     *
     * @param symbol The name of the symbol.
     * @param value The symbol's value.
     */
    public void put(String symbol, Object value)
    {
        if (protectedSymbols.contains(symbol)) {
            throw new ExecutionException("You cannot change the value of '" + symbol + "'");
        }
        table.put(symbol, value);
    }

    /**
     * Set a variable so it cannot be assigned to. The variable must be added
     * to the symbol table before making it protected, of course.
     *
     * @param symbol The variable to protect.
     */
    public void protect(String symbol)
    {
        protectedSymbols.add(symbol);
    }

    /**
     * Get all the symbols in the table.
     *
     * @return All the symbols in the table.
     */
    public Set<String> getSymbolNames()
    {
        return table.keySet();
    }
}
