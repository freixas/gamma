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
package gamma.execution.hcode;

import gamma.execution.ArgInfo;
import gamma.execution.HCodeEngine;
import gamma.math.Util;
import java.util.ArrayList;
import java.util.List;

/**
 * Assign a value to a variable or a property.
 * <p>
 * Arg 1 is the address where the variable should be stored.
 * Arg 2 is the value.
 *
 * @author Antonio Freixas
 */
public class JumpIfFalseHCode extends ArgInfoHCode implements Jump
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.DOUBLE);
        argInfo = new ArgInfo(1, argTypes, 0);
    }

    private final int id;
    private int jumpLocation;

    public JumpIfFalseHCode(int id)
    {
        this.id = id;
        this.jumpLocation = 0;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
     public int getJumpLocation()
    {
        return jumpLocation;
    }

    @Override
    public void setJumpLocation(int jumpLocation)
    {
        this.jumpLocation = jumpLocation;
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        Double arg = (Double)data.get(0);
        data.clear();

        boolean isFalse = Util.fuzzyZero(arg);
        if (isFalse) engine.goTo(jumpLocation);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
