/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.execution.hcode;

/**
 * This interface identifies the various Jump h-codes.
 *
 * @author Antonio Freixas
 */
public interface Jump
{
    /**
     * Get the label associated with this jump instruction.
     *
     * @return The label associated with this jump instruction.
     */
    int getId();

    /**
     * Get the location to jump to.
     *
     * @return The location to jump to.
     */
    int getJumpLocation();

    /**
     * Set the location to jump to.
     *
     * @param location The location to jump to.
     */
    void setJumpLocation(int location);


}
