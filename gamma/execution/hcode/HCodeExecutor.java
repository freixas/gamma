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

import gamma.execution.HCodeEngine;
import gamma.execution.HCodeProgram;
import java.util.List;

/**
 * Most of the code is in the Executor base class. This class provides the
 * correct data access.
 *
 * @author Antonio Freixas
 */
public class HCodeExecutor extends Executor
{
    private final HCodeProgram program;

    public HCodeExecutor(HCodeEngine engine)
    {
        this.program = engine.getProgram();
    }

    @Override
    public List<Object> getData(ExecutorContext context)
    {
        return program.getData((HCode)context);
    }

}