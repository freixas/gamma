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

import gamma.ProgrammingException;
import gamma.execution.lcode.StyleStruct;
import gamma.math.Util;
import gamma.value.Bounds;
import gamma.value.Coordinate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author Antonio Freixas
 */
public class Axis
{
    static final double MIN_MINOR_TICK_MARK_LENGTH = 3.0;
    static final double MIN_MAJOR_TICK_MARK_LENGTH = 10.0;
    static final double IDEAL_TICK_SPACING = 10.0;

    public static void draw(Context context, double v, gamma.value.Line line,
                            double tickScale, boolean positiveOnly, String xLabel,
                            StyleStruct styles)
    {
        try {
            GraphicsContext gc = context.gc;

            // Save the current graphics context

            gc.save();

            // Set up the gc for line drawing

            Line.setupLineGc(context, styles);

            // *********************************************
            // *** Find out the current scale.           ***
            // *********************************************

            // We multiply by this scale to convert a from screen units to
            // world units. We divide by this to go from world units to screen
            // units.

            double viewportScale = gc.getTransform().inverseDeltaTransform(1.0, 0).getX();

            // *********************************************
            // *** Set up the transformed system.        ***
            // *********************************************

            // Rotate the gc so that our line can be drawn horizontal. After
            // the rotation is applied, the gc transform will transform rotated
            // world to screen units and screen to rotated world units

            double angle = line.getAngle();
            Coordinate origin = line.getCoordinate();
            Affine rotTransform = gc.getTransform();
            rotTransform.appendRotation(angle, origin.x, origin.t);
            gc.setTransform(rotTransform);

            // *********************************************
            // *** Intersect the axis with the viewport. ***
            // *********************************************

            // A line has some thickness. If we have tick marks, we can think of
            // the line is being thicker, so we can apply the same algorithm
            // whether a line has tick marks or not.

            double height = styles.lineThickness;

            // If we have tick marks, the height increases

            if (styles.ticks) {
                height += MIN_MAJOR_TICK_MARK_LENGTH;
            }

            // The thickness of the line is evenly distributed to either side

            double halfHeight = height / 2.0;

            // Convert this from pixel coordinates to world unit coordinates

            double halfHeightMajor = viewportScale * halfHeight;

            // Create a bounding box for the line. The bounds should be
            // specified as though the line has been rotated the same
            // as the graphics context. This means we need to transform our
            // origin to screen units and then inverse transform to rotated
            // world units

            Bounds lineBounds = new Bounds(
                positiveOnly && v >= 0.0 ? origin.x : Double.NEGATIVE_INFINITY,
                origin.t - halfHeightMajor,
                positiveOnly && v < 0.0 ? origin.x : Double.POSITIVE_INFINITY,
                origin.t + halfHeightMajor);

            Bounds rotatedBounds = context.getCanvasBounds();
            Bounds intersection = lineBounds.intersect(rotatedBounds);

            // If there is no intersection, we're done

            if (intersection == null) {
                gc.restore();
                return;
            }

            // *********************************************
            // *** Draw the line.                        ***
            // *********************************************

            // The line is in the bounding box. Draw it

            gc.strokeLine(intersection.min.x, origin.t, intersection.max.x, origin.t);

            // *********************************************
            // *** Draw the tick marks.                  ***
            // *********************************************

            // Check for tick marks

            if (!styles.ticks) {
                gc.restore();
                return;
            }

            // Calculate the pixels per world space units

            double pixelWidth = context.canvas.getWidth();
            double worldUnitsWidth = context.getCanvasBounds().getWidth();
            double pixelsPerWorldUnit = pixelWidth / worldUnitsWidth;

            // Get the world space units per ideal tick spacing in pixels

            double worldUnitsPerIdealPixelSpacing = IDEAL_TICK_SPACING / pixelsPerWorldUnit;

            // We try to get as close to the ideal spacing as we can, while
            // keeping the units to powers of 10, The tick spacing is in
            // world units

            double tickSpacing = Math.pow(10, Math.ceil(Math.log10(worldUnitsPerIdealPixelSpacing)));

            // The faster the velocity, the further apart the tick marks are.
            // For v = 0, tickScale = 1.0
            // For v = 1, tickScale = + infinity

            tickSpacing *= tickScale;

            // We need to widen the intersection box to account for the
            // width of the the tick marks. Note that the width is in pixel
            // space but the intersection is in world space

            double worldTickThickness = viewportScale * styles.tickThickness;
            double worldMajorTickThickness = viewportScale * styles.majorTickThickness;

            double padding = Math.max(worldTickThickness, worldMajorTickThickness) / 2.0;
            padding *= viewportScale;

            intersection.min.x -= padding;
            intersection.max.x += padding;

            // We'll draw from the minimum of the intersection box to the
            // maximum

            double halfHeightMinor =
                viewportScale * (styles.lineThickness + MIN_MINOR_TICK_MARK_LENGTH) / 2;

            // Determine the first tick mark and its value

            double minDistance = intersection.min.x - origin.x;
            double maxDistance = intersection.max.x;

            double firstTick = minDistance - (minDistance % tickSpacing);
            int tickNumber = Util.toInt(firstTick / tickSpacing);
            firstTick += origin.x;

            gc.setLineWidth(worldTickThickness);
            for (double x = firstTick; x <= maxDistance; x += tickSpacing, tickNumber++) {
                if (tickNumber != 0) {

                    // Major tick

                    if (tickNumber % 10 == 0.0) {
                        gc.setLineWidth(worldMajorTickThickness);
                        gc.strokeLine(x, origin.t - halfHeightMajor, x, origin.t + halfHeightMajor);
                        gc.setLineWidth(worldTickThickness);
                    }

                    // Minor tick

                    else {
                        gc.strokeLine(x, origin.t - halfHeightMinor, x, origin.t + halfHeightMinor);
                    }
                }
            }

            // Restore the original graphics context

            gc.restore();
        }
        catch (NonInvertibleTransformException e)
        {
            throw new ProgrammingException("Axis.draw()", e);
        }
    }
}
