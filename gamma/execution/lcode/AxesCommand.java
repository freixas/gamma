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

import customfx.ResizableCanvas;
import gamma.drawing.Axis;
import gamma.drawing.Context;
import gamma.drawing.T;
import gamma.execution.LCodeEngine;
import gamma.math.Lorentz;
import gamma.value.Line;

/**
 *
 * @author Antonio Freixas
 */
public class AxesCommand extends CommandExec
{
    public AxesCommand()
    {
    }

    @Override
    public void execute(LCodeEngine engine, Struct cmdStruct, StyleStruct styles)
    {
        Context context = engine.getContext();

        // Draw the X axis

        AxesStruct struct = (AxesStruct)cmdStruct;
        if (struct.x) {
            Line line = new Line(Line.AxisType.X, struct.frame);
            Axis.draw(context, line, struct.positiveOnly, struct.xLabel, styles);
        }

        // Draw the t axis

        if (struct.t) {
            Line line = new Line(Line.AxisType.T, struct.frame);
            Axis.draw(context, line, struct.positiveOnly, struct.tLabel, styles);
        }
    }

}
