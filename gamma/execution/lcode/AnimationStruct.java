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

import gamma.execution.ExecutionException;
import gamma.value.Frame;

/**
 *
 * @author Antonio Freixas
 */
public class AnimationStruct extends Struct
{
    public String control = "loop";
    public int reps = 500;
    public double speed = 1.0;


    public AnimationStruct()
    {
    }

    public void repsRangeCheck()
    {
        if (reps < 1 || reps > 500) {
            throw new ExecutionException("Animation reps must be between 1 and 500");
        }
    }

    public void speedRangeCheck()
    {
        if (speed < 0.1 || speed > 10.0) {
            throw new ExecutionException("Animation speed must be between 0.1 and 10.0");
        }
    }

    @Override
    public void relativeTo(Frame prime)
    {
        // Do nothing
    }

}
