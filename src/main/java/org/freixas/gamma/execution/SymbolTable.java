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

import org.freixas.gamma.ProgrammingException;

import java.util.ArrayList;

/**
 * A symbol table is used to store values associated with and accessed through
 * symbols (variable names).
 * <p>
 * This is the master symbol table. It contains only ephemeral variables, those
 * that last for a single execution of a script, but it works in conjunction
 * with the static and dynamic symbol tables.
 * <ul>
 *     <li>The contains() will identify if a symbol exists in any symbol table.
 *     <li>The directContains() will identify if a symbol exists in this symbol table.
 *     <li>The get() method will get a value from any symbol table.
 *     <li>The put() method will store a value in this table unless the symbol
 *     exists in the static symbol table, in which case it stores the value there.
 *     It will generate an error if the symbol exists in the dynamic symbol table.
 *     <li>The remove() method will remove a symbol from any symbol table.
 * </ul>
 * <p>
 * We want to allow dynamic variables to be assigned once per execution. When
 * we try to create one and it exists in the dynamic symbol table, we check this
 * table. If we find it here, we have an error. If we don't find it in either,
 * them we add it to both. We never use the value stored here.
 * <p>
 * This symbol table supports protected symbols, symbols whose values cannot be
 * changed and which cannot be removed.
 *
 * @author Antonio Freixas
 */
public class SymbolTable extends BaseSymbolTable
{
    private final ArrayList<String> protectedSymbols;

    private final StaticSymbolTable staticSymbolTable;
    private final DynamicSymbolTable dynamicSymbolTable;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a symbol table.
     *
     * @param engine The h-code engine.
     */
    public SymbolTable(HCodeEngine engine)
    {
        super(engine);
        this.protectedSymbols = new ArrayList<>();

        this.staticSymbolTable = engine.getStaticSymbolTable();
        this.dynamicSymbolTable = engine.getDynamicSymbolTable();
    }

    // **********************************************************************
    // *
    // * Getters/Setters
    // *
    // **********************************************************************

    /**
     * Returns true if the given name is in any symbol table.
     *
     * @param name The name of the symbol.
     * @return  True if the given name is in any symbol table.
     */
    public final boolean contains(String name)
    {
        return
            super.contains(name) ||
                staticSymbolTable.contains(name) ||
                dynamicSymbolTable.contains(name);
    }

    /**
     * Returns true if the given name is in this symbol table.
     *
     * @param name The name of the symbol.
     * @return  True if the given name is in this symbol table.
     */
    public final boolean directContains(String name)
    {
        return
            super.contains(name);
    }

    /**
     * Get a symbol from any symbol table. Since this always returns a value,
     * use contains() to ensure the symbol is in the symbol table.
     *
     * @param symbol The name of the symbol.
     *
     * @return The symbol's value or null if it is not in any table (but null
     * is also a valid value).
     */
    public Object get(String symbol)
    {
        Object value;

        // If it is a static variable, get the value from the static symbol table

        if (staticSymbolTable.contains(symbol)) {
        value = staticSymbolTable.get(symbol);
    }

        // If this is a dynamic variable, get the value from the dynamic symbol
        // table

        else if (dynamicSymbolTable.contains(symbol)) {
            value = dynamicSymbolTable.get(symbol);
        }

        // Otherwise, get it from here

        else {
            value = super.get(symbol);
        }

        return value;
    }

    /**
     * Set the value of a symbol in this symbol table unless it already exists
     * in the static symbol table, in which case put it there. If the symbol is
     * in the dynamic symbol table, then we are trying to set a dynamic variable
     * using a regular or static assignment, which is an error.
     * <p>
     * If the symbol doesn't in either table, create it here.
     *
     * @param name The name of the symbol.
     * @param value The symbol's value.
     */
    public void put(String name, Object value)
    {
        if (dynamicSymbolTable.contains(name)) {
            throw new ExecutionException("You cannot change the value of a dynamic variable after it's created");
        }
        if (staticSymbolTable.contains(name)) {
            staticSymbolTable.put(name, value);
        }
        else {
            super.put(name, value);
        }
    }

    /**
     * Set the value of a symbol in this symbol table.
     * <p>
     * If the symbol doesn't in either table, create it here.
     *
     * @param name The name of the symbol.
     * @param value The symbol's value.
     */
    public void directPut(String name, Object value)
    {
        super.put(name, value);
    }

    /**
     * Remove a symbol from any symbol table. Protected symbols cannot be
     * removed.
     *
     * @param name The symbol to remove.
     */
    public void remove(String name)
    {
        if (protectedSymbols.contains(name)) {
            throw new ProgrammingException("SymbolTable.remove(): Trying to remove protected symbol '" + name + "'");
        }
        staticSymbolTable.remove(name);
        dynamicSymbolTable.remove(name);
        super.remove(name);
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
}
