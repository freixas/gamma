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
import gamma.execution.ExecutionException;
import gamma.math.OffsetAcceleration;
import gamma.math.Util;

/**
 * A worldline segment is a segment of an offset acceleration curve. See
 * the OffsetAcceleration class for the details of an offset acceleration
 * curve.
 * <p>
 * The offset acceleration curve is specified with the same parameters as in
 * the OffsetAcceleration class. The vPoint also serves as the starting point
 * for the segment, which is always the earlier of the segment's two endpoints.
 * <p>
 * The later endpoint is defined by applying a delta time, tau, or distance to
 * the earlier endpoint. The delta must be a positive value.
 * <p>
 * Once a segment has been created, it can be modified to extend backward or
 * forward in time (or both) by an infinite amount.
 * <p>
 * A WorldlineSegment provides most but not all of the functions found in
 * OffsetAcceleration. The functions work a bit differently: if a specific
 * source value does not fall between the start and end of the segment, then
 * NaN is returned.
 * <p>
 * Here's the list of supported methods:
 * <table>
 * <caption><em>List of methods</em></caption>
 * <tr>
 * <td></td>
 * <td><b>v</b></td>
 * <td><b>d</b></td>
 * <td><b>t</b></td>
 * <td><b>tau</b></td>
 * </tr>
 * <tr>
 * <td><b>v</b></td>
 * <td>-</td>
 * <td>dToV()</td>
 * <td>tToV()</td>
 * <td>tauToV()</td>
 * </tr>
 * <tr>
 * <td><b>x</b></td>
 * <td>vToX()</td>
 * <td>dToX()</td>
 * <td>tToX()</td>
 * <td>tauToX()</td>
 * </tr>
 * <tr>
 * <td><b>d</b></td>
 * <td>vToD()</td>
 * <td>-</td>
 * <td>tToD()</td>
 * <td>tauToD()</td>
 * </tr>
 * <tr>
 * <td><b>t</b></td>
 * <td>vToD()</td>
 * <td>dToT()</td>
 * <td>-</td>
 * <td>tauToT()</td>
 * </tr>
 * <tr>
 * <td><b>tau</b></td>
 * <td>vToTau()</td>
 * <td>dToTau()</td>
 * <td>tToTau()</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td><b>gamma</b></td>
 * <td>vToGamma()</td>
 * <td>dToGamma()</td>
 * <td>tToGamma()</td>
 * <td>tauToGamma()</td>
 * </tr>
 * </table>
 * <p>
 * <strong>Units</strong>
 * <p>
 * The equations have been simplified by assuming c = 1, so the velocities need
 * to be in fractions of light speed. This means that if stdT is in seconds,
 * stdX is in light seconds, etc.
 * <p>
 * The units don't matter as long as they are consistent.
 * <p>
 * The tricky one is acceleration. If time is in years, distance should be in
 * light years, and acceleration should be in light years per years squared. For
 * a 1g acceleration using units years and light years, 1 g = 1.03227407852535
 * ly / year<sup>2</sup>.
 * <p>
 * See http://web.physics.ucsb.edu/~fratus/phys103/LN/IGR.pdf See
 * https://math.ucr.edu/home/baez/physics/Relativity/SR/Rocket/rocket.html
 *
 * @see gamma.math.Acceleration
 * @see gamma.math.OffsetAcceleration
 *
 * @author Antonio Freixas
 */
public class WorldlineSegment
{

    /**
     * The type of the delta value used to construct the worldline segment.
     */
    public enum LimitType
    {
        NONE, T, TAU, D
    }

    private final HyperbolaEndpoint min;
    private final HyperbolaEndpoint max;

    private final HyperbolaEndpoint originalMin;
    private final HyperbolaEndpoint originalMax;

    private final double a;
    private final OffsetAcceleration curve;
    private CurveSegment curveSegment;

