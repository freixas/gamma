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

import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.OffsetAcceleration;
import org.freixas.gamma.math.Util;

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
public class WorldlineSegment implements ExecutionMutable, Displayable
{
    /**
     * The type of the delta value used to construct the worldline segment.
     */
    public enum LimitType implements ExecutionImmutable
    {
        NONE, T, TAU, D, V
    }

    private WorldlineEndpoint min;
    private WorldlineEndpoint max;

    private final WorldlineEndpoint originalMin;
    private final WorldlineEndpoint originalMax;

    private final double a;
    private final OffsetAcceleration curve;
    private boolean isLastSegment;
    private CurveSegment curveSegment;

    private final boolean zeroAcceleration;
    private final boolean constantVelocity;
    private final boolean zeroVelocity;
    private final boolean increasingVelocity;
    private final boolean decreasingVelocity;

    private boolean constantTime;
    private boolean constantTau;
    private boolean constantDistance;

    /**
     * Create a new WorldlineSegment.
     *
     * @param type The type of the delta value.
     * @param limitValue A non-negative increment to the time, tau, or distance or
     * any value for velocity.
     * @param a The acceleration.
     * @param v The velocity at vPoint.
     * @param vPoint A point at which the velocity of the acceleration curve is v.
     * @param tau Tau at vPoint.
     * @param d Distance at vPoint.
     * @throws IllegalArgumentException if the type is invalid or delta is negative.
     */
    public WorldlineSegment(LimitType type, double limitValue, double a, double v,
                            Coordinate vPoint, double tau, double d)
    {
        if (limitValue < 0 && type != LimitType.V) {
            throw new ExecutionException("Observer segment limit delta cannot be negative");
        }

        this.a = a;
        this.min = new WorldlineEndpoint(v, vPoint.x, vPoint.t, tau, d);

        zeroAcceleration = Util.fuzzyZero(a);

        // Create the offset curve

        this.curve = new OffsetAcceleration(a, v, vPoint, tau, d);

        double maxT = vPoint.t;
        if (null != type) // Convert all deltas to standard form (offset by time)
        {
            switch (type) {
                case T ->
                    maxT = vPoint.t + limitValue;
                case TAU -> {
                    double finalTau = tau + limitValue;
                    if (limitValue == 0.0) {
                        maxT = vPoint.t;
                    }
                    maxT = curve.tauToT(finalTau);
                }
                case D -> {
                    double finalD = d + limitValue;

                    // If the distance moved is 0, we have a zero-length
                    // segment

                    if (limitValue == 0.0) {
                        maxT = vPoint.t;
                    }
                    else if (limitValue > 0.0 && a == 0.0 && v == 0.0) {
                        throw new ExecutionException("Observer segment distance delta is > 0, but acceleration and velocity are 0");
                    }
                    else {
                        maxT = curve.dToT(finalD);
                    }
                }
                case V -> {
                    if (zeroAcceleration) {
                        if (Util.fuzzyEQ(min.v, limitValue)) {
                            maxT = min.t;
                        }
                        else {
                            throw new ExecutionException("The observer segment's final velocity will never be reached since the current velocity is a constant " + min.v);
                        }
                    }
                    else {
                        maxT = curve.vToT(limitValue);
                        if (Util.fuzzyLT(maxT, min.t)) {
                            throw new ExecutionException("The observer segment's final velocity will never be reached. The starting velocity is " + min.v);
                        }
                    }
                }
                case NONE -> {
                    // maxT already set to vPoint.t
                }
            }
        }

        // Generate the max limit. The x value won't be correct if the segment
        // includes the curve's turn-around point

        this.max = new WorldlineEndpoint(maxT, curve);

        // At this point:
        //
        // max.v could be less than min.v
        // max.x could be less than min.x, which is OK because it is not used in a test
        // max.t is always greater or equal to than min.t
        // max.tau is always greater or equal to min.tau
        // max.d is always greater or equal to min.d
        //
        // Comparisons with v need to be done carefully

        constantVelocity = zeroAcceleration && Util.fuzzyEQ(min.v, max.v);
        zeroVelocity = zeroAcceleration && constantVelocity && Util.fuzzyZero(v);
        increasingVelocity = Util.fuzzyLT(min.v, max.v);
        decreasingVelocity = Util.fuzzyGT(min.v, max.v);

        constantTime = Util.fuzzyEQ(min.t, max.t);
        constantTau = Util.fuzzyEQ(min.tau, max.tau);
        constantDistance = Util.fuzzyEQ(min.d, max.d);

        if (zeroAcceleration) {

            // We have a line segment

            curveSegment = new LineSegment(min.x, min.t, max.x, max.t);
        }
        else {

            // We have a hyperbolic segment

            curveSegment = new HyperbolicSegment(a, min, max, curve);
       }

        // We sometimes need to get the original endpoints and not ones
        // that have perhaps been modified to be at +/- infinity.

        this.originalMin = new WorldlineEndpoint(min);
        this.originalMax = new WorldlineEndpoint(max);

        // We need to know if we're the last segment in a worldline. For
        // now, assume we're not

        isLastSegment = false;


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
        this.min = new WorldlineEndpoint(v, start.x, start.t, tau, d);
        this.curve = new OffsetAcceleration(a, v, start, tau, d);
        this.max = new WorldlineEndpoint(end.t, curve);

        assert Util.fuzzyEQ(max.x, end.x);

        this.originalMin = new WorldlineEndpoint(min);
        this.originalMax = new WorldlineEndpoint(max);

        zeroAcceleration = Util.fuzzyZero(a);
        constantVelocity = Util.fuzzyEQ(min.v, max.v);
        zeroVelocity = zeroAcceleration && constantVelocity && Util.fuzzyZero(v);
        increasingVelocity = Util.fuzzyLT(min.v, max.v);
        decreasingVelocity = Util.fuzzyGT(min.v, max.v);

        constantTime = Util.fuzzyEQ(min.t, max.t);
        constantTau = Util.fuzzyEQ(min.tau, max.tau);
        constantDistance = Util.fuzzyEQ(min.d, max.d);

        if (zeroAcceleration) {

            // We have a line segment

            curveSegment = new LineSegment(min.x, min.t, max.x, max.t);
        }
        else {

            // We have a hyperbolic segment

            curveSegment = new HyperbolicSegment(a, min, max, curve);
       }

       isLastSegment = false;
    }

