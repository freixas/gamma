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
package gamma.execution.lcode;

import gamma.drawing.Dimension;
import gamma.drawing.T;
import gamma.execution.LCodeEngine;
import gamma.math.Util;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author Antonio Freixas
 */
public class DisplayCommand extends CommandExec
{
    @Override
    public void execute(LCodeEngine engine, Struct cmdStruct, StyleStruct styles)
    {
        // The display command is right after the initial setup, after any
        // zoom/pan event, and after any resize event

        DisplayStruct struct = (DisplayStruct)cmdStruct;

        Canvas canvas = engine.getCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // We need to make sure we size the window properly.
        // We will either find both width and height omitted or only one.
        // If they are both omitted, we will size the drawing area to match
        // the parent; if only one or the other is omitted, we set it to
        // the parent's size.

        double width = struct.width;
        double height = struct.height;

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        if (width == Struct.INT_NOT_SET && height == Struct.INT_NOT_SET) {
            width = canvasWidth;
            height = canvasHeight;
        }
        else if (width == Struct.INT_NOT_SET) {
            width = canvasWidth;
            struct.width = Util.toInt(width);
        }
        else if (height == Struct.INT_NOT_SET) {
            height = canvasHeight;
            struct.height = Util.toInt(height);
        }

        // Make sure the canvas matches

        if (width != canvasWidth) canvas.setWidth(width);
        if (height != canvasHeight) canvas.setHeight(height);

        // At this point, the canvas is at the size we want. Fill it with the
        // background color

        gc.setFill(styles.backgroundColor.getJavaFXColor());
        gc.fillRect(0, 0, width, height);

        // We need to tell the transform engine that the viewport size
        // just changed

        T t = engine.getTransform();
        t.setViewport(new Dimension(width, height));
    }

}
