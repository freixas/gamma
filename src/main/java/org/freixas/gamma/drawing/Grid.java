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
package org.freixas.gamma.drawing;

import org.freixas.gamma.css.value.StyleProperties;
import org.freixas.gamma.execution.lcode.GridStruct;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Bounds;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Frame;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author Antonio Freixas
 */
public class Grid
{
    static public final double MIN_GRID_SIZE = 20;

    static public void draw(Context context, GridStruct struct, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;
        Frame frame = struct.frame;

        // Save the current graphics context

        gc.save();

        double v = frame.getV();

        // The viewport is a square box that looks into part of our rest
        // frame's world space. Let's convert it to the given frame's world
        // space. In this frame, the viewport is no longer a rectangle, so
        // convert it to one by find its bounds

        Bounds bounds = context.bounds;

        // The bounds of the transformed box are set by the top left and lower
        // right corners.

        Coordinate p1 = frame.toFrame(bounds.min.x, bounds.min.t);
        Coordinate p2 = frame.toFrame(bounds.min.x, bounds.max.t);
        Coordinate p3 = frame.toFrame(bounds.max.x, bounds.min.t);
        Coordinate p4 = frame.toFrame(bounds.max.x, bounds.max.t);

        Bounds transformedBounds =  new Bounds(
            Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x)),
            Math.min(Math.min(p1.t, p2.t), Math.min(p3.t, p4.t)),
            Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x)),
            Math.max(Math.max(p1.t, p2.t), Math.max(p3.t, p4.t)));

        // We don't want to crowd things. We've chosen a certain minimum size in
        // screen coordinates. Since our minimum size is in screen coordinates,
        // we need to find the equivalent size in our transformed space

        double minRestFrameSpacing = MIN_GRID_SIZE * context.invScale;
        double minTransformedSpacing = Relativity.invLengthContraction(minRestFrameSpacing, v);

        // We want to use the largest power of 10 that is larger than the
        // spacing we've calculated. For instance, if the spacing is 30, we
        // want to use 100, not 10

        double spacing = Math.pow(10, Math.ceil(Math.log10(minTransformedSpacing)));

        // Set up the gc

        Color color = styles.xDivColor;
        double lineThickness = styles.xDivLineThickness;
        StyleProperties.LineStyle lineStyle = styles.xDivLineStyle;

        Color majorColor = styles.xMajorDivColor;
        double majorLineThickness = styles.xMajorDivLineThickness;
        StyleProperties.LineStyle majorLineStyle = styles.xMajorDivLineStyle;

        Line.setupLineGc(context, color, lineThickness, lineStyle);

        // Draw the X lines

        double startX = transformedBounds.min.x - (transformedBounds.min.x % spacing);
        int lineNumber = Util.toInt(startX / spacing);

        if (struct.x) {
            double x;
            int iX;
            for (x = startX, iX = lineNumber; x <= transformedBounds.max.x; x += spacing, iX++) {
                Coordinate c1 = frame.toRest(x, transformedBounds.min.t);
                Coordinate c2 = frame.toRest(x, transformedBounds.max.t);

                if (iX % 10 == 0) {
                    Line.setupLineGc(context, majorColor, majorLineThickness, majorLineStyle);
                    gc.strokeLine(c1.x, c1.t, c2.x, c2.t);
                    Line.setupLineGc(context, color, lineThickness, lineStyle);
               }
                else {
                    gc.strokeLine(c1.x, c1.t, c2.x, c2.t);
                }
            }
        }

        // Draw the T lines

        color = styles.tDivColor;
        lineThickness = styles.tDivLineThickness;
        lineStyle = styles.tDivLineStyle;

        majorColor = styles.tMajorDivColor;
        majorLineThickness = styles.tMajorDivLineThickness;
        majorLineStyle = styles.tMajorDivLineStyle;


        Line.setupLineGc(context, color, lineThickness, lineStyle);

        double startT = transformedBounds.min.t - (transformedBounds.min.t % spacing);
        lineNumber = Util.toInt(startT / spacing);

        if (struct.t) {
            double t;
            int iT;
            for (t = startT, iT = lineNumber; t <= transformedBounds.max.t; t += spacing, iT++) {
                Coordinate c1 = frame.toRest(transformedBounds.min.x, t);
                Coordinate c2 = frame.toRest(transformedBounds.max.x, t);

                if (iT % 10 == 0) {
                    Line.setupLineGc(context, majorColor, majorLineThickness, majorLineStyle);
                    gc.strokeLine(c1.x, c1.t, c2.x, c2.t);
                    Line.setupLineGc(context, color, lineThickness, lineStyle);
                }
                else {
                    gc.strokeLine(c1.x, c1.t, c2.x, c2.t);
                }
            }
        }

        // Restore the original graphics context

        gc.restore();
    }

}
