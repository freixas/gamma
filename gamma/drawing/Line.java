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

import gamma.execution.lcode.LineStruct;
import gamma.execution.lcode.StyleStruct;
import gamma.value.Bounds;
import gamma.value.LineSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author Antonio Freixas
 */
public class Line
{

    /**
     * Draw a line.
     *
     * @param context The drawing context.
     * @param struct The line command structure.
     * @param styles The styles to use.
     */
    public static void draw(Context context, LineStruct struct, StyleStruct styles)
    {
//        Bounds clip = context.bounds.intersect(struct.clip);
//        if (clip == null) return;

        LineSegment segment = struct.line.intersect(context.bounds);
        if (segment == null) return;

        draw(context, segment, styles);
    }

    /**
     * Draw a line segment.
     *
     * @param context The drawing context.
     * @param segment The line segment to draw.
     * @param styles The styles to use.
     */
    static public void draw(
        Context context, LineSegment segment, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        setupLineGc(context, styles);

        drawRaw(context, segment, styles);

        // Restore the original graphics context

        gc.restore();
    }

    /**
     * Draw a line segment.Don't save, restore or setup the graphics context.
     *
     * @param context The drawing context.
     * @param segment The line segment to draw.
     * @param styles The styles to use.
     */
    static public void drawRaw(Context context, LineSegment segment, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Draw the line

        gc.strokeLine(
            segment.point1.x, segment.point1.t,
            segment.point2.x, segment.point2.t);

        // Draw the arrowheads

        boolean bothArrows = styles.arrow.equals("both");
        boolean startArrow = styles.arrow.equals("start") || bothArrows;
        boolean endArrow = styles.arrow.equals("end") || bothArrows;

        double angle = 0.0;
        if (startArrow || endArrow) {
            angle = segment.getAngle();
        }

        if (startArrow) {
            Arrow.draw(context, segment.point1, angle + 180.0, styles);
        }
        if (endArrow) {
            Arrow.draw(context, segment.point2, angle, styles);
        }
    }

    /**
     * Set up the graphics context for drawing a line. We only set up the
     * things that can be handled by the graphics context: color, line thickness,
     * and line style.
     *
     * @param context The context.
     * @param styles The styles structure.
     */
    static public void setupLineGc(Context context, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;
        double scale = context.invScale;

        // Set the line color

        gc.setStroke(styles.javaFXColor);

        // *** NOTE: For now, we'll assume the stroke style is CENTER
        // Set the line thickness

        double worldLineThickness = styles.lineThickness * scale;
        gc.setLineWidth(worldLineThickness);

        // Set the line style

        if (styles.lineStyle.equals("dashed")) {
            double dashLength = 5.0 * scale;
            gc.setLineDashes(dashLength, dashLength);
        }
        else if (styles.lineStyle.equals("dotted")) {
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineDashes(worldLineThickness / 10.0, worldLineThickness * 2);
        }
    }

}
