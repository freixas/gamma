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
import java.util.Iterator;

/**
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
     * @return The copied object.
     */
    public static Object copy(Object obj)
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

    @SuppressWarnings("unchecked")
    public static void copy(Collection src, Collection dst)
    {
        Iterator iter = src.iterator();
        while (iter.hasNext()) {
            dst.add(copy(iter.next()));
        }
    }

}