    /**
     * Copy constructor.
     *
     * @param other The other worldline to copy.
     */
    public WorldlineSegment(WorldlineSegment other)
    {
        this.min = other.min;
        this.max = other.max;

        this.originalMin = other.originalMin;
        this.originalMax = other.originalMax;

        this.a = other.a;
        this.curve = other.curve;
        this.curveSegment = other.curveSegment;

        this.isLastSegment = other.isLastSegment;

        this.zeroAcceleration = other.zeroAcceleration;
        this.constantVelocity = other.constantVelocity;
        this.zeroVelocity = other.zeroVelocity;
        this.increasingVelocity = other.increasingVelocity;
        this.decreasingVelocity = other.decreasingVelocity;

        this.constantTime = other.constantTime;
        this.constantTau = other.constantTau;
        this.constantDistance = other.constantDistance;
    }

    @Override
    public Object createCopy()
    {
        return new WorldlineSegment(this);
    }

    public OffsetAcceleration getCurve()
    {
        return curve;
    }

    public CurveSegment getCurveSegment()
    {
        return curveSegment;
    }

    public WorldlineEndpoint getOriginalMin()
    {
        return originalMin;
    }

    public WorldlineEndpoint getOriginalMax()
    {
        return originalMax;
    }

    /**
     * Set this segment to extend infinitely in time to the past.
     */
    public void setInfinitePast()
    {
        if (curveSegment instanceof LineSegment) {
            ConcreteLine line = new ConcreteLine(Line.AxisType.T, min.v, new Coordinate(min.x, min.t));
            curveSegment = new ConcreteLine(line, true, false);
        }
        else if (curveSegment instanceof ConcreteLine line) {
            curveSegment = new ConcreteLine(line, true, line.isInfinitePlus());
        }

        double v = Util.fuzzyGT(a, 0) ? Double.NEGATIVE_INFINITY: (Util.fuzzyLT(a, 0) ? Double.POSITIVE_INFINITY : min.v);
        double x = Util.fuzzyGT(a, 0) ? Double.POSITIVE_INFINITY : (Util.fuzzyLT(a, 0) ? Double.NEGATIVE_INFINITY : (Util.fuzzyGT(min.v, 0) ? Double.NEGATIVE_INFINITY : (Util.fuzzyLT(min.v, 0) ? Double.POSITIVE_INFINITY : min.x)));
        double t = Double.NEGATIVE_INFINITY;
        double d = zeroVelocity ? min.d : Double.NEGATIVE_INFINITY;
        double tau = Double.NEGATIVE_INFINITY;

        min = new WorldlineEndpoint(v, x, t, tau, d);
        if (curveSegment instanceof HyperbolicSegment) {
            curveSegment = new HyperbolicSegment(a, min, max, curve);
        }

        constantTime = Util.fuzzyEQ(min.t, max.t);
        constantTau = Util.fuzzyEQ(min.tau, max.tau);
        constantDistance = Util.fuzzyEQ(min.d, max.d);
    }

