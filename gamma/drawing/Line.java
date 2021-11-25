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
import gamma.value.LineSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author Antonio Freixas
 */
public class Line
{

    /**
     * Draw a line segment.
     *
     * @param context The drawing context.
     * @param segment The line segment to draw.
     * @param styles The styles to use.
     */
    static public void draw(Context context, LineSegment segment, StyleStruct styles)
    {
        draw(context, segment, styles.lineThickness, styles.arrow, styles);
    }

    /**
     * Draw a line segment.Override some styles with specific settings.
     *
     * @param context The drawing context.
     * @param segment The line segment to draw.
     * @param thickness The line thickness.
     * @param arrow Whether to draw arrowheads and which end(s) to draw them on.
     * @param styles The styles to use.
     */
    static public void draw(
        Context context, LineSegment segment,
        double thickness, String arrow,
        StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        setupLineGc(context, styles);

        drawRaw(context, segment, arrow);

        // Restore the original graphics context

        gc.restore();
    }

    /**
     * Draw a line segment. Don't save, restore or setup the graphics context.
     *
     * @param context The drawing context.
     * @param segment The line segment to draw.
     * @param arrow Whether to draw arrowheads and which end(s) to draw them on.
     */
    static public void drawRaw(Context context, LineSegment segment, String arrow)
    {
        GraphicsContext gc = context.gc;

        // Draw the line

        gc.strokeLine(
            segment.point1.x, segment.point1.t,
            segment.point2.x, segment.point2.t);

        // Draw the arrowheads

        if (arrow.equals("both") || arrow.equals("start")) {
            // TO DO
            // Draw the arrowhead at the start
        }
        if (arrow.equals("both") || arrow.equals("end")) {
            // TO DO
            // Draw the arrowhead at the end
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
        setupLineGc(context, styles.javaFXColor, styles.lineStyle, styles.lineThickness);
    }

    /**
     * Set up the graphics context for drawing a line.
     *
     * @param context The context.
     * @param color A JavaFX Color.
     * @param lineStyle The line style ("solid", "dashed", or "dotted").
     * @param lineThickness The line thickness in pixels.
     */
    static public void setupLineGc(Context context, Color color, String lineStyle, double lineThickness)
    {
        GraphicsContext gc = context.gc;
        double scale = context.getCurrentInvScale();

        // Set the line color

        gc.setStroke(color);

        // *** NOTE: For now, we'll assume the stroke style is CENTER
        // Set the line thickness

        double worldLineThickness = lineThickness * scale;
        gc.setLineWidth(worldLineThickness);

        // Set the line style

        if (lineStyle.equals("dashed")) {
            double dashLength = 5.0 * scale;
            gc.setLineDashes(dashLength, dashLength);
        }
        else if (lineStyle.equals("dotted")) {
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineDashes(worldLineThickness / 10.0, worldLineThickness * 2);
        }
    }


}
