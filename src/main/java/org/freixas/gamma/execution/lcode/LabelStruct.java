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

import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Frame;

/**
 *
 * @author Antonio Freixas
 */
public class LabelStruct extends Struct
{
    public Coordinate location;
    public String text = "";
    public double rotation = 0.0;
    public boolean locationSet = false;

    public LabelStruct()
    {
    }

    @Override
    public void finalizeValues()
    {
        rotation = Util.normalizeAngle180(rotation);
    }

    @Override
    public void relativeTo(Frame prime)
    {
        location = prime.toFrame(location);
        rotation = Relativity.toPrimeAngle(rotation, prime.getV());
    }
}
