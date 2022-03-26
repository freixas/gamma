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

import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.Relativity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A concrete observer  is the main implementation of an observer. The observer's
 * worldline is infinite.
 *
 * @author Antonio Freixas
 */
public class ConcreteObserver extends Observer
{
    private final Coordinate origin;
    private final double tauInit;
    private final double dInit;

    private final ArrayList<WorldlineSegment> segments;
    private transient WorldlineSegment lastSegment = null;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Create a concrete observer. This constructor defines the origin of
     *  the observer's worldline (which is at an arbitrary point) and the tau
     *  and distance values at the origin.
     *
     * @param initializer This defines the origin, and the tau and distance
     * values at the origin.
     */
    public ConcreteObserver(WInitializer initializer)
    {
        this.origin = initializer.getOrigin();
        this.tauInit = initializer.getTau();
        this.dInit =  initializer.getD();

        this.segments = new ArrayList<>();
    }

    /**
     *  Create a concrete observer. This constructor defines the origin of
     *  the observer's worldline (which is at an arbitrary point) and the tau
     *  and distance values at the origin. It also adds a series of segments
     *  that begin at the origin and continue toward positive time. Each segment
     *  has an initial velocity and a constant acceleration as well as a limit
     *  that defines how long the segment is.
     *
     * @param initializer This defines the origin, and the tau and distance
     * values at the origin.
     * @param wSegments The segments to add.
     */
    public ConcreteObserver(WInitializer initializer, ArrayList<WSegment> wSegments)
    {
        this.origin = initializer.getOrigin();
        this.tauInit = initializer.getTau();
        this.dInit =  initializer.getD();

        this.segments = new ArrayList<>();

        if (wSegments.size() < 1) {

            // Create a default segment

            wSegments.add(new WSegment(0.0, 0.0, WorldlineSegment.LimitType.NONE, Double.NaN));
        }

        Iterator<WSegment> iter = wSegments.iterator();
        while (iter.hasNext()) {
            WSegment wSegment = iter.next();

            double a = wSegment.getA();

            // Not the last segment

            if (iter.hasNext()) {
                addSegment(
                        wSegment.getType(),
                        wSegment.getLimit(),
                        a,
                        wSegment.getV());
            }

            // The last segment

            else {
                addFinalSegment(a, wSegment.getV());
            }
        }
    }

    /**
     * Copy constructor.
     *
     * @param other The other ConcreteObserver to copy.
     */
    @SuppressWarnings("unused")
    public ConcreteObserver(ConcreteObserver other)
    {
        this.origin = new Coordinate(other.origin);
        this.tauInit = other.tauInit;
        this.dInit = other.dInit;

        this.segments = new ArrayList<>();
        ExecutionMutableSupport.copy(other.segments, this.segments);
    }

    // **********************************************************************
    // *
    // * Segments
    // *
    // **********************************************************************

    private void addSegment(WorldlineSegment.LimitType type, double delta, double a, double v)
    {
        WorldlineSegment segment;

        // Add the first segment

        if (segments.size() < 1) {
            if (Double.isNaN(v)) v = 0.0;
            segment = new WorldlineSegment(type, delta, a, v, origin, tauInit, dInit);
            segment.setInfinitePast();
        }

        // Add any segment that is not the first and not the last

        else {
            WorldlineEndpoint maxLimit = lastSegment.getMax();
            if (Double.isNaN(v)) v = maxLimit.v;
            segment = new WorldlineSegment(type, delta, a, v, new Coordinate(maxLimit.x, maxLimit.t), maxLimit.tau, maxLimit.d);
        }

        segments.add(segment);
        lastSegment = segment;
    }

    private void addFinalSegment(double a, double v)
    {
        WorldlineSegment segment;
        if (segments.size() < 1) {
            if (Double.isNaN(v)) v = 0.0;
            segment = new WorldlineSegment(WorldlineSegment.LimitType.T, 0, a, v, origin, tauInit, dInit);
            segment.setInfinitePast();
            segment.setInfiniteFuture();
        }
        else {
            WorldlineEndpoint maxLimit = lastSegment.getMax();
            if (Double.isNaN(v)) v = maxLimit.v;
            segment = new WorldlineSegment(WorldlineSegment.LimitType.T, 0, a, v, new Coordinate(maxLimit.x, maxLimit.t), maxLimit.tau, maxLimit.d);
            segment.setInfiniteFuture();
        }

        segments.add(segment);
        lastSegment = segment;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

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
    public ConcreteObserver relativeTo(Frame prime)
    {
        ConcreteObserver worldline = new ConcreteObserver(new WInitializer(prime.toFrame(origin), tauInit, dInit));

        ArrayList<WorldlineSegment>newSegments = worldline.getSegments();

        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment = iter.next();
            WorldlineSegment newSegment;

            newSegment = new WorldlineSegment(
                segment.getA(),
                Relativity.vPrime(segment.getOriginalMin().v, prime.getV()),
                prime.toFrame(segment.getOriginalMin().x, segment.getOriginalMin().t),
                prime.toFrame(segment.getOriginalMax().x, segment.getOriginalMax().t),
                segment.getOriginalMin().tau, segment.getOriginalMin().d);
            newSegments.add(newSegment);

            // Last segment

            if (!iter.hasNext()) {

                // One and only segment

                if (segments.size() == 1) {
                    newSegment.setInfinitePast();
                    newSegment.setInfiniteFuture();
                }

                // Last, but not first, segment

                else {
                    newSegment.setInfiniteFuture();
                }
            }

            // First, but not last, segment

            else if (newSegments.size() == 1) {
                newSegment.setInfinitePast();
            }
        }

        return worldline;
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
     * @return The velocity or NaN.
     */
    @Override
    public double dToV(double d)
    {
        for (WorldlineSegment segment : segments) {
            double v = segment.dToV(d);
            if (!Double.isNaN(v)) return v;
        }
        return Double.NaN;
    }

    /**
     * Given d, return x.
     *
     * @param d The distance in the rest frame
     * @return The position in the rest frame or NaN.
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
     * @return The time in the rest frame or NaN.
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
     * @return The time in the accelerated frame or NaN.
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
            Coordinate coord = segment.intersect(line);
            if (coord != null) return coord;
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
        else if (other instanceof ConcreteObserver concreteObserver) {
            for (WorldlineSegment segment1 : segments) {
                for (WorldlineSegment segment2 : concreteObserver.segments) {
                    Coordinate coord = segment1.intersect(segment2);
                    if (coord != null) return coord;
                }
            }
        }
        return null;
    }

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        StringBuilder str = new StringBuilder(
            "Observer:\n" +
            "origin " + origin.toDisplayableString(engine) +
            ", tau at origin " + engine.toDisplayableString(tauInit) +
            ", distance at origin " + engine.toDisplayableString(dInit) + "\n");

        if (segments.size() > 0) {
            for (int i = 0; i < segments.size(); i++) {
                str.append(String.format("  %2d)", i + 1));
                str.append(" ");
                str.append(segments.get(i).toDisplayableString(engine));
                str.append("\n");
            }
        }
        return str.toString();
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        StringBuilder str =
            new StringBuilder("[ Observer " +
                "Origin          : " + origin + "\n" +
                "Initial tau     : " + tauInit + "\n" +
                "Initial distance: " + dInit + "\n");

        if (segments.size() > 0) {
            str.append("Segments:");

            for (int i = 0; i < segments.size(); i++) {
                str.append("\n  ").append(i).append(": ").append(segments.get(i).toString().replaceAll("(?m)^", "  "));
            }
        }
        else {
            str.append("No segments");
        }
        str.append("]");
        return str.toString();
    }

}
