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
package gamma.value;

import gamma.ProgrammingException;
import gamma.value.Coordinate;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Antonio Freixas
 */
public class Worldline
{
    private final Coordinate origin;
    private final double tauInit;
    private final double dInit;

    private final LinkedList<WorldlineSegment> segments;
    private WorldlineSegment lastSegment = null;

    public Worldline(WInitializer initializer)
    {
        this.origin = initializer.getOrigin();
        this.tauInit = initializer.getTau();
        this.dInit =  initializer.getD();

        segments = new LinkedList<>();
    }

    public void addSegment(WorldlineSegment.LimitType type, double delta, double a, double v)
    {
        WorldlineSegment segment;
        if (segments.size() < 1) {
            segment = new WorldlineSegment(type, delta, a, v, origin, tauInit, dInit);
            segment.setInfinitePast();
        }
        else {
            WorldlineSegment.Endpoint maxLimit = lastSegment.getMax();
            segment = new WorldlineSegment(type, delta, a, v, new Coordinate(maxLimit.x, maxLimit.t), maxLimit.tau, maxLimit.d);
        }

        segments.add(segment);
        lastSegment = segment;
    }

    public void addFinalSegment(double a, double v)
    {
        WorldlineSegment segment;
        if (segments.size() < 1) {
            segment = new WorldlineSegment(WorldlineSegment.LimitType.T, 0, a, v, origin, tauInit, dInit);
            segment.setInfinitePast();
            segment.setInfiniteFuture();
        }
        else {
            WorldlineSegment.Endpoint maxLimit = lastSegment.getMax();
            segment = new WorldlineSegment(WorldlineSegment.LimitType.T, 0, a, v, new Coordinate(maxLimit.x, maxLimit.t), maxLimit.tau, maxLimit.d);
        }

        segment.setInfiniteFuture();
        segments.add(segment);
        lastSegment = segment;
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
     * @throws ArithmeticException When there are no matching v's.
     */
    public double dToV(double d)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double v = iter.next().dToV(d);
            if (!Double.isNaN(d)) return v;
        }
        throw new ArithmeticException("dToV(): No matching velocity for d.");
    }

    /**
     * Given d, return x.
     *
     * @param d The distance in the rest frame
     * @return The position in the rest frame.
     * @throws ArithmeticException When there are no matching x's.
     */
    public double dToX(double d)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double x = iter.next().dToX(d);
            if (!Double.isNaN(x)) return x;
        }
        throw new ArithmeticException("dToX(): No matching x coordinate for d.");
   }

    /**
     * Given d, return t.
     *
     * @param d The distance in the rest frame
     * @return The time in the rest frame.
     * @throws ArithmeticException When there are no or infinite matching t's.
     */
    public double dToT(double d)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double t = iter.next().dToT(d);
            if (!Double.isNaN(t)) return t;
        }
        throw new ArithmeticException("dToT(): No or infinite matching times for d.");
    }

    /**
     * Given d, return tau.
     *
     * @param d The distance in the rest frame
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When there are no or infinite matching taus.
     */
    public double dToTau(double d)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double tau = iter.next().dToTau(d);
            if (!Double.isNaN(tau)) return tau;
        }
        throw new ArithmeticException("dToTau(): No or infinite matching taus for d.");
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
    public double tToV(double t)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double v = iter.next().tToV(t);
            if (!Double.isNaN(v)) return v;
        }
        throw new ProgrammingException("tToV(): No matching velocity for t.");
     }

    /**
     * Given t, return x.
     *
     * @param t The time in the rest frame.
     * @return The position in the rest frame.
     */
    public double tToX(double t)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double x = iter.next().tToX(t);
            if (!Double.isNaN(x)) return x;
        }
        throw new ProgrammingException("tToX(): No matching x coordiante for t.");
    }

    /**
     * Given t, return d.
     *
     * @param t The time in the rest frame.
     * @return The distance in the rest frame.
     */
    public double tToD(double t)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double d = iter.next().tToD(t);
            if (!Double.isNaN(d)) return d;
        }
        throw new ProgrammingException("tToD(): No matching distance for t.");
    }

    /**
     * Given t, calculate tau.
     *
     * @param t The time in the rest frame.
     * @return The time in the accelerated frame.
     */
    public double tToTau(double t)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double tau = iter.next().tToTau(t);
            if (!Double.isNaN(tau)) return tau;
        }
        throw new ProgrammingException("tToTau(): No matching tau for t.");
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
    public double tauToV(double tau)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double v = iter.next().tauToV(tau);
            if (!Double.isNaN(v)) return v;
        }
        throw new ProgrammingException("tauToV(): No matching velocity for tau.");
    }

    /**
     * Given tau, return x.
     *
     * @param tau The time in the accelerated frame.
     * @return The position in the rest frame.
     */
    public double tauToX(double tau)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double x = iter.next().tauToX(tau);
            if (!Double.isNaN(x)) return x;
        }
        throw new ProgrammingException("tauToX(): No matching x coordinate for tau.");
    }

    /**
     * Given tau, return d.
     * <p>
     * Since initially v = 0, if the acceleration is 0, then d = 0.
     *
     * @param tau The time in the accelerated frame.
     * @return The distance in the rest frame.
     */
    public double tauToD(double tau)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double d = iter.next().tauToD(tau);
            if (!Double.isNaN(d)) return d;
        }
        throw new ProgrammingException("tauToD(): No matching distance for tau.");
    }

    /**
     * Given tau, return t.
     *
     * @param tau The time in the accelerated frame.
     * @return The time in the rest frame.
     */
    public double tauToT(double tau)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            double t = iter.next().tauToT(tau);
            if (!Double.isNaN(t)) return t;
        }
        throw new ProgrammingException("tauToT(): No matching time for tau.");
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
    public Coordinate intersect(Line line)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            Coordinate coord = iter.next().intersect(line);
            if (coord != null) return coord;
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
    public Coordinate intersect(Worldline other)
    {
        ListIterator<WorldlineSegment> iter = segments.listIterator();
        while (iter.hasNext()) {
            WorldlineSegment segment1 = iter.next();
            ListIterator<WorldlineSegment> iter2 = other.segments.listIterator();
            while (iter2.hasNext()) {
                WorldlineSegment segment2 = iter2.next();
                Coordinate coord = segment1.intersect(segment2);
                if (coord != null) return coord;
            }
        }
        return null;
    }
}
