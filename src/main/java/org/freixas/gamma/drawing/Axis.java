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

import javafx.geometry.Dimension2D;
import org.freixas.gamma.css.value.StyleProperties;
import org.freixas.gamma.execution.lcode.AxesStruct;
import org.freixas.gamma.execution.lcode.LabelStruct;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Bounds;
import org.freixas.gamma.value.ConcreteLine;
import org.freixas.gamma.value.Coordinate;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import org.freixas.gamma.value.LineSegment;

/**
 *
 * @author Antonio Freixas
 */
public class Axis
{
    static final double IDEAL_TICK_SPACING = 20.0;

    public static void draw(Context context, AxesStruct struct,
                            AxesStruct.AxisStruct axisStruct, double tickScale,
                            StyleStruct styles)
    {
        GraphicsContext gc = context.gc;
        boolean isXAxis = axisStruct.axisType == org.freixas.gamma.value.Line.AxisType.X;

        // Save the current graphics context

        gc.save();

        // Set up the gc for line drawing

        Color color;
        double lineThickness;
        StyleProperties.LineStyle lineStyle;

        if (isXAxis) {
            color = styles.xColor;
            lineThickness = styles.xLineThickness;
            lineStyle = styles.xLineStyle;
        }
        else {
            color = styles.tColor;
            lineThickness = styles.tLineThickness;
            lineStyle = styles.tLineStyle;
        }
        Line.setupLineGc(context, color, lineThickness, lineStyle);

        // Get the values we need

        double v = struct.frame.getV();
        org.freixas.gamma.value.Line line = new ConcreteLine(axisStruct.axisType, struct.frame);

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

        double height = lineThickness;

        // If we have tick marks, the height increases

        if ((isXAxis && styles.xTicks) || (!isXAxis && styles.tTicks)) {
            height += Math.max(styles.majorTickLength, styles.tickLength);
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

        double tickSpacing = 0;
        double minDistance;
        double maxDistance = 0;
        int tickNumber = 0;
        double firstTick = 0;
        double x;
        boolean drawBothAxes = false;
        int tickCount;

        if ((isXAxis && styles.xTicks) || (!isXAxis && styles.tTicks)) {

            Color divColor ;
            Color majorDivColor;
            double divLineThickness;
            double majorDivLineThickness;
            StyleProperties.LineStyle divLineStyle;
            StyleProperties.LineStyle majorDivLineStyle;

            if (isXAxis) {
                divColor = styles.xDivColor;
                majorDivColor = styles.xMajorDivColor;
                divLineThickness = styles.xDivLineThickness;
                majorDivLineThickness = styles.xMajorDivLineThickness;
                divLineStyle = styles.xDivLineStyle;
                majorDivLineStyle = styles.xMajorDivLineStyle;
            }
            else {
                divColor = styles.tDivColor;
                majorDivColor = styles.tMajorDivColor;
                divLineThickness = styles.tDivLineThickness;
                majorDivLineThickness = styles.tMajorDivLineThickness;
                divLineStyle = styles.tDivLineStyle;
                majorDivLineStyle = styles.tMajorDivLineStyle;
            }

            Line.setupLineGc(context, divColor, divLineThickness, divLineStyle);

            boolean majorIsDifferent =
                !divColor.equals(majorDivColor) || divLineThickness != majorDivLineThickness ||
                divLineStyle != majorDivLineStyle;

            // Get the world space units per ideal tick spacing in pixels

            double worldUnitsPerIdealPixelSpacing = IDEAL_TICK_SPACING * viewportScale;

            // We try to get as close to the ideal spacing as we can, while
            // keeping the units to powers of 10, The tick spacing is in
            // world units

            tickSpacing = Math.pow(10, Math.ceil(Math.log10(worldUnitsPerIdealPixelSpacing)));

            // The faster the velocity, the further apart the tick marks are.
            // For v = 0, tickScale = 1.0
            // For v = 1, tickScale = + infinity

            tickSpacing *= tickScale;

            // We need to widen the intersection box to account for the
            // width of the tick marks. Note that the width is in pixel
            // space but the intersection is in world space

            double worldTickThickness = viewportScale * divLineThickness;
            double worldMajorTickThickness = viewportScale * majorDivLineThickness;

            double padding = Math.max(worldTickThickness, worldMajorTickThickness) / 2.0;
            padding *= viewportScale;

            intersection.min.x -= padding;
            intersection.max.x += padding;

            // We'll draw from the minimum of the intersection box to the
            // maximum

            double halfHeightMinor =
                viewportScale * (lineThickness + styles.tickLength) / 2;

            // Determine the first tick mark and its value

            minDistance = intersection.min.x - origin.x;
            maxDistance = intersection.max.x;

            firstTick = minDistance - (minDistance % tickSpacing);
            tickNumber = Util.toInt(firstTick / tickSpacing);
            firstTick += origin.x;

            gc.setLineCap(StrokeLineCap.BUTT);
            Line.setupLineGc(context, divColor, divLineThickness, divLineStyle);

            // There are a few things we do differently if we draw both axes or
            // only draw one

            drawBothAxes = struct.x && struct.t;

            for (x = firstTick, tickCount = tickNumber; x <= maxDistance; x += tickSpacing, tickCount++) {
                if (tickCount != 0 || !drawBothAxes) {

                    // Major tick

                    if (tickCount % 10 == 0) {
                        if (majorIsDifferent) {
                            gc.setLineCap(StrokeLineCap.BUTT);
                            Line.setupLineGc(context, majorDivColor, majorDivLineThickness, majorDivLineStyle);
                            gc.strokeLine(x, origin.t - halfHeightMajor, x, origin.t + halfHeightMajor);
                            gc.setLineCap(StrokeLineCap.BUTT);
                            Line.setupLineGc(context, divColor, divLineThickness, divLineStyle);
                        }
                        else {
                            gc.strokeLine(x, origin.t - halfHeightMajor, x, origin.t + halfHeightMajor);
                        }
                    }

                    // Minor tick

                    else {
                        gc.strokeLine(x, origin.t - halfHeightMinor, x, origin.t + halfHeightMinor);
                    }
                }
            }
        }

        // *********************************************
        // *** Set up for drawing text.              ***
        // *********************************************

        String labelText;
        boolean drawTickLabels;

        if (isXAxis) {
            labelText = struct.xLabel;
            drawTickLabels = styles.xTicks && styles.xTickLabels;
        }
        else {
            labelText = struct.tLabel;
            drawTickLabels = styles.tTicks && styles.tTickLabels;
        }
        boolean drawLabels = labelText != null && labelText.length() > 0;


        if (!drawTickLabels && ! drawLabels) {
            gc.restore();
            return;
        }

        // The text system has problems with rotations in the graphics
        // context combined with scaling the Y axis by -1. So if we have any
        // labels, we need to undo the rotation

        gc.setTransform(originalTransform);

        // We need to set up a new transform, one that takes rotated
        // world units (the line is horizontal) and transforms them to unrotated
        // world units (the line is angled). We need this because the line is
        // still in rotated world units

        Affine revRotation = new Affine();
        revRotation.appendRotation(angle, origin.x, origin.t);

        Color textColor;
        Font font;
        LabelStruct labelStruct = new LabelStruct();

        double savedTextRotation = styles.textRotation;
        double savedTextPaddingTop = styles.textPaddingTop;
        double savedTextPaddingBottom = styles.textPaddingBottom;
        double savedTextPaddingLeft = styles.textPaddingLeft;
        double savedTextPaddingRight = styles.textPaddingRight;
        StyleProperties.TextAnchor savedTextAnchor = styles.textAnchor;

        Dimension2D maxTickDimension;
        StyleProperties.TextAnchor anchorPlus;
        StyleProperties.TextAnchor anchorMinus;

        // If we have axes labels, we will place them on the side opposite the
        // tick mark labels. These variables will record which side the tick
        // mark labels are on for positive coordinates and negative coordinates.
        // For the t axis, below means to the right

        boolean positiveTickLabelsBelow = true;
        boolean negativeTickLabelsBelow = true;

        // *********************************************
        // *** Draw the tick labels.                 ***
        // *********************************************

        if (drawTickLabels) {

            if (isXAxis) {
                textColor = styles.xTextColor;
                font = styles.xTickFont;
            }
            else {
                textColor = styles.tTextColor;
                font = styles.tTickFont;
            }

            // Set up the tick labels

            String format;
            int printEvery;

            // Half height is half the height of a largest tick mark in screen
            // units

            double pad = halfHeight + 2.0;

            format = getTickFormat(firstTick, maxDistance, tickSpacing);

            if (v >= 0.0) {
                if (isXAxis) {
                    if (angle <= 22.5) {
                        anchorPlus =                                      // Horz to slightly up CCW
                            anchorMinus = StyleProperties.TextAnchor.TC;
                        positiveTickLabelsBelow =
                            negativeTickLabelsBelow = true;
                    }
                    else {
                        anchorPlus = StyleProperties.TextAnchor.TL;      // Closer to +45
                        anchorMinus = StyleProperties.TextAnchor.BR;
                        positiveTickLabelsBelow = true;
                        negativeTickLabelsBelow = false;
                   }
                }
                else /* T axis */ {
                    if (angle <= 67.5)  {
                        anchorPlus = StyleProperties.TextAnchor.BR;      // Closer to +45
                        anchorMinus = StyleProperties.TextAnchor.TL;
                        positiveTickLabelsBelow = false;
                        negativeTickLabelsBelow = true;
                    }
                    else {
                        anchorPlus =                                     // Vert to slightly down CW
                        anchorMinus = StyleProperties.TextAnchor.MR;
                        positiveTickLabelsBelow =
                            negativeTickLabelsBelow = true;
                    }
                }
            }
            else /* v < 0 */ {
                if (isXAxis) {
                    if (angle >= -22.5) {
                        anchorPlus =                                      // Horz to slightly down CW
                        anchorMinus = StyleProperties.TextAnchor.TC;
                        positiveTickLabelsBelow =
                            negativeTickLabelsBelow = true;
                    }
                    else {
                        anchorPlus = StyleProperties.TextAnchor.BL;      // Closer to -45
                        anchorMinus = StyleProperties.TextAnchor.TR;
                        positiveTickLabelsBelow = false;
                        negativeTickLabelsBelow = true;
                    }
                }
                else /* T axis */ {
                    if (angle >= -67.5)  {
                        anchorPlus = StyleProperties.TextAnchor.BL;      // Closer to -45
                        anchorMinus = StyleProperties.TextAnchor.TR;
                        positiveTickLabelsBelow = true;
                        negativeTickLabelsBelow = false;
                    }
                    else {
                        anchorPlus =                                     // Vert to slightly down CCW
                        anchorMinus = StyleProperties.TextAnchor.MR;
                        positiveTickLabelsBelow =
                            negativeTickLabelsBelow = false;
                    }
                }
            }

            // Figure out how many labels we should skip depending on label size
            // and tickSpacing

            maxTickDimension = getMaxTickDimensions(firstTick, maxDistance, format, font);
            printEvery = getLabelSkip(maxTickDimension, tickSpacing * tickScale, viewportScale);

            styles.textRotation = 0.0;
            styles.textPaddingTop = pad;
            styles.textPaddingBottom = pad;
            styles.textPaddingLeft = pad;
            styles.textPaddingRight = pad;

            for (x = firstTick, tickCount = tickNumber; x <= maxDistance; x += tickSpacing, tickCount++) {
                if (tickCount != 0 || !drawBothAxes) {
                    if (tickCount % printEvery == 0) {
                        double tickValue = (x - origin.x) / tickScale;
                        if (v < 0 && axisStruct.axisType == org.freixas.gamma.value.Line.AxisType.T) tickValue = -tickValue;
                        Point2D pos1 = revRotation.transform(x, origin.t);

                        labelStruct.location = new Coordinate(pos1.getX(), pos1.getY());
                        labelStruct.text = String.format(format, tickValue);

                        styles.textAnchor = tickValue >= 0 || !drawBothAxes ? anchorPlus : anchorMinus;
                        Label.draw(context, labelStruct, textColor, font, styles);
                    }
                }
            }
        }

        // *********************************************
        // *** Draw the axis label.                  ***
        // *********************************************

        if (drawLabels) {

            // The position is on the right edge of the axis line. If the axis
            // line is off-screen, skip drawing the label

            Bounds bounds = context.getCanvasBounds();
            LineSegment segment = line.intersect(bounds);
            if (segment != null) {

                // The line segment we just created is the exact portion of the
                // axis line that crosses the viewport. It is angled normally.
                // We need to take this segment of the axis and rotate it so
                // that it's horizontal

                Affine rotation = new Affine();
                rotation.appendRotation(-angle, origin.x, origin.t);

                Point2D p1 = rotation.transform(segment.getPoint1().x, segment.getPoint1().t);
                Point2D p2 = rotation.transform(segment.getPoint2().x, segment.getPoint2().t);

                // Convert these from Point2Ds to Coordinates  and sort them
                // so that the second point has the greater X value

                Coordinate leftEndPoint;
                Coordinate rightEndPoint;

                if (p1.getX() < p2.getX()) {
                    leftEndPoint = new Coordinate(p1);
                    rightEndPoint = new Coordinate(p2);
                }
                else {
                    leftEndPoint = new Coordinate(p2);
                    rightEndPoint = new Coordinate(p1);
                }

                // We need to know if the right/top side of the axis is positive
                // or negative. This will affect where we place the labels

                boolean positiveEndShowing;
                if (isXAxis) {
                    positiveEndShowing = rightEndPoint.t >= 0;
                }
                else {
                    positiveEndShowing =
                        Math.max(leftEndPoint.t, rightEndPoint.t) >= 0;
                }

                // Set up the label text

                labelStruct.text = labelText;

                // Set up the label color and font

                textColor = styles.textColor;
                font = styles.font;

               // Disable padding--we're going to precisely place the label
                // ourselves

                styles.textPaddingTop =
                    styles.textPaddingBottom =
                    styles.textPaddingLeft =
                    styles.textPaddingRight = 0;

                // Get the dimensions of the text string in world units

                javafx.geometry.Bounds labelBounds2D = Label.getTextBounds(labelText, font);

                // Create a bounding box for the initial position we want for
                // the label. This bounding box is in rotated space, where the
                // axis is horizontal.
                //
                // If there are tick labels, we will place the axis labels on
                // the opposite side

                Bounds labelBounds;

                if (isXAxis) {

                    // The label's bounding box has its right edge at the right
                    // edge of the axis

                    if (posi)

                    styles.textAnchor = StyleProperties.TextAnchor.TR;
                    labelBounds = new Bounds(
                        new Coordinate(rightEndPoint.x - (labelBounds2D.getWidth() * viewportScale), rightEndPoint.t - ((5 + halfHeight + labelBounds2D.getHeight()) * viewportScale)),
                        new Coordinate(rightEndPoint.x, rightEndPoint.t - (5 + halfHeight) * viewportScale)
                    );
                }

                // The t axis is more complicated. If the velocity is negative,
                // the rotation puts the label we want on the left side, not the
                // right

                else {

                    // The label's bounding box has its right edge at the right
                    // edge of the axis and its bottom edge above the axis

                    styles.textAnchor = StyleProperties.TextAnchor.BR;
                    if (angle >= 0.0) {
                        labelBounds = new Bounds(
                            new Coordinate(rightEndPoint.x - (labelBounds2D.getWidth() * viewportScale), rightEndPoint.t + (5 + halfHeight) * viewportScale),
                            new Coordinate(rightEndPoint.x, rightEndPoint.t + ((5 + halfHeight + labelBounds2D.getHeight()) * viewportScale))
                        );
                    }
                    else {

                        // The label's bounding box has its left edge at the left
                        // edge of the axis and its top edge below the axis

                        styles.textAnchor = StyleProperties.TextAnchor.BL;
                        labelBounds = new Bounds(
                            new Coordinate(leftEndPoint.x, leftEndPoint.t - ((5 + halfHeight + labelBounds2D.getHeight()) * viewportScale)),
                            new Coordinate(leftEndPoint.x + (labelBounds2D.getWidth() * viewportScale), leftEndPoint.t - (5 + halfHeight) * viewportScale)
                        );
                    }
                }

                // debugDrawBounds(context, labelBounds, revRotation, Color.RED);

                // We've placed the label boundary right up against the left or
                // right side of the axis segment. That's too tight and could go
                // off the screen once the label is rotated.
                //
                // We'll create bounds that are just a bit smaller than the
                // viewport and adjust the label bounds to fit completely
                // within. Since our label bounds are rotated, we will need
                // to rotate the viewport bounds to match.
                //
                // The edges we need to compare against depend on the axis and
                // its angle

                double labelRightEdge  = bounds.max.x - 10 * viewportScale;
                double labelTopEdge    = bounds.max.t - 10 * viewportScale;
                double labelLeftEdge   = bounds.min.x + 10 * viewportScale;
                double labelBottomEdge = bounds.min.t + 10 * viewportScale;

                // We create special label bounds that extend a long ways in
                // both the left and right sides. Since we will intersect the
                // sides with the bounds, this lets us find the optimal position
                // to place the label

                Bounds extendedLabelBounds = new Bounds(-Double.MAX_VALUE, labelBounds.min.t, Double.MAX_VALUE, labelBounds.max.t);

                // Positive angles only need to worry about the top and right
                // edges (the label is always on the right or top end of the
                // axis)

                if (angle >= 0.0) {
                    LineSegment rightEdge = rotateSegment(labelRightEdge, labelBottomEdge, labelRightEdge, labelTopEdge, rotation);
                    LineSegment topEdge   = rotateSegment(labelLeftEdge, labelTopEdge, labelRightEdge, labelTopEdge, rotation);
                    adjustBounds(labelBounds, extendedLabelBounds, rightEdge, true);
                    adjustBounds(labelBounds, extendedLabelBounds, topEdge, true);
                }

                // For the X axis, negative angles only need to worry about the right
                // and bottom edges

                else if (isXAxis) {
                    LineSegment rightEdge  = rotateSegment(labelRightEdge, labelBottomEdge, labelRightEdge, labelTopEdge, rotation);
                    LineSegment bottomEdge = rotateSegment(labelLeftEdge, labelBottomEdge, labelRightEdge, labelBottomEdge, rotation);
                    adjustBounds(labelBounds, extendedLabelBounds, rightEdge, true);
                    adjustBounds(labelBounds, extendedLabelBounds, bottomEdge, true);
                }

                // For the T axis, negative angles only need to worry about the left
                // and top edges.

                else {
                    LineSegment leftEdge = rotateSegment(labelLeftEdge, labelBottomEdge, labelLeftEdge, labelTopEdge, rotation);
                    LineSegment topEdge  = rotateSegment(labelLeftEdge, labelTopEdge, labelRightEdge, labelTopEdge, rotation);
                    adjustBounds(labelBounds, extendedLabelBounds, leftEdge, false);
                    adjustBounds(labelBounds, extendedLabelBounds, topEdge, false);
                }

                // debugDrawBounds(context, labelBounds, revRotation, Color.GREEN);

                // Calculate the rotation of the label

                styles.textRotation = angle;
                if (!isXAxis && angle < 0.0) styles.textRotation = angle + 180;

                // Now reverse rotate the label's anchor point to its actual position

                Point2D location;

                if (styles.textAnchor == StyleProperties.TextAnchor.TR) {
                    location = revRotation.transform(labelBounds.max.x, labelBounds.max.t);
                }
                else {
                    if (angle >= 0.0) {
                        location = revRotation.transform(labelBounds.max.x, labelBounds.min.t);
                    }
                    else {
                        location = revRotation.transform(labelBounds.max.x, labelBounds.max.t);
                    }
                }

                labelStruct.location = new Coordinate(location.getX(), location.getY());

                // Draw the label

                Label.draw(context, labelStruct, textColor, font, styles);
            }
        }

        // *********************************************
        // *** Restore the original graphics context.***
        // *********************************************


        styles.textRotation = savedTextRotation;
        styles.textPaddingTop = savedTextPaddingTop;
        styles.textPaddingBottom = savedTextPaddingBottom;
        styles.textPaddingLeft = savedTextPaddingLeft;
        styles.textPaddingRight = savedTextPaddingRight;
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
     * Given a range, determine the maximum dimensions (in viewport units) of
     * any tick label. This method could be improved to deal with rotated labels
     * (or non-rotated labels displayed along a rotated axis), but with some
     * loss of performance and symmetry.
     *
     * @param minValue The smallest value of the range.
     * @param maxValue The largest value of the range.
     * @param format The format string by which to convert numbers to string.
     * @param font The font which will be used to display the tick marks.
     * @param scale The invScale to use to convert screen units to world units.
     *
     * @return A number that can be used with the remainder operator to determine
     * whether to display or skip a particular tick mark.
     */
    private static Dimension2D getMaxTickDimensions(
        double minValue, double maxValue, String format, Font font)
    {
        String minString = String.format(format, minValue);
        javafx.geometry.Bounds minBounds = Label.getTextBounds(minString, font);
        String maxString = String.format(format, maxValue);
        javafx.geometry.Bounds maxBounds = Label.getTextBounds(maxString, font);

        double maxWidth = Math.max(minBounds.getWidth(), maxBounds.getWidth());
        double maxHeight = Math.max(minBounds.getHeight(), maxBounds.getHeight());

        return new Dimension2D(maxWidth, maxHeight);
    }

    /**
     * Given a range, determine how many values to skip so that side-by-side
     * tick mark labels don't overlap. This method could be improved to deal
     * with rotated labels (or non-rotated labels displayed along a rotated
     * axis), but with some loss of performance and symmetry.
     *
     * @param maxTickDimension The maximum width and height of any tick label in
     * world units.
     * @param delta The delta size.
     * @param scale The invScale to use to convert screen units to world units.
     *
     * @return A number that can be used with the remainder operator to determine
     * whether to display or skip a particular tick mark.
     */
    private static int getLabelSkip(Dimension2D maxTickDimension, double delta, double scale)
    {
        double maxWidth = maxTickDimension.getWidth() * scale * 2;
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

    private static LineSegment rotateSegment(double x1, double t1, double x2, double t2, Affine rotation)
    {
        Point2D p1 = rotation.transform(x1, t1);
        Point2D p2 = rotation.transform(x2, t2);
        return new LineSegment(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    private static void adjustBounds(Bounds bounds, Bounds extendedBounds, LineSegment segment, boolean compareRightEdge)
    {
        LineSegment newSegment = segment.intersect(extendedBounds);
        if (newSegment == null) return;

        if (compareRightEdge) {
            double newX = Math.min(newSegment.getPoint1().x, newSegment.getPoint2().x);
            double delta = newX - bounds.max.x;
            bounds.min.x += delta;
            bounds.max.x += delta;
        }
        else {
            double newX = Math.max(newSegment.getPoint1().x, newSegment.getPoint2().x);
            double delta = newX - bounds.min.x;
            bounds.min.x += delta;
            bounds.max.x += delta;
        }
    }

    private static void debugDrawBounds(Context context, Bounds bounds, Affine transform, Color color)
    {
        Point2D p1 = transform.transform(bounds.min.x, bounds.min.t);
        Point2D p2 = transform.transform(bounds.min.x, bounds.max.t);
        Point2D p3 = transform.transform(bounds.max.x, bounds.max.t);
        Point2D p4 = transform.transform(bounds.max.x, bounds.min.t);

        context.gc.setStroke(color);
        context.gc.beginPath();
        context.gc.moveTo(p1.getX(), p1.getY());
        context.gc.lineTo(p2.getX(), p2.getY());
        context.gc.lineTo(p3.getX(), p3.getY());
        context.gc.lineTo(p4.getX(), p4.getY());
        context.gc.lineTo(p1.getX(), p1.getY());
        context.gc.stroke();
    }

}
