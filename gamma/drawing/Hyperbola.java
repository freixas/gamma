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
import gamma.math.OffsetAcceleration;
import gamma.value.HyperbolicSegment;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author Antonio Freixas
 */
public class Hyperbola
{
    public final static double SMOOTHNESS = 5;

    /**
     * Draw hyperbolic segment. This method sets up the graphics context to
     * use the line styles and clips the hyperbolic segment to the viewport.
     *
     * @param context The drawing context.
     * @param segment The hyperbolic segment to draw.
     * @param styles The drawing styles.
     */
    static public void draw(Context context, HyperbolicSegment segment, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Clip the segment

        segment = segment.intersect(context.bounds);
        if (segment == null) return;

        // Save the current graphics context

        gc.save();

        // Set up the line styles

        Line.setupLineGc(context, styles);

        // Draw the segment

        drawRaw(context, segment);

        // Restore the original graphics context

        gc.restore();
    }

    /**
     * Draw a hyperbolic segment.
     * <p>
     * <ul>
     * <li>The graphics context should be set up.
     * <li>The segment should have been clipped to the viewport.
     * <li>The segment should not be null.
     * </ul>
     *
     * @param context The drawing context.
     * @param segment The hyperbolic segment to draw.
     */
    static public void drawRaw(Context context, HyperbolicSegment segment)
    {
        GraphicsContext gc = context.gc;

        double tStep = SMOOTHNESS * context.invScale;
        OffsetAcceleration curve = segment.getCurve();

        gc.beginPath();

        double t = segment.getMin().t;
        gc.moveTo(segment.getMin().x, t);

        for (t = t + tStep; t <= segment.getMax().t; t += tStep) {
            double x = curve.tToX(t);
            gc.lineTo(x, t);
        }
        gc.lineTo(segment.getMax().x, segment.getMax().t);

        gc.stroke();
    }

}
