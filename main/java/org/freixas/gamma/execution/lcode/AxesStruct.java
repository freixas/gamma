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

import gamma.execution.HCodeEngine;
import gamma.value.Frame;
import gamma.value.Line;

/**
 *
 * @author Antonio Freixas
 */
public class AxesStruct extends Struct
{
    public class AxisStruct
    {
        public Line.AxisType axisType;
        public String label;
    }

    public Frame frame;
    public boolean positiveOnly = false;
    public boolean x = true;
    public boolean t = true;
    public String xLabel = "";
    public String tLabel = "";

    public AxisStruct xAxis;
    public AxisStruct tAxis;

    public AxesStruct()
    {
        this.frame = HCodeEngine.getDefFrame();
    }

    @Override
    public void finalizeValues()
    {
        xAxis = new AxisStruct();
        xAxis.axisType = Line.AxisType.X;
        xAxis.label = xLabel;

        tAxis = new AxisStruct();
        tAxis.axisType = Line.AxisType.T;
        tAxis.label = tLabel;
    }

    @Override
    public void relativeTo(Frame prime)
    {
        frame = frame.relativeTo(prime);
    }
}
