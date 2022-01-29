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
import java.util.ArrayList;
import java.util.List;
import org.freixas.gamma.parser.Token;

/**
 *
 * @author Antonio Freixas
 */
public class LineInfoHCode extends ArgInfoHCode
{
    private final static ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argInfo = new ArgInfo(0, argTypes, 0);
    }

    private final Token<?> token;

    public LineInfoHCode(Token<?> token)
    {
        this.token = token;
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        data.clear();

        engine.setTokenContext(token.getContext());
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }
}
