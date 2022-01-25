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
import java.util.ArrayList;

/**
 * An observer has an initial origin, tau and distance. The observer then
 * travels through as series of periods of constant velocity or constant
 * acceleration.
 *
 * @author Antonio Freixas
 */
abstract public class Observer implements ExecutionImmutable, Displayable
{
    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the worldline segments in this worldline.
     *
     * @return The worldline segments in this worldline.
     */
    abstract public ArrayList<WorldlineSegment> getSegments();

    /**
     * Create a new version of this observer that is relative to the given
     * frame rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new observer.
     */
    abstract public Observer relativeTo(Frame prime);

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
    abstract public double vToX(double v);

    /**
     * Given v, return d. If v matches no segment points, return NaN. If v
     * matches all segment points return the d that occurs earliest in time.
     *
     * @param v The velocity.
     * @return The distance in the rest frame or NaN.
     * @throws ExecutionException When there are no matching d's.
     */
    abstract public double vToD(double v);

    /**
     * Given v, return t. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest time.
     *
     * @param v The velocity.
     * @return The time in the rest frame or NaN.
     * @throws ExecutionException When there are no matching t's.
     */
    abstract public double vToT(double v);

    /**
     * Given v, return tau. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest tau.
     *
     * @param v The velocity.
     * @return The time in the accelerated frame or NaN.
     * @throws ExecutionException When there are no matching tau's.
     */
    abstract public double vToTau(double v);

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
    abstract public double dToV(double d);

    /**
     * Given d, return x.
     *
     * @param d The distance in the rest frame
     * @return The position in the rest frame.
     * @throws ExecutionException When there are no matching x's.
     */
    abstract public double dToX(double d);

    /**
     * Given d, return t.
     *
     * @param d The distance in the rest frame
     * @return The time in the rest frame.
     * @throws ExecutionException When there are no or infinite matching t's.
     */
    abstract public double dToT(double d);

    /**
     * Given d, return tau.
     *
     * @param d The distance in the rest frame
     * @return The time in the accelerated frame.
     * @throws ExecutionException When there are no or infinite matching taus.
     */
    abstract public double dToTau(double d);

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
    abstract public double tToV(double t);

    /**
     * Given t, return x.
     *
     * @param t The time in the rest frame.
     * @return The position in the rest frame.
     */
    abstract public double tToX(double t);

    /**
     * Given t, return d.
     *
     * @param t The time in the rest frame.
     * @return The distance in the rest frame.
     */
    abstract public double tToD(double t);

    /**
     * Given t, calculate tau.
     *
     * @param t The time in the rest frame.
     * @return The time in the accelerated frame.
     */
    abstract public double tToTau(double t);

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
    abstract public double tauToV(double tau);

    /**
     * Given tau, return x.
     *
     * @param tau The time in the accelerated frame.
     * @return The position in the rest frame.
     */
    abstract public double tauToX(double tau);

    /**
     * Given tau, return d.
     * <p>
     * Since initially v = 0, if the acceleration is 0, then d = 0.
     *
     * @param tau The time in the accelerated frame.
     * @return The distance in the rest frame.
     */
    abstract public double tauToD(double tau);

    /**
     * Given tau, return t.
     *
     * @param tau The time in the accelerated frame.
     * @return The time in the rest frame.
     */
    abstract public double tauToT(double tau);

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
    abstract public Coordinate intersect(Line line);

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
    abstract public Coordinate intersect(Observer other);

}
