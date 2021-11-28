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

import gamma.execution.ArgInfo;
import gamma.execution.HCodeEngine;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public abstract class Function
{
    static final HashMap<String, Function> functions = new HashMap<>();

    static {
        functions.put("gamma",   new gammaFunction());

        functions.put("toXAngle",   new toXAngleFunction());
        functions.put("toTAngle",   new toTAngleFunction());

        functions.put("dToT",   new dToTFunction());
        functions.put("dToTau", new dToTauFunction());
        functions.put("dToV",   new dToVFunction());
        functions.put("dToX",   new dToXFunction());

        functions.put("tToD",   new tToDFunction());
        functions.put("tToTau", new tToTauFunction());
        functions.put("tToV",   new tToVFunction());
        functions.put("tToX",   new tToXFunction());

        functions.put("tauToD", new tauToDFunction());
        functions.put("tauToT", new tauToTFunction());
        functions.put("tauToV", new tauToVFunction());
        functions.put("tauToX", new tauToXFunction());

        functions.put("intersect", new intersectFunction());
        functions.put("toString",  new toStringFunction());
    }

    static public Function get(String name)
    {
        return functions.get(name);
    }

    /**
     * Execute the function.
     *
     * @param engine The HCode engine.
     * @param code The function arguments.
     * @return The function's output.
     */
    abstract public Object execute(HCodeEngine engine, List<Object> code);

    /**
     * Return the number of arguments required. -1 means the size is on the
     * stack.
     *
     * @return The number of arguments required.
     */
    abstract public ArgInfo getArgInfo();

}
