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
import gamma.execution.function.Function;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class FunctionHCode extends HCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> code)
    {
        String funcName = (String)code.get(code.size() - 3);

        Function function = Function.get(funcName);
        if (function == null) {
            engine.throwExecutionException("Uknown function '" + funcName + "'");
        }

        List<Object> funcCode = code.subList(0, code.size() - 3);

        // Check the arguments

        @SuppressWarnings("null")
        ArgInfo funcArgInfo = function.getArgInfo();

        // Get the number of arguments

        int numOfArgs = funcArgInfo.getNumberOfArgs();

        if (numOfArgs == -1) numOfArgs = funcCode.size();
        if (numOfArgs != funcCode.size()) {
            engine.throwExecutionException("Incorrect number of arguments for function '" + funcName + "'. Expected " + numOfArgs + ", received " + funcCode.size());
        }

        funcArgInfo.checkTypes(funcCode);

        // Execute the hCode

        Object result = function.execute(engine, funcCode);

        // Remove all of the function hcode

        code.clear();

        // Add the result

        code.add(result);
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
