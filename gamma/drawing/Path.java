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
import gamma.math.Util;
import gamma.value.Coordinate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;

/**
 *
 * @author Antonio Freixas
 */
public class Path
{
    public static void draw(Context context, gamma.value.Path path, boolean closed, boolean stroke, boolean fill, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        gc.beginPath();
        for (int i = 0; i < path.size(); i++) {
            Coordinate coord = path.get(i);
            if (i == 0) {
                gc.moveTo(coord.x, coord.t);
            }
            else {
                gc.lineTo(coord.x, coord.t);
            }
        }
        if (closed) {
            gc.closePath();
        }

       if (stroke) {
           Line.setupLineGc(context, styles);
           gc.stroke();
       }

       // Add arrow heads

       if (stroke && !closed && !styles.arrow.equals("none")) {
           int size = path.size();
           if (size > 2) {
               if (styles.arrow.equals("end") || styles.arrow.equals("both")) {
                   Coordinate start = path.get(size - 2);
                   Coordinate end = path.get(size - 1);
                   double angle = Util.getAngle(start, end);
                   Arrow.draw(context, end, angle, styles);
               }
               if (styles.arrow.equals("start") || styles.arrow.equals("both")) {
                   Coordinate start = path.get(1);
                   Coordinate end = path.get(0);
                   double angle = Util.getAngle(start, end);
                   Arrow.draw(context, end, angle, styles);
               }
           }
       }

       if (fill) {
           setupFillGc(context, styles);
           gc.fill();
       }

        // Restore the original graphics context

        gc.restore();
    }

    public static void setupFillGc(Context context, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        gc.setFillRule(FillRule.EVEN_ODD);
        gc.setFill(styles.javaFXBackgroundColor);
    }

}
