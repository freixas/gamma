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

import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import java.util.List;

/**
 * A GenericFunction can stand in for many individual Function classes.
 * <p>
 * To create a generic Function, create a static lambda for the code, using one of
 * the several LambdaFunction interfaces provided. Add the lambda to the hash
 * map with a given key.
 * <p>
 * In FunctionHCode, add a GenericFunction with the given name to the function
 * map.
 *
 * @author Antonio Freixas
 */
public class GenericFunction extends Function
{
    private final LambdaFunction func;
    private FunctionExecutor functionExecutor;
    private List<Object> data;

    /**
     * Create a generic Function.The name identifies the desired functionality.
     *
     * @param type The type associated with the Function's lambda function.
     */
    public GenericFunction(Function.Type type)
    {
        func = Function.map.get(type);
        if (func == null) {
            throw new ExecutionException("GenericFunction(): Failed to find '" + type + "'");
        }
    }

    /**
     * Execute the generic Function.
     *
     * @param engine The HCodeEngine.
     * @param data The data corresponding to this function.
     */
    public void execute(HCodeEngine engine, List<Object> data)
    {
        this.data = data;
        this.functionExecutor = engine.getFunctionExecutor();
        functionExecutor.execute(this, engine, func);
    }

    public List<Object> getData()
    {
        return data;
    }

    @Override
    public int getNumberOfArgs()
    {
        return functionExecutor.getNumberOfArgs(this, func);
    }

    @Override
    public int getNumberOfReturnedValues()
    {
        return functionExecutor.getNumberOfReturnedValues(this, func);
    }
}
