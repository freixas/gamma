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
import gamma.math.Util;
import gamma.value.BoundedLine;
import gamma.value.Bounds;
import gamma.value.ConcreteLine;
import gamma.value.ConcreteObserver;
import gamma.value.Interval;
import gamma.value.IntervalObserver;
import gamma.value.Line;
import gamma.value.Observer;
import java.util.HashMap;

/**
 * A Function is a construct similar to an HCode. It is used by the
 * FunctionHCode to implement various functions.
 *
 * @author Antonio Freixas
 */
public abstract class Function extends ExecutorContext
{
    public enum Type
    {
        DTOT, DTOTAU, DTOV, DTOX,
        TTOD, TTOTAU, TTOV, TTOX,
        TAUTOD, TAUTOT, TAUTOV, TAUTOX,
        VTOD, VTOT, VTOTAU, VTOX,
        SET_BOUNDS, CLEAR_BOUNDS,
        SET_INTERVAL, CLEAR_INTERVAL,
        TO_STRING,
        ABS, ACOS, ACOSH, ASIN, ASINH, ATAN, ATAN2, ATANH,
        CEIL, COS, COSH,
        E, EXP,
        FLOOR,
        LOG, LOG10,
        MAX, MIN,
        PI,
        RANDOM,
        ROUND,
        SIGN, SIN, SINH, SQRT,
        TAN, TANH
    }

