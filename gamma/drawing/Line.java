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

import customfx.ResizableCanvas;
import gamma.execution.lcode.StyleStruct;
import gamma.value.LineSegment;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author Antonio Freixas
 */
public class Line
{
    static public void draw(Context context, Node clip, LineSegment segment, double thickness, StyleStruct styles)
    {
        ResizableCanvas canvas = context.canvas;
        GraphicsContext gContext = canvas.getGraphicsContext2D();
        gContext.save();
        Node savedClip = canvas.getClip();

        if (clip != null) {
            canvas.setClip(clip);
        }

        // For now, we'll assume the stroke style is CENTER
        gContext.setStroke(styles.color.getJavaFXColor());
        gContext.setLineWidth(thickness);
        if (styles.lineStyle.equals("dashed")) {
            gContext.setLineDashes(5.0, 5.0);
        }
        else if (styles.lineStyle.equals("dotted")) {
            gContext.setLineCap(StrokeLineCap.ROUND);
            gContext.setLineDashes(1.0, 1.0);
        }

        gContext.strokeLine(
            segment.getPoint1().x, segment.getPoint1().t,
            segment.getPoint2().x, segment.getPoint2().t);

        canvas.setClip(savedClip);
        gContext.restore();
    }

}
