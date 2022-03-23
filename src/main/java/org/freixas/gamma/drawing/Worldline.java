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

import javafx.scene.canvas.GraphicsContext;
import org.freixas.gamma.css.value.StyleProperties;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.execution.lcode.WorldlineStruct;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.*;

import java.util.ArrayList;

/**
 * Draw a worldline.
 *
 * @author Antonio Freixas
 */
public class Worldline
{
    /**
     * Draw a worldline.
     *
     * @param context The drawing context.
     * @param struct The worldline properties.
     * @param styles The style properties.
     */
    static public void draw(Context context, WorldlineStruct struct, StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();
        StyleProperties.Arrow savedArrow = styles.arrow;
        styles.arrow = StyleProperties.Arrow.NONE;

        // Set up the gc for line drawing

        Line.setupLineGc(context, styles);

        org.freixas.gamma.value.Observer observer = struct.observer;

        // A concrete observer is not bounded (it's infinite)

        if (observer instanceof ConcreteObserver concreteObserver) {
            ArrayList<WorldlineSegment>segments = concreteObserver.getSegments();

            for (WorldlineSegment segment : segments) {

                CurveSegment curveSegment = segment.getCurveSegment();

                // Is this a line segment? If so, intersect it with the viewport and
                // draw the intersecting segment, if any

                if (curveSegment instanceof LineSegment lineSegment) {
                    lineSegment = lineSegment.intersect(context.bounds);
                    if (lineSegment != null) {
                        Line.drawRaw(context, lineSegment, styles);
                    }
                }

                // Is this a hyperbolic segment? If so, intersect it with the
                // viewport and draw the intersecting curve, if any

                else if (curveSegment instanceof HyperbolicSegment hyperbolicSegment) {
                    hyperbolicSegment = hyperbolicSegment.intersect(context.bounds);
                    if (hyperbolicSegment != null) {
                        Hyperbola.drawRaw(context, hyperbolicSegment);
                    }
                }

                // Is this a line segment with one or two infinite ends (e.g. a
                // line). If so, intersect it with the viewport and draw the
                // intersecting line segment, if any

                else if (curveSegment instanceof org.freixas.gamma.value.Line line) {
                    LineSegment lineSegment = line.intersect(context.bounds);
                    if (lineSegment != null) {
                        Line.drawRaw(context, lineSegment, styles);
                    }
                }
            }
        }

        // An interval observer is bounded along the t axis

        else if (observer instanceof IntervalObserver intervalObserver) {
            WorldlineEndpoint min = intervalObserver.getMin();
            WorldlineEndpoint max = intervalObserver.getMax();

            Bounds intervalBounds = new Bounds(Double.NEGATIVE_INFINITY, min.t, Double.POSITIVE_INFINITY, max.t);
            Bounds bounds = context.bounds.intersect(intervalBounds);
            if (bounds != null) {
                ArrayList<WorldlineSegment>segments = intervalObserver.getSegments();

                for (WorldlineSegment segment : segments) {
                    int inRange = intervalObserver.inRange(segment);
                    if (inRange == 1) break;

                    if (inRange == 0) {
                        CurveSegment curveSegment = segment.getCurveSegment();

                        // Is this a line segment? If so, intersect it with the viewport and
                        // draw the intersecting segment, if any

                        if (curveSegment instanceof LineSegment lineSegment) {
                            lineSegment = lineSegment.intersect(bounds);
                            if (lineSegment != null) {
                                Line.drawRaw(context, lineSegment, styles);
                            }
                        }

                        // Is this a hyperbolic segment? If so, intersect it with the
                        // viewport and draw the intersecting curve, if any

                        else if (curveSegment instanceof HyperbolicSegment hyperbolicSegment) {
                            hyperbolicSegment = hyperbolicSegment.intersect(bounds);
                            if (hyperbolicSegment != null) {
                                Hyperbola.drawRaw(context, hyperbolicSegment);
                            }
                        }

                        // Is this a line segment with one or two infinite ends (e.g. a
                        // line). If so, intersect it with the viewport and draw the
                        // intersecting line segment, if any

                        else if (curveSegment instanceof org.freixas.gamma.value.Line line) {
                            LineSegment lineSegment = line.intersect(bounds);
                            if (lineSegment != null) {
                                Line.drawRaw(context, lineSegment, styles);
                            }
                        }
                    }
                }

                // We know we drew something because the bounding box isn't null
                // Add the arrows

                styles.arrow = savedArrow;
                if (styles.arrow != StyleProperties.Arrow.NONE) {
                    double[] angles = getAngles(min, max);
                    if (styles.arrow == StyleProperties.Arrow.START || styles.arrow == StyleProperties.Arrow.BOTH) {
                        if (context.bounds.inside(min.x, min.t)) {
                            Arrow.draw(context, new Coordinate(min.x, min.t), angles[0], styles);
                        }
                    }
                    if (styles.arrow == StyleProperties.Arrow.END || styles.arrow == StyleProperties.Arrow.BOTH) {
                        if (context.bounds.inside(max.x, max.t)) {
                            Arrow.draw(context, new Coordinate(max.x, max.t), angles[1], styles);
                        }
                    }
                }
            }

        }

        // Restore the original graphics context

        styles.arrow = savedArrow;
        gc.restore();
    }

    /**
     * Get the angles at the start or end of an interval observer. These are
     * used to draw arrowheads.
     *
     * @param min The point on the worldline with the smallest t value.
     * @param max The point on the worldline with the largest t value.
     *
     * @return An array containing the angles to use for the arrowheads. The first
     * value corresponds to the angle for the arrowhead at the smallest t value
     * of the worldline.
     */
    static private double[] getAngles(WorldlineEndpoint min, WorldlineEndpoint max)
    {
        double angleAtStart, angleAtEnd;
        double vStart = min.v;
        double vEnd = max.v;

        angleAtStart = Relativity.vToTAngle(vStart);
        angleAtEnd   = Relativity.vToTAngle(vEnd);

        double signStart = Util.sign(vStart);
        double signEnd   = Util.sign(vEnd  );

        // We need to make sure the arrowheads point in the right direction.
        // This depends on whether we are at the start or end and whether the
        // velocity is positive or negative

        if (signStart < 0) {
            if (Util.fuzzyZero(vStart)) {
                angleAtStart = 90.0;
            }
        }
        else {
            if (Util.fuzzyZero(vStart)) {
                angleAtStart = -90.0;
            }
            else {
                angleAtStart += 180.0;
            }
        }

        if (signEnd < 0) {
            if (Util.fuzzyZero(vEnd)) {
                angleAtEnd = -90.0;
            }
            else {
                angleAtEnd += 180.0;
            }
        }
        else {
            if (Util.fuzzyZero(vEnd)) {
                angleAtEnd = 90.0;
            }
        }
        return new double[]{ angleAtStart, angleAtEnd };
    }

}
