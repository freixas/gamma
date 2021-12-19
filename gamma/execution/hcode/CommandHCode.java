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
import gamma.execution.lcode.Command;
import gamma.execution.lcode.CommandFactory;
import gamma.value.PropertyList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class CommandHCode extends ArgInfoHCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.PROPERTY_LIST);
        argTypes.add(ArgInfo.Type.STRING);
        argInfo = new ArgInfo(2, argTypes, 0);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        PropertyList properties = (PropertyList)data.get(0);
        String name =             (String)      data.get(1);
        data.clear();

        Command command = CommandFactory.createCommand(engine, name, properties);
        engine.addCommand(command);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
