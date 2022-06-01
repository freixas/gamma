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
import org.freixas.gamma.math.Util;

import java.util.ArrayList;

/**
 * An interval observer is an observer whose worldline is limited to a range
 * specific by time, tau, or distance.
 *
 * @author Antonio Freixas
 */
public class IntervalObserver extends Observer
{
    private final ConcreteObserver observer;
    private final Interval interval;

    private final WorldlineEndpoint min;
    private final WorldlineEndpoint max;

    private final ArrayList<WorldlineSegment> segments;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Crete an interval observer given another observer and an interval.
     *
     * @param observer The observer to use.
     * @param interval The interval to apply to the observer.
     */
    public IntervalObserver(Observer observer, Interval interval)
    {
        if (observer == null) throw new ExecutionException("setInterval() has a null observer");
        if (interval == null) throw new ExecutionException("setInterval() has a null interval");

        // If the observer is itself an IntervalObserver, grab the embedded
        // ConcreteObserver inside it

        if (observer instanceof IntervalObserver intervalObserver) {
            this.observer = intervalObserver.observer;
        }

        // If the observer is a ConcreteObserver, use it

        else if (observer instanceof ConcreteObserver concreteObserver) {
            this.observer = concreteObserver;
        }

        // Anything else is a programming error

        else {
            throw new ProgrammingException("IntervalObserver: observer is not an interval or concrete observer");
        }

        // The interval is never used except in the constructor and for display

	    this.interval = interval;

        double minT = 0;
        double maxT = 0;

        // Inside this class, the interval is always a time range

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

        // Find the minimum and maximum points for the observer's worldline
        // that are within the interval

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

        // Create a modified list of segments that are exactly within the interval

        this.segments = new ArrayList<>();

        for (WorldlineSegment segment : observer.getSegments()) {
            int inRange = inRange(segment);
            if (inRange == 1) break;        // Past the interval
            if (inRange == 0) {             // Some part is inside the interval

                WorldlineEndpoint sMin = segment.getMin();
                WorldlineEndpoint sMax = segment.getMax();

                // If the current segment is completely inside the interval,
                // just copy a reference to it to our segment list

                if (Util.fuzzyLE(minT, sMin.t) && Util.fuzzyGE(maxT, sMax.t)) {
                    segments.add(segment);
                    continue;
                }

                // The current segment must be partly inside the interval. We
                // need to create a segment that is entirely within the interval

                WorldlineSegment newSegment = new WorldlineSegment(segment, minT, maxT);
                segments.add(newSegment);
            }
        }
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
        return segments;
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

        double minT = Double.isInfinite(min.t) ? min.t : prime.toFrame(min.x, min.t).t;
        double maxT = Double.isInfinite(max.t) ? max.t : prime.toFrame(max.x, max.t).t;

        return new IntervalObserver(relObserver, new Interval(Interval.Type.T, minT, maxT));
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
        for (WorldlineSegment segment : segments) {
            double x = segment.vToX(v);
            if (!Double.isNaN(x)) return x;
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
        for (WorldlineSegment segment : segments) {
            double d = segment.vToD(v);
            if (!Double.isNaN(d)) return d;

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
        for (WorldlineSegment segment : segments) {
            double t = segment.vToT(v);
            if (!Double.isNaN(t)) return t;

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
        for (WorldlineSegment segment : segments) {
            double tau = segment.vToTau(v);
            if (!Double.isNaN(tau)) return tau;
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
        for (WorldlineSegment segment : segments) {
            double v = segment.dToV(d);
            if (!Double.isNaN(d)) return v;
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
        for (WorldlineSegment segment : segments) {
            double x = segment.dToX(d);
            if (!Double.isNaN(x)) return x;

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
        for (WorldlineSegment segment : segments) {
            double t = segment.dToT(d);
            if (!Double.isNaN(t)) return t;
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
        for (WorldlineSegment segment : segments) {
            double tau = segment.dToTau(d);
            if (!Double.isNaN(tau)) return tau;

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
        for (WorldlineSegment segment : segments) {
            double v = segment.tToV(t);
            if (!Double.isNaN(v)) return v;
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
        for (WorldlineSegment segment : segments) {
            double x = segment.tToX(t);
            if (!Double.isNaN(x)) return x;
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
        for (WorldlineSegment segment : segments) {
            double d = segment.tToD(t);
            if (!Double.isNaN(d)) return d;
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
        for (WorldlineSegment segment : segments) {
            double tau = segment.tToTau(t);
            if (!Double.isNaN(tau)) return tau;
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
        for (WorldlineSegment segment : segments) {
            double v = segment.tauToV(tau);
            if (!Double.isNaN(v)) return v;
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
        for (WorldlineSegment segment : segments) {
            double x = segment.tauToX(tau);
            if (!Double.isNaN(x)) return x;
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
        for (WorldlineSegment segment : segments) {
            double d = segment.tauToD(tau);
            if (!Double.isNaN(d)) return d;
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
        for (WorldlineSegment segment : segments) {
            double t = segment.tauToT(tau);
            if (!Double.isNaN(t)) return t;
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
        for (WorldlineSegment segment : segments) {
            OffsetAcceleration curve = segment.getCurve();
            CurveSegment curveSegment = segment.getCurveSegment();

            Coordinate[] results = curve.intersect(line);
            if (results == null) continue;

            // Return the first intersection that occurs within the bounds
            // of this segment (if any)

            for (Coordinate intersection : results) {

                // Find out if this intersection occurs within the bounds of this
                // segment and within our interval

                if (curveSegment.getBounds().inside(intersection))
                    return intersection;
            }
        }
        return null;
    }

    /**
     * Find the intersection of this worldline with another. We check segments
     * one at a time from earliest to latest. We check our first segment against
     * all the other worldline's segments, then our second segment, etc. We
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
            for (WorldlineSegment segment1 : otherObserver.getSegments()) {
                for (WorldlineSegment segment2 : observer.getSegments()) {
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
        if (Util.fuzzyGT(min.t, segment.getMax().t)) return -1;
        if (Util.fuzzyGT(segment.getMin().t, max.t)) return  1;
        return 0;
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
