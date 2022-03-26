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
package org.freixas.gamma.value;

import org.freixas.gamma.execution.ExecutionException;

/**
 * This class holds a command property, which is a name paired with a value.
 *
 * @author Antonio Freixas
 */
public class Property implements PropertyElement, ExecutionImmutable
{
    private final String name;
    private final Object value;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create a property.
     *
     * @param name The property's name.
     * @param value The property's value.
     */
    public Property(String name, Object value)
    {
        this.name = name;
        if (value == null) throw new ExecutionException("The property's value is null");
        this.value = value;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

}
