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
package gamma.execution.function;

import gamma.execution.hcode.GenericFunction;
import gamma.execution.hcode.ExecutorContext;
import java.util.HashMap;

/**
 * A Function is a construct similar to an HCode. It is used by the
 * FunctionHCode to implement various functions.
 *
 * @author Antonio Freixas
 */
public abstract class Function extends ExecutorContext
{
    static final HashMap<String, Function> functions = new HashMap<>();

    static {
        functions.put("rgb",        new rgbFunction());
        functions.put("gamma",      new gammaFunction());

        functions.put("toXAngle",   new toXAngleFunction());
        functions.put("toTAngle",   new toTAngleFunction());

        functions.put("dToT",       new GenericFunction("dToT"));
        functions.put("dToTau",     new GenericFunction("dToTau"));
        functions.put("dToV",       new GenericFunction("dToV"));
        functions.put("dToX",       new GenericFunction("dToX"));

        functions.put("tToD",       new GenericFunction("tToD"));
        functions.put("tToTau",     new GenericFunction("tToTau"));
        functions.put("tToV",       new GenericFunction("tToV"));
        functions.put("tToX",       new GenericFunction("tToX"));

        functions.put("tauToD",     new GenericFunction("tauToD"));
        functions.put("tauToT",     new GenericFunction("tauToT"));
        functions.put("tauToV",     new GenericFunction("tauToV"));
        functions.put("tauToX",     new GenericFunction("tauToX"));

        functions.put("vToD",       new GenericFunction("vToD"));
        functions.put("vToT",       new GenericFunction("vToT"));
        functions.put("vToTau",     new GenericFunction("vToTau"));
        functions.put("vToX",       new GenericFunction("vToX"));

        functions.put("intersect",  new intersectFunction());
        functions.put("toString",   new GenericFunction("toString"));

	functions.put("abs",	    new GenericFunction("abs"));
	functions.put("acos",	    new GenericFunction("acos"));
	functions.put("acosh",	    new GenericFunction("acosh"));
	functions.put("asin",	    new GenericFunction("asin"));
	functions.put("asinh",	    new GenericFunction("asinh"));
	functions.put("atan",	    new GenericFunction("atan"));
	functions.put("atan2",	    new GenericFunction("atan2"));
	functions.put("atanh",	    new GenericFunction("atanh"));
	functions.put("ceil",	    new GenericFunction("ceil"));
	functions.put("cos",	    new GenericFunction("cos"));
	functions.put("cosh",       new GenericFunction("cosh"));
	functions.put("e",	    new GenericFunction("e"));
	functions.put("exp",	    new GenericFunction("exp"));
	functions.put("floor",	    new GenericFunction("floor"));
	functions.put("log",	    new GenericFunction("log"));
	functions.put("log10",	    new GenericFunction("log10"));
	functions.put("max",	    new GenericFunction("max"));
	functions.put("min",	    new GenericFunction("min"));
	functions.put("pi",	    new GenericFunction("pi"));
	functions.put("random",	    new GenericFunction("random"));
	functions.put("round",	    new GenericFunction("round"));
	functions.put("sign",	    new GenericFunction("sign"));
	functions.put("sin",	    new GenericFunction("sin"));
	functions.put("sinh",       new GenericFunction("sinh"));
	functions.put("sqrt",	    new GenericFunction("sqrt"));
	functions.put("tan",	    new GenericFunction("tan"));
	functions.put("tanh",       new GenericFunction("tanh"));
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
