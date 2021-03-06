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
 * This interfaces is used for signaling that a given object is
 * mutable during execution; the user can change object's members from
 * the script.
 *
 * @author Antonio Freixas
 */
public interface ExecutionMutable
{
    /**
     * The createCopy() method works like clone(), except without
     * requiring a check for CloneNotSupportedException. It performs a deep
     * copy of the object.
     * <p>
     * A Mutable subclass with a Mutable parent should call super.createCopy()
     * to obtain an object reference. Otherwise, it can create the reference
     * itself by calling its constructor.
     *
     * @return The copied object.
     */
    public Object createCopy();
}
