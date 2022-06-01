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
package org.freixas.gamma.math;

import org.freixas.gamma.value.ConcreteLine;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.ExecutionImmutable;
import org.freixas.gamma.value.Line;
import org.freixas.gamma.value.WorldlineSegment;


/**
 * This is the offset acceleration class. It provides information about an
 * accelerating object starting from the following assumptions:
 * <ul>
 * <li>The acceleration is constant.
 * <li>The acceleration curve crosses a given point.
 * <li>This point has a given velocity, tau, and distance.
 * <li>The acceleration curve is infinite with respect to time.
 * </ul>
 * <p>
 * <p>
 * We'll use the following terms in this documentation:
 * <ul>
 * <li>a = acceleration
 * <li>v = velocity
 * <li>x = a position in the rest frame
 * <li>d = a distance in the rest frame
 * <li>t = time in the rest frame
 * <li>tau = time in the accelerated frame
 * <li>gamma = 1 / sqrt(1 - v<sup>2</sup>)
 * </ul>
 * <p>
 * Distance (d) is a measure of the distance from a point to an arbitrary
 * 0-distance point. The distance is positive if the point in question exists
 * later in time than the 0-distance point and negative otherwise.
 * <p>
 * Gamma is useful for calculation time dilation and length contraction.
 * <p>
 * A standard acceleration curve is constrained so that at (0, 0) v = tau = d = 0.
 * An offset acceleration curve allows the standard acceleration to be offset in
 * position and time. It also allows the accelerated frame's clock's 0 time to
 * be set to any arbitrary point on the curve, and the distance's 0 point to also
 * be set to another arbitrary point.
 * <p>
 * Let's examine how this offset acceleration curves work when the acceleration
 * is non-zero. This image shows two offset acceleration curves, one for positive
 * acceleration (blue) and one for negative acceleration (red).
 * <p>
 * <img alt="offset acceleration curves" src="{@docRoot}/doc-files/offset-acceleration-curve.jpg" >
 * <p>
 * Since every possible velocity maps to a specific point on an acceleration
 * curve, we use the given velocity to identify a point on the standard
 * acceleration curve and then translate this point to a given coordinate that
 * we is called vPoint. Keep in mind that vPoint is <em>not</em> the curve's
 * offset, but is used to calculate the offset.
 * <p>
 * We set the accelerated frame's clock to tau and the distance to d at vPoint.
 * <p>
 * Now, let's look at the case where we have zero acceleration.
 * <p>
 * <img alt="offset zero acceleration curves" src="{@docRoot}/doc-files/offset-zero-acceleration-curve.jpg" >
 * <p>
 * V now sets the velocity of an inertial frame and vPoint now sets the offset. For
 * a standard acceleration curve, the velocity is always 0; for an offset
 * acceleration curve, any velocity is allowed.
 * <p>
 * The origin of standard acceleration curve has tau and d set to 0. The translated
 * origin now has tau and d set to arbitrary values.
 * <p>
 * The standard acceleration class (Acceleration) is a static class as the
 * methods take just two values. The offset acceleration class would not be
 * efficient as a static class. While it could be considered an extension of the
 * Acceleration class, Java syntax does not allow it to be.
 * <p>
 * <strong>The Mappings</strong>
 * <p>
 * Every point on an acceleration curve has a value for each of v, x, d, t, tau,
 * and gamma. This class provides methods to go from any v, x, d, t, and tau
 * value to any of the other values, where possible.
 * <ul>
 * <li><strong>For v:</strong>
 * <ul>
 * <li>If a &ne; 0, every valid value for v (from -1 to +1, exclusive)
 * corresponds to a unique point on the curve and every point on the curve has a
 * unique v value.
 * <li>If a = 0, then one v value is associated with
 * <em>every</em> point on the curve; every other v is associated with no
 * points.
 * </ul>
 * <li><strong>For x:</strong>
 * <ul>
 * <li>If a &gt; 0, then x can be associated with 0, 1 or 2 points.
 * <li>If a = 0, then one x value is associated with
 * <em>every</em> point on the curve; every other x is associated with no
 * points.
 * </ul>
 * <li><strong>For d:</strong>
 * <ul>
 * <li>If a &ne; 0, every value corresponds to a unique point on the curve and
 * every point on the curve has a unique d value.
 * <li>If a = 0, then one d value is associated with
 * <em>every</em> point on the curve; every other d is associated with no
 * points.
 * </ul>
 * <li><strong>For t</strong>, every value of t is associated with a unique
 * point on the curve and every point on the curve has a unique t value.
 * <li><strong>For tau</strong>, every value of tau is associated with a unique
 * point on the curve and every point on the curve has a unique tau value.
 * </ul>
 * <p>
 * We don't try to go from gamma to a curve point. Every point's gamma can be
 * calculated from v. Even when each curve point's v value is unique, gamma is
 * not: +v and -v have the same gamma value.
 * <p>
 * The following list of methods shows all the available conversions:
 * <table>
 * <caption><em>List of methods</em></caption>
 * <tr>
 * <td></td>
 * <td><b>v</b></td>
 * <td><b>x</b></td>
 * <td><b>d</b></td>
 * <td><b>t</b></td>
 * <td><b>tau</b></td>
 * </tr>
 * <tr>
 * <td><b>v</b></td>
 * <td>-</td>
 * <td>xToV()</td>
 * <td>dToV()</td>
 * <td>tToV()</td>
 * <td>tauToV()</td>
 * </tr>
 * <tr>
 * <td><b>x</b></td>
 * <td>vToX()</td>
 * <td>-</td>
 * <td>dToX()</td>
 * <td>tToX()</td>
 * <td>tauToX()</td>
 * </tr>
 * <tr>
 * <td><b>d</b></td>
 * <td>vToD()</td>
 * <td>xToD()</td>
 * <td>-</td>
 * <td>tToD()</td>
 * <td>tauToD()</td>
 * </tr>
 * <tr>
 * <td><b>t</b></td>
 * <td>vToD()</td>
 * <td>xToT()</td>
 * <td>dToT()</td>
 * <td>-</td>
 * <td>tauToT()</td>
 * </tr>
 * <tr>
 * <td><b>tau</b></td>
 * <td>vToTau()</td>
 * <td>xToTau()</td>
 * <td>dToTau()</td>
 * <td>tToTau()</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td><b>gamma</b></td>
 * <td>vToGamma()</td>
 * <td>xToGamma()</td>
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
 * @see org.freixas.gamma.math.Acceleration
 * @see org.freixas.gamma.value.WorldlineSegment
 *
 * @author Antonio Freixas
 */
