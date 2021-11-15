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
import javafx.scene.text.Font;
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
                            gamma.value.Line.AxisType axisType,
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
            // world to screen units and inverse transform screen to rotated world units

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

            // Set up for tick labels

            String format = null;
            String anchor = null;
            double fontAngle= 0.0;
            int printEvery = 0;
            double pad = 5.0;

            if (styles.tickLabels) {
                format = getTickFormat(firstTick, maxDistance);
                if (angle >= 0) {
                    if (axisType == gamma.value.Line.AxisType.X) {
                        anchor = "TC";
                        fontAngle = -angle;
                    }
                    else {
                        anchor = "MR";
                        fontAngle = -90;
                    }
                }
                else {
                    if (axisType == gamma.value.Line.AxisType.X) {
                        anchor = "TC";
                        fontAngle = -angle;
                    }
                    else {
                        anchor = "MR";
                        fontAngle = 90;
                    }
                }
               if (angle == 90.0) {
                    fontAngle = 90;
                    anchor = "ML";
                    pad = 20.0;
                }

                // Figure out how many labels we should skip depending on label size
                // and tickSpacing

                printEvery = getLabelSkip(firstTick, maxDistance, tickSpacing * tickScale, format, styles.font, viewportScale);
            }

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
                    if (styles.tickLabels) {
                        if (tickNumber % printEvery == 0) {
                            Text.draw(context, x, origin.t, String.format(format, x / tickScale), fontAngle,
                                      styles.javaFXColor, styles.font, pad, anchor);
                        }
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

    private static String getTickFormat(double minValue, double maxValue)
    {
        minValue = Math.abs(minValue);
        maxValue = Math.abs(maxValue);
        minValue = minValue < 2 * Double.MIN_NORMAL ? 0 : Math.floor(Math.log10(minValue));
        maxValue = maxValue < 2 * Double.MIN_NORMAL ? 0 : Math.floor(Math.log10(maxValue));
        int significantDigits = (int)Math.max(Math.abs(minValue), Math.abs(maxValue)) + 1;
        return "%." + significantDigits + "g";
    }

    private static int getLabelSkip(double minValue, double maxValue, double step, String format, Font font, double scale)
    {
        String minString = String.format(format, minValue);
        javafx.geometry.Bounds minBounds = Text.getTextBounds(minString, font);
        String maxString = String.format(format, maxValue);
        javafx.geometry.Bounds maxBounds = Text.getTextBounds(maxString, font);

        double maxWidth = Math.max(minBounds.getWidth(), maxBounds.getWidth()) * scale * 2;
        double s = maxWidth / step;

        int printEvery;
        if (s > 50) printEvery = 100;
        else if (s > 20) printEvery = 50;
        else if (s > 10) printEvery = 20;
        else if (s > 5) printEvery = 10;
        else if (s > 2) printEvery = 5;
        else if (s > 1) printEvery = 2;
        else printEvery = 1;

        return printEvery;

    }

}
