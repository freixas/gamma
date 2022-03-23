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
import org.freixas.gamma.execution.lcode.PathStruct;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Coordinate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;

/**
 * Draw a path.
 *
 * @author Antonio Freixas
 */
public class Path
{
    /**
     * Draw a path.
     *
     * @param context The drawing context.
     * @param struct The path properties.
     * @param styles The style properties.
     */
    static public void draw(Context context, PathStruct struct, StyleStruct styles)
    {
        // Quick test to see if we need to bother with this path

        if (!context.bounds.intersects(struct.path.getBounds())) {
            return;
        }

        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        org.freixas.gamma.value.Path path = struct.path;

        gc.beginPath();
        for (int i = 0; i < path.size(); i++) {
            Coordinate coord = struct.path.get(i);
            if (i == 0) {
                gc.moveTo(coord.x, coord.t);
            }
            else {
                gc.lineTo(coord.x, coord.t);
            }
        }

        // If the user wanted a closed path, close it

        if (struct.closed) {
            gc.closePath();
        }

        // If the user wanted a filled path, fill it

        if (struct.fill) {
            setupFillGc(context, styles);
            gc.fill();
        }

        // If the user wanted the path stroked, stroke it

        if (struct.stroke) {
            Line.setupLineGc(context, styles);
            gc.stroke();
        }

       // Add arrow heads

       if (struct.stroke && !struct.closed && styles.arrow != StyleProperties.Arrow.NONE) {
           int size = path.size();
           if (size > 1) {
               if (styles.arrow == StyleProperties.Arrow.END || styles.arrow == StyleProperties.Arrow.BOTH) {
                   Coordinate start = path.get(size - 2);
                   Coordinate end = path.get(size - 1);
                   double angle = Util.getAngle(start, end);
                   Arrow.draw(context, end, angle, styles);
               }
               if (styles.arrow == StyleProperties.Arrow.START || styles.arrow == StyleProperties.Arrow.BOTH) {
                   Coordinate start = path.get(1);
                   Coordinate end = path.get(0);
                   double angle = Util.getAngle(start, end);
                   Arrow.draw(context, end, angle, styles);
               }
           }
       }

        // Restore the original graphics context

        gc.restore();
    }

    /**
     * Set up the graphic context for drawing fills. We set the background
     * color from the style properties. The fill rule is EVEN_ODD.
     *
     * @param context The drawing context.
     * @param styles The style properties.
     */
    static public void setupFillGc(Context context, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        gc.setFillRule(FillRule.EVEN_ODD);
        gc.setFill(styles.backgroundColor);
    }

}
