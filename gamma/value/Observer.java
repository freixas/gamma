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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * An observer has an initial origin, tau and distance. The observer then
 * travels through as series of periods of constant velocity or constant
 * acceleration.
 *
 * @author Antonio Freixas
 */
public class Observer
{
    private final Worldline worldline;

//    private final WInitializer initializer;
//    private final ArrayList<WSegment> segments;

    /**
     * Create an observer.
     *
     * @param initializer The data used to start the worldline.
     * @param segments  A list of segments with the information needed to
     * create the worldline segments. The segment list may be empty, All
     * segments must have a limit other than NONE except the last segment (where
     * we ignored the limit type anyway).
     */
    public Observer(WInitializer initializer, ArrayList<WSegment> segments)
    {
//        this.initializer = initializer;
//        this.segments = segments;

        worldline = new Worldline(initializer);

        if (segments.size() < 1) {

            // Create a default segment

            segments.add(new WSegment(0.0, 0.0, WorldlineSegment.LimitType.NONE, Double.NaN));
        }

        Iterator<WSegment> iter = segments.iterator();
        while (iter.hasNext()) {
            WSegment wSegment = iter.next();

        // Temporary hack - TO DO !!!!!!!!!!!
        // Convert acceleration to proper units

        double a = wSegment.getA() * 1.032295276;

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

    private Observer(Worldline worldline)
    {
        this.worldline = worldline;
    }

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
    public Observer relativeTo(Frame prime)
    {
        Worldline newWorldline = worldline.relativeTo(prime);
        return new Observer(newWorldline);
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
        return worldline.dToV(d);
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
        return worldline.dToX(d);
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
        return worldline.dToT(d);
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
    public Coordinate intersect(Observer other)
    {
        return worldline.intersect(other.worldline);

    }

    @Override
    public String toString()
    {
        return "Observer:\n" + worldline.toString().replaceAll("(?m)^", "  ");
    }


}