    /**
     * Create a new WorldlineSegment.
     *
     * @param type The type of the delta value.
     * @param delta A non-negative increment to the time, tau, or distance.
     * @param a The acceleration.
     * @param v The velocity at vPoint.
     * @param vPoint A point at which the velocity of the acceleration curve is v.
     * @param tau Tau at vPoint.
     * @param d Distance at vPoint.
     * @throws IllegalArgumentException if the type is invalid or delta is negative.
     */
    public WorldlineSegment(LimitType type, double delta, double a, double v,
                            Coordinate vPoint, double tau, double d)
    {
        if (delta < 0) {
            throw new ExecutionException("Observer segment limit delta cannot be negative");
        }

        this.a = a;
        this.min = new HyperbolaEndpoint(v, vPoint.x, vPoint.t, tau, d);

        // Create the offset curve

        this.curve = new OffsetAcceleration(a, v, vPoint, tau, d);

        double maxT = vPoint.t;
        if (null != type) // Convert all deltas to standard form (offset by time)
        {
            switch (type) {
                case T ->
                    maxT = vPoint.t + delta;
                case TAU -> {
                    double finalTau = tau + delta;
                    if (delta == 0.0) {
                        maxT = vPoint.t;
                    }
                    maxT = curve.tauToT(finalTau);
                }
                case D -> {
                    double finalD = d + delta;

                    // If the distance moved is 0, we have a zero-length
                    // segment

                    if (delta == 0.0) {
                        maxT = vPoint.t;
                    }
                    else if (delta > 0.0 && a == 0.0 && v == 0.0) {
                        throw new ExecutionException("Observer segment distance delta is > 0, but acceleration and velocity are 0");
                    }
                    else {
                        maxT = curve.dToT(finalD);
                    }
                }
                case NONE -> {
                    // maxT already set to vPoint.t
                }
            }
        }

        // Generate the max limit. The x value won't be correct if the segment
        // includes the curve's turn-around point

        this.max = new HyperbolaEndpoint(maxT, curve);

        // At this point:
        //
        // max.v could be less than min.v
        // max.x could be less than min.x, which is OK because it is not used in a test
        // max.t is always greater or equal to than min.t
        // max.tau is always greater or equal to min.tau
        // max.d is always greater or equal to min.d
        //
        // Comparisons with v need to be done carefully

        if (Util.fuzzyZero(a)) {

            // We have a line segment

            curveSegment = new LineSegment(min.x, min.t, max.x, max.t);
        }
        else {

            // We have a hyperbolic segment

            curveSegment = new HyperbolicSegment(a, min, max, curve);
       }

        // We sometimes need to get the original endpoints and not ones
        // that have perhaps been modified to be at +/- infinit.

        this.originalMin = new HyperbolaEndpoint(min);
        this.originalMax = new HyperbolaEndpoint(max);
    }

    /**
     * This constructor is used only when transforming an existing
     * worldline to be relative to a new frame. It forces the endpoints of
     * each segment to match given values.
     *
     * @param a The acceleration.
     * @param v The velocity at the initial point.
     * @param start The starting coordinate.
     * @param end The ending coordinate.
     * @param tau Tau at the start.
     * @param d Distance at the start.
     */
    public WorldlineSegment(double a, double v, Coordinate start,
                            Coordinate end, double tau, double d)
    {
        this.a = a;
        this.min = new HyperbolaEndpoint(v, start.x, start.t, tau, d);
        this.curve = new OffsetAcceleration(a, v, start, tau, d);
        this.max = new HyperbolaEndpoint(end.t, curve);

        assert Util.fuzzyEQ(max.x, end.x);

        this.originalMin = new HyperbolaEndpoint(min);
        this.originalMax = new HyperbolaEndpoint(max);
        
        if (Util.fuzzyZero(a)) {

            // We have a line segment

            curveSegment = new LineSegment(min.x, min.t, max.x, max.t);
        }
        else {

            // We have a hyperbolic segment

            curveSegment = new HyperbolicSegment(a, min, max, curve);
       }
    }

    public OffsetAcceleration getCurve()
    {
        return curve;
    }

    public CurveSegment getCurveSegment()
    {
        return curveSegment;
    }

    public HyperbolaEndpoint getOriginalMin()
    {
        return originalMin;
    }

    public HyperbolaEndpoint getOriginalMax()
    {
        return originalMax;
    }

    /**
     * Set this segment to extend infinitely in time to the past.
     */
    public void setInfinitePast()
    {
        if (curveSegment instanceof LineSegment) {
            curveSegment = new Line(Line.AxisType.T, min.v, new Coordinate(min.x, min.t));
            ((Line)curveSegment).setIsInfinitePlus(false);
        }
        else if (curveSegment instanceof Line) {
            ((Line)curveSegment).setIsInfiniteMinus(true);
        }
        min.x = Util.fuzzyGT(a, 0) ? Double.POSITIVE_INFINITY : (Util.fuzzyLT(a, 0) ? Double.NEGATIVE_INFINITY : (Util.fuzzyGT(min.v, 0) ? Double.NEGATIVE_INFINITY : (Util.fuzzyLT(min.v, 0) ? Double.POSITIVE_INFINITY : min.x)));
        min.v = min.v < max.v ? Double.NEGATIVE_INFINITY: (min.v > max.v ? Double.POSITIVE_INFINITY : min.v);
        min.d = Util.fuzzyZero(a) && Util.fuzzyZero(min.v) ? min.d : Double.NEGATIVE_INFINITY;
        min.t = Double.NEGATIVE_INFINITY;
        min.tau = Double.NEGATIVE_INFINITY;
    }

