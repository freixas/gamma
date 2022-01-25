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

import org.freixas.gamma.execution.HCodeEngine;

/**
 * This interfaces signals that this object can be displayed to the user.
 * Double values should call the HCodeEngine's toString(Double) method to
 * convert the double to a string.
 *
 * @author Antonio Freixas
 */
public interface Displayable
{
    /**
     * Convert this value to
     * @param engine
     * @return
     */
    public String toDisplayableString(HCodeEngine engine);

}
