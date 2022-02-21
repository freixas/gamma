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

import org.freixas.gamma.execution.ArgInfo;
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.value.ConcreteObserver;
import org.freixas.gamma.value.WInitializer;
import org.freixas.gamma.value.WSegment;
import org.freixas.gamma.value.WorldlineSegment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class ObserverHCode extends ArgInfoHCode
{
    static private final ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.W_INITIALIZER);
        argTypes.add(ArgInfo.Type.W_SEGMENT);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> data)
    {
        // The total number of segments is the size of the code stack, minus
        // the worldline initializer

        int numOfSegments = data.size() - 1;

        // Grab the worldline initializer

        WInitializer initializer = (WInitializer)data.get(0);

        ArrayList<WSegment> segments = new ArrayList<>();

        // Check to see if we have any segments.

        for (int i = 1; i < numOfSegments + 1; i++) {

            // Grab each of the segments

            WSegment segment = (WSegment)data.get(i);

            // Check that all segments have a limit except for the last

            if (i < numOfSegments && segment.getType() == WorldlineSegment.LimitType.NONE) {
                throw new ExecutionException("All worldline segments except the last need a limit");
            }

            segments.add(segment);
        }

        data.clear();

        data.add(new ConcreteObserver(initializer, segments));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
