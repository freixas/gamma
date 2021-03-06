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
package org.freixas.gamma.execution.hcode;

import org.freixas.gamma.execution.ArgInfo;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.Util;
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
public class JumpIfTrueHCode extends ArgInfoHCode implements Jump
{
    static private final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(1, argTypes, 0);
    }

    private final int id;
    private int jumpLocation;

    public JumpIfTrueHCode(int id)
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
        Object arg = data.get(0);
        data.clear();

        boolean isFalse =
            arg == null ||
            (arg instanceof Double && Util.fuzzyZero((Double)arg));

        if (!isFalse) engine.goTo(jumpLocation);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
