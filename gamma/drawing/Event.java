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
import gamma.value.Frame;
import gamma.value.HyperbolicSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.text.Font;

/**
 *
 * @author Antonio Freixas
 */
public class Event
{
    private static final double DIAMOND_DIAMETER_SCALE = Math.sqrt(2);

    private static final double[] pathX = new double[5];
    private static final double[] pathY = new double[5];

    static {
        double[] outerAngles = {
            Util.normalizeAngle360(90),
            Util.normalizeAngle360(90 - 72),
            Util.normalizeAngle360(90 - 2 * 72),
            Util.normalizeAngle360(90 - 3 * 72),
            Util.normalizeAngle360(90 - 4 * 72)
        };
        double[] outerPointX = new double[5];
        double[] outerPointY = new double[5];
        for (int i = 0; i < 5; i++) {
            outerPointX[i] = Math.cos(Math.toRadians(outerAngles[i])) * 2;
            outerPointY[i] = Math.sin(Math.toRadians(outerAngles[i])) * 2;
        }
        for (int i = 0; i < 5; i++) {
            int ix = (i * 2) % 5;
            pathX[i] = outerPointX[ix];
            pathY[i] = outerPointY[ix];
        }
   }

    public static void draw(
        Context context,
        Coordinate location, Frame boostTo,
        String text,
        boolean boostX, HyperbolicSegment segment, boolean positiveDirection,
        StyleStruct styles)
    {
        draw(
            context,
            location, boostTo,
            text,
            boostX, segment, positiveDirection,
            styles.eventDiameter, styles.eventShape, styles.javaFXColor,
            styles.textRotation, styles.font, styles.textPadding, styles.textAnchor,
            styles);
    }

    public static void draw(
        Context context,
        Coordinate location, Frame boostTo,
        String text,
        boolean boostX, HyperbolicSegment segment, boolean positiveDirection,
        String shape, Color color,
        StyleStruct styles)
    {
        draw(
            context,
            location, boostTo,
            text,
            boostX, segment, positiveDirection,
            styles.eventDiameter, shape, color,
            styles.textRotation, styles.font, styles.textPadding, styles.textAnchor,
            styles);
    }

    public static void draw(
        Context context,
        Coordinate location, Frame boostTo,
        String text,
        boolean boostX, HyperbolicSegment segment, boolean positiveDirection,
        double diameter, String shape, Color color,
        double angle, Font font, double padding, String anchor,
        StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        diameter *= context.invScale;
        double halfDiameter = diameter / 2.0;

        gc.setStroke(color);
        gc.setFill(color);

        switch(shape) {
            case "circle" -> {
                gc.fillOval(location.x - halfDiameter, location.t - halfDiameter, diameter, diameter);
            }
            case "square" -> {
                gc.fillRect(location.x - halfDiameter, location.t - halfDiameter, diameter, diameter);
            }
            case "diamond" -> {
                double halfHeightDiameter = halfDiameter * DIAMOND_DIAMETER_SCALE;
                gc.beginPath();
                gc.moveTo(location.x, location.t + halfHeightDiameter);
                gc.lineTo(location.x + halfDiameter, location.t);
                gc.lineTo(location.x, location.t - halfHeightDiameter);
                gc.lineTo(location.x - halfDiameter, location.t);
                gc.lineTo(location.x, location.t + halfHeightDiameter);
                gc.closePath();
                gc.fill();
            }
            case "star" -> {
                gc.beginPath();
                for (int i = 0; i < pathX.length; i++) {
                    if (i == 0) {
                        gc.moveTo(pathX[i] * halfDiameter + location.x, pathY[i] * halfDiameter + location.t);
                    }
                    else {
                        gc.lineTo (pathX[i] * halfDiameter + location.x, pathY[i] * halfDiameter + location.t);
                    }
                }
                gc.closePath();
                gc.setFillRule(FillRule.NON_ZERO);
                gc.fill();
            }
        }

        if (text.length() > 0) {
            Text.draw(context, location.x, location.t, text, angle, color, font, padding, anchor);
        }

        // Restore the original graphics context

        gc.restore();
        gc.save();

        // Draw a hyperbola from this event to a designated boostTo Frame

        // Set up the line styles

        Line.setupLineGc(context, styles);

        if (boostTo != null) {
            if (boostX) {
                segment = segment.intersect(context.bounds);
                if (segment != null) {
                    Hyperbola.drawRaw(context, segment);
                    if (styles.arrow.equals("start") || styles.arrow.equals("both")) {
                        Arrow.draw(context, location, styles., styles)
                    }
                }
            }
            else {
                gc.rotate(90);
                gc.scale(1, -1);
                segment = segment.intersect(context.getCurrentCanvasBounds());
                if (segment != null) {
                    Hyperbola.drawRaw(context, segment);
                }
            }
        }

        // Restore the original graphics context

        gc.restore();
    }

}
