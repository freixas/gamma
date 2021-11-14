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

import gamma.ProgrammingException;
import gamma.execution.lcode.StyleStruct;
import gamma.value.LineSegment;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author Antonio Freixas
 */
public class Line
{
    static public void draw(
        Context context, Node clip, LineSegment segment,
        double thickness, String arrow,
        StyleStruct styles)
    {
        Canvas canvas = context.canvas;
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();
        Node savedClip = canvas.getClip();

        if (clip != null) {
            canvas.setClip(clip);
        }

        setupLineGc(context, styles);

        // Draw the line

        gc.strokeLine(
            segment.getPoint1().x, segment.getPoint1().t,
            segment.getPoint2().x, segment.getPoint2().t);

        // Draw the arrowheads

        if (arrow.equals("both") || arrow.equals("start")) {
            // TO DO
            // Draw the arrowhead at the start
        }
        if (arrow.equals("both") || arrow.equals("end")) {
            // TO DO
            // Draw the arrowhead at the end
        }

        // Restore the original graphics context

        canvas.setClip(savedClip);
        gc.restore();
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
        try {
            GraphicsContext gc = context.gc;

            // Set the line color

            gc.setStroke(color);

            // *** NOTE: For now, we'll assume the stroke style is CENTER
            // Set the line thickness

            gc.setLineWidth(gc.getTransform().inverseDeltaTransform(lineThickness, 0.0).getX());

            // Set the line style

            if (lineStyle.equals("dashed")) {
                gc.setLineDashes(5.0, 5.0);
            }
            else if (lineStyle.equals("dotted")) {
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.setLineDashes(1.0, 1.0);
            }
        }
        catch (NonInvertibleTransformException ex) {
            Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
