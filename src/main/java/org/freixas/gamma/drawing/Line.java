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

import gamma.css.value.StyleProperties;
import gamma.execution.lcode.LineStruct;
import gamma.css.value.StyleStruct;
import gamma.value.BoundedLine;
import gamma.value.ConcreteLine;
import gamma.value.CurveSegment;
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
     * Draw a line.
     *
     * @param context The drawing context.
     * @param struct The line command structure.
     * @param styles The styles to use.
     */
    public static void draw(Context context, LineStruct struct, StyleStruct styles)
    {
        StyleProperties.Arrow arrowStyle = styles.arrow;

        // Normal lines are infinite and have no arrowheads

        if (struct.line instanceof ConcreteLine) {
            styles.arrow = StyleProperties.Arrow.NONE;
        }

        // Bounded lines are allowed arrows on any finite end

        else if (struct.line instanceof BoundedLine boundedLine) {
            if (boundedLine.isInfiniteMinus()) suppressStartArrow(styles);
            if (boundedLine.isInfinitePlus()) suppressEndArrow(styles);
        }

        LineSegment segment = struct.line.intersect(context.bounds);

        // For bounded lines (which can have arrows), we need to find out if the
        // arrow head has been clipped and disable it.

        // NOTE: This method clips an arrowhead that might still be partly visible.
        // An alternate method would be to resize the bounds to allow for a
        // potential arrowhead

        if (struct.line instanceof BoundedLine boundedLine) {
            CurveSegment curve = boundedLine.getCurveSegment();

            if (curve instanceof LineSegment lineSegment) {
                if (!context.bounds.inside(lineSegment.getPoint1())) suppressStartArrow(styles);
                if (!context.bounds.inside(lineSegment.getPoint2())) suppressEndArrow(styles);
            }

            else if (curve instanceof ConcreteLine concreteLine) {
                if (!concreteLine.isInfiniteMinus() &&
                    !context.bounds.inside(concreteLine.getCoordinate())) suppressStartArrow(styles);
                if (!concreteLine.isInfinitePlus() &&
                    !context.bounds.inside(concreteLine.getCoordinate())) suppressEndArrow(styles);
            }
        }

        if (segment != null) {
            draw(context, segment, styles);
        }

        // Restore the original arrow style

        styles.arrow = arrowStyle;
    }

    private static void suppressStartArrow(StyleStruct styles)
    {
        if (styles.arrow == StyleProperties.Arrow.START) {
            styles.arrow = StyleProperties.Arrow.NONE;
        }
        else if (styles.arrow == StyleProperties.Arrow.BOTH) {
            styles.arrow = StyleProperties.Arrow.END;
        }
    }

    private static void suppressEndArrow(StyleStruct styles)
    {
        if (styles.arrow == StyleProperties.Arrow.END) {
            styles.arrow = StyleProperties.Arrow.NONE;
        }
        else if (styles.arrow == StyleProperties.Arrow.BOTH) {
            styles.arrow = StyleProperties.Arrow.START;
        }
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
            segment.getPoint1().x, segment.getPoint1().t,
            segment.getPoint2().x, segment.getPoint2().t);

        // Draw the arrowheads

        boolean bothArrows = styles.arrow == StyleProperties.Arrow.BOTH;
        boolean startArrow = styles.arrow == StyleProperties.Arrow.START|| bothArrows;
        boolean endArrow = styles.arrow == StyleProperties.Arrow.END || bothArrows;

        double angle = 0.0;
        if (startArrow || endArrow) {
            angle = segment.getAngle();
        }

        if (startArrow) {
            Arrow.draw(context, segment.getPoint1(), angle + 180.0, styles);
        }
        if (endArrow) {
            Arrow.draw(context, segment.getPoint2(), angle, styles);
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

        gc.setStroke(styles.color);

        // *** NOTE: For now, we'll assume the stroke style is CENTER
        // Set the line thickness

        double worldLineThickness = styles.lineThickness * scale;
        gc.setLineWidth(worldLineThickness);

        // Set the line style

        if (styles.lineStyle == StyleProperties.LineStyle.DASHED) {
            double dashLength = 5.0 * scale;
            gc.setLineDashes(dashLength, dashLength);
        }
        else if (styles.lineStyle == StyleProperties.LineStyle.DASHED) {
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineDashes(worldLineThickness / 10.0, worldLineThickness * 2);
        }
    }

    /**
     * Set up the graphics context for drawing a line.We only set up the things
     * that can be handled by the graphics context: color, line thickness, and
     * line style.
     *
     * @param context The context.
     * @param color The line color.
     * @param lineThickness The line thickness.
     * @param lineStyle The line style.
     */
    static public void setupLineGc(Context context, Color color, double lineThickness, StyleProperties.LineStyle lineStyle)
    {
        GraphicsContext gc = context.gc;
        double scale = context.invScale;

        // Set the line color

        gc.setStroke(color);

        // *** NOTE: For now, we'll assume the stroke style is CENTER
        // Set the line thickness

        double worldLineThickness = lineThickness * scale;
        gc.setLineWidth(worldLineThickness);

        // Set the line style

        if (lineStyle == StyleProperties.LineStyle.DASHED) {
            double dashLength = 5.0 * scale;
            gc.setLineDashes(dashLength, dashLength);
        }
        else if (lineStyle == StyleProperties.LineStyle.DASHED) {
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineDashes(worldLineThickness / 10.0, worldLineThickness * 2);
        }
    }
}
