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
import org.freixas.gamma.math.OffsetAcceleration;
import org.freixas.gamma.math.Util;

/**
 * This is a hyperbolic segment between two points.
 *
 * @author Antonio Freixas
 */
public class HyperbolicSegment extends CurveSegment implements ExecutionImmutable
{
    private final double a;
    private final WorldlineEndpoint min;
    private final WorldlineEndpoint max;
    private final OffsetAcceleration curve;
    private final Bounds bounds;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a hyperbolic segment. The acceleration cannot be 0.
     *
     * @param a The acceleration.
     * @param min The endpoint of the segment that occurs earlier in time.
     * @param max The endpoint of the segment that occurs later in time.
     * @param curve The OffsetAcceleration curve along which this hyperbolic
     * segment lies.
     */
    public HyperbolicSegment(double a, WorldlineEndpoint min,
                             WorldlineEndpoint max, OffsetAcceleration curve)
    {
        if (Util.fuzzyZero(a)) {
            throw new ProgrammingException("Hyperbolic Segment(): Acceleration is 0");
        }
        if (Util.fuzzyGT(min.t, max.t)) {
            throw new ProgrammingException("Hypebolic Segment(): Min is greater than max");
        }
        this.a = a;
        this.min = min;
        this.max = max;
        this.curve = curve;
        this.bounds = getBounds(min.x, min.t, max.x, max.t, curve);
    }

    /**
     * Create a hyperbolic segment. The acceleration cannot be 0.
     * <p>
     * The endpoints can be deduced from the starting and ending times and the
     * offset acceleration curve.
     *
     * @param a The acceleration.
     * @param minT The time at which the segment starts.
     * @param maxT The time at which the segment ends.
     * @param curve  The offset acceleration curve along which this hyperbolic
     * segment lies.
     */
    public HyperbolicSegment(double a, double minT, double maxT, OffsetAcceleration curve)
    {
        if (Util.fuzzyZero(a)) {
            throw new ProgrammingException("Hyperbolic Segment(): Acceleration is 0");
        }
        if (Util.fuzzyGT(minT, maxT)) {
            throw new ProgrammingException("Hypebolic Segment(): Min time is greater than max time");
        }
        this.a = a;
        this.min = new WorldlineEndpoint(minT, curve);
        this.max = new WorldlineEndpoint(maxT, curve);
        this.curve = curve;
        this.bounds = getBounds(min.x, min.t, max.x, max.t, curve);
    }

    /**
     * Copy constructor.
     *
     * @param other The other segment to copy.
     */
    public HyperbolicSegment(HyperbolicSegment other)
    {
        this.a = other.a;
        this.min = other.min;
        this.max = other.max;
        this.curve = other.curve;
        this.bounds = other.bounds;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the acceleration.
     *
     * @return The acceleration.
     */
    public double getA()
    {
        return a;
    }

    /**
     * Get the minimum endpoint, the one that occurs earlier in time.
     *
     * @return The minimum endpoint.
     */
    public WorldlineEndpoint getMin()
    {
        return min;
    }

    /**
     * Get the maximum endpoint, the one that occurs later in time.
     *
     * @return The minimum endpoint.
     */
   public WorldlineEndpoint getMax()
    {
        return max;
    }

    /**
     * Get the OffsetAcceleration curve along which this hyperbolic segment
     * lies.
     *
     * @return The OffsetAcceleration curve along which this hyperbolic segment
     * lies.
     */
    public OffsetAcceleration getCurve()
    {
        return curve;
    }

    // **********************************************************************
    // *
    // * CurveSegment Support
    // *
    // **********************************************************************

    @Override
    public Bounds getBounds()
    {
        return new Bounds(bounds);
    }

    // **********************************************************************
    // *
    // * Intersection
    // *
    // **********************************************************************

    /**
     * Intersect this hyperbolic segment with a bounding box. This method
     * returns a hyperbolic segment that lies at least partly within the bounds
     * or null if there is no intersection.
     *
     * @param bounds The bounds to intersect with.
     *
     * @return The hyperbolic segment that lies at least partly within the
     * bounding box or null if there is no intersection.
     */
    public HyperbolicSegment intersect(Bounds bounds)
    {
        // Intersect the bounds with the segment along the time axis

        if (min.t > bounds.max.t) {
            return null;
        }
        double minT = Math.max(min.t, bounds.min.t);

        if (max.t < bounds.min.t) {
            return null;
        }
        double maxT = Math.min(max.t, bounds.max.t);

        // Find the x values corresponding to the t intersections

        double minX = curve.tToX(minT);
        double maxX = curve.tToX(maxT);

        // We can use the min and max coordinates to form a new bounding box
        // for the segment. Intersect these bounds with the ones we were
        // originally given. Even though we know the t coordinates lie within
        // the bounds, we don't know that the x coordinates do (the segment
        // might be to the left or right of the bounding box). If there is no
        // intersection, we're done

        Bounds intersection = getBounds(minX, minT, maxX, maxT, curve).intersect(bounds);
        if (intersection == null) return null;

        // Now we'll create a new hyperbolic segment based on the left and
        // right sides of the intersection.

        minT = intersection.min.t;
        maxT = intersection.max.t;

        // If the segment's minimum x is outside the intersection, find its
        // corresponding t value (if it is inside, we leave the t value alone)

        if (minX < intersection.min.x) {
            minT = xToTWithin(intersection.min.x, minT, maxT, false);
        }
        else if (minX > intersection.max.x) {
            minT = xToTWithin(intersection.max.x, minT, maxT, false);
        }

        // Do the same for the max x

        if (maxX < intersection.min.x) {
            maxT = xToTWithin(intersection.min.x, minT, maxT, true);
        }
        else if (maxX > intersection.max.x) {
            maxT = xToTWithin(intersection.max.x, minT, maxT, true);
        }

        // We create one more HyperbolicSegment

        return new HyperbolicSegment(a, minT, maxT, curve);
    }

    // **********************************************************************
    // *
    // * Display Support
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        return "HyperbolicSegment{" +
               "\n  a=" + a +
               "\n  min=" + min +
               "\n  max=" + max +
               "\n}";
    }

