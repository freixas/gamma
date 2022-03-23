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

import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.OffsetAcceleration;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.*;

/**
 *
 * @author Antonio Freixas
 */
public class EventStruct extends Struct
{
    static Coordinate coord = new Coordinate(0.0, 0.0);

    public Coordinate location = coord;
    public boolean locationSet = false;
    public String text = "";
    public double rotation = 0.0;
    public boolean frameRelativeRotation = false;
    public Frame boostTo = null;

    public boolean boostX = false;
    public CurveSegment segment;

    public double angleAtStart;
    public double angleAtEnd;
    public Coordinate endPoint;

    public EventStruct()
    {
    }

    @Override
    public void finalizeValues()
    {
        preCalculateValues(null);
        rotation = Util.normalizeAngle180(rotation);
    }

    @Override
    public void relativeTo(Frame prime)
    {
        location = prime.toFrame(location);
        if (frameRelativeRotation) {
            rotation = Relativity.toPrimeAngle(rotation, prime.getV());
        }
        if (boostTo != null) {
            boostTo = boostTo.relativeTo(prime);
        }
        preCalculateValues(prime);
    }

    private void preCalculateValues(Frame prime)
    {
        // We only calculate this if the user asked for event boosting

        if (boostTo != null) {

            // We're going to divide spacetime into four quadrants by drawing
            // the worldlines of a beam of light crossing (0,0). The hyperbolic
            // curve that passes through a point will stay entirely within the
            // point's quadrant.

            double x = location.x;
            double t = location.t;

            // We can't boost (0,0) to anything except (0,0) so there's not
            // much point in drawing a boost line

            if (Util.fuzzyZero(x) && Util.fuzzyZero(t)) {
                boostTo = null;
                return;
            }

            // boostX is true when the event lies in the left or right quadrants,
            // including the edges of the quadrants

            boostX = Util.fuzzyGT(Math.abs(x), Math.abs(t));

            // Handle the special case when the point lies on the diagonals. In
            // this case, we don't draw a hyperbola, we draw a straight line

            if (Util.fuzzyEQ(Math.abs(x), Math.abs(t))) {

                // Find the location of the point when it's boosted into the
                // boost frame

                endPoint = boostTo.toRest(x, t);

                // Then further transform that point into the drawing frame if
                // we have one

                if (prime != null) endPoint = prime.toRest(endPoint);

                // Create the line segment

                segment = new LineSegment(x, t, endPoint.x, endPoint.t);

                // Line is at 45 degrees

                if (Util.fuzzyEQ(x, t)) {
                    if (t < endPoint.t) {
                        angleAtStart = -135;
                        angleAtEnd = 45;
                    }
                    else {
                        angleAtStart = 45;
                        angleAtEnd = -135;
                    }
                }

                // Line is at -45 degrees

                else {
                    if (t < endPoint.t) {
                        angleAtStart = -45;
                        angleAtEnd = 135;
                    }
                    else {
                        angleAtStart = 135;
                        angleAtEnd = -45;
                    }
                }

                return;
            }

            // If the point is in the top or bottom quadrant, mirror it into
            // the left or right quadrant

            if (!boostX) {
                double temp = x;
                x = t;
                t = temp;
            }

            // Find the location of the point when it's boosted into the boost
            // frame

            endPoint = boostTo.toRest(x, t);

            // Then further transform that point into the drawing frame if
            // we have one

            if (prime != null) endPoint = prime.toRest(endPoint);

            // We want to transform this point to an inertial frame where the
            // transformed point is on the x-axis

            double crossingPoint;

            // If the point is already on the x-axis, leave it as is

            if (Util.fuzzyZero(t)) {
                crossingPoint = x;
            }

            // Otherwise, find the crossing point

            else {
                double v = t / x;
                crossingPoint = Relativity.xPrime(x, t, v);
            }

            // The hyperbolic curve representing all the possible positions of
            // the point relative to all possible inertial frames is a curve
            // that passes through the point and has an acceleration of 1/x.
            //
            // Note that we've eliminated any possibility that the crossing
            // point is 0

            double a = 1.0 / crossingPoint;
            OffsetAcceleration curve = new OffsetAcceleration(a, 0, new Coordinate(crossingPoint, 0.0), 0.0, 0.0);

            // Create a hyperbolic segment for the curve

            double minT = Math.min(t, endPoint.t);
            double maxT = Math.max(t, endPoint.t);
            segment = new HyperbolicSegment(a, minT, maxT, curve);

            // For arrowhead purposes, the start is the event location and
            // the end is the boostTo point. We'll pre-calculate the angles
            // in case arrowheads are enabled. The velocity at the endpoints
            // gives us the angle

            double vStart = curve.tToV(t);
            double vEnd = curve.tToV(endPoint.t);
            angleAtStart = Relativity.vToTAngle(vStart);
            angleAtEnd   = Relativity.vToTAngle(vEnd);

            // We can't just draw the arrowheads using these angles. We need to
            // know whether the velocity at each point is >= 0 and whether the
            // end point is greater than start point relative to t

            double signStart = Util.sign(vStart) * Util.sign(endPoint.t - t);
            double signEnd   = Util.sign(vEnd  ) * Util.sign(endPoint.t - t);

            // The start velocity <  0 and the end point is > the start point OR
            // The start velocity >= 0 and the end point is < the start point

            if (signStart < 0) {

                // If the velocity at the start is 0, the arrow points up

                if (Util.fuzzyZero(vStart)) {
                    angleAtStart = 90.0;
                }

                // If the velocity is not 0, leave the angle alone
            }

            // The start velocity >= 0 and the end point is > the start point OR
            // The start velocity <  0 and the end point is < the start point

            else {

                // If the velocity at the start is 0,  the arrow points down

                if (Util.fuzzyZero(vStart)) {
                    angleAtStart = -90.0;
                }

                // Otherwise, it points the opposite of whatever we calculated

                else {
                    angleAtStart += 180.0;
                }
            }

            // The end velocity <  0 and the end point is > the start point OR
            // The end velocity >= 0 and the end point is < the start point

            if (signEnd < 0) {

                // If the velocity at the end is 0, the arrow points down

                if (Util.fuzzyZero(vEnd)) {
                    angleAtEnd = -90.0;
                }

                // If the velocity is not 0, flip the angle

                else {
                    angleAtEnd += 180.0;
                }
            }

            // The end velocity >= 0 and the end point is > the start point OR
            // The end velocity <  0 and the end point is < the start point

            else {

                // If the velocity at the end is 0, the arrow points up

                if (Util.fuzzyZero(vEnd)) {
                    angleAtEnd = 90.0;
                }

                // If the velocity is not 0, leave the angle alone
            }

            // If we are boosting a point in the upper or lower quadrants, we
            // need to undo the mirroring we did earlier

            if (!boostX) {
                double temp = endPoint.x;
                endPoint.x = endPoint.t;
                endPoint.t = temp;

                angleAtStart = 90 - angleAtStart;
                angleAtEnd = 90 - angleAtEnd;
            }
        }
    }
}
