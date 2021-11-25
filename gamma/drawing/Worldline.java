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
package gamma.drawing;

import gamma.execution.lcode.StyleStruct;
import gamma.value.Coordinate;
import gamma.value.CurveSegment;
import gamma.value.HyperbolicSegment;
import gamma.value.LineSegment;
import gamma.value.Observer;
import gamma.value.WorldlineSegment;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author Antonio Freixas
 */
public class Worldline
{
    public static void draw(Context context, Observer observer, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        // Set up the gc for line drawing

        Line.setupLineGc(context, styles);

        gamma.value.Worldline worldline = observer.getWorldline();
        ArrayList<WorldlineSegment>segments = worldline.getSegments();

        Iterator<WorldlineSegment> iter = segments.iterator();
        while (iter.hasNext()) {

            WorldlineSegment segment = iter.next();
            CurveSegment curveSegment = segment.getCurveSegment();

            // Is this a line segment? If so, intersect it with the viewport and
            // draw the intersecting segment, if any

            if (curveSegment instanceof LineSegment lineSegment) {
                lineSegment = lineSegment.intersect(context.bounds);
                if (lineSegment != null) {
                    Line.drawRaw(context, lineSegment, "none");
                }
            }

            // Is this a hyperbolic segment? If so, intersect it with the
            // viewport and draw the intersecting curve, if any

            else if (curveSegment instanceof HyperbolicSegment hyperbolicSegment) {
                hyperbolicSegment = hyperbolicSegment.intersect(context.bounds);
                if (hyperbolicSegment != null) {
                    Hyperbola.drawRaw(context, hyperbolicSegment);
                }
            }

            // Is this a line segment with one or two infinite ends (e.g. a
            // line). If so, intersect it with the viewport and draw the
            // intersecting line segment, if any

            else if (curveSegment instanceof gamma.value.Line line) {
                LineSegment lineSegment = line.intersect(context.bounds);
                if (lineSegment != null) {
                    Line.drawRaw(context, lineSegment, "none");
                }
            }
        }

        // Restore the original graphics context

        gc.restore();
    }

}