    /**
     * Set this segment to extend infinitely in time to the future.
     */
    public void setInfiniteFuture()
    {
        if (curveSegment instanceof LineSegment) {
            ConcreteLine line = new ConcreteLine(Line.AxisType.T, min.v, new Coordinate(min.x, min.t));
            curveSegment = new ConcreteLine(line, false, true);
        }
        else if (curveSegment instanceof ConcreteLine line) {
            curveSegment = new ConcreteLine(line, line.isInfiniteMinus(), true);
        }

        double v = Util.fuzzyGT(a, 0) ? Double.POSITIVE_INFINITY: (Util.fuzzyLT(a, 0) ? Double.NEGATIVE_INFINITY : max.v);
        double x = Util.fuzzyGT(a, 0) ? Double.POSITIVE_INFINITY : (Util.fuzzyLT(a, 0) ? Double.NEGATIVE_INFINITY : (Util.fuzzyGT(max.v, 0) ? Double.POSITIVE_INFINITY : (Util.fuzzyLT(max.v, 0) ? Double.NEGATIVE_INFINITY : max.x)));
        double t = Double.POSITIVE_INFINITY;
        double d = zeroVelocity ? max.d : Double.POSITIVE_INFINITY;
        double tau = Double.POSITIVE_INFINITY;

        max = new WorldlineEndpoint(v, x, t, tau, d);
        if (curveSegment instanceof HyperbolicSegment) {
            curveSegment = new HyperbolicSegment(a, min, max, curve);
        }

        constantTime = Util.fuzzyEQ(min.t, max.t);
        constantTau = Util.fuzzyEQ(min.tau, max.tau);
        constantDistance = Util.fuzzyEQ(min.d, max.d);

        isLastSegment = true;
    }

    /**
     * Get the x, v, t, tau, and d values associated with the earlier endpoint.
     * These may be +infinite or -infinity. Modifying the returned object will
     * not affect this segment at all.
     *
     * @return A structure holding the x, v, t, tau, and d values associated
     * with the earlier endpoint.
     */
    public final WorldlineEndpoint getMin()
    {
        return min;
    }

