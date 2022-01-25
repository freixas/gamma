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
package org.freixas.gamma.value;

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.OffsetAcceleration;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * An observer has an initial origin, tau and distance. The observer then
 * travels through as series of periods of constant velocity or constant
 * acceleration.
 *
 * @author Antonio Freixas
 */
public class IntervalObserver extends Observer
{
    private final ConcreteObserver observer;
    private final Interval interval;

    private final WorldlineEndpoint min;
    private final WorldlineEndpoint max;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    public IntervalObserver(Observer observer, Interval interval)
    {
        if (observer == null) throw new ExecutionException("setInterval() has a null observer");
        if (interval == null) throw new ExecutionException("setInterval() has a null interval");

        if (observer instanceof IntervalObserver intervalObserver) {
            this.observer = intervalObserver.observer;
        }
        else if (observer instanceof ConcreteObserver concreteObserver) {
            this.observer = concreteObserver;
        }
        else {
            throw new ProgrammingException("IntervalLine: line is not a interval or concrete line");
        }

        // The interval is never used except in the constructor and for display

	this.interval = interval;

        double minT = 0;
        double maxT = 0;

        if (null != interval.getType()) switch (interval.getType()) {
            case T -> {
                minT = interval.getMin();
                maxT = interval.getMax();
            }
            case TAU -> {
                minT = this.observer.dToTau(interval.getMin());
                maxT = this.observer.dToTau(interval.getMax());
            }
            case D -> {
                minT = this.observer.dToT(interval.getMin());
                maxT = this.observer.dToT(interval.getMax());
            }
        }

        this.min = new WorldlineEndpoint(
            this.observer.tToV(minT),
            this.observer.tToX(minT),
            minT,
            this.observer.tToTau(minT),
            this.observer.tToD(minT));

        this.max = new WorldlineEndpoint(
            this.observer.tToV(maxT),
            this.observer.tToX(maxT),
            maxT,
            this.observer.tToTau(maxT),
            this.observer.tToD(maxT));
    }

