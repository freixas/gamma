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
package gamma.execution.lcode;

import gamma.drawing.Axis;
import gamma.drawing.Context;
import gamma.value.Line;

/**
 *
 * @author Antonio Freixas
 */
public class AxesCommandExec extends CommandExec
{
    public AxesCommandExec()
    {
    }

    @Override
    public void execute(Context context, Struct cmdStruct, StyleStruct styles)
    {
        AxesStruct struct = (AxesStruct)cmdStruct;
        double v = struct.frame.getV();
        double v2 = v * v;
        double tickScale = Math.sqrt(1 + v2) / Math.sqrt(1 - v2);

        // Draw the X axis

        if (struct.x) {
            Line line = new Line(Line.AxisType.X, struct.frame);
            Axis.draw(context, v, line, Line.AxisType.X, tickScale, struct.positiveOnly, struct.xLabel, styles);
        }

        // Draw the t axis

        if (struct.t) {
            Line line = new Line(Line.AxisType.T, struct.frame);
            Axis.draw(context, v, line, Line.AxisType.T, tickScale, struct.positiveOnly, struct.tLabel, styles);
        }
    }

}
