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

import java.util.Arrays;
import java.util.List;

/**
 * An base class extended by any object that can hold a property accessible
 * by a script writer.
 *
 * @author Antonio Freixas
 */
public abstract class ObjectContainer
{
    private final List<String> container;

    /**
     * Set up the publicly accessible object properties.
     *
     * @param propertyNames An array containing the names of the object
     * properties.
     */
    public ObjectContainer(String[] propertyNames)
    {
        this.container = Arrays.asList(propertyNames);
    }

    /**
     * Returns true if this object has a publicly accessible object property
     * with the given name.
     *
     * @param name The name of the object property.
     *
     * @return True if the object property exists and is publicly accessible.
     */
    public boolean hasProperty(String name)
    {
        return container.contains(name);
    }

    /**
     * Get the value of a publicly accessible object property.
     *
     * @param name The name of the publicly accessible object property.
     *
     * @return The value of a publicly accessible object property.
     */
    public abstract Object getProperty(String name);

    /**
     * Set the value of a publicly accessible object property.
     *
     * @param name The name of the publicly accessible object property.
     * @param value The value the set the publicly accessible object property
     * to.
     */
    public abstract void setProperty(String name, Object value);

}
