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
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class FunctionHCode extends ArgInfoHCode
{
    private static final ArgInfo argInfo;

    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.ANY);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        String funcName = (String)data.get(data.size() - 2);

        Function function = Function.get(funcName);
        if (function == null) {
            throw new ExecutionException("Uknown function '" + funcName + "'");
        }

        // The function arguments omit the argument count and function name

        List<Object> funcData = data.subList(0, data.size() - 2);

        // Execute a Function that uses ArgInfo

        if (function instanceof ArgInfoFunction argInfoFunction) {

            // Check the arguments

            @SuppressWarnings("null")
            ArgInfo funcArgInfo = argInfoFunction.getArgInfo();

            // Get the number of arguments

            int numOfArgs = funcArgInfo.getNumberOfArgs();

            if (numOfArgs == -1) numOfArgs = funcData.size();
            if (numOfArgs != funcData.size()) {
                throw new ExecutionException("Incorrect number of arguments for function '" + funcName + "'. Expected " + numOfArgs + ", received " + funcData.size());
            }

            funcArgInfo.checkTypes(funcData);

            // Execute the function

            Object result = argInfoFunction.execute(engine, funcData);

            // Remove all of the function hcode

            data.clear();

            // Add the result

            data.add(result);
        }

        // Execute a generic Function

        else if (function instanceof GenericFunction genericFunction) {
            LinkedList<Object> funcDataCopy = new LinkedList<>();
            funcDataCopy.addAll(funcData);
            genericFunction.execute(engine, funcDataCopy);
            data.clear();
            data.addAll(funcDataCopy);
        }
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
