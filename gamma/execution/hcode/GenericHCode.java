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

import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import java.util.HashMap;

/**
 * A GenericHCode can stand in for many individual HCode classes.
 * <p>
 * To create a generic HCode, create a static lambda for the code, using one of
 * the several LambdaFunction interfaces provided. Add the lambda to the hash
 * map with a given key.
 * <p>
 * In the Parser, when the HCode is added, create a GenericHcode with the key
 * name in the constructor.
 *
 * @author Antonio Freixas
 */
public class GenericHCode extends HCode
{
    static final HashMap<String, LambdaFunction> map = new HashMap<>();

    static final FunctionalTwoArg<Double, Double, Double> sub = (engine, arg1, arg2) -> arg1 - arg2;

    static {
        map.put("sub", sub);
    }

    private final LambdaFunction func;
    private HCodeExecutor hCodeExecutor;

    /**
     * Create a generic HCode. The name identifies the desired HCode
     * functionality.
     *
     * @param name The name associated with the HCode's lambda function.
     */
    public GenericHCode(String name)
    {
        func = map.get(name);
        if (func == null) {
            throw new ExecutionException("GenericHCode(): Failed to find '" + name + "'");
        }
    }

    /**
     * Execute the generic HCode.
     *
     * @param engine The HCodeEngine.
     */
    public void execute(HCodeEngine engine)
    {
        this.hCodeExecutor = engine.getHCodeExecutor();
        engine.getHCodeExecutor().execute(this, engine, func);
    }

    @Override
    public int getNumberOfArgs()
    {
        return hCodeExecutor.getNumberOfArgs(this, func);
    }

    @Override
    public int getNumberOfReturnedValues()
    {
        return hCodeExecutor.getNumberOfReturnedValues(this, func);
    }

}
