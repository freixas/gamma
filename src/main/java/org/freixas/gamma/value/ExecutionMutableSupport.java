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

import org.freixas.gamma.ProgrammingException;

import java.util.Collection;

/**
 * This class provides functionality used to support the ExecutionMutable
 * interface.
 *
 * @author Antonio Freixas
 */
public class ExecutionMutableSupport
{
    /**
     * Copy objects. Return references to copies of mutable objects and return
     * direct references to immutable objects.
     *
     * @param obj The object to copy.
     *
     * @return The copied item.
     */
    static public Object copy(Object obj)
    {
        if (obj instanceof String ||
            obj instanceof Integer ||
            obj instanceof Double ||
            obj instanceof ExecutionImmutable) {
          return obj;
        }
        else if (obj instanceof ExecutionMutable mutObj) {
            return mutObj.createCopy();
        }
        throw new ProgrammingException("ExecutionMutableSupport:copy()");
    }

    /**
     * Copy the src collection to the destination. Do a shallow copy of primitives
     * and immutable objects and a deep copy of mutable objects.
     *
     * @param src The source collection.
     * @param dst The destination collection.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public void copy(Collection src, Collection dst)
    {
        for (Object o : src) {
            dst.add(copy(o));
        }
    }

}
