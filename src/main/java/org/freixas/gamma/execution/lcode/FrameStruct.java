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

import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.value.Frame;

/**
 *
 * @author Antonio Freixas
 */
public class FrameStruct extends Struct
{
    public Frame frame;

    public FrameStruct()
    {
        this.frame = HCodeEngine.getDefFrame();
    }

    @Override
    public void relativeTo(Frame prime)
    {
        // Do nothing
    }

}