    /**
     * Set this segment to extend infinitely in time to the future.
     */
    public void setInfiniteFuture()
    {
        if (curveSegment instanceof LineSegment) {
            curveSegment = new Line(Line.AxisType.T, max.v, new Coordinate(max.x, max.t));
            ((Line)curveSegment).setIsInfiniteMinus(false);
        }
        else if (curveSegment instanceof Line) {
            ((Line)curveSegment).setIsInfinitePlus(true);
        }
        max.x = Util.fuzzyGT(a, 0) ? Double.POSITIVE_INFINITY : (Util.fuzzyLT(a, 0) ? Double.NEGATIVE_INFINITY : (Util.fuzzyGT(max.v, 0) ? Double.POSITIVE_INFINITY : (Util.fuzzyLT(max.v, 0) ? Double.NEGATIVE_INFINITY : max.x)));
        max.v = Util.fuzzyLT(max.v, max.v) ? Double.POSITIVE_INFINITY: (Util.fuzzyGT(max.v, max.v) ? Double.NEGATIVE_INFINITY : max.v);
        max.d = Util.fuzzyZero(a) && Util.fuzzyZero(max.v) ? max.d : Double.POSITIVE_INFINITY;
        max.t = Double.POSITIVE_INFINITY;
        max.tau = Double.POSITIVE_INFINITY;
    }

    /**
     * Get the x, v, t, tau, and d values associated with the earlier endpoint.
     * These may be +infinite or -infinity. Modifying the returned object will
     * not affect this segment at all.
     *
     * @return A structure holding the x, v, t, tau, and d values associated
     * with the earlier endpoint.
     */
    public final HyperbolaEndpoint getMin()
    {
        return new HyperbolaEndpoint(min);
    }

    /**
     * Get the x, v, t, tau, and d values associated with the later endpoint.
     * These may be +infinite or -infinity. Modifying the returned object will
     * not affect this segment at all.
     *
     * @return A structure holding the x, v, t, tau, and d values associated
     * with the later endpoint.
     */
    public final HyperbolaEndpoint getMax()
    {
        return new HyperbolaEndpoint(max);
    }

    /**
     * Get the bounds for this segment.
     *
     * @return The bounds for this segment.
     */
    public Bounds getBounds()
    {
        return curveSegment.getBounds();
    }

