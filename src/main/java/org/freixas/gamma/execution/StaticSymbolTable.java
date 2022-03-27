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
import org.freixas.gamma.value.DisplayVariable;
import org.freixas.gamma.value.DynamicVariable;

import java.util.*;

/**
 * A symbol table is used to store values associated with and accessed through
 * symbols (variable names).
 * <p>
 * This is the static symbol table. It contains variables that persist through
 * multiple re-executions of a script.
 *
 * @author Antonio Freixas
 */
public class StaticSymbolTable extends BaseSymbolTable
{
    /**
     * Create a static symbol table.
     *
     * @param engine The h-code engine.
     */
    public StaticSymbolTable(HCodeEngine engine)
    {
        super(engine);
    }
}