    // DTOT
    static final FunctionalTwoArg<Double, Observer, Double> dToT = (engine, dbl, observer) -> {
        try { return observer.dToT(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToT()", ex);  }
    };
    // DTOTAU
    static final FunctionalTwoArg<Double, Observer, Double> dToTau = (engine, dbl, observer) -> {
        try { return observer.dToTau(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToTau()", ex); }
    };
    // DTOV
    static final FunctionalTwoArg<Double, Observer, Double> dToV = (engine, dbl, observer) -> {
        try { return observer.dToV(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToV()", ex); }
    };
    // DTOX
    static final FunctionalTwoArg<Double, Observer, Double> dToX = (engine, dbl, observer) -> {
        try { return observer.dToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function dToX()", ex); }
    };
    // TTOD
    static final FunctionalTwoArg<Double, Observer, Double> tToD = (engine, dbl, observer) -> {
        try { return observer.tToD(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToD()", ex); }
    };
    // TOTTAU
    static final FunctionalTwoArg<Double, Observer, Double> tToTau = (engine, dbl, observer) -> {
        try { return observer.tToTau(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToTau()", ex); }
    };
    // TTOV
    static final FunctionalTwoArg<Double, Observer, Double> tToV = (engine, dbl, observer) -> {
        try { return observer.tToV(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToV()", ex); }
    };
    // TOTX
    static final FunctionalTwoArg<Double, Observer, Double> tToX = (engine, dbl, observer) -> {
        try { return observer.tToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tToX()", ex); }
    };
    // TAUTOD
    static final FunctionalTwoArg<Double, Observer, Double> tauToD = (engine, dbl, observer) -> {
        try { return observer.tauToD(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToD()", ex); }
    };
    // TAUTOT
    static final FunctionalTwoArg<Double, Observer, Double> tauToT = (engine, dbl, observer) -> {
        try { return observer.tauToT(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToT()", ex); }
    };
    // TAUTOV
    static final FunctionalTwoArg<Double, Observer, Double> tauToV = (engine, dbl, observer) -> {
        try { return observer.tauToV(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToV()", ex); }
    };
    // TAUTOX
    static final FunctionalTwoArg<Double, Observer, Double> tauToX = (engine, dbl, observer) -> {
        try { return observer.tauToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function tauToX()", ex); }
    };
    // VTOD
    static final FunctionalTwoArg<Double, Observer, Double> vToD = (engine, dbl, observer) -> {
        try { return observer.vToD(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToD()", ex); }
    };
    // VTOT
    static final FunctionalTwoArg<Double, Observer, Double> vToT = (engine, dbl, observer) -> {
        try { return observer.vToT(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToT()", ex); }
    };
    // VTOTAU
    static final FunctionalTwoArg<Double, Observer, Double> vToTau = (engine, dbl, observer) -> {
        try { return observer.vToTau(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToTau()", ex); }
    };
    // VTOX
    static final FunctionalTwoArg<Double, Observer, Double> vToX = (engine, dbl, observer) -> {
        try { return observer.vToX(dbl); } catch (Exception ex) { throw new ExecutionException("Error in function vToX()", ex); }
    };

    // SET_BOUNDS
    static final FunctionalTwoArg<Line, Bounds, BoundedLine> setBounds = (engine, line, bounds) -> new BoundedLine(line, bounds);
    // CLEAR_BOUNDS
    static final FunctionalOneArg<Line, Line> clearBounds = (engine, line) -> {
        if (line instanceof ConcreteLine concreteLine) {
            return concreteLine;
        }
        else if (line instanceof BoundedLine boundedLine) {
            return boundedLine.getLine();
        }
        return null;
    };
    // SET_INTERVAL
    static final FunctionalTwoArg<Observer, Interval, IntervalObserver> setInterval = (engine, observer, interval) -> new IntervalObserver(observer, interval);
    // CLEAR_INTERVAL
    static final FunctionalOneArg<Observer, Observer> clearInterval = (engine, observer) -> {
        if (observer instanceof ConcreteObserver concreteObserver) {
            return concreteObserver;
        }
        else if (observer instanceof IntervalObserver intervalObserver) {
            return intervalObserver.getObserver();
        }
        return null;
    };

    // TO_STRING
    static final FunctionalTwoArg<Double, Double, String> toString = (engine, arg1, arg2) -> {
        int digits = Util.toInt(arg2);
        if (digits < 0) {
            throw new ExecutionException("Invalid number of digits in float to string conversion");
        }
        return Util.toString(arg1, digits);
    };

    // ABS
    static final FunctionalOneArg<Double, Double> abs = (engine, arg1) -> Math.abs(arg1);
    // ACOS
    static final FunctionalOneArg<Double, Double> acos = (engine, arg1) -> Math.toDegrees(Math.acos(arg1));
    // ACOSH
    static final FunctionalOneArg<Double, Double> acosh = (engine, arg1) -> Util.acosh(arg1);
    // ASIN
    static final FunctionalOneArg<Double, Double> asin = (engine, arg1) -> Math.toDegrees(Math.asin(arg1));
    // ASINH
    static final FunctionalOneArg<Double, Double> asinh = (engine, arg1) -> Util.asinh(arg1);
    // ATAN
    static final FunctionalOneArg<Double, Double> atan = (engine, arg1) -> Math.toDegrees(Math.atan(arg1));
    // ATAN2
    static final FunctionalTwoArg<Double, Double, Double> atan2 = (engine, arg1, arg2) -> Math.toDegrees(Math.atan2(arg1, arg2));
    // ATANH
    static final FunctionalOneArg<Double, Double> atanh = (engine, arg1) -> Util.atanh(arg1);
    // CEIL
    static final FunctionalOneArg<Double, Double> ceil = (engine, arg1) -> Math.ceil(arg1);
    // COS
    static final FunctionalOneArg<Double, Double> cos = (engine, arg1) -> Math.cos(Math.toRadians(arg1));
    // COSH
    static final FunctionalOneArg<Double, Double> cosh = (engine, arg1) -> Math.cosh(arg1);
    // E
    static final FunctionalNoArg<Double> e = (engine) -> Math.E;
    // EXP
    static final FunctionalOneArg<Double, Double> exp = (engine, arg1) -> Math.exp(arg1);
    // FLOOR
    static final FunctionalOneArg<Double, Double> floor = (engine, arg1) -> Math.floor(arg1);
    // LOG
    static final FunctionalOneArg<Double, Double> log = (engine, arg1) -> Math.log(arg1);
    // LOG10
    static final FunctionalOneArg<Double, Double> log10 = (engine, arg1) -> Math.log10(arg1);
    // MAX
    static final FunctionalTwoArg<Double, Double, Double> max = (engine, arg1, arg2) -> Math.max(arg1, arg2);
    // MIN
    static final FunctionalTwoArg<Double, Double, Double> min = (engine, arg1, arg2) -> Math.min(arg1, arg2);
    // PI
    static final FunctionalNoArg<Double> pi = (engine) -> Math.PI;
    // RANDOM
    static final FunctionalNoArg<Double> random = (engine) -> Math.random();
    // ROUND
    static final FunctionalOneArg<Double, Double> round = (engine, arg1) -> (double)Math.round(arg1);
    // SIGN
    static final FunctionalOneArg<Double, Double> sign = (engine, arg1) -> Util.sign(arg1);
    // SIN
    static final FunctionalOneArg<Double, Double> sin = (engine, arg1) -> Math.sin(Math.toRadians(arg1));
    // SINH
    static final FunctionalOneArg<Double, Double> sinh = (engine, arg1) -> Math.sinh(arg1);
    // SQRT
    static final FunctionalOneArg<Double, Double> sqrt = (engine, arg1) -> Math.sqrt(arg1);
    // TAN
    static final FunctionalOneArg<Double, Double> tan = (engine, arg1) -> Math.tan(Math.toRadians(arg1));
    // TANH
    static final FunctionalOneArg<Double, Double> tanh = (engine, arg1) -> Math.tanh(arg1);

    // Map generic functions to their matching lambda

    static final HashMap<Type, LambdaFunction> map = new HashMap<>();

    static {
        map.put(Type.DTOT,          dToT);
        map.put(Type.DTOTAU,        dToTau);
        map.put(Type.DTOV,          dToV);
        map.put(Type.DTOX,          dToX);

        map.put(Type.TTOD,          tToD);
        map.put(Type.TTOTAU,        tToTau);
        map.put(Type.TTOV,          tToV);
        map.put(Type.TTOX,          tToX);

        map.put(Type.TAUTOD,        tauToD);
        map.put(Type.TAUTOT,        tauToT);
        map.put(Type.TAUTOV,        tauToV);
        map.put(Type.TAUTOX,        tauToX);

        map.put(Type.VTOD,          vToD);
        map.put(Type.VTOT,          vToT);
        map.put(Type.VTOTAU,        vToTau);
        map.put(Type.VTOX,          vToX);

        map.put(Type.SET_BOUNDS,    setBounds);
        map.put(Type.CLEAR_BOUNDS,  clearBounds);
        map.put(Type.SET_INTERVAL,   setInterval);
        map.put(Type.CLEAR_INTERVAL, clearInterval);

        map.put(Type.TO_STRING,     toString);

        map.put(Type.ABS,           abs);
        map.put(Type.ACOS,          acos);
        map.put(Type.ACOSH,         acosh);
        map.put(Type.ASIN,          asin);
        map.put(Type.ASINH,         asinh);
        map.put(Type.ATAN,          atan);
        map.put(Type.ATAN2,         atan2);
        map.put(Type.ATANH,         atanh);
        map.put(Type.CEIL,          ceil);
        map.put(Type.COS,           cos);
        map.put(Type.COSH,          cosh);
        map.put(Type.E,             e);
        map.put(Type.EXP,           exp);
        map.put(Type.FLOOR,         floor);
        map.put(Type.LOG,           log);
        map.put(Type.LOG10,         log10);
        map.put(Type.MAX,           max);
        map.put(Type.MIN,           min);
        map.put(Type.PI,            pi);
        map.put(Type.RANDOM,        random);
        map.put(Type.ROUND,         round);
        map.put(Type.SIGN,          sign);
        map.put(Type.SIN,           sin);
        map.put(Type.SINH,          sinh);
        map.put(Type.SQRT,          sqrt);
        map.put(Type.TAN,           tan);
        map.put(Type.TANH,          tanh);
    }

    // All functions, generic and otherwise

    static final HashMap<String, Function> functions = new HashMap<>();

    static {
        functions.put("rgb",        new rgbFunction());
        functions.put("gamma",      new gammaFunction());

        functions.put("toXAngle",   new toXAngleFunction());
        functions.put("toTAngle",   new toTAngleFunction());

        functions.put("dToT",       new GenericFunction(Type.DTOT));
        functions.put("dToTau",     new GenericFunction(Type.DTOTAU));
        functions.put("dToV",       new GenericFunction(Type.DTOV));
        functions.put("dToX",       new GenericFunction(Type.DTOX));

        functions.put("tToD",       new GenericFunction(Type.TTOD));
        functions.put("tToTau",     new GenericFunction(Type.TTOTAU));
        functions.put("tToV",       new GenericFunction(Type.TTOV));
        functions.put("tToX",       new GenericFunction(Type.TTOX));

        functions.put("tauToD",     new GenericFunction(Type.TAUTOD));
        functions.put("tauToT",     new GenericFunction(Type.TAUTOT));
        functions.put("tauToV",     new GenericFunction(Type.TAUTOV));
        functions.put("tauToX",     new GenericFunction(Type.TAUTOX));

        functions.put("vToD",       new GenericFunction(Type.VTOD));
        functions.put("vToT",       new GenericFunction(Type.VTOT));
        functions.put("vToTau",     new GenericFunction(Type.VTOTAU));
        functions.put("vToX",       new GenericFunction(Type.VTOX));

        functions.put("setBounds",   new GenericFunction(Type.SET_BOUNDS));
        functions.put("clearBounds", new GenericFunction(Type.CLEAR_BOUNDS));
        functions.put("setInterval",   new GenericFunction(Type.SET_INTERVAL));
        functions.put("clearInterval", new GenericFunction(Type.CLEAR_INTERVAL));

        functions.put("intersect",  new intersectFunction());
        functions.put("toString",   new GenericFunction(Type.TO_STRING));

	functions.put("abs",	    new GenericFunction(Type.ABS));
	functions.put("acos",	    new GenericFunction(Type.ACOS));
	functions.put("acosh",	    new GenericFunction(Type.ACOSH));
	functions.put("asin",	    new GenericFunction(Type.ASIN));
	functions.put("asinh",	    new GenericFunction(Type.ASINH));
	functions.put("atan",	    new GenericFunction(Type.ATAN));
	functions.put("atan2",	    new GenericFunction(Type.ATAN2));
	functions.put("atanh",	    new GenericFunction(Type.ATANH));
	functions.put("ceil",	    new GenericFunction(Type.CEIL));
	functions.put("cos",	    new GenericFunction(Type.COS));
	functions.put("cosh",       new GenericFunction(Type.COSH));
	functions.put("e",	    new GenericFunction(Type.E));
	functions.put("exp",	    new GenericFunction(Type.EXP));
	functions.put("floor",	    new GenericFunction(Type.FLOOR));
	functions.put("log",	    new GenericFunction(Type.LOG));
	functions.put("log10",	    new GenericFunction(Type.LOG10));
	functions.put("max",	    new GenericFunction(Type.MAX));
	functions.put("min",	    new GenericFunction(Type.MIN));
	functions.put("pi",	    new GenericFunction(Type.PI));
	functions.put("random",	    new GenericFunction(Type.RANDOM));
	functions.put("round",	    new GenericFunction(Type.ROUND));
	functions.put("sign",	    new GenericFunction(Type.SIGN));
	functions.put("sin",	    new GenericFunction(Type.SIN));
	functions.put("sinh",       new GenericFunction(Type.SINH));
	functions.put("sqrt",	    new GenericFunction(Type.SQRT));
	functions.put("tan",	    new GenericFunction(Type.TAN));
	functions.put("tanh",       new GenericFunction(Type.TANH));
    }

    /**
     * Given a name, find the matching function.
     *
     * @param name The name of the function.
     * @return The Function found, or null if none.
     */

    static public Function get(String name)
    {
        return functions.get(name);
    }

}
