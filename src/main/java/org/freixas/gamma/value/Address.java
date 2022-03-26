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

/**
 * This class acts like a pointer in C/C++. It allows us to get/set a value
 * stored elsewhere.
 *
 * @author Antonio Freixas
 */
public abstract class Address implements ExecutionImmutable
{
    /**
     * Return true if the object referenced by this address exists.
     *
     * @return True if the object referenced by this address exists.
     */
    public abstract boolean exists();

    // **********************************************************************
    // *
    // * Getter/Setter
    // *
    // **********************************************************************

    /**
     * Return the object referenced by this address. If the object doesn't
     * exist, return null.
     *
     * @return The object referenced by this address.
     */
    public abstract Object getValue();

    /**
     * Set the value of the object referenced by this address.
     *
     * @param value The value of the object referenced by this address.
     */
    public abstract void setValue(Object value);

    // **********************************************************************
    // *
    // * Informational
    // *
    // **********************************************************************

    /**
     * Return true if the object referenced by this address is exactly of the
     * same type as the given object.
     *
     * @param obj The object whose type we want to match.
     * @return True if the object referenced by this address is exactly of the
     * same type as the given object.
     */
    public boolean typeMatches(Object obj)
    {
        if (!exists()) return false;
        Object value = getValue();

        // Null could be a legal value, but we can't determine the object
        // type from it

        if (value == null) return false;

        return obj.getClass() == value.getClass();
    }

}