    /**
     * Get the x, v, t, tau, and d values associated with the later endpoint.
     * These may be +infinite or -infinity. Modifying the returned object will
     * not affect this segment at all.
     *
     * @return A structure holding the x, v, t, tau, and d values associated
     * with the later endpoint.
     */
    public final WorldlineEndpoint getMax()
    {
        return max;
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
        if (increasingVelocity && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (decreasingVelocity && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (constantVelocity) return min.x;
        double x = curve.vToX(v);
        if (!isLastSegment && Util.fuzzyEQ(x, max.x)) return Double.NaN;
        return x;
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
        if (increasingVelocity && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (decreasingVelocity && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (constantVelocity) return min.d;
        double d = curve.vToD(v);
        if (!isLastSegment && Util.fuzzyEQ(d, max.d)) return Double.NaN;
        return d;
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
        if (increasingVelocity && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (decreasingVelocity && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (constantVelocity) return min.t;
        double t = curve.vToT(v);
        if (!isLastSegment && Util.fuzzyEQ(t, max.x)) return Double.NaN;
        return t;
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
        if (increasingVelocity && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
        if (decreasingVelocity && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
        if (constantVelocity) return min.tau;
        double tau = curve.vToTau(v);
        if (!isLastSegment && Util.fuzzyEQ(tau, max.tau)) return Double.NaN;
        return tau;
    }

//    /**
//     * Given v, return gamma. If v matches no segment points, return NaN. If v
//     * matches all segment points return gamma for the v that occurs earliest in
//     * time.
//     *
//     * @param v The velocity.
//     * @return Gamma of NaN.
//     */
//    public double vToGamma(double v)
//    {
//        if (increasingVelocity && (Util.fuzzyLT(v, min.v) || Util.fuzzyGT(v, max.v))) return Double.NaN;
//        if (decreasingVelocity && (Util.fuzzyLT(v, max.v) || Util.fuzzyGT(v, min.v))) return Double.NaN;
//        return curve.vToGamma(v);
//    }

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
        if (constantDistance) return min.v;
        if (!isLastSegment && Util.fuzzyEQ(d, max.d)) return Double.NaN;
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
        if (!isLastSegment && constantDistance) return min.x;
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
        if (constantDistance) return min.t;
        if (!isLastSegment && Util.fuzzyEQ(d, max.d)) return Double.NaN;
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
        if (constantDistance) return min.tau;
        if (!isLastSegment && Util.fuzzyEQ(d, max.d)) return Double.NaN;
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
        if (constantDistance) return curve.vToGamma(min.v);
        if (!isLastSegment && Util.fuzzyEQ(d, max.d)) return Double.NaN;
        return curve.dToGamma(d);
    }

    // **********************************************************
    // *
    // * Source is t
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
        if (constantTime) return min.v;
        if (!isLastSegment && Util.fuzzyEQ(t, max.t)) return Double.NaN;
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
        if (constantTime) return min.x;
        if (!isLastSegment && Util.fuzzyEQ(t, max.t)) return Double.NaN;
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
        if (constantTime) return min.d;
        if (!isLastSegment && Util.fuzzyEQ(t, max.t)) return Double.NaN;
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
        if (constantTime) return min.tau;
        if (!isLastSegment && Util.fuzzyEQ(t, max.t)) return Double.NaN;
        return curve.tToTau(t);
    }

//    /**
//     * Given t, return gamma. If t matches no segment points, return NaN.
//     *
//     * @param t The time in the rest frame.
//     * @return Gamma.
//     */
//    public double tToGamma(double t)
//    {
//        if (Util.fuzzyLT(t, min.t) || Util.fuzzyGT(t, max.t)) return Double.NaN;
//        return curve.tToGamma(t);
//    }

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
        if (constantTau) return min.v;
        if (!isLastSegment && Util.fuzzyEQ(tau, max.tau)) return Double.NaN;
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
        if (constantTau) return min.x;
        if (!isLastSegment && Util.fuzzyEQ(tau, max.tau)) return Double.NaN;
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
        if (constantTau) return min.d;
        if (!isLastSegment && Util.fuzzyEQ(tau, max.tau)) return Double.NaN;
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
        if (constantTau) return min.t;
        if (!isLastSegment && Util.fuzzyEQ(tau, max.tau)) return Double.NaN;
        return curve.tauToT(tau);
    }

//   /**
//     * Given tau, return gamma. If tau matches no segment points, return NaN.
//     *
//     * @param tau The time in the accelerated frame or NaN.
//     * @return Gamma.
//     */
//    public double tauToGamma(double tau)
//    {
//        if (Util.fuzzyLT(tau, min.tau) || Util.fuzzyGT(tau, max.tau)) return Double.NaN;
//        return curve.tauToGamma(tau);
//    }

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

        Coordinate[] results = curve.intersect(line);
        if (results == null) {
            return null;
        }

        // Return the first intersection that occurs within the bounds
        // of this segment (if any)

        for (Coordinate intersection : results) {
            if (curveSegment.getBounds().inside(intersection)) return intersection;
        }

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
        // TO DO
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

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return
            "acceleration " + engine.toDisplayableString(a / (1.032295276 * engine.getSetStatement().getUnits())) + "g" +
            ", velocity " + engine.toDisplayableString(min.v) +
            " to " + engine.toDisplayableString(max.v);
    }


}
