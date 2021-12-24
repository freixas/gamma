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

import gamma.execution.lcode.AxesStruct;
import gamma.execution.lcode.LabelStruct;
import gamma.execution.lcode.StyleStruct;
import gamma.math.Util;
import gamma.value.Bounds;
import gamma.value.ConcreteLine;
import gamma.value.Coordinate;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;

/**
 *
 * @author Antonio Freixas
 */
public class Axis
{
    static final double MIN_MINOR_TICK_MARK_LENGTH = 3.0;
    static final double MIN_MAJOR_TICK_MARK_LENGTH = 10.0;
    static final double IDEAL_TICK_SPACING = 20.0;

    public static void draw(Context context, AxesStruct struct,
                            AxesStruct.AxisStruct axisStruct, double tickScale,
                            StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        // Set up the gc for line drawing

        Line.setupLineGc(context, styles);

        // Get the values we need

        double v = struct.frame.getV();
        gamma.value.Line line = new ConcreteLine(axisStruct.axisType, struct.frame);

        // *********************************************
        // *** Find out the current invScale.           ***
        // *********************************************

        // We multiply by this invScale to convert a from screen units to
        // world units. We divide by this to go from world units to screen
        // units.

        double viewportScale = context.invScale;

        // *********************************************
        // *** Set up the transformed system.        ***
        // *********************************************

        // Rotate the gc so that our line can be drawn horizontal. After
        // the rotation is applied, the gc transform will transform rotated
        // world to screen units and inverse transform screen to rotated world units

        double angle = line.getAngle();
        Coordinate origin = line.getCoordinate();
        Affine originalTransform = gc.getTransform();
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
            struct.positiveOnly && v >= 0.0 ? origin.x : Double.NEGATIVE_INFINITY,
            origin.t - halfHeightMajor,
            struct.positiveOnly && v < 0.0 ? origin.x : Double.POSITIVE_INFINITY,
            origin.t + halfHeightMajor);

        Bounds rotatedBounds = context.getCurrentCanvasBounds();
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

        // Get the world space units per ideal tick spacing in pixels

        double worldUnitsPerIdealPixelSpacing = IDEAL_TICK_SPACING * viewportScale;

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

        double worldTickThickness = viewportScale * styles.divThickness;
        double worldMajorTickThickness = viewportScale * styles.majorDivThickness;

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

        gc.setStroke(styles.javaFXDivColor);
        gc.setLineWidth(worldTickThickness);

        // If we only print out one of the axes, don't skip labeling 0

        boolean skipZero = struct.x && struct.t;

        double x;
        int tickCount;
        for (x = firstTick, tickCount = tickNumber; x <= maxDistance; x += tickSpacing, tickCount++) {
            if (tickCount != 0 || !skipZero) {

                // Major tick

                if (tickCount % 10 == 0) {
                    gc.setStroke(styles.javaFXMajorDivColor);
                    gc.setLineWidth(worldMajorTickThickness);
                    gc.strokeLine(x, origin.t - halfHeightMajor, x, origin.t + halfHeightMajor);
                    gc.setStroke(styles.javaFXMajorDivColor);
                    gc.setLineWidth(worldMajorTickThickness);
                }

                // Minor tick

                else {
                    gc.strokeLine(x, origin.t - halfHeightMinor, x, origin.t + halfHeightMinor);
                }
            }
        }

        // *********************************************
        // *** Draw the tick labels.                 ***
        // *********************************************

        if (!styles.tickLabels) {
            gc.restore();
            return;
        }

        LabelStruct labelStruct = new LabelStruct();
        double savedTextRotation = styles.textRotation;
        double savedTextPadding = styles.textPadding;
        String savedTextAnchor = styles.textAnchor;

        // The text system has problems with rotations in the graphics
        // context combined with scaling the Y axis by -1. So if we have tick
        // labels, we need to undo the rotation

        gc.setTransform(originalTransform);

        // We need to set up a new transform, one that takes rotated
        // world units and transforms them to unrotated world units. We
        // need this because the line is still in rotated world units

        Affine revRotation = new Affine();
        revRotation.appendRotation(angle, origin.x, origin.t);

        // Set up the tick labels

        String format;
        String anchorPlus;
        String anchorMinus;
        int printEvery;

        // Half height is half the height of a major tickmark in screen
        // units

        double pad = halfHeight + 2.0;

        format = getTickFormat(firstTick, maxDistance, tickSpacing);

        if (v >= 0.0) {
            if (axisStruct.axisType == gamma.value.Line.AxisType.X) {
                if (angle <= 22.5) {
                    anchorPlus = "TC";      // Horz to slightly up CCW
                    anchorMinus = "TC";
                }
                else {
                    anchorPlus = "TL";      // Closer to +45
                     anchorMinus = "BR";
               }
            }
            else /* T axis */ {
                if (angle <= 67.5)  {
                    anchorPlus = "BR";      // Closer to +45
                    anchorMinus = "TL";
                }
                else {
                    anchorPlus = "MR";      // Vert to slighty down CW
                    anchorMinus = "MR";
                }
            }
        }
        else /* v < 0 */ {
            if (axisStruct.axisType == gamma.value.Line.AxisType.X) {
                if (angle >= -22.5) {
                    anchorPlus = "TC";      // Horz to slightly down CW
                    anchorMinus = "TC";
                }
                else {
                    anchorPlus = "BL";      // Closer to -45
                    anchorMinus = "TR";
                }
            }
            else /* T axis */ {
                if (angle >= -67.5)  {
                    anchorPlus = "BL";      // Closer to -45
                    anchorMinus = "TR";
                }
                else {
                    anchorPlus = "MR";      // Vert to slightly down CCW
                    anchorMinus = "MR";
                }
            }
        }

