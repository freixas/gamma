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
package org.freixas.gamma.execution.lcode;

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.drawing.Context;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author Antonio Freixas
 */
public class DisplayCommandExec extends CommandExec
{
    private boolean resize;
    private double fixedWidth;
    private double fixedHeight;

    @Override
    public void execute(Context context, Struct cmdStruct, StyleStruct styles)
    {
        Canvas canvas = context.canvas;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // The canvas is only resized if the width and height remain
        // undefined in the structure

        double width;
        double height;
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

            if (resize) {
                width = ((Region)canvas.getParent()).getWidth();
                height = ((Region)canvas.getParent()).getHeight();
            }
            else {
                width = fixedWidth;
                height = fixedHeight;
            }
            canvas.setWidth(width);
            canvas.setHeight(height);

            // Get the new center

            centerX = width / 2.0;
            centerT = height / 2.0;

            Point2D newCenter = gc.getTransform().inverseTransform(centerX, centerT);

            // Determine the difference and use it to restore the original
            // coordinate to the center

            gc.translate(
                newCenter.getX()- originalCenter.getX(),
                newCenter.getY()- originalCenter.getY());

            context.invScale = context.getCurrentInvScale();
            context.bounds = context.getCurrentCanvasBounds();

        }
        catch (NonInvertibleTransformException e)
        {
            throw new ProgrammingException("DisplayCommandExec.execute()", e);
        }

        if (!resize) {
            Region parent = ((Region)canvas.getParent());
            Rectangle clip = (Rectangle)parent.getClip();
            if (clip != null) {
                clip.setWidth(parent.getWidth());
                clip.setHeight(parent.getHeight());
                parent.setClip(clip);
            }

            clip = (Rectangle)canvas.getClip();
            if (clip != null) {
                clip.setWidth(canvas.getWidth());
                clip.setHeight(canvas.getHeight());
                canvas.setClip(clip);
            }
        }

        // Clear the display area

        clearDisplay(context, styles.backgroundColor);
    }

    /**
     * Set up the diagram. Call this the first time the lcode is run.
     *
     * @param context The drawing context.
     * @param struct The display structure.
     * @param styles The styles.
     */
    public void initializeCanvas(Context context, DisplayStruct struct, StyleStruct styles)
    {
        Canvas canvas = context.canvas;

        // *** CANVAS SIZE SETUP ***

        // We need to make sure we size the window properly.
        // We will either find both width and height omitted or only one.
        // If they are both omitted, we will size the drawing area to match
        // the parent; if only one or the other is omitted, we set it to
        // the parent's size.

        double width = struct.width;
        double height = struct.height;
        resize = false;

        // Find out which screen we are on

        Screen screen = context.engine.getWindow().getScreen();
        Rectangle2D bounds = screen.getVisualBounds();

        // Convert width and height to pixels if needed

        double multiplier = 1.0;
        switch (struct.units) {
            case "inches" -> { multiplier = screen.getDpi() * screen.getOutputScaleX(); }
            case "mm" -> { multiplier = screen.getDpi() * screen.getOutputScaleX() / 25.4; }
        }
        if (width != Struct.INT_NOT_SET) {
            width *= multiplier;
            width = Math.min(width, bounds.getWidth() * screen.getOutputScaleX());
        }
        if (height != Struct.INT_NOT_SET) {
            height *= multiplier;
            height = Math.min(height, bounds.getHeight() * screen.getOutputScaleY());
        }

        Region parent = ((Region)canvas.getParent());
        double parentWidth = parent.getWidth();
        double parentHeight = parent.getHeight();

        // Neither width or height specified: set the canvas size equal to
        // its parent's width and height

        if (width == Struct.INT_NOT_SET && height == Struct.INT_NOT_SET) {
            width = parentWidth;
            height = parentHeight;
            resize = true;
        }

        // Only the height was specified: leave the height alone, and set the
        // canvas width equal to the parent's width

        else if (width == Struct.INT_NOT_SET) {
            fixedWidth = width = parentWidth;
            fixedHeight = height;
        }

        // Only the width was specified: leave the width alone, and set the
        // canvas height equal to the parent's height. Then modify the
        // display structure so that the heihgt is fixed from now on

        else if (height == Struct.INT_NOT_SET) {
            fixedWidth = width;
            fixedHeight = height = parentHeight;
        }

        // Both specified

        else {
            fixedWidth = width;
            fixedHeight = height;
        }

        fixedWidth = fixedWidth / screen.getOutputScaleX();
        fixedHeight = fixedHeight / screen.getOutputScaleY();

        // We now know the width and height we want for the canvas, so set it.
        // We should listen only for window resizes to prevent this method
        // from potentially being recursively called, but let's minimize
        // any changes anyway

        if (width != canvas.getWidth()) {
            canvas.setWidth(width);
        }
        if (height != canvas.getHeight()) {
            canvas.setHeight(height);
        }

        // We are not going to resize the canvas. This means that the canvas
        // might exceed its parent's size or it might not fill the parent.
        // Add a clip to the parent so that we don't draw outside its bounds.
        // Add a clip to the canvas so that we don't draw outside its bounds.

        if (!resize) {
            Rectangle clip = new Rectangle();
            clip.setWidth(parent.getWidth());
            clip.setHeight(parent.getHeight());
            parent.setClip(clip);

            clip = new Rectangle();
            clip.setWidth(fixedWidth);
            clip.setHeight(fixedHeight);
            canvas.setClip(clip);
        }
        else {
            parent.setClip(null);
            canvas.setClip(null);
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

        // Set real invScale.
        // The user's invScale is such that number of world units for the lesser of
        // the width or height is (100 / user invScale). A invScale of 1 gives 100
        // units across, a invScale of 2 gives 50 world units, a invScale of .5 gives
        // 200 world units, etc.

        double realScale = Math.min(width, height) / (100.0 / struct.scale);
        gc.scale(realScale, realScale);

        context.invScale = context.getCurrentInvScale();
        context.bounds = context.getCurrentCanvasBounds();
    }

    private final static Affine identifyTransform = new Affine();

    private void clearDisplay(Context context, Color color)
    {
        GraphicsContext gc = context.gc;

        // Currently, there is a bug where, with certain scales and translations,
        // the bounds in world units don't match the bounds in screen
        // unit. This code makes sure the entire canvas is cleared, but
        // the problem remains for other things that depend on knowing the
        // canvas bounds in world units.

        gc.save();

        gc.setTransform(identifyTransform);
        gc.setFill(color);
        context.gc.fillRect(0.0, 0.0, context.canvas.getWidth(), context.canvas.getHeight());

        context.gc.restore();
    }

}