public class OffsetAcceleration implements ExecutionImmutable
{
    private final double a;
    private final double vInit;
    private final Coordinate vPoint;
    private final double vPointTau;
    @SuppressWarnings("FieldCanBeLocal")
    private final double vPointD;

    private final Coordinate offset;
    private final double dOffset;

    private final Coordinate stdVPoint;
    private final double stdVPointTau;
    private final double stdVPointD;

    private final boolean zeroAcceleration;
    private final boolean zeroVelocity;

    /**
     * Create a new OffsetAcceleration object.
     *
     * @param a The acceleration.
     * @param v The velocity at vPoint.
     * @param vPoint A point through which the acceleration curve passes with velocity v.
     * @param tau Tau at vPoint.
     * @param d Distance at vPoint.
     */
    public OffsetAcceleration(double a, double v, Coordinate vPoint, double tau, double d)
    {
        this.zeroAcceleration = Util.fuzzyZero(a);
        this.zeroVelocity = zeroAcceleration && Util.fuzzyZero(v);

        this.a = zeroAcceleration ? 0.0 : a;
        this.vInit = zeroVelocity ? 0.0 : v;
        this.vPoint = vPoint;
        this.vPointTau = tau;
        this.vPointD = d;


        // Handle the case where the acceleration is 0

        if (Util.fuzzyZero(a)) {
            // In this case, the std curve point matching vPoint is always
            // the origin

            stdVPoint = new Coordinate(0, 0);
            stdVPointTau = 0;
            stdVPointD = 0;

            // Calculate the offset from vPoint to stdVpoint.
            // We subtract the offset to go from offset to standard coordinates.
            // We add the offset to go from standard to offset coordinates.

            offset = vPoint;

            // dOffset is the same as d in this case.
            //
            // We subtract the offset to go from offset to standard coordinates.
            // We add the offset to go from standard to offset coordinates.

            dOffset = d;
        }

        // Handle the case where the acceleration is not 0

        else {

            // Find the point identified by v in the standard curve

            double stdX =   Acceleration.vToX(a, v);
            double stdT =   Acceleration.vToT(a, v);
            stdVPoint = new Coordinate(stdX, stdT);

            // Tau on the standard curve at the standard equivalent of vPoint

            stdVPointTau = Acceleration.vToTau(a, v);

            // D on the standard curve at the standard equivalent of vPoint

            stdVPointD = Acceleration.xToD(a, stdX, stdT >= 0.0);

            // Calculate the offset from vPoint to stdVpoint.
            // We subtract the offset to go from offset to standard coordinates.
            // We add the offset to go from standard to offset coordinates.
            //
            // The offset is also the point on the offset acceleration curve
            // at which the turn-around point occurs (the point where v = 0).

            offset = new Coordinate(vPoint.x - stdVPoint.x, vPoint.t - stdVPoint.t);

            // Calculate the offset for d (if we translated the offset
            // curve to its standard location, what would d's value be at its
            // standard curve equivalent?).
            // We subtract the offset to go from offset to standard coordinates.
            // We add the offset to go from standard to offset coordinates.

            dOffset = d - stdVPointD;
        }
    }

