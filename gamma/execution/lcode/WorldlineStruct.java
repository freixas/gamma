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

import static gamma.execution.lcode.LineStruct.max;
import static gamma.execution.lcode.LineStruct.min;
import gamma.value.Coordinate;
import gamma.value.Frame;
import gamma.value.Observer;

/**
 *
 * @author Antonio Freixas
 */
public class WorldlineStruct extends Struct
{
    public Observer observer;
    public boolean observerSet = false;
    public Coordinate clipBL = min;
    public Coordinate clipUR = max;

    public WorldlineStruct()
    {
    }

    @Override
    public void relativeTo(Frame prime)
    {
        observer = observer.relativeTo(prime);
        clipBL = prime.toFrame(clipBL);
        clipUR = prime.toFrame(clipUR);
    }
}
