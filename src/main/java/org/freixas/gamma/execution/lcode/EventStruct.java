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

import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.OffsetAcceleration;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Frame;
import org.freixas.gamma.value.HyperbolicSegment;

/**
 *
 * @author Antonio Freixas
 */
public class EventStruct extends Struct
{
    static Coordinate coord = new Coordinate(0.0, 0.0);

    public Coordinate location = coord;
    public Frame boostTo = null;
    public String text = "";

    public boolean boostX = false;
    public HyperbolicSegment segment;

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
    }

    @Override
    public void relativeTo(Frame prime)
    {
        location = prime.toFrame(location);
        if (boostTo != null) {
            boostTo = boostTo.relativeTo(prime);
        }
        preCalculateValues(prime);
    }

    private void preCalculateValues(Frame prime)
    {
        if (boostTo != null) {
            // Find the x value for this coordinate when the t value is 0

            if (!Util.fuzzyZero(location.x) || !Util.fuzzyZero(location.t)) {
                double x = location.x;
                double t = location.t;

                boostX = Math.abs(x) > Math.abs(t);

                // Swap the x and t coordinates so the x coordinate is always the
                // larger one

                if (!boostX) {
                    double temp = x;
                    x = t;
                    t = temp;
                }

                // Find the acceleration for a curve that crosses this point

                double v = t / x;
                double crossingPoint = Relativity.xPrime(x, t, v);
                double a = 1 / crossingPoint;

                OffsetAcceleration curve = new OffsetAcceleration(a, 0, new Coordinate(crossingPoint, 0.0), 0.0, 0.0);

                // Transform the point into the boosted frame

                Coordinate boostPoint = boostTo.toRest(x, t);

                // Then further transform that point into the drawing frame if
                // we have one

                if (prime != null) boostPoint = prime.toRest(boostPoint);

                // Create a hyperbolic segment for the curve

                double minT = Math.min(t, boostPoint.t);
                double maxT = Math.max(t, boostPoint.t);

                segment = new HyperbolicSegment(a, minT, maxT, curve);

                // For arrowhead purposes, the start is the event location and
                // the end is the boostTo point. We'll pre-calculate the angles
                // in case arrowheads are enabled. The velocity at the endpoints
                // gives us the angle

                double vStart = curve.tToV(t);
                double vEnd = curve.tToV(boostPoint.t);

                angleAtStart = Relativity.vToTAngle(vStart);
                angleAtEnd   = Relativity.vToTAngle(vEnd);

                double signStart = Util.sign(vStart) * Util.sign(boostPoint.t - t);
                double signEnd   = Util.sign(vEnd  ) * Util.sign(boostPoint.t - t);

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
                endPoint =
                    t <= boostPoint.t ?
                    new Coordinate(segment.getMax().x, segment.getMax().t) :
                    new Coordinate(segment.getMin().x, segment.getMin().t);
            }
        }
    }
}
