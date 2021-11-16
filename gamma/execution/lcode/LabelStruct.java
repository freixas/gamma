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
package gamma.execution.lcode;

import gamma.execution.HCodeEngine;
import gamma.math.Lorentz;
import gamma.math.Util;
import gamma.value.Coordinate;
import gamma.value.Frame;

/**
 *
 * @author Antonio Freixas
 */
public class LabelStruct extends Struct
{
    public String text = "";
    public Coordinate location;
    public boolean locationSet = false;
    public double rotation = 0.0;

    public LabelStruct()
    {
    }

    @Override
    public void finalizeValues()
    {
        rotation = Util.normalizeAngle90(rotation);
    }

    @Override
    public void relativeTo(Frame prime)
    {
        location = prime.toFrame(location);
        rotation = Lorentz.toPrimeAngle(rotation, prime.getV());
    }
}
