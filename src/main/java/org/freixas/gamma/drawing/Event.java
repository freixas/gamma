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

import org.freixas.gamma.css.value.StyleProperties;
import org.freixas.gamma.execution.lcode.EventStruct;
import org.freixas.gamma.execution.lcode.LabelStruct;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.HyperbolicSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;
import org.freixas.gamma.value.LineSegment;

/**
 * Draw events.
 *
 * @author Antonio Freixas
 */
public class Event
{
    /**
     * The diameter scaling use to map the event diameter for diamond shapes
     */
    static private final double DIAMOND_DIAMETER_SCALE = Math.sqrt(2);

    // Pre-calculate the path of a star shape

    static private final double[] pathX = new double[5];
    static private final double[] pathY = new double[5];

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

    /**
     * Draw an event
     *
     * @param context The drawing context.
     * @param struct The event properties.
     * @param styles The style properties.
     */
    static public void draw(Context context, EventStruct struct, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        // Get the event diameter

        double diameter = styles.eventDiameter;

        // Scale the diameter and get half the distance. We'll use the half
        // distance to center the event shape on the event location

        diameter *= context.invScale;
        double halfDiameter = diameter / 2.0;

        // Set up the gc

        gc.setStroke(styles.color);
        gc.setFill(styles.color);

        Coordinate location = struct.location;

        // Draw the event, using the event shape to define the drawing
        // algorithm we use

        switch(styles.eventShape) {
            case CIRCLE -> gc.fillOval(location.x - halfDiameter, location.t - halfDiameter, diameter, diameter);
            case SQUARE -> gc.fillRect(location.x - halfDiameter, location.t - halfDiameter, diameter, diameter);
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

        // If the event has some associated text, draw it as well. We'll treat
        // it as though the text came from the label command

        String text = struct.text;

        if (text.length() > 0) {
            LabelStruct labelStruct = new LabelStruct();
            labelStruct.location = location;
            labelStruct.text = text;
            labelStruct.rotation = struct.rotation;
            Label.draw(context, labelStruct, styles);
        }

        // Restore the original graphics context

        gc.restore();
        gc.save();

        // Events can be boosted, so we may also need to draw the boost line and
        // some arrow heads. If it's boosted, draw a hyperbola from this event
        // to the designated boostTo Frame

        // Set up the line styles

        Line.setupLineGc(context, styles);

        if (struct.boostTo != null) {

            if (styles.arrow == StyleProperties.Arrow.START || styles.arrow == StyleProperties.Arrow.BOTH) {
                Arrow.draw(context, location, struct.angleAtStart, styles);
            }
            if (styles.arrow == StyleProperties.Arrow.END || styles.arrow == StyleProperties.Arrow.BOTH) {
                Arrow.draw(context, struct.endPoint, struct.angleAtEnd, styles);
            }

            // Draw a straight boost line

            if (struct.segment instanceof LineSegment lineSegment) {
                LineSegment segment;

                segment = lineSegment.intersect(context.bounds);

                // Draw the line segment minus the arrows (which have already
                // been taken care of)

                if (segment != null) {
                    StyleProperties.Arrow savedArrow = styles.arrow;
                    styles.arrow = StyleProperties.Arrow.NONE;
                    Line.drawRaw(context, segment, styles);
                    styles.arrow = savedArrow;
                }
            }

            // Draw a hyperbolic boost line

            else if (struct.segment instanceof HyperbolicSegment hyperbolicSegment) {

                // The hyperbolas of the boost line depend on which region the
                // event lies. Draw the worldline of a beam of light passing through
                // (0,0). If an event is in the left or right quadrants, then
                // boostX is true. If an event is in the top or bottom, quadrants,
                // boostX is false

                HyperbolicSegment segment;

                if (struct.boostX) {
                    segment = hyperbolicSegment.intersect(context.bounds);
                }
                else {
                    gc.rotate(90);
                    gc.scale(1, -1);
                    segment = hyperbolicSegment.intersect(context.getCurrentCanvasBounds());
                }
                if (segment != null) {
                    Hyperbola.drawRaw(context, segment);
                }
            }
        }

        // Restore the original graphics context

        gc.restore();
    }

}