    /**
     * Get the acceleration for this segment.
     *
     * @return The acceleration for this segment.
     */
    public double getA()
    {
        return a;
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
    public double vToX(double v)
    {
        if (Util.fuzzyLT(min.v, max.v) && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (Util.fuzzyGT(min.v, max.v) && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (Util.fuzzyEQ(min.v, max.v)) return min.x;
        return curve.vToX(v);
    }

    /**
     * Given v, return d. If v matches no segment points, return NaN. If v
     * matches all segment points return the d that occurs earliest in time.
     *
     * @param v The velocity.
     * @return The distance in the rest frame or NaN.
     */
    public double vToD(double v)
    {
        if (Util.fuzzyLT(min.v, max.v) && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (Util.fuzzyGT(min.v, max.v) && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (Util.fuzzyEQ(min.v, max.v)) return min.d;
        return curve.vToD(v);
    }

    /**
     * Given v, return t. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest time.
     *
     * @param v The velocity.
     * @return The time in the rest frame or NaN.
     */
    public double vToT(double v)
    {
        if (Util.fuzzyLT(min.v, max.v) && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (Util.fuzzyGT(min.v, max.v) && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (Util.fuzzyEQ(min.v, max.v)) return min.t;
        return curve.vToT(v);
    }

    /**
     * Given v, return tau. If v matches no segment points, return NaN. If v
     * matches all segment points return the earliest tau.
     *
     * @param v The velocity.
     * @return The time in the accelerated frame or NaN.
     */
    public double vToTau(double v)
    {
        if (Util.fuzzyLT(min.v, max.v) && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (Util.fuzzyGT(min.v, max.v) && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (Util.fuzzyEQ(min.v, max.v)) return min.tau;
         return curve.vToTau(v);
    }

    /**
     * Given v, return gamma. If v matches no segment points, return NaN. If v
     * matches all segment points return gamma for the v that occurs earliest in
     * time.
     *
     * @param v The velocity.
     * @return Gamma of NaN.
     */
    public double vToGamma(double v)
    {
        if (Util.fuzzyLT(min.v, max.v) && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (Util.fuzzyGT(min.v, max.v) && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        return curve.vToGamma(v);
    }

    // **********************************************************
    // *
    // * Source is d
    // *
    // **********************************************************

    /**
     * Given d, return v. If d matches no segment points, return NaN. If d
     * matches all segment points return the d that occurs earliest in time.
     *
     * @param d The distance in the rest frame
     * @return The velocity or NaN.
     */
    public double dToV(double d)
    {
        if (Util.fuzzyLT(d, min.d) || Util.fuzzyGT(d, max.d)) return Double.NaN;
        if (Util.fuzzyEQ(min.d, max.d)) return min.v;
        return curve.dToV(d);
     }

    /**
     * Given d, return x. If d matches no segment points, return NaN.
     *
     * @param d The distance in the rest frame
     * @return The position in the rest frame or NaN.
     */
    public double dToX(double d)
    {
        if (Util.fuzzyLT(d, min.d) || Util.fuzzyGT(d, max.d)) return Double.NaN;
        if (Util.fuzzyEQ(min.d, max.d)) return min.x;
        return curve.dToX(d);
    }

    /**
     * Given d, return t. If d matches no segment points, return NaN. If d
     * matches all segment points return the earliest time.
     *
     * @param d The distance in the rest frame
     * @return The time in the rest frame or NaN.
     */
    public double dToT(double d)
    {
        if (Util.fuzzyLT(d, min.d) || Util.fuzzyGT(d, max.d)) return Double.NaN;
        if (Util.fuzzyEQ(min.d, max.d)) return min.t;
        return curve.dToT(d);
    }

    /**
     * Given d, return tau. If d matches no segment points, return NaN. If d
     * matches all segment points return the earliest tau.
     *
     * @param d The distance in the rest frame
     * @return The time in the accelerated frame or NaN.
     */
    public double dToTau(double d)
    {
        if (Util.fuzzyLT(d, min.d) || Util.fuzzyGT(d, max.d)) return Double.NaN;
        if (Util.fuzzyEQ(min.d, max.d)) return min.tau;
        return curve.dToTau(d);
    }

    /**
     * Given d, return gamma.
     *
     * @param d The distance in the rest frame.
     * @return Gamma. If d matches no segment points, return NaN. If d matches
     * all segment points return gamma for the v that occurs earliest in time.
     */
     public double dToGamma(double d)
    {
        if (Util.fuzzyLT(d, min.d) || Util.fuzzyGT(d, max.d)) return Double.NaN;
        if (Util.fuzzyEQ(min.d, max.d)) return curve.vToGamma(min.v);
        return curve.dToGamma(d);
    }

    // **********************************************************
    // *
    // * fromT methods
    // *
    // **********************************************************

    /**
     * Given t, return v. If t matches no segment points, return NaN.
     *
     * @param t The time in the rest frame.
     * @return The velocity or NaN
     */
    public double tToV(double t)
    {
        if (Util.fuzzyLT(t, min.t) || Util.fuzzyGT(t, max.t)) return Double.NaN;
        if (Util.fuzzyEQ(min.t, max.t)) return min.v;
        return curve.tToV(t);
    }

    /**
     * Given t, return x. If t matches no segment points, return NaN.
     *
     * @param t The time in the rest frame.
     * @return The position in the rest frame or NaN.
     */
    public double tToX(double t)
    {
        if (Util.fuzzyLT(t, min.t) || Util.fuzzyGT(t, max.t)) return Double.NaN;
        if (Util.fuzzyEQ(min.t, max.t)) return min.x;
        return curve.tToX(t);
    }

    /**
     * Given t, return d. If t matches no segment points, return NaN.
     *
     * @param t The time in the rest frame.
     * @return The distance in the rest frame or NaN.
     */
    public double tToD(double t)
    {
        if (Util.fuzzyLT(t, min.t) || Util.fuzzyGT(t, max.t)) return Double.NaN;
        if (Util.fuzzyEQ(min.t, max.t)) return min.d;
        return curve.tToD(t);
    }

    /**
     * Given t, calculate tau. If t matches no segment points, return NaN.
     *
     * @param t The time in the rest frame.
     * @return The time in the accelerated frame.
     */
    public double tToTau(double t)
    {
        if (Util.fuzzyLT(t, min.t) || Util.fuzzyGT(t, max.t)) return Double.NaN;
        if (Util.fuzzyEQ(min.t, max.t)) return min.tau;
        return curve.tToTau(t);
    }

    /**
     * Given t, return gamma. If t matches no segment points, return NaN.
     *
     * @param t The time in the rest frame.
     * @return Gamma.
     */
    public double tToGamma(double t)
    {
        if (Util.fuzzyLT(t, min.t) || Util.fuzzyGT(t, max.t)) return Double.NaN;
        return curve.tToGamma(t);
    }

    // **********************************************************
    // *
    // * Source is tau
    // *
    // **********************************************************

    /**
     * Given tau, return v. If tau matches no segment points, return NaN.
     *
     * @param tau The time in the accelerated frame.
     * @return The velocity or NaN.
     */
    public double tauToV(double tau)
    {
        if (Util.fuzzyLT(tau, min.tau) || Util.fuzzyGT(tau, max.tau)) return Double.NaN;
        if (Util.fuzzyEQ(min.tau, max.tau)) return min.v;
        return curve.tauToV(tau);
    }

    /**
     * Given tau, return x. If tau matches no segment points, return NaN.
     *
     * @param tau The time in the accelerated frame.
     * @return The position in the rest frame or NaN.
     */
    public double tauToX(double tau)
    {
        if (Util.fuzzyLT(tau, min.tau) || Util.fuzzyGT(tau, max.tau)) return Double.NaN;
        if (Util.fuzzyEQ(min.tau, max.tau)) return min.x;
        return curve.tauToX(tau);
    }

    /**
     * Given tau, return d. If tau matches no segment points, return NaN.
     * <p>
     * Since initially v = 0, if the acceleration is 0, then d = 0.
     *
     * @param tau The time in the accelerated frame.
     * @return The distance in the rest frame or NaN.
     */
    public double tauToD(double tau)
    {
        if (Util.fuzzyLT(tau, min.tau) || Util.fuzzyGT(tau, max.tau)) return Double.NaN;
        if (Util.fuzzyEQ(min.tau, max.tau)) return min.d;
        return curve.tauToD(tau);
    }

    /**
     * Given tau, return t. If tau matches no segment points, return NaN.
     *
     * @param tau The time in the accelerated frame.
     * @return The time in the rest frame or NaN.
     */
    public double tauToT(double tau)
    {
        if (Util.fuzzyLT(tau, min.tau) || Util.fuzzyGT(tau, max.tau)) return Double.NaN;
        if (Util.fuzzyEQ(min.tau, max.tau)) return min.t;
        return curve.tauToT(tau);
    }

   /**
     * Given tau, return gamma. If tau matches no segment points, return NaN.
     *
     * @param tau The time in the accelerated frame or NaN.
     * @return Gamma.
     */
    public double tauToGamma(double tau)
    {
        if (Util.fuzzyLT(tau, min.tau) || Util.fuzzyGT(tau, max.tau)) return Double.NaN;
        return curve.tauToGamma(tau);
    }

    // **********************************************************
    // *
    // * Intersections
    // *
    // **********************************************************

    /**
     * Find the intersection of this worldline segment with a line. We return
     * the first intersection with the earliest time coordinate. If there is no
     * intersection, we return null.
     *
     * @param line The line to intersect with.
     * @return The intersection or null if none.
     */
    public Coordinate intersect(Line line)
    {
        // Find where the segment's curve intersects the line (if anywhere)

        Coordinate intersection = curve.intersect(line, false);
        if (intersection == null) return null;

        // Find out if this intersection occurs within the bounds of this
        // segment.

        if (curveSegment.getBounds().inside(intersection)) return intersection;
        return null;
    }

    /**
     * Find the intersection of this worldline segment with another. We
     * return the first intersection with the earliest time coordinate. If there
     * is no intersection, we return null.
     *
     * @param other The other observer.
     * @return The intersection or null if none.
     */
    public Coordinate intersect(WorldlineSegment other)
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "Acceleration: " + a +"\n" +
               "Starting values:\n" +
               min.toString().replaceAll("(?m)^", "  ") + "\n" +
               "Ending values:" +
               max.toString().replaceAll("(?m)^", "  ");
    }



}
