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
import gamma.value.Observer;
import gamma.value.WInitializer;
import gamma.value.WSegment;
import gamma.value.WorldlineSegment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class ObserverHCode extends HCode
{
    private static final ArgInfo argInfo;
    static {
        ArrayList<ArgInfo.Type> argTypes = new ArrayList<>();
        argTypes.add(ArgInfo.Type.W_INITIALIZER);
        argTypes.add(ArgInfo.Type.W_SEGMENT);
        argInfo = new ArgInfo(-1, argTypes);
    }

    @Override
    public void execute(HCodeEngine engine, List<Object> code)
    {
        // The total number of segments is the size of the code stack, minus
        // the worldline initializer, the argument count, and the hCode.

        int numOfSegments = code.size() - 3;

        // Grab the worldline initializer

        WInitializer initializer = (WInitializer)code.get(0);

        ArrayList<WSegment> segments = new ArrayList<>();

        // Check to see if we have any segments.

        for (int i = 1; i < numOfSegments + 1; i++) {

            // Grab each of the segments

            WSegment segment = (WSegment)code.get(i);

            // Check that all segments have a limit except for the last

            if (i < numOfSegments && segment.getType() == WorldlineSegment.LimitType.NONE) {
                engine.throwExecutionException("All worldline segments except the last need a limit");
            }

            segments.add(segment);
        }

        code.clear();

        code.add(new Observer(initializer, segments));
    }

    @Override
    public ArgInfo getArgInfo()
    {
        return argInfo;
    }

}
