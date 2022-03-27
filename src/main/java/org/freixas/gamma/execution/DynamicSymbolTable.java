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

import org.freixas.gamma.MainWindow;
import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.value.DisplayVariable;
import org.freixas.gamma.value.DynamicVariable;

import java.util.*;

/**
 * A symbol table is used to store values associated with and accessed through
 * symbols (variable names).
 * <p>
 * This is the dynamic symbol table. It contains variables that persist through
 * multiple re-executions of a script. The initial value is set by the script
 * writer, but after that, they are set by the program (through the animation
 * engine or through GUI controls manipulated by the end user).
 *
 * @author Antonio Freixas
 */

public class DynamicSymbolTable extends BaseSymbolTable
{
    private int lastOrderNumber;
    private boolean hasDisplayVariables;

    public DynamicSymbolTable(HCodeEngine engine)
    {
        super(engine);
        lastOrderNumber = -1;
        hasDisplayVariables = false;
    }

    @Override
    public Object get(String name)
    {
        Object var = super.get(name);
        if (var == null) return null;
        return ((DynamicVariable)var).getCurrentValue();
    }

    /**
     * Get a dynamic variable. This is an object that contains more than just
     * the symbol's value.
     *
     * @param name The name of the dynamic variable to get.
     *
     * @return The dynamic variable.
     */
    public DynamicVariable getDynamicVariable(String name)
    {
        Object var = super.get(name);
        if (var == null) return null;
        return (DynamicVariable)var;
    }

    /**
     * Set the value of a symbol. If the symbol doesn't exist, create it.
     * <p>
     * The value should always be a DynamicVariable.
     *
     * @param name The name of the symbol.
     * @param value The symbol's value.
     */
    @Override
    public void put(String name, Object value)
    {
        if (!(value instanceof DynamicVariable)) {
            throw new ProgrammingException("DynamicSymbolTable.put(): Expected a DynamicVariable");
        }

        // If the symbol is already here, then it means this is not the initial
        // execution of the script

        if (contains(name)) return;

        // If the value is a display variable, note that we have display variables
        // and set its display order

        if (value instanceof DisplayVariable displayVar) {
            hasDisplayVariables = true;
            displayVar.setDisplayOrder(++lastOrderNumber);
        }

        super.put(name, value);
    }

    /**
     * Add display controls to the main window. This is used by the
     * DiagramEngine as well as the AnimationEngine.
     *
     * @param window The main window.
     */

    public void addDisplayControls(MainWindow window)
    {
        if (!hasDisplayVariables) return;
        Set<String> symbolNames = getSymbolNames();

        // Grab all display variables and place them in a collection

        ArrayList<DisplayVariable> list = new ArrayList<>();

        for (String symbolName : symbolNames) {
            DynamicVariable dynamicVariable = getDynamicVariable(symbolName);
            if (dynamicVariable instanceof DisplayVariable var) {
                list.add(var);
            }
        }

        if (list.size() > 0) {

            // Now we need to sort the list into display order

            list.sort(Comparator.comparingInt(DisplayVariable::getDisplayOrder));

            // Display the items

            for (DisplayVariable displayVariable : list) {
                window.addDisplayControl(displayVariable);
            }
        }
    }

}
