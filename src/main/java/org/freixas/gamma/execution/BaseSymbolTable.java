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
package org.freixas.gamma.execution;

import java.util.HashMap;
import java.util.Set;

/**
 * The base symbol table is the base class for all symbol tables. Symbol table
 * are used to store values associated with and accessed through symbols
 * (variable names).
 *
 * @author Antonio Freixas
 */
public class BaseSymbolTable
{
    private final HCodeEngine engine;
    private final HashMap<String, Object> hashMap;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a base symbol table.
     *
     * @param engine The h-code engine.
     */
    public BaseSymbolTable(HCodeEngine engine)
    {
        this.engine = engine;
        this.hashMap = new HashMap<>();
    }

    // **********************************************************************
    // *
    // * Getters/Setters
    // *
    // **********************************************************************

    /**
     * Get the h-code engine.
     *
     * @return The h-code engine.
     */
    public HCodeEngine getHCodeEngine()
    {
        return engine;
    }

    /**
     * Returns true if the given symbol is in this symbol table.
     *
     * @param name The name of the symbol.
     * @return  True if the given name is in any symbol table.
     */
    public boolean contains(String name)
    {
        return hashMap.containsKey(name);
    }

    /**
     * Get a symbol from the symbol table. Since this always returns a value,
     * use contains() to ensure the symbol is in the symbol table.
     * <p>
     *
     * @param name The name of the symbol.
     *
     * @return The symbol's value or null if it is not in any table.
     */
    public Object get(String name)
    {
         return hashMap.get(name);
    }

    /**
     * Set the value of a symbol in this symbol table. If the symbol doesn't
     * exist, create it.
     *
     * @param symbol The name of the symbol.
     * @param value The symbol's value.
     */
    public void put(String symbol, Object value)
    {
       hashMap.put(symbol, value);
    }

    /**
     * Remove a symbol from this symbol table.
     *
     * @param symbol The symbol to remove.
     */
    public void remove(String symbol)
    {
        hashMap.remove(symbol);
    }

   /**
     * Get all the symbols in this symbol table.
     *
     * @return All the symbols in the table.
     */
    public Set<String> getSymbolNames()
    {
        return hashMap.keySet();
    }
}
