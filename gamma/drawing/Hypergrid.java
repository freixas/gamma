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

import gamma.execution.lcode.HypergridStruct;
import gamma.css.value.StyleStruct;
import gamma.math.OffsetAcceleration;
import gamma.math.Util;
import gamma.value.Bounds;
import gamma.value.Coordinate;
import gamma.value.HyperbolicSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

/**
 *
 * @author Antonio Freixas
 */
public class Hypergrid
{
    public static final double MIN_GRID_SIZE = 20;

    public static void draw(Context context, HypergridStruct struct,
                            StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        Bounds bounds = context.bounds;

        // We don't want to crowd things. We've chosen a certain minimum size in
        // screen coordinates. Since our minimum size is in screen coordinates,
        // we need to find the equivalent size in our transformed space

        double minSpacing = MIN_GRID_SIZE * context.invScale;

        // We want to use the largest power of 10 that is larger than the
        // spacing we've calculated. For instance, if the spacing is 30, we
        // want to use 100, not 10

        double spacing = Math.pow(10, Math.ceil(Math.log10(minSpacing)));

        // Set up the gc

        Line.setupLineGc(context, styles);

        // Draw the left  and right quadrants

        double startX = bounds.min.x - (bounds.min.x % spacing);
        int lineNumber = Util.toInt(startX / spacing);
        if (struct.left || struct.right) {
            double x;
            int iX;
            for (x = startX, iX = lineNumber; x <= bounds.max.x; x += spacing, iX++) {
                if (x < 0 && !struct.left) continue;
                if (x > 0 && !struct.right) continue;
                if (Util.fuzzyZero(x)) continue;
                double a = 1/x;
                OffsetAcceleration curve = new OffsetAcceleration(a, 0, new Coordinate(x, 0.0), 0.0, 0.0);
                HyperbolicSegment segment = new HyperbolicSegment(a, bounds.min.t, bounds.max.t, curve);
                HyperbolicSegment segment2 = segment.intersect(bounds);
                if (segment2 != null) {
                    Hyperbola.drawRaw(context, segment2);
                }
            }
        }

        // Draw the top and bottom quadrants

        gc.rotate(90);
        bounds = context.getCurrentCanvasBounds();

        startX = bounds.min.x - (bounds.min.x % spacing);
        lineNumber = Util.toInt(startX / spacing);
        if (struct.bottom || struct.top) {
            double x;
            int iX;
            for (x = startX, iX = lineNumber; x <= bounds.max.x; x += spacing, iX++) {
                if (x < 0 && !struct.bottom) continue;
                if (x > 0 && !struct.top) continue;
                if (Util.fuzzyZero(x)) continue;
                double a = 1/x;
                OffsetAcceleration curve = new OffsetAcceleration(a, 0, new Coordinate(x, 0.0), 0.0, 0.0);
                HyperbolicSegment segment = new HyperbolicSegment(a, bounds.min.t, bounds.max.t, curve);
                HyperbolicSegment segment2 = segment.intersect(bounds);
                if (segment2 != null) {
                    Hyperbola.drawRaw(context, segment2);
                }
            }
        }
        // Restore the original graphics context

        gc.restore();
    }

}