    public double getA()
    {
        return a;
    }

    /**
     * Return the offset of this curve from the standard acceleration
     * curve. The offset coordinate is the point that would be (0, 0) on the
     * standard acceleration curve.
     *
     * @return The offset of this curve from the standard acceleration curve.
     */
    public final Coordinate getOffset()
    {
        return this.offset;
    }

    // **********************************************************
    // *
    // *
    // * Source is v
    // *
    // **********************************************************

     /**
     * Given v, return x.
     * <p>
     * If a = 0 and v &ne; 0,there are no answers so an ArithmeticException is thrown.
     *
     * @param v The velocity.
     * @return The position in the rest frame.
     * @throws ArithmeticException When a = 0 and v &ne; 0.
     */
    public final double vToX(double v)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) return stdVPoint.x;
            throw new ArithmeticException("Position can't be calculated from velocity when the acceleration is 0.");
        }
        return Acceleration.vToX(a, v) + offset.x;
    }

     /**
     * Given v, return d.
     *
     * @param v The velocity.
     * @return The distance in the rest frame.
     * @throws ArithmeticException When a = 0 and v &ne; 0.
     */
    public final double vToD(double v)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) return stdVPointD;
            throw new ArithmeticException("Distance can't be calculated from velocity when then acceleration is 0.");
        }
        return Acceleration.vToD(a, v) + dOffset;
    }

    /**
     * Given v, return t.
       *
     * @param v The velocity.
     * @return The time in the rest frame.
     * @throws ArithmeticException When a = 0.
     */
    public final double vToT(double v)
    {
        if (zeroAcceleration) {
            throw new ArithmeticException("Time can't be calculated from velocity when then acceleration is 0.");
        }
        return Acceleration.vToT(a, v) + offset.t;
    }

    /**
     * Given v, return tau.
     *
     * @param v The velocity.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When a = 0.
     */
    public final double vToTau(double v)
    {
        if (zeroAcceleration) {
            throw new ArithmeticException("Tau can't be calculated from velocity when then acceleration is 0.");
        }
        return toOffsetTau(Acceleration.vToTau(a, v));
    }

    /**
     * Given v, return gamma.
     *
     * @param v The velocity.
     * @return Gamma.
     */
    public final double vToGamma(double v)
    {
        // The acceleration is moot. Since we have v, we can derive gamma
        // directly

        return Relativity.gamma(v);
    }

    // **********************************************************
    // *
    // * Source is x
    // *
    // **********************************************************

    /**
     * Given x, return v.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the velocity matching the x value that
     * occurs earlier in time.
     *
     * @param x The position in the rest frame
     * @return The velocity.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    public final double xToV(double x)
    {
        return xToV(x, false);
    }

    /**
     * Given x, return v.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the velocity returned is for the x
     * value that occurs later in time.
     *
     * @param x The position in the rest frame.
     * @param later If true, use the x coordinate the occurs later in time for
     * determining the velocity.
     * @return The velocity.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    public final double xToV(double x, boolean later)
    {
        if (zeroAcceleration) {
            if (!zeroVelocity) return vInit;
            if (Util.fuzzyEQ(x, vPoint.x)) return 0;
            throw new ArithmeticException("The position matches no point on the acceleration curve.");
        }
        return tToV(xToT(x, later));
    }

    /**
     * Given x, return d.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the distance matching the x value
     * that occurs earlier in time.
     *
     * @param x The position in the rest frame.
     * @return The distance in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    public final double xToD(double x)
    {
        return xToD(x, false);
    }

    /**
     * Given x, return d.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the distance returned is for the x
     * value that occurs later in time.
     *
     * @param x The position in the rest frame.
     * @param later If true, use the x coordinate the occurs later in time for
     * determining the distance.
     * @return The distance in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    public final double xToD(double x, boolean later)
    {
        // If the acceleration is 0, convert x to standard d and convert that to d

        if (zeroAcceleration) {
            return (x - offset.x) + dOffset;
        }

        x -= offset.x;
        if (Util.fuzzyLT(a * x, 0)) {
            throw new ArithmeticException("The position matches no point on the acceleration curve.");
        }

        double d = Math.abs(x);
        d = later ? d : -d;
        return d + dOffset;
    }

    /**
     * Given x, return t.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the earlier time.
     *
     * @param x The position in the rest frame.
     * @return The time in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    public final double xToT(double x)
    {
        return xToT(x, false);
    }

    /**
     * Given x, return t.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the time returned is for the x
     * value that occurs later in time.
     *
     * @param x The position in the rest frame.
     * @param later If true, use the x coordinate the occurs later in time for
     * determining the time to return.
     * @return The time in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    public final double xToT(double x, boolean later)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) {
                throw new ArithmeticException("The position matches every point on the acceleration curve.");
            }
            return linearXToT(x);
	}

        return Acceleration.xToT(a, x - offset.x, later) + offset.t;
    }

    /**
     * Given x, return tau.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the earlier tau.
     *
     * @param x The position in the rest frame.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    public final double xToTau(double x)
    {
        return xToTau(x, false);
    }

    /**
     * Given x, return tau.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the tau returned is for the x
     * value that occurs later in time.
     *
     * @param x The position in the rest frame.
     * @param later If true, use the x coordinate the occurs later in time for
     * determining the tau to return.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    public final double xToTau(double x, boolean later)
    {
        if (zeroAcceleration) {
	    if (zeroVelocity) {
                throw new ArithmeticException("The position matches every point on the acceleration curve.");
            }
	    return linearXToTau(x);
	}

        return toOffsetTau(Acceleration.xToTau(a, x - offset.x, later));
    }

    /**
     * Given x, return gamma.
     *
     * @param x The position in the rest frame.
     * @return Gamma.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    public final double xToGamma(double x)
    {
	if (zeroAcceleration) return Relativity.gamma(vInit);
	return Acceleration.xToGamma(a, x - offset.x);
    }

    // **********************************************************
    // *
    // * Source is d
    // *
    // **********************************************************

    /**
     * Given d, return v.
     *
     * @param d The distance in the rest frame
     * @return The velocity.
     * @throws ArithmeticException When a = 0, v = 0, and d != d at VPoint.
     */

    public final double dToV(double d)
    {
        if (zeroAcceleration) {
            if (!zeroVelocity) return vInit;
            if (Util.fuzzyEQ(d, stdVPointD)) return 0;
            throw new ArithmeticException("The distance matches no point on the acceleration curve.");
        }
        return Acceleration.dToV(a, d - dOffset);
    }

    /**
     * Given d, return x.
     *
     * @param d The distance in the rest frame.
     * @return The position in the rest frame.
     * @throws ArithmeticException When a = 0, v = 0 and d != d at vPoint.
     */
    public final double dToX(double d)
    {
        if (zeroAcceleration) {
            if (zeroVelocity && !Util.fuzzyEQ(d, stdVPointD)) {
                throw new ArithmeticException("The distance matches no point on the acceleration curve.");
            }
            d = d - dOffset;
            if (vInit < 0.0)  d = -d;
            return d + offset.x;
        }
        return Acceleration.dToX(a, d - dOffset) + offset.x;
    }

    /**
     * Given d, return t.
     *
     * @param d The distance in the rest frame.
     * @return The time in the rest frame.
     * @throws ArithmeticException When a = 0 and v = 0.
     */
    public final double dToT(double d)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) {
                if (Util.fuzzyEQ(d, stdVPointD)) {
                    throw new ArithmeticException("The distance matches every point on the acceleration curve.");
                }
                throw new ArithmeticException("The distance matches no points on the acceleration curve.");
            }
            return linearXToT(dToX(d));
	}

        return Acceleration.dToT(a, d - dOffset) + offset.t;
    }

    /**
     * Given d, return tau.
     *
     * @param d The distance in the rest frame.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When a = 0 and v = 0.
     */
    public final double dToTau(double d)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) {
                if (Util.fuzzyEQ(d, stdVPointD)) {
                    throw new ArithmeticException("The distance matches every point on the acceleration curve.");
                }
                throw new ArithmeticException("The distance matches no points on the acceleration curve.");
            }
            return linearXToTau(dToX(d));
	}

        return toOffsetTau(Acceleration.dToTau(a, d - dOffset));
    }

    /**
     * Given d, return gamma.
     * <p>
     * If the acceleration is 0, then all points have the same gamma.
     *
     * @param d The distance in the rest frame.
     * @return Gamma.
     * @throws ArithmeticException When a = 0, v = 0, and d != d at VPoint.
     */
     public final double dToGamma(double d)
    {
        if (zeroAcceleration) {
            if (!zeroVelocity) return Relativity.gamma(vInit);
            if (Util.fuzzyEQ(d, stdVPointD)) return 1;
            throw new ArithmeticException("The distance matches no point on the acceleration curve.");
        }
        return Acceleration.dToGamma(a, d);
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
    public final double tToV(double t)
    {
	if (zeroAcceleration) return vInit;
	return Acceleration.tToV(a, t - offset.t);
    }

    /**
     * Given t, return x.
     *
     * @param t The time in the rest frame.
     * @return The position in the rest frame.
     */
    public final double tToX(double t)
    {
	if (zeroAcceleration) {
            return linearTToX(t);
        }
	return Acceleration.tToX(a, t - offset.t) + offset.x;
    }

    /**
     * Given t, return d.
     *
     * @param t The time in the rest frame.
     * @return The distance in the rest frame.
     */
    public final double tToD(double t)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) return dOffset;
            double stdT = t - offset.t;
            double stdX = vInit * (stdT);
            double d = Math.abs(stdX);
            d = Util.sign(stdT) * d;
            return d + dOffset;
        }
        return Acceleration.tToD(a, t - offset.t) + dOffset;
    }

    /**
     * Given t, calculate tau.
     *
     * @param t The time in the rest frame.
     * @return The time in the accelerated frame.
     */
    public final double tToTau(double t)
    {
        if (zeroAcceleration) {
            return toOffsetTau(Relativity.tToTau(t - offset.t, vInit));
        }
        return toOffsetTau(Acceleration.tToTau(a, t - offset.t));
    }

    /**
     * Given t, return gamma.
     *
     * @param t The time in the rest frame.
     * @return Gamma.
     */
    public final double tToGamma(double t)
    {
	if (zeroAcceleration) return Relativity.gamma(vInit);
	return Acceleration.tToGamma(a, t - offset.t);
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
    public final double tauToV(double tau)
    {
	if (zeroAcceleration) return vInit;
	return Acceleration.tauToV(a, toStdTau(tau));
    }

    /**
     * Given tau, return x.
     *
     * @param tau The time in the accelerated frame.
     * @return The position in the rest frame.
     */
    public final double tauToX(double tau)
    {
	if (zeroAcceleration) {
            if (zeroVelocity) return vPoint.x;
	    return vInit * Relativity.tauToT(toStdTau(tau), vInit) + offset.x;
        }
	return Acceleration.tauToX(a, toStdTau(tau)) + offset.x;
    }

    /**
     * Given tau, return d.
     *
     * @param tau The time in the accelerated frame.
     *
     * @return The distance in the rest frame..
     */
    public final double tauToD(double tau)
    {
        if (zeroAcceleration) {
            if (zeroVelocity) return dOffset;
            double stdTau = toStdTau(tau);
            double stdT = stdTau / Relativity.gamma(vInit);
            double stdX = vInit * (stdT);
            double d = Math.abs(stdX);
            d = Util.sign(stdT) * d;
            return d + dOffset;
        }
         return Acceleration.tauToD(a, toStdTau(tau)) + dOffset;
    }

    /**
     * Given tau, return t.
     *
     * @param tau The time in the accelerated frame.
     * @return The time in the rest frame.
     */
    public final double tauToT(double tau)
    {
        if (zeroAcceleration) {
            return Relativity.tauToT(toStdTau(tau), vInit) + offset.t;
        }
        return Acceleration.tauToT(a, toStdTau(tau)) + offset.t;
    }

   /**
     * Given tau, return gamma.
     *
     * @param tau The time in the accelerated frame.
     * @return Gamma.
     */
    public final double tauToGamma(double tau)
    {
	if (zeroAcceleration) return Relativity.gamma(vInit);
	return Acceleration.tauToGamma(a, toStdTau(tau));
    }

    /**
     * Transform a tau on the standard curve into a tau on the offset curve.
     * <p>
     * We find the delta tau between the standard tau and the tau on the
     * standard curve at the point corresponding to vPoint. We add this delta to
     * vPointTau.
     *
     * @param stdTau A tau value on the standard curve
     * @return The equivalent stdTau on the offset curve.
     */
    private double toOffsetTau(double stdTau)
    {
        return vPointTau + (stdTau - stdVPointTau);
    }

    /**
     * Transform a tau on the offset curve into a tau on the standard curve.
     * <p>
     * We find the delta tau between the offset tau and the tau on the point at
     * vPoint. We add this delta to the point on the standard curve
     * corresponding to vPoint. vPointTau.
     *
     * @param tau A tau value on the standard curve
     * @return The equivalent tau on the offset curve.
     */
    private double toStdTau(double tau)
    {
        return stdVPointTau + (tau - vPointTau);
    }

    /**
     * Given x, return t.
     * <p>
     * Use this only when the acceleration is 0.
     *
     * @param x The position in the rest frame.
     * @return The time in the rest frame.
     */
    private double linearXToT(double x)
    {
        if (x == Double.NEGATIVE_INFINITY) {
            return Util.fuzzyGE(vInit, 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        else if (x == Double.POSITIVE_INFINITY) {
            return Util.fuzzyGE(vInit, 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return ((x - offset.x) / vInit) + offset.t;
    }

    /**
     * Given t, return x.
     * <p>
     * USe this only when the acceleration is 0.
     *
     * @param t The time in the rest frame
     * @return The position in the rest frame.
     */
    private double linearTToX(double t)
    {
        if (t == Double.NEGATIVE_INFINITY) {
            return zeroVelocity ? vPoint.x : (vInit > 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        }
        else if (t == Double.POSITIVE_INFINITY) {
            return zeroVelocity ? vPoint.x : (vInit > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
        }
        return ((t - offset.t) * vInit) + offset.x;
    }

    /**
     * Given x, return tau.
     * <p>
     * Use this only when the acceleration is 0.
     *
     * @param x The position in the rest frame.
     * @return The time in the accelerated frame.
     */
    private double linearXToTau(double x)
    {
        if (x == Double.NEGATIVE_INFINITY) {
            return Util.fuzzyGE(vInit, 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        else if (x == Double.POSITIVE_INFINITY) {
            return Util.fuzzyGE(vInit, 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return toOffsetTau(Relativity.tToTau((x - offset.x) / vInit, vInit));
    }

    // **********************************************************
    // *
    // * Intersections
    // *
    // **********************************************************

    /**
     * Find the intersection of an offset acceleration curve with a line.
     *
     * @param line The line to intersect with.
     *
     * @return The intersections. There can be zero, one, or two intersections.
     * If there are zero intersections, the returned value is null.
     */
    public final Coordinate[] intersect(Line line)
    {
        // If the acceleration is 0, our offset acceleration curve is also a
        // line

        if (zeroAcceleration) {
            Coordinate intersection = line.intersect(new ConcreteLine(Line.AxisType.T, vInit, vPoint));
            if (intersection == null) return null;
            return new Coordinate[] { intersection };
        }

        // Now we translate the curve and line to the standard system, intersect
        // there and translate the results back

        Line translatedLine = line.offsetLine(offset);
        Coordinate[] results = Acceleration.intersect(a, translatedLine);
        if (results != null) {
            for (Coordinate intersection : results) {
                intersection.add(offset);
            }
        }
        return results;
    }

    /**
     * Find the intersection of a standard acceleration curve with a line.
     *
     * @param other The other observer.
     * @return The intersection or null if none.
     */
    public final Coordinate intersect(WorldlineSegment other)
    {
        return null;
    }

}
