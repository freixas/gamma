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

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.execution.LCodeEngine;
import org.freixas.gamma.value.Bounds;
import org.freixas.gamma.value.Coordinate;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 * We need to pass around the context for drawing, which consists of
 * various things, some of which may need to be added in the future. We
 * encapsulate all these values in this class.
 *
 * @author Antonio Freixas
 */
public class Context
{
    public final LCodeEngine engine;
    public final Canvas canvas;
    public final GraphicsContext gc;

    public double invScale;
    public Bounds bounds;

    public Context(LCodeEngine engine, Canvas canvas)
    {
        this.engine = engine;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        this.invScale = getCurrentInvScale();
        this.bounds = getCurrentCanvasBounds();
    }

    /**
     * Return the inverse scale for the canvas. Normal scaling goes from world
     * units to screen units. Multiply a screen length by the inverse scale to
     * get corresponding world unit length.
     * <p>
     * This scale value is maintained by the zoom and pan methods and may not
     * match the actual canvas bounds if local transforms have been applied. Use
     * getCurrentInvScale in those situations.
     *
     * @return The canvas inverse scale.
     */
    public final double getInvScale()
    {
        return invScale;
    }

    /**
     * Return the inverse scale for the canvas. Normal scaling goes from world
     * units to screen units. Multiply a screen length by the inverse scale to
     * get corresponding world unit length.
     * <p>
     * This returns a scale based on all the transforms that have been applied
     * to whatever is the current graphics context. It does not work if any
     * rotations have been applied.
     *
     * @return The canvas inverse scale.
     */
    public final double getCurrentInvScale()
    {
        try {
            Point2D point = gc.getTransform().inverseDeltaTransform(1D, 0D);
            return point.getX();
        }
        catch (NonInvertibleTransformException e) {
            throw new ProgrammingException("Context.getCurrentScale()", e);
        }
    }

    /**
     * Return the inverse scale for the canvas. Normal scaling goes from world
     * units to screen units. Multiply a screen length by the inverse scale to
     * get corresponding world unit length.
     * <p>
     * This returns a scale based on all the transforms that have been applied
     * to whatever is the current graphics context. This version works even if
     * the graphics context has been rotated. However, it assumes the magnitude
     * of the x and y scaling is the same.
     *
     * @return The canvas inverse scale.
     */
    public final double getCurrentRotatedInvScale()
    {
        try {
            Point2D point = gc.getTransform().inverseDeltaTransform(1D, 0D);
            double a = point.getX();
            double b = point.getY();
            return Math.sqrt(a * a + b * b);
        }
        catch (NonInvertibleTransformException e) {
            throw new ProgrammingException("Context.getCurrentScale()", e);
        }
    }

    /**
     * Get a bounding box for the canvas in world units. This is maintained
     * by the zoom and pan methods and may not match the actual canvas bounds if
     * local transforms have been applied.
     *
     * @return The canvas curBounds.
     */
    public final Bounds getCanvasBounds()
    {
        return bounds;
    }

    /**
     * Get a bounding box for the canvas in world units. This returns
     * the bounding box that includes all the transforms that have been applied
     * to whatever is the current graphics context.
     *
     * @return The bounding box for the canvas in world units.
     */
    public final Bounds getCurrentCanvasBounds()
    {
        try {
            // The bounding box in screen units

            Bounds screenBounds = new Bounds(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

            // The inverse transform goes from screen units to world units

            Affine inverse = gc.getTransform().createInverse();
            Bounds worldBounds = screenBounds.transform(inverse);
            return worldBounds;
        }
        catch (NonInvertibleTransformException e) {
            throw new ProgrammingException("Context.getCanvasBounds()", e);
        }
    }

}