    private IntervalObserver(Observer observer, Interval interval,
                              WorldlineEndpoint min, WorldlineEndpoint max)
    {
        if (observer == null) {
            throw new ProgrammingException("BoundedLine: Trying to attach an interval to a null observer");
        }

        if (observer instanceof IntervalObserver intervalObserver) {
            this.observer = intervalObserver.observer;
        }
        else if (observer instanceof ConcreteObserver concreteObserver) {
            this.observer = concreteObserver;
        }
        else {
            throw new ProgrammingException("IntervalLine: line is not a interval or concrete line");
        }

        // The interval is never used except in the constructor and for display

	this.interval = interval;

        this.min = min;
        this.max = max;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Return the ConcreteObserver wrapped in this IntervalObserver.
     *
     * @return The ConcreteObserver wrapped in this IntervalObserver.
     */
    public ConcreteObserver getObserver()
    {
        return this.observer;
    }

    /**
     * Get the minimum worldline endpoint.
     *
     * @return The minimum worldline endpoint.
     */
    public WorldlineEndpoint getMin()
    {
        return min;
    }

    /**
     * Get the maximum worldline endpoint.
     *
     * @return The maximum worldline endpoint.
     */
    public WorldlineEndpoint getMax()
    {
        return max;
    }

    @Override
    public ArrayList<WorldlineSegment> getSegments()
    {
        return observer.getSegments();
    }

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    @Override
    public IntervalObserver relativeTo(Frame prime)
    {
        ConcreteObserver relObserver = observer.relativeTo(prime);
        Coordinate endPoint = prime.toFrame(min.x, min.t);
        WorldlineEndpoint relMin = new WorldlineEndpoint(
            Relativity.vPrime(min.v, prime.getV()),
            endPoint.x,
            endPoint.t,
            relObserver.tToTau(endPoint.t),
            relObserver.tToD(endPoint.t)
        );
        endPoint = prime.toFrame(max.x, max.t);
        WorldlineEndpoint relMax = new WorldlineEndpoint(
            Relativity.vPrime(max.v, prime.getV()),
            endPoint.x,
            endPoint.t,
            relObserver.tToTau(endPoint.t),
            relObserver.tToD(endPoint.t)
        );

        // The interval is not changed

        return new IntervalObserver(relObserver, interval, relMin, relMax);
    }

    // **********************************************************
    // *
    // * Source is v
    // *
    // **********************************************************

    /**
     * Given v, return x. If v matches no segment points, return NaN. If v
     * matches all segment points return the x that occurs earliest in time.
     *
     * @param v The velocity.
     * @return The position in the rest frame or NaN.
     */
    @Override
    public double vToX(double v)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the velocity and acceleration are 0, then the x value will
                // be the same everywhere in the segment, so we need do nothing
                // special

                double x = segment.vToX(v);
                if (!Double.isNaN(x)) return x;
            }
        }
        return Double.NaN;
    }

    /**
     * Given v, return d. If v matches no segment points, return NaN. If v
     * matches all segment points return the d that occurs earliest in time.
     *
     * @param v The velocity.
     * @return The distance in the rest frame or NaN.
     */
    @Override
    public double vToD(double v)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the velocity and acceleration are 0, then the d value will
                // be the same everywhere in the segment, so we need do nothing
                // special

                double d = segment.vToD(v);
                if (!Double.isNaN(d)) return d;
            }
        }
        return Double.NaN;
    }

    /**
     * Given v, return t. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest time.
     *
     * @param v The velocity.
     * @return The time in the rest frame or NaN.
     */
    @Override
    public double vToT(double v)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the velocity and acceleration are 0, then we need to make
                // sure we get a t value within the range

                WorldlineEndpoint sMin = segment.getMin();
                WorldlineEndpoint sMax = segment.getMax();
                if (Util.fuzzyLT(sMin.v, sMax.v) && (Util.fuzzyLT(v, sMax.v) || Util.fuzzyGT(v, sMax.v))) return Double.NaN;
                if (Util.fuzzyGT(sMin.v, sMax.v) && (Util.fuzzyLT(v, sMax.v) || Util.fuzzyGT(v, sMin.v))) return Double.NaN;
                if (Util.fuzzyEQ(sMin.v, sMax.v)) return min.t;

                double t = segment.vToT(v);
                if (!Double.isNaN(t)) return t;
            }
        }
        return Double.NaN;
    }

    /**
     * Given v, return tau. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest tau.
     *
     * @param v The velocity.
     * @return The time in the accelerated frame or NaN.
     */
    @Override
    public double vToTau(double v)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the velocity and acceleration are 0, then we need to make
                // sure we get a tau value within the range

                WorldlineEndpoint sMin = segment.getMin();
                WorldlineEndpoint sMax = segment.getMax();
                if (Util.fuzzyLT(sMin.v, sMax.v) && (Util.fuzzyLT(v, sMax.v) || Util.fuzzyGT(v, sMax.v))) return Double.NaN;
                if (Util.fuzzyGT(sMin.v, sMax.v) && (Util.fuzzyLT(v, sMax.v) || Util.fuzzyGT(v, sMin.v))) return Double.NaN;
                if (Util.fuzzyEQ(sMin.v, sMax.v)) return min.tau;

                double tau = segment.vToTau(v);
                if (!Double.isNaN(tau)) return tau;
            }

        }
        return Double.NaN;
    }

    // **********************************************************
    // *
    // * Source is d
    // *
    // **********************************************************

    /**
     * Given d, return v.
     *
     * @param d The distance in the rest frame.
     * @return The velocity.
     */
    @Override
    public double dToV(double d)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the distance doesn't change, the velocity will be the same
                // (0) at every point

                double v = segment.dToV(d);
                if (!Double.isNaN(d)) return v;
            }
        }
        return Double.NaN;
    }

    /**
     * Given d, return x.
     *
     * @param d The distance in the rest frame
     * @return The position in the rest frame.
     */
    @Override
    public double dToX(double d)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the distance doesn't change, neither does the X value

                double x = segment.dToX(d);
                if (!Double.isNaN(x)) return x;
            }
        }
        return Double.NaN;
    }

    /**
     * Given d, return t.
     *
     * @param d The distance in the rest frame
     * @return The time in the rest frame.
     */
    @Override
    public double dToT(double d)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the distance doesn't change, get the correct T value

                WorldlineEndpoint sMin = segment.getMin();
                WorldlineEndpoint sMax = segment.getMax();
                if (Util.fuzzyLT(d, sMin.d) || Util.fuzzyGT(d, sMin.d)) return Double.NaN;
                if (Util.fuzzyEQ(sMin.d, sMin.d)) return min.t;

                double t = segment.dToT(d);
                if (!Double.isNaN(t)) return t;
            }
        }
        return Double.NaN;
    }

    /**
     * Given d, return tau.
     *
     * @param d The distance in the rest frame
     * @return The time in the accelerated frame.
     */
    @Override
    public double dToTau(double d)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                // If the distance doesn't change, get the correct T value

                WorldlineEndpoint sMin = segment.getMin();
                WorldlineEndpoint sMax = segment.getMax();
                if (Util.fuzzyLT(d, sMin.d) || Util.fuzzyGT(d, sMin.d)) return Double.NaN;
                if (Util.fuzzyEQ(sMin.d, sMin.d)) return min.tau;

                double tau = segment.dToTau(d);
                if (!Double.isNaN(tau)) return tau;
            }
        }
        return Double.NaN;
    }

    // **********************************************************
    // *
    // * Source is t
    // *
    // **********************************************************

    /**
     * Given t, return v.
     *
     * @param t The time in the rest frame.
     * @return The velocity.
     */
    @Override
    public double tToV(double t)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double v = segment.tToV(t);
                if (!Double.isNaN(v)) return v;
            }
        }
        return Double.NaN;
     }

    /**
     * Given t, return x.
     *
     * @param t The time in the rest frame.
     * @return The position in the rest frame.
     */
    @Override
    public double tToX(double t)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double x = segment.tToX(t);
                if (!Double.isNaN(x)) return x;
            }
        }
        return Double.NaN;
    }

    /**
     * Given t, return d.
     *
     * @param t The time in the rest frame.
     * @return The distance in the rest frame.
     */
    @Override
    public double tToD(double t)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {

                double d = segment.tToD(t);
                if (!Double.isNaN(d)) return d;
            }
        }
        return Double.NaN;
    }

    /**
     * Given t, calculate tau.
     *
     * @param t The time in the rest frame.
     * @return The time in the accelerated frame.
     */
    @Override
    public double tToTau(double t)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double tau = segment.tToTau(t);
                if (!Double.isNaN(tau)) return tau;
            }
        }
        return Double.NaN;
    }

    // **********************************************************
    // *
    // * Source is tau
    // *
    // **********************************************************

    /**
     * Given tau, return v.
     *
     * @param tau The time in the accelerated frame.
     * @return The velocity.
     */
    @Override
    public double tauToV(double tau)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double v = segment.tauToV(tau);
                if (!Double.isNaN(v)) return v;
            }
        }
        return Double.NaN;
    }

    /**
     * Given tau, return x.
     *
     * @param tau The time in the accelerated frame.
     * @return The position in the rest frame.
     */
    @Override
    public double tauToX(double tau)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double x = segment.tauToX(tau);
                if (!Double.isNaN(x)) return x;
            }
        }
        return Double.NaN;
    }

    /**
     * Given tau, return d.
     * <p>
     * Since initially v = 0, if the acceleration is 0, then d = 0.
     *
     * @param tau The time in the accelerated frame.
     * @return The distance in the rest frame.
     */
    @Override
    public double tauToD(double tau)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double d = segment.tauToD(tau);
                if (!Double.isNaN(d)) return d;
            }
        }
        return Double.NaN;
    }

    /**
     * Given tau, return t.
     *
     * @param tau The time in the accelerated frame.
     * @return The time in the rest frame.
     */
    @Override
    public double tauToT(double tau)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                double t = segment.tauToT(tau);
                if (!Double.isNaN(t)) return t;
            }
        }
        return Double.NaN;
    }

    // **********************************************************
    // *
    // * Intersections
    // *
    // **********************************************************

    /**
     * Find the intersection of this worldline with a line. We
     * check segments one at a time from earliest to latest. We return the
     * first intersection with the earliest time coordinate. If there is no
     * intersection with any segment, we return null.
     *
     * @param line The line to intersect with.
     * @return The intersection or null if none.
     */
    @Override
    public Coordinate intersect(Line line)
    {
        ListIterator<WorldlineSegment> iter = observer.getSegments().listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            int inRange = inRange(segment);
            if (inRange == 1) break;
            if (inRange == 0) {
                OffsetAcceleration curve = segment.getCurve();
                CurveSegment curveSegment = segment.getCurveSegment();

                Coordinate[] results = curve.intersect(line);
                if (results == null) continue;

                // Return the first intersection that occurs within the bounds
                // of this segment (if any)

                for (Coordinate intersection : results) {

                    // Find out if this intersection occurs within the bounds of this
                    // segment and within our interval

                    if (curveSegment.getBounds().inside(intersection) && inRange(intersection)) return intersection;
                }
            }
        }
        return null;
    }

    /**
     * Find the intersection of this worldline with another. We check segments
     * one at a time from earliest to latest. We check our first segment against
     * all of the other worldline's segments, then our second segment, etc. We
     * return the first intersection with the earliest time coordinate. If there
     * is no intersection with any segment, we return null.
     *
     * @param other The other observer.
     * @return The intersection or null if none.
     */
    @Override
    public Coordinate intersect(Observer other)
    {
        if (other instanceof IntervalObserver intervalObserver) {
            return intervalObserver.intersect(this);
        }
        else if (other instanceof ConcreteObserver otherObserver) {
            ListIterator<WorldlineSegment> iter = otherObserver.getSegments().listIterator();
            while (iter.hasNext()) {
                WorldlineSegment segment1 = iter.next();
                ListIterator<WorldlineSegment> iter2 = observer.getSegments().listIterator();
                while (iter2.hasNext()) {
                    WorldlineSegment segment2 = iter2.next();
                    Coordinate coord = segment1.intersect(segment2);
                    if (coord != null) return coord;
                }
            }
        }
        return null;
    }

    // **********************************************************
    // *
    // * Support methods
    // *
    // **********************************************************

    /**
     * Determine the relationship between the interval and the segment's
     * range.
     * <ul>
     * <li>Return -1 if the interval is completely less than the segment's range.
     * <li>Return 1 if the interval is completely greater than the segment's range.
     * <li>Return 0 if any part of the interval is within the segment's range.
     * </ul>
     *
     * @param segment The segment whose range we want to check.
     * @return A value that specifies the relationship between the interval
     * and the segment's range.
     */
    public int inRange(WorldlineSegment segment)
    {
        if (Util.fuzzyGE(min.t, segment.getMax().t)) return -1;
        if (Util.fuzzyGT(segment.getMin().t, max.t)) return  1;
        return 0;
    }

    public boolean inRange(Coordinate c)
    {
        // We don't need to check the X coordinate. If the T coordinate is in
        // the interval, the X coordinate will be as well

        return Util.fuzzyLE(min.t, c.t) && Util.fuzzyGE(max.t, c.t);
    }

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Interval Observer " + observer.toDisplayableString(engine) + " interval " +
               interval.toDisplayableString(engine) +
               "]";
    }

}
