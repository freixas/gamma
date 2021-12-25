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

import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An observer has an initial origin, tau and distance. The observer then
 * travels through as series of periods of constant velocity or constant
 * acceleration.
 *
 * @author Antonio Freixas
 */
public class ConcreteObserver extends Observer
{
    private final Worldline worldline;

    /**
     * Create an observer.
     *
     * @param initializer The data used to start the worldline.
     * @param segments  A list of segments with the information needed to
     * create the worldline segments. The segment list may be empty, All
     * segments must have a limit other than NONE except the last segment (where
     * we ignored the limit type anyway).
     */
    public ConcreteObserver(WInitializer initializer, ArrayList<WSegment> segments)
    {
        worldline = new Worldline(initializer);

        if (segments.size() < 1) {

            // Create a default segment

            segments.add(new WSegment(0.0, 0.0, WorldlineSegment.LimitType.NONE, Double.NaN));
        }

        Iterator<WSegment> iter = segments.iterator();
        while (iter.hasNext()) {
            WSegment wSegment = iter.next();

            double a = wSegment.getA();

            // Not the last segment

            if (iter.hasNext()) {
                worldline.addSegment(
                        wSegment.getType(),
                        wSegment.getDelta(),
                        a,
                        wSegment.getV());
            }

            // The last segment

            else {
                worldline.addFinalSegment(a, wSegment.getV());
            }
        }
    }

    private ConcreteObserver(Worldline worldline)
    {
        this.worldline = worldline;
    }

    @Override
    public Worldline getWorldline()
    {
        return worldline;
    }

    /**
     * Create a new version of this observer that is relative to the given
     * frame rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new observer.
     */
    @Override
    public Observer relativeTo(Frame prime)
    {
        Worldline newWorldline = worldline.relativeTo(prime);
        return new ConcreteObserver(newWorldline);
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
     * @throws ExecutionException When there are no matching x's.
     */
    @Override
    public double vToX(double v)
    {
        return worldline.vToX(v);
    }

    /**
     * Given v, return d. If v matches no segment points, return NaN. If v
     * matches all segment points return the d that occurs earliest in time.
     *
     * @param v The velocity.
     * @return The distance in the rest frame or NaN.
     * @throws ExecutionException When there are no matching d's.
     */
    @Override
    public double vToD(double v)
    {
        return worldline.vToD(v);
    }

    /**
     * Given v, return t. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest time.
     *
     * @param v The velocity.
     * @return The time in the rest frame or NaN.
     * @throws ExecutionException When there are no matching t's.
     */
    @Override
    public double vToT(double v)
    {
        return worldline.vToT(v);
    }

    /**
     * Given v, return tau. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest tau.
     *
     * @param v The velocity.
     * @return The time in the accelerated frame or NaN.
     * @throws ExecutionException When there are no matching tau's.
     */
    @Override
    public double vToTau(double v)
    {
        return worldline.vToTau(v);
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
     * @throws ExecutionException When there are no matching v's.
     */
    @Override
    public double dToV(double d)
    {
        return worldline.dToV(d);
    }

    /**
     * Given d, return x.
     *
     * @param d The distance in the rest frame
     * @return The position in the rest frame.
     * @throws ExecutionException When there are no matching x's.
     */
    @Override
    public double dToX(double d)
    {
        return worldline.dToX(d);
   }

    /**
     * Given d, return t.
     *
     * @param d The distance in the rest frame
     * @return The time in the rest frame.
     * @throws ExecutionException When there are no or infinite matching t's.
     */
    @Override
    public double dToT(double d)
    {
        return worldline.dToT(d);
    }

    /**
     * Given d, return tau.
     *
     * @param d The distance in the rest frame
     * @return The time in the accelerated frame.
     * @throws ExecutionException When there are no or infinite matching taus.
     */
    @Override
    public double dToTau(double d)
    {
        return worldline.dToTau(d);
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
        return worldline.tToV(t);
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
        return worldline.tToX(t);
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
        return worldline.tToD(t);
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
        return worldline.tToTau(t);
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
        return worldline.tauToV(tau);
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
        return worldline.tauToX(tau);
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
        return worldline.tauToD(tau);
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
        return worldline.tauToT(tau);
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
        return worldline.intersect(line);
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
        if (other instanceof ConcreteObserver concreteObserver) {
            return worldline.intersect(concreteObserver.worldline);
        }
        else if (other instanceof IntervalObserver intervalObserver) {
            return intervalObserver.intersect(this);
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
        return "[ Observer " + worldline.toDisplayableString(engine) + "]";
    }

    @Override
    public String toString()
    {
        return "Observer:\n" + worldline.toString().replaceAll("(?m)^", "  ");
    }

}
