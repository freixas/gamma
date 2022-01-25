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
package org.freixas.gamma.execution.lcode;

import org.freixas.gamma.value.Bounds;
import org.freixas.gamma.value.Frame;
import org.freixas.gamma.value.Observer;

/**
 *
 * @author Antonio Freixas
 */
public class WorldlineStruct extends Struct
{
    public Observer observer;
    public boolean observerSet = false;
    public Bounds clip = null;

    public Bounds bounds;

    public WorldlineStruct()
    {
    }

    @Override
    public void relativeTo(Frame prime)
    {
        observer = observer.relativeTo(prime);
        // clip = new Bounds(prime.toFrame(clip.min), prime.toFrame(clip.max));
    }
}