    // **********************************************************************
    // *
    // * Private
    // *
    // **********************************************************************

    /**
     * Get the bounding box for an offset acceleration curve with the given
     * minimum and maximum values.
     *
     * @param minX The minimum X coordinate.
     * @param minT The minimum T coordinate. This should be less than the
     * maximum T coordinate.
     * @param maxX The maximum X coordinate.
     * @param maxT The maximum T coordinate. This should be greater than the
     * maximum T coordinate.
     * @param curve The offset acceleration curve.
     *
     * @return The bounding box for the offset acceleration curve.
     */
    static private Bounds getBounds(
        double minX, double minT, double maxX, double maxT, OffsetAcceleration curve)
    {
        Coordinate offset = curve.getOffset();
        Bounds bounds;

        // We need to see if the turn-around point for the curve is included
        // in this segment. The turn-around point is the offset point.

        if (Util.fuzzyLE(minT, offset.t) && Util.fuzzyLE(offset.t, maxT)) {

            // We include the turn-around point. In this case, bounds depend
            // on whether acceleration is positive or negative

            if (Util.fuzzyLT(curve.getA(), 0)) {
                bounds = new Bounds(Math.min(minX, maxX), minT, offset.x, maxT);
            }
            else {
                bounds = new Bounds(offset.x, minT, Math.max(minX, maxX), maxT);
            }
        }

        // Otherwise, we can treat this just like a line segment: the bounding
        // box for the segment is the same as the bounding box for the endpoints

        else {
            bounds = new Bounds(minX, minT, maxX, maxT);
        }

        return bounds;
    }

    /**
     * Given a hyperbolic curve, when we convert an x coordinate to a t
     * coordinate the x-to-t mapping can yield two solutions. This method
     * looks at both options and picks the one that's within a given range.
     *
     * @param x The x coordinate.
     * @param min The minimum t coordinate allowed (inclusive).
     * @param max The maxiumum t coordinate allowed (inclusive).
     * @param later Prefer the t value later in time.
     *
     * @return The t coordinate that lies between the min and max values.
     */
    private double xToTWithin(double x, double min, double max, boolean later)
    {
        // Get both possible t values

        double t1 = curve.xToT(x, false);
        double t2 = curve.xToT(x, true);

        // If the earlier one lies outside the range, return the later one

        if (Util.fuzzyLT(t1, min) || Util.fuzzyGT(t1, max)) return t2;

        // If the later one lies outside the range, return  the earlier one

        if (Util.fuzzyLT(t2, min) || Util.fuzzyGT(t2, max)) return t1;

        // If both are inside the range, return the larger t value if later
        // was true, otherwise return the smaller t value

        if (later) return Math.max(t1, t2);
        return Math.min(t1, t2);

    }

}
