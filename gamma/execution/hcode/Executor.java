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

import gamma.GammaRuntimeException;
import gamma.ProgrammingException;
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class can be used to implement some HCodes and HCode Functions with just
 * a few lines of codes. Specifically, it makes it possible to avoid having to
 * create a new class for many HCodes and HCode Functions.
 * <p>
 * The HCode's or Function's code can be a lambda implementation of several
 * interfaces provided for this purpose. The interfaces all extend the
 * LambdaFunction interface.
 * <p>
 * The special classes GenericHCode and GenericFunction manage and contain all
 * various lambdas.
 *
 * @author Antonio Freixas
 */
public abstract class Executor
{
    static final Class<?>[] noObjects =    { HCodeEngine.class };
    static final Class<?>[] oneObject =    { HCodeEngine.class, Object.class };
    static final Class<?>[] twoObjects =   { HCodeEngine.class, Object.class, Object.class };
    static final Class<?>[] threeObjects = { HCodeEngine.class, Object.class, Object.class, Object.class };
    static final Class<?>[] fourObjects =  { HCodeEngine.class, Object.class, Object.class, Object.class, Object.class };

    static final Class<?>[] varObjects =   { HCodeEngine.class, Object[].class };

    static final Class<?>[][] objectTypes = { noObjects, oneObject, twoObjects, threeObjects, fourObjects };

    /**
     * Execute code that is implemented using the LambdaFunction interface.
     *
     * @param context The object type on which this executor operates.
     * @param engine The HCode engine.
     * @param func A LambdaFunction class typically created using a lamdba
     * expression.
     * @param execute True if this function should be executed. If false, the
     * stack state is maintained, but the function is not executed.
     */
    public void execute(ExecutorContext context, HCodeEngine engine, LambdaFunction func, boolean execute)
    {
        // We need to know the number of arguments.

        List<Object> args = getData(context);
        int numOfArgs = args.size();

        boolean isVarArgs = func instanceof VariableArg || func instanceof VariableArgNoRet;

        // Use Reflection to find a method whose signature matches the types
        // of the data values

        Class<?>[] params;
        if (isVarArgs) {
            params = varObjects;
        }
        else {
            params = objectTypes[numOfArgs];
        }
        Method method = getMethod(func, params);

        // Execute the method and return the result

        Object[] reflectionArgs;
        if (isVarArgs) {
            Object[] varArgs = { engine, args.toArray() };
            reflectionArgs = varArgs;
        }
        else {
            args.add(0, engine);            // The engine is the first parameter
            reflectionArgs = args.toArray();
        }

        Object result;
        if (execute) {
            result = executeImpl(method, engine, func, reflectionArgs);
        }
        else {
            result = new Object();      // Dummy return value
        }

        args.clear();
        if (getNumberOfReturnedValues(context, func) > 0) {
            args.add(result);
        }
    }

    /**
     * Find a matching execution method. If we don't find one, then we received
     * values of the wrong type and an exception is thrown.
     *
     * @param func A LambdaFunction class typically created using a lamdba
     * expression.
     * @param params An array of Object classes. Generic functions wind up
     * reflecting all their generic arguments as type Object.
     * @return The method.
     */
    private Method getMethod(LambdaFunction func, Class<?>[] params)
    {
        Method method = null;
        try {
            method = func.getClass().getMethod("execute", params);
        }
        catch (NoSuchMethodException e) {
            throw new ExecutionException("Incorrect number or types of arguments");
        }
        catch (IllegalArgumentException e) {
            throw new ExecutionException("Invalid Argument", e);
        }
        catch (SecurityException e) {
            throw new ProgrammingException("Executor.executeImpl(): invoke() failed", e);
        }
        return method;
    }

    /**
     * Call the execution method.
     *
     * @param method The Method to call.
     * @param args An array of arguments.
     * @return The Object returned by the Method (which may be null).
     */
    @SuppressWarnings("unchecked")
    private Object executeImpl(Method method, HCodeEngine engine, LambdaFunction func, Object[] args)
    {
        Object result = null;
        try {
            // result = ((FunctionalOneArg<Double, Double>)func).execute(engine, (Double)args[1]);
            result = method.invoke(func, args);
        }
        catch (IllegalArgumentException e) {
            throw new ExecutionException("Invalid Argument", e);
        }
        catch (InvocationTargetException e) {
            Throwable ex = e.getCause();
            if (ex instanceof ExecutionException executionException) throw executionException;
            if (ex instanceof ProgrammingException programmingException) throw programmingException;
            throw new GammaRuntimeException(ex);
        }
        catch (SecurityException | IllegalAccessException e) {
            throw new ProgrammingException("Executor.executeImpl(): invoke() failed", e);
        }
        return result;
    }

    /**
     * Get the number of arguments required by this LambdaFunction.
     *
     * @param context The object type on which this executor operates.
     * @param func A LambdaFunction class typically created using a lamdba
     * expression.
     * @return The number of arguments required by this LambdaFunction.
     */
    public int getNumberOfArgs(ExecutorContext context, LambdaFunction func)
    {
        int numOfArgs = 0;

        if (func instanceof FunctionalOneArg || func instanceof FunctionalOneArgNoRet) {
            numOfArgs = 1;
        }
        else if (func instanceof FunctionalTwoArg || func instanceof FunctionalTwoArgNoRet) {
            numOfArgs = 2;
        }
        else if (func instanceof FunctionalThreeArg || func instanceof FunctionalThreeArgNoRet) {
            numOfArgs = 3;
        }
        else if (func instanceof FunctionalFourArg || func instanceof FunctionalFourArgNoRet) {
            numOfArgs = 4;
        }
        else if (func instanceof VariableArg || func instanceof VariableArgNoRet) {
            numOfArgs = -1;
        }
        return numOfArgs;
    }

    public int getNumberOfReturnedValues(ExecutorContext context, LambdaFunction func)
    {
        int numOfRets = 1;
        if (func instanceof FunctionalOneArgNoRet ||
            func instanceof FunctionalTwoArgNoRet ||
            func instanceof FunctionalThreeArgNoRet ||
            func instanceof FunctionalFourArgNoRet ||
            func instanceof VariableArgNoRet) {
            numOfRets = 0;
        }
        return numOfRets;
    }

    /**
     * Get the data that becomes the arguments the lambda expression works on.
     *
     * @param context The object type on which this executor operates.

     * @return The data that becomes the arguments the lambda expression works on.
     */
    abstract public List<Object> getData(ExecutorContext context);

}
