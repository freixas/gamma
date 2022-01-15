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

import gamma.css.value.StyleProperties;
import gamma.execution.lcode.EventStruct;
import gamma.execution.lcode.LabelStruct;
import gamma.css.value.StyleStruct;
import gamma.math.Util;
import gamma.value.Coordinate;
import gamma.value.HyperbolicSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;

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

    public static void draw(Context context, EventStruct struct, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        double diameter = styles.eventDiameter;

        diameter *= context.invScale;
        double halfDiameter = diameter / 2.0;

        gc.setStroke(styles.color);
        gc.setFill(styles.color);

        Coordinate location = struct.location;

        switch(styles.eventShape) {
            case CIRCLE -> {
                gc.fillOval(location.x - halfDiameter, location.t - halfDiameter, diameter, diameter);
            }
            case SQUARE -> {
                gc.fillRect(location.x - halfDiameter, location.t - halfDiameter, diameter, diameter);
            }
            case DIAMOND -> {
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
            case STAR -> {
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

        String text = struct.text;

        if (text.length() > 0) {
            LabelStruct labelStruct = new LabelStruct();
            labelStruct.location = location;
            labelStruct.text = text;
            Label.draw(context, labelStruct, styles);
        }

        // Restore the original graphics context

        gc.restore();
        gc.save();

        // Draw a hyperbola from this event to a designated boostTo Frame

        // Set up the line styles

        Line.setupLineGc(context, styles);

        if (struct.boostTo != null) {
            if (struct.boostX) {
                HyperbolicSegment segment = struct.segment.intersect(context.bounds);
                if (segment != null) {
                    Hyperbola.drawRaw(context, segment);
                    if (styles.arrow == StyleProperties.Arrow.START || styles.arrow == StyleProperties.Arrow.BOTH) {
                        Arrow.draw(context, location, struct.angleAtStart, styles);
                    }
                    if (styles.arrow == StyleProperties.Arrow.END || styles.arrow == StyleProperties.Arrow.BOTH) {
                        Arrow.draw(context, struct.endPoint, struct.angleAtEnd, styles);
                    }
                }
            }
            else {
                gc.rotate(90);
                gc.scale(1, -1);
                HyperbolicSegment segment = struct.segment.intersect(context.getCurrentCanvasBounds());
                if (segment != null) {
                    Hyperbola.drawRaw(context, segment);
                }
                if (styles.arrow == StyleProperties.Arrow.START || styles.arrow == StyleProperties.Arrow.BOTH) {
                    Arrow.draw(context, location, struct.angleAtStart, styles);
                }
                if (styles.arrow == StyleProperties.Arrow.END || styles.arrow == StyleProperties.Arrow.BOTH) {
                    Arrow.draw(context, struct.endPoint, struct.angleAtEnd, styles);
                }
            }
        }

        // Restore the original graphics context

        gc.restore();
    }

}
