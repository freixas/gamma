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

import gamma.ProgrammingException;
import gamma.drawing.Context;
import gamma.math.Util;
import gamma.value.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author Antonio Freixas
 */
public class DisplayCommandExec extends CommandExec
{

    @Override
    public void execute(Context context, Struct cmdStruct, StyleStruct styles)
    {
        Canvas canvas = context.canvas;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // The canvas is only resized if the width and height remain
        // undefined in the structure

        double width = ((DisplayStruct)cmdStruct).width;
        double height = ((DisplayStruct)cmdStruct).height;

        if (width == Struct.INT_NOT_SET && height == Struct.INT_NOT_SET) {

            try {
                // When we resize, we want to keep the coordinate at the center
                // of the canvas unchanged (NOTE: I am assuming that a resize
                // leaves the upper left corner's coordinate unchanged

                // Let's get the coordinate at the center

                width = canvas.getWidth();
                height = canvas.getHeight();
                double centerX = width / 2.0;
                double centerT = height / 2.0;

                Point2D originalCenter = gc.getTransform().inverseTransform(centerX, centerT);

                // Now resize

                double parentWidth = ((Region)canvas.getParent()).getWidth();
                double parentHeight = ((Region)canvas.getParent()).getHeight();

                // If we only listen to window resizes, the following statements
                // should not cause any recursion

                canvas.setWidth(parentWidth);
                canvas.setHeight(parentHeight);

                // Get the new center

                centerX = parentWidth / 2.0;
                centerT = parentHeight / 2.0;

                Point2D newCenter = gc.getTransform().inverseTransform(centerX, centerT);

                // Determine the difference and use it to restore the original
                // coordinate to the center

                gc.translate(
                    newCenter.getX()- originalCenter.getX(),
                    newCenter.getY()- originalCenter.getY());
            }
            catch (NonInvertibleTransformException e)
            {
                throw new ProgrammingException("DisplayCommandExec.execute()", e);
            }
        }

        // Clear the display area

        clearDisplay(context, styles.backgroundColor);
    }

    /**
     * Set up the diagram.Call this the first time the lcode is run.
     *
     * @param context The drawing context.
     * @param struct The display structure.
     * @param styles The styles.
     */
    public void initializeCanvas(Context context, DisplayStruct struct, StyleStruct styles)
    {
        Canvas canvas = context.canvas;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // *** CANVAS SIZE SETUP ***

        // We need to make sure we fontSize the window properly.
        // We will either find both width and height omitted or only one.
        // If they are both omitted, we will fontSize the drawing area to match
        // the parent; if only one or the other is omitted, we set it to
        // the parent's fontSize.

        double width = struct.width;
        double height = struct.height;
        boolean resize = false;

        Region parent = ((Region)canvas.getParent());
        double parentWidth = parent.getWidth();
        double parentHeight = parent.getHeight();

        // Neither width or height specified: set the canvas fontSize equal to
        // its parent's width and height

        if (width == Struct.INT_NOT_SET && height == Struct.INT_NOT_SET) {
            width = parentWidth;
            height = parentHeight;
            resize = true;
        }

        // Only the height was specified: leave the height alone, and set the
        // canvas width equal to the parent's width. Then modify the
        // display structure so that the width is fixed from now on

        else if (width == Struct.INT_NOT_SET) {
            width = parentWidth;
            struct.width = Util.toInt(width);
        }

        // Only the width was specified: leave the width alone, and set the
        // canvas height equal to the parent's height. Then modify the
        // display structure so that the heihgt is fixed from now on

        else if (height == Struct.INT_NOT_SET) {
            height = parentHeight;
            struct.height = Util.toInt(height);
        }

        // We now know the width and height we want for the canvas, so set it.
        // We should listen only for window resizes to prevent this method
        // from potentially being recursively called, but let's minimize
        // any changes anyway

        if (width != canvas.getWidth()) {
            canvas.setWidth(width);
            parent.setMinWidth(width);
        }
        if (height != canvas.getHeight()) {
            canvas.setHeight(height);
            parent.setMinHeight(height);
        }

        // If we are going to be resizing, make sure the parent's
        // minimum fontSize is flexible

        if (resize) {
            parent.setMinWidth(1D);
            parent.setMinHeight(1D);
        }

        // *** COORDINATE SYSTEM SETUP ***

        setInitialZoomPan(context, struct);

    }
    /**
     * Set the zoom/pan to the initial values specified by the user.
     * This occurs when the lcodes are first run or whenever the user
     * asks for a reset.
     *
     * @param context The drawing context.
     * @param struct The display structure.
     */
    public void setInitialZoomPan(Context context, DisplayStruct struct)
    {
        GraphicsContext gc = context.gc;
        double width  = context.canvas.getWidth();
        double height = context.canvas.getHeight();

         // Start by resetting the canvas transform to the identify transform

        gc.setTransform(new Affine());

        // Translate (0, 0) to the lower left

        gc.translate(0, height);

        // Reverse the scaling system so the t coordinates go up instead of
        // down

        gc.scale(1.0, -1.0);

        // Now we want to move our origin to the position given by the user,
        // which is specified as a percentage of the width or height

        double originX = (width  / 100.0) * struct.origin.x;
        double originT = (height / 100.0) * struct.origin.t;

        gc.translate(originX, originT);

        // Set real scale.
        // The user's scale is such that number of world units for the lesser of
        // the width or height is (100 / user scale). A scale of 1 gives 100
        // units across, a scale of 2 gives 50 world units, a scale of .5 gives
        // 200 world units, etc.

        double realScale = Math.min(width, height) / (100.0 / struct.scale);
        gc.scale(realScale, realScale);

    }

    private void clearDisplay(Context context, Color backgroundColor)
    {
        Bounds bounds = context.getCanvasBounds();
        context.gc.setFill(backgroundColor.getJavaFXColor());
        context.gc.fillRect(bounds.min.x, bounds.min.t, bounds.getWidth(), bounds.getHeight());
    }

}
