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
import gamma.execution.function.Function;
import gamma.execution.function.FunctionExecutor;
import gamma.math.Util;
import gamma.value.Observer;
import java.util.HashMap;
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
    static final HashMap<String, LambdaFunction> map = new HashMap<>();

    static final FunctionalTwoArg<Double, Double, String> toString = (engine, arg1, arg2) -> {
        int digits = Util.toInt(arg2);
        if (digits < 0) {
            throw new ExecutionException("Invalid number of digits in float to string conversion");
        }
        return Util.toString(arg1, digits);
    };

    static final FunctionalTwoArg<Double, Observer, Double> dToT = (engine, dbl, observer) -> {
        try { return observer.dToT(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToT()", ex);  }
    };
    static final FunctionalTwoArg<Double, Observer, Double> dToTau = (engine, dbl, observer) -> {
        try { return observer.dToTau(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToTau()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> dToV = (engine, dbl, observer) -> {
        try { return observer.dToV(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToV()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> dToX = (engine, dbl, observer) -> {
        try { return observer.dToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToX()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tToD = (engine, dbl, observer) -> {
        try { return observer.tToD(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToD()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tToTau = (engine, dbl, observer) -> {
        try { return observer.tToTau(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToTau()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tToV = (engine, dbl, observer) -> {
        try { return observer.tToV(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToV()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tToX = (engine, dbl, observer) -> {
        try { return observer.tToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToX()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tauToD = (engine, dbl, observer) -> {
        try { return observer.tauToD(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToD()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tauToT = (engine, dbl, observer) -> {
        try { return observer.tauToT(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToT()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tauToV = (engine, dbl, observer) -> {
        try { return observer.tauToV(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToV()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> tauToX = (engine, dbl, observer) -> {
        try { return observer.tauToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToX()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> vToD = (engine, dbl, observer) -> {
        try { return observer.vToD(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToD()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> vToT = (engine, dbl, observer) -> {
        try { return observer.vToT(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToT()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> vToTau = (engine, dbl, observer) -> {
        try { return observer.vToTau(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToTau()", ex); }
    };
    static final FunctionalTwoArg<Double, Observer, Double> vToX = (engine, dbl, observer) -> {
        try { return observer.vToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToX()", ex); }
    };

    static final FunctionalOneArg<Double, Double> abs = (engine, arg1) -> Math.abs(arg1);
    static final FunctionalOneArg<Double, Double> acos = (engine, arg1) -> Math.toDegrees(Math.acos(arg1));
    static final FunctionalOneArg<Double, Double> acosh = (engine, arg1) -> Util.acosh(arg1);
    static final FunctionalOneArg<Double, Double> asin = (engine, arg1) -> Math.toDegrees(Math.asin(arg1));
    static final FunctionalOneArg<Double, Double> asinh = (engine, arg1) -> Util.asinh(arg1);
    static final FunctionalOneArg<Double, Double> atan = (engine, arg1) -> Math.toDegrees(Math.atan(arg1));
    static final FunctionalTwoArg<Double, Double, Double> atan2 = (engine, arg1, arg2) -> Math.toDegrees(Math.atan2(arg1, arg2));
    static final FunctionalOneArg<Double, Double> atanh = (engine, arg1) -> Util.atanh(arg1);
    static final FunctionalOneArg<Double, Double> ceil = (engine, arg1) -> Math.ceil(arg1);
    static final FunctionalOneArg<Double, Double> cos = (engine, arg1) -> Math.cos(Math.toRadians(arg1));
    static final FunctionalOneArg<Double, Double> cosh = (engine, arg1) -> Math.cosh(arg1);
    static final FunctionalNoArg<Double> e = (engine) -> Math.E;
    static final FunctionalOneArg<Double, Double> exp = (engine, arg1) -> Math.exp(arg1);
    static final FunctionalOneArg<Double, Double> floor = (engine, arg1) -> Math.floor(arg1);
    static final FunctionalOneArg<Double, Double> log = (engine, arg1) -> Math.log(arg1);
    static final FunctionalOneArg<Double, Double> log10 = (engine, arg1) -> Math.log10(arg1);
    static final FunctionalTwoArg<Double, Double, Double> max = (engine, arg1, arg2) -> Math.max(arg1, arg2);
    static final FunctionalTwoArg<Double, Double, Double> min = (engine, arg1, arg2) -> Math.min(arg1, arg2);
    static final FunctionalNoArg<Double> pi = (engine) -> Math.PI;
    static final FunctionalNoArg<Double> random = (engine) -> Math.random();
    static final FunctionalOneArg<Double, Double> round = (engine, arg1) -> (double)Math.round(arg1);
    static final FunctionalOneArg<Double, Double> sign = (engine, arg1) -> (double)Util.sign(arg1);
    static final FunctionalOneArg<Double, Double> sin = (engine, arg1) -> Math.sin(Math.toRadians(arg1));
    static final FunctionalOneArg<Double, Double> sinh = (engine, arg1) -> Math.sinh(arg1);
    static final FunctionalOneArg<Double, Double> sqrt = (engine, arg1) -> Math.sqrt(arg1);
    static final FunctionalOneArg<Double, Double> tan = (engine, arg1) -> Math.tan(Math.toRadians(arg1));
    static final FunctionalOneArg<Double, Double> tanh = (engine, arg1) -> Math.tanh(arg1);

   static {
       map.put("toString",      toString);

       map.put("dToT",          dToT);
       map.put("dToTau",        dToTau);
       map.put("dToV",          dToV);
       map.put("dToX",          dToX);

       map.put("tToD",          tToD);
       map.put("tToTau",        tToTau);
       map.put("tToV",          tToV);
       map.put("tToX",          tToX);

       map.put("tauToD",        tauToD);
       map.put("tauToT",        tauToT);
       map.put("tauToV",        tauToV);
       map.put("tauToX",        tauToX);

       map.put("vToD",          vToD);
       map.put("vToT",          vToT);
       map.put("vToTau",        vToTau);
       map.put("vToX",          vToX);

       map.put("abs",           abs);
       map.put("acos",          acos);
       map.put("acosh",         acosh);
       map.put("asin",          asin);
       map.put("asinh",         asinh);
       map.put("atan",          atan);
       map.put("atan2",         atan2);
       map.put("atanh",         atanh);
       map.put("ceil",          ceil);
       map.put("cos",           cos);
       map.put("cosh",          cosh);
       map.put("e",             e);
       map.put("exp",           exp);
       map.put("floor",         floor);
       map.put("log",           log);
       map.put("log10",         log10);
       map.put("max",           max);
       map.put("min",           min);
       map.put("pi",            pi);
       map.put("random",        random);
       map.put("round",         round);
       map.put("sign",          sign);
       map.put("sin",           sin);
       map.put("sinh",          sinh);
       map.put("sqrt",          sqrt);
       map.put("tan",           tan);
       map.put("tanh",          tanh);
   }

    private final LambdaFunction func;
    private FunctionExecutor functionExecutor;
    private List<Object> data;

    /**
     * Create a generic Function. The name identifies the desired functionality.
     *
     * @param name The name associated with the Function's lambda function.
     */
    public GenericFunction(String name)
    {
        func = map.get(name);
        if (func == null) {
            throw new ExecutionException("GenericFunction(): Failed to find '" + name + "'");
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
