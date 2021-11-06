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
import java.util.List;

/**
 * An hcode is a high-level instruction for an imaginary machine which we
 * emulate using the HCodeEngine.
 *
 * @author Antonio Freixas
 */
abstract public class HCode
{
    /**
     * Execute the HCode.
     *
     * @param engine The HCode engine.
     * @param code The code to execute, with all arguments.
     */
    abstract public void execute(HCodeEngine engine, List<Object> code);

    /**
     * Return the number of arguments required. -1 means the size is on the
     * stack.
     *
     * @return The number of arguments required.
     */
    abstract public ArgInfo getArgInfo();

}
