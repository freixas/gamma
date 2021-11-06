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
import gamma.value.PropertyElement;
import gamma.value.PropertyList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class PropertyListHCode extends HCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.PROPERTY_ELEMENT);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> code)
    {
        int numOfProperties = code.size() - 2;

        PropertyList properties = new PropertyList();
        for (int i = 0; i < numOfProperties; i++) {
            PropertyElement element = (PropertyElement)code.get(i);
            properties.add(element);
        }

        code.clear();

        code.add(properties);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
