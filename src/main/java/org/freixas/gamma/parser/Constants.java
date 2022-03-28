/*
 * Copyright (c) 2022 Antonio Freixas
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
package org.freixas.gamma.parser;

import java.util.HashMap;

/**
 * This class controls pre-defined script constants.
 */
public class Constants
{
    static HashMap<String, Object>map;

    static {
        map = new HashMap<>();
        map.put("INF", Double.POSITIVE_INFINITY);
        map.put("inf", Double.POSITIVE_INFINITY);
        map.put("NULL", null);
        map.put("null", null);
        map.put("TRUE", 1.0);
        map.put("FALSE", 0.0);
        map.put("true", 1.0);
        map.put("false", 0.0);
        map.put("PI", Math.PI);
        map.put("E", Math.E);
    }

    /**
     * Returns true if the given name represents a script constant.
     *
     * @param name The name to check.
     *
     * @return True if the given name represents a script constant.
     */
    static public boolean isConstant(String name)
    {
        return map.containsKey(name);
    }

    /**
     * Returns the value of a constant with a given name. Since the names
     * "NULL" and "null" return null as their values, this method cannot be
     * used to determine if a particular name represents a constant.
     *
     * @param name The name of the constant whose value we want.
     *
     * @return The value of the constant (may be null).
     */
    static public Object get(String name)
    {
        return map.get(name);
    }
}