        // Figure out how many labels we should skip depending on label size
        // and tickSpacing

        printEvery = getLabelSkip(firstTick, maxDistance, tickSpacing * tickScale, format, styles.font, viewportScale);
        styles.textRotation = 0.0;
        styles.textPadding = pad;

        for (x = firstTick, tickCount = tickNumber; x <= maxDistance; x += tickSpacing, tickCount++) {
            if (tickCount != 0 || !skipZero) {
                if (tickCount % printEvery == 0) {
                    double tickValue = (x - origin.x) / tickScale;
                    if (v < 0 && axisStruct.axisType == gamma.value.Line.AxisType.T) tickValue = -tickValue;
                    Point2D pos1 = revRotation.transform(x, origin.t);

                    labelStruct.location = new Coordinate(pos1.getX(), pos1.getY());
                    labelStruct.text = String.format(format, tickValue);

                    styles.textAnchor = tickValue >= 0 ? anchorPlus : anchorMinus;
                    Label.draw(context, labelStruct, styles);
                }
            }
        }

        // Restore the original graphics context

        styles.textRotation = savedTextRotation;
        styles.textPadding = savedTextPadding;
        styles.textAnchor = savedTextAnchor;
        gc.restore();
    }

    /**
     * Determine the number of significant digits needed to display a range
     * from min to max with steps of delta. There should be enough digits so
     * no two adjacent steps display the same value.
     *
     * @param minValue The smallest value of the range.
     * @param maxValue The largest value of the range.
     * @param delta The step size.
     *
     * @return A format string which can be used to produce values with the
     * given number of significant digits (using String.format()).
     */
    private static String getTickFormat(double minValue, double maxValue, double delta)
    {
        minValue = Math.abs(minValue);
        maxValue = Math.abs(maxValue);
        delta = Math.abs(delta);

        minValue = Util.fuzzyEQ(minValue, 0.0) ? 0 : Math.floor(Math.log10(minValue));
        maxValue = Util.fuzzyEQ(maxValue, 0.0) ? 0 : Math.floor(Math.log10(maxValue));
        delta =    Util.fuzzyEQ(delta, 0.0)    ? 0 : Math.floor(Math.log10(delta));

//        if (minValue >= 0) minValue += 1;
//        if (maxValue >= 0) maxValue += 1;
//        if (delta >= 0) delta += 1;

        int sig1 = (int)(Math.abs(minValue - delta)) + 1;
        int sig2 = (int)(Math.abs(maxValue - delta)) + 1;
        int significantDigits = Math.max(sig1, sig2);

        int mostSignificantDigit = (int)Math.max(minValue, maxValue);
        if (mostSignificantDigit >= 0) mostSignificantDigit += 1;

        int eFormatChars = significantDigits + 6;
        int fFormatChars;
        String fFormat;

        if (mostSignificantDigit > 0) {
            if (mostSignificantDigit >= significantDigits) {
                fFormatChars = mostSignificantDigit;
                fFormat = "%" + fFormatChars + ".0f";
            }
            else {
                fFormatChars = mostSignificantDigit + 1;
                fFormat = "%" + fFormatChars  + "." + (significantDigits - mostSignificantDigit) + "f";
            }
        }
        else {
            fFormatChars = -mostSignificantDigit + significantDigits + 1;
            fFormat = "%" + fFormatChars + "." + (fFormatChars - 2) + "f";
        }

        if (eFormatChars < fFormatChars) {
            return "%" + eFormatChars + "." + (significantDigits - 1) + "e";
        }
        return fFormat;
    }

    /**
     * Given a range, determine how many values to skip so that side-by-side
     * tick mark labels don't overlap. This method could be improved to deal
     * with rotated labels (or non-rotated labels displayed along a rotated
     * axis), but with some loss of performance and symmetry.
     *
     * @param minValue The smallest value of the range.
     * @param maxValue The largest value of the range.
     * @param delta The delta size.
     * @param format The format string by which to convert numbers to string.
     * @param font The font which will be used to display the string.
     * @param scale The invScale to use to convert screen units to world units.
     *
     * @return A number that can be used with the remainder operator to determine
     * whether to display or skip a particular tick mark.
     */
    private static int getLabelSkip(
        double minValue, double maxValue, double delta, String format, Font font, double scale)
    {
        String minString = String.format(format, minValue);
        javafx.geometry.Bounds minBounds = Label.getTextBounds(minString, font);
        String maxString = String.format(format, maxValue);
        javafx.geometry.Bounds maxBounds = Label.getTextBounds(maxString, font);

        double maxWidth = Math.max(minBounds.getWidth(), maxBounds.getWidth()) * scale * 2;
        double s = maxWidth / delta;

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
