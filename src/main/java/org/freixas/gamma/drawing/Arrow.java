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

import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.value.Bounds;
import org.freixas.gamma.value.Coordinate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;

/**
 *
 * @author Antonio Freixas
 */
public class Arrow
{
    public final static double ARROW_WIDTH = 10;
    public final static double ARROW_HEIGHT = 8;

    static public void draw(Context context, Coordinate location, double angle,
                            StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        // Rotate the viewport around the location so that we can draw the
        // arrow so that it points to the right (the zero-degree angle)

        Affine transform = gc.getTransform();
        transform.appendRotation(angle, location.x, location.t);
        gc.setTransform(transform);

        // Create a bounding box for the arrow and see if it intersects with the
        // viewport

        double arrowWidth = ARROW_WIDTH * context.invScale;
        double halfArrowHeight = ARROW_HEIGHT * context.invScale;

        double minX = location.x - arrowWidth;
        double minT = location.t - halfArrowHeight;
        double maxT = location.t + halfArrowHeight;

        Bounds bounds = context.getCurrentCanvasBounds();
        Bounds arrowBounds =
            new Bounds(minX, minT, location.x, maxT);
        Bounds intersect = arrowBounds.intersect(bounds);
        if (intersect != null) {

            // Set up the line styles

            Line.setupLineGc(context, styles);
            gc.setLineJoin(StrokeLineJoin.MITER);

            // Draw the arrow head

            gc.beginPath();
            gc.moveTo(minX, minT);
            gc.lineTo(location.x, location.t);
            gc.lineTo(minX, maxT);

            gc.stroke();
        }

        // Restore the original graphics context

        gc.restore();
    }

}
