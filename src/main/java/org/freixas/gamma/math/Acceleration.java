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

import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.Line;

/**
 * This is the standard acceleration class. It provides information about an
 * accelerating object starting from the following assumptions:
 * <ul>
 * <li>The acceleration is constant.
 * <li>At (0, 0) v = tau = d = 0.
 * <li>The acceleration curve is infinite with respect to time.
 * </ul>
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
 * Distance (d) is a measure of the distance from a point to (0, 0). The
 * distance is positive if the point is after time 0 and negative otherwise. D
 * is always +abs(x) when t or tau is positive; otherwise it is -abs(x).
 * <p>
 * Gamma is useful for calculation time dilation and length contraction.
 * <p>
 * There are three possible types of acceleration curves, depending on whether
 * acceleration is positive (blue), negative (red), or zero (green):
 * <p>
 * <img alt="acceleration curves" src="{@docRoot}/doc-files/standard-acceleration-curve.jpg" >
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
 * <li>If a = 0, then if v = 0, it is associated with
 * <em>every</em> point on the curve; v &ne; 0 is associated with no points.
 * </ul>
 * <li><strong>For x:</strong>
 * <ul>
 * <li>If a &gt; 0, then when x &gt; 0, it is associated with two curve points;
 * if x = 0, it is associated with one point; if x &lt; 0, it is associated with
 * no points.
 * <li>If a &lt; 0, then when x &lt; 0, it is associated with two curve
 * points; if x = 0, it is associated with one point; if x &gt; 0, it is
 * associated with no points.
 * <li>If a = 0, then when x = 0, it is associated with
 * <em>every</em> point on the curve; x &ne; 0 is associated with no points.
 * </ul>
 * <li><strong>For d:</strong>
 * <ul>
 * <li>If a &ne; 0, every value corresponds to a unique point on the curve and
 * every point on the curve has a unique d value.
 * <li>If a = 0, then if d = 0, it is associated with
 * <em>every</em> point on the curve; d &ne; 0 is associated with no points.
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
 * The equations used have been simplified by assuming c = 1, so the velocities
 * need to be in fractions of light speed. This means that if t is in seconds, x
 * is in light seconds, etc.
 * <p>
 * The units don't matter as long as they are consistent.
 * <p>
 * The tricky one is acceleration. If time is in years, distance should be in
 * light years, and acceleration should be in light years per years squared. For
 * a 1g acceleration using units years and light years, 1 g = 1.03227407852535
 * ly / year<sup>2</sup>.
 * <p>
 * See http://web.physics.ucsb.edu/~fratus/phys103/LN/IGR.pdf<br>
 * See http://math.ucr.edu/home/baez/physics/Relativity/SR/Rocket/rocket.html
 *
 * @see org.freixas.gamma.math.OffsetAcceleration
 * @author Antonio Freixas
 */
public final class Acceleration
{
    private Acceleration() {}

    // **********************************************************
    // *
    // * Source is v
    // *
    // **********************************************************

     /**
     * Given v, return x.
     *
     * @param a The acceleration.
     * @param v The velocity.
     * @return The position in the rest frame.
     * @throws ArithmeticException When a = 0 and v &ne; 0.
     */
    static public double vToX(double a, double v)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(v)) return 0;
            throw new ArithmeticException("Position can't be calculated from velocity when the acceleration is 0 but the velocity is non-zero.");
        }
        return tToX(a, vToT(a, v));
    }

     /**
     * Given v, return d.
     *
     * @param a The acceleration.
     * @param v The velocity.
     * @return The distance in the rest frame.
     * @throws ArithmeticException When a = 0 and v &ne; 0.
     */
    static public double vToD(double a, double v)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(v)) return 0;
            throw new ArithmeticException("Distance can't be calculated from velocity when the acceleration is 0, but the velocity is non-zero.");
        }

        // For a standard acceleration curve, d and x are the same except for
        // the sign. If t is negative, then d is negative and if t is positive,
        // then d is positive, regardless of the sign of x.

        double x = vToX(a, v);
        double t = vToT(a, v);
        double d = Math.abs(x);
        return (Util.fuzzyLT(t, 0)) ? -d : d;
    }

    /**
     * Given v, return t.
     *
     * @param a The acceleration.
     * @param v The velocity.
     * @return The time in the rest frame.
     * @throws ArithmeticException When a = 0.
     */
    static public double vToT(double a, double v)
    {
        if (Util.fuzzyZero(a)) {
            throw new ArithmeticException("Time can't be calculated from velocity when the acceleration is 0.");
        }
        return v / (a * Math.sqrt(1 - v * v));
    }

    /**
     * Given v, return tau.
     *
     * @param a The acceleration.
     * @param v The velocity.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When a = 0.
     */
    static public double vToTau(double a, double v)
    {
        if (Util.fuzzyZero(a)) {
            throw new ArithmeticException("Tau can't be calculated from velocity when the acceleration is 0.");
        }
        return Util.atanh(v) / a;
    }

    /**
     * Given v, return gamma.
     *
     * @param v The velocity.
     * @return Gamma.
     */
    @SuppressWarnings("unused")
    static public double vToGamma(double v)
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
     * answers. This method returns the velocity matching the x value
     * that occurs earlier in time.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @return The velocity.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    @SuppressWarnings("unused")
    static public double xToV(double a, double x)
    {
        return xToV(a, x, false);
    }

    /**
     * Given x, return v.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the velocity returned is for the x
     * value that occurs later in time
       *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @param later Whether we should consider time as positive.
     * @return The velocity.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    static public double xToV(double a, double x, boolean later)
    {
        return tToV(a, xToT(a, x, later));
    }

    /**
     * Given x, return d.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the distance matching the x value
     * that occurs earlier in time.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @return The distance in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    @SuppressWarnings("unused")
    static public double xToD(double a, double x)
    {
        return xToD(a, x, false);
    }

    /**
     * Given x, return d.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the distance returned is for the x
     * value that occurs later in time.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @param later If true, return +d, else return -d.
     * @return The corresponding distance in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.
     */
    static public double xToD(double a, double x, boolean later)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(x)) return 0;
            throw new ArithmeticException("The position matches no point on the acceleration curve.");
        }
        if (Util.fuzzyLT(a * x, 0)) {
            throw new ArithmeticException("The position matches no point on the acceleration curve.");
        }

        double d = Math.abs(x);
        return (later) ? d : -d;
    }

    /**
     * Given x, return t.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the earlier time.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @return The time in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    @SuppressWarnings("unused")
    static public double xToT(double a, double x)
    {
        return xToT(a, x, false);
    }

    /**
     * Given x, return t.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the time returned is for the x
     * value that occurs later in time.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @param later If true, return +t, else return -t.
     * @return The corresponding time in the rest frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    static public double xToT(double a, double x, boolean later)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(x)) {
                throw new ArithmeticException("The position matches every point on the acceleration curve.");
            }
            else {
                throw new ArithmeticException("The position matches no points on the acceleration curve.");
            }
        }
        if (Util.fuzzyLT(a * x, 0)) {
            throw new ArithmeticException("The position matches no points on the acceleration curve.");
        }
        if (Util.fuzzyZero(x)) x = 0.0;
        double t = Math.sqrt(x * x + (2 * x) / a);
        return later ? t : -t;
    }

    /**
     * Given x, return tau.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. This method returns the earlier tau.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    @SuppressWarnings("unused")
    static public double xToTau(double a, double x)
    {
        return xToTau(a, x, false);
    }

    /**
     * Given x, return tau.
     * <p>
     * When the x coordinate crosses the curve, there can be two correct
     * answers. If "later" is true, then the tau returned is for the x
     * value that occurs later in time.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @param later If true, return +tau, else return -tau.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve or matches all the curve points.
     */
    static public double xToTau(double a, double x, boolean later)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(x)) {
                throw new ArithmeticException("The position matches every point on the acceleration curve.");
            }
            else {
                throw new ArithmeticException("The position matches no points on the acceleration curve.");
            }
        }
        if (Util.fuzzyZero(x)) x = 0.0;
        double ax = a * x;
        if (Util.fuzzyLT(ax, 0)) {
            throw new ArithmeticException("The position matches no points on the acceleration curve.");
        }

        double tau = Util.acosh(ax + 1) / a;
        double sign = Util.sign(a);
        return later ? sign * tau : -sign * tau;
    }

    /**
     * Given x, return gamma.
     *
     * @param a The acceleration.
     * @param x The position in the rest frame.
     * @return Gamma.
     * @throws ArithmeticException When the x coordinate doesn't cross the
     * offset acceleration curve.

     */
    static public double xToGamma(double a, double x)
    {
        if (Util.fuzzyZero(a)) {
            return 1;
        }
        if (Util.fuzzyZero(x)) x = 0.0;
        double ax = a * x;
        if (Util.fuzzyLT(ax, 0)) {
            throw new ArithmeticException("The position matches no points on the acceleration curve.");
        }
        return ax + 1;
    }

    // **********************************************************
    // *
    // * Source is d
    // *
    // **********************************************************

    /**
     * Given d, return v.
     *
     * @param a The acceleration.
     * @param d The distance in the rest frame.
     * @return The velocity.
     * @throws ArithmeticException When a = 0 and d != 0.
     */

    static public double dToV(double a, double d)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(d)) return 0;
            throw new ArithmeticException("The distance matches no points on the acceleration curve.");
        }
        double x = (Util.fuzzyLT(a, 0)) ? -Math.abs(d) : Math.abs(d);
        boolean later = d >= 0;
        return tToV(a, xToT(a, x, later));
    }

    /**
     * Given d, return x.
     *
     * @param a The acceleration.
     * @param d The distance in the rest frame.
     * @return The position in the rest frame.
     * @throws ArithmeticException When a = 0 and d != 0.
     */
    static public double dToX(double a, double d)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(d)) return 0;
            throw new ArithmeticException("The distance matches no points on the acceleration curve.");
        }

        if (Util.fuzzyZero(d)) d = 0.0;
        double x = Math.abs(d);
        return Util.sign(a) * x;
    }

    /**
     * Given d, return t.
     *
     * @param a The acceleration.
     * @param d The distance in the rest frame.
     * @return The time in the rest frame.
     * @throws ArithmeticException When a = 0.
     */
    static public double dToT(double a, double d)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(d)) {
                throw new ArithmeticException("The distance matches every point on the acceleration curve.");
            }
            throw new ArithmeticException("The distance matches no points on the acceleration curve.");
        }

        // If a > 0, t = xToT(a,  |d|, d >= 0)
        // If a < 0, t = xToT(a, -|d|, d >= 0);

        return (a > 0) ? xToT(a, Math.abs(d), d >= 0) : xToT(a, -Math.abs(d), d >= 0);
    }

    /**
     * Given d, return tau.
     * <p>
     * If the acceleration is 0 and d != 0, then there are no answers and if
     * d = 0 there are infinite answers, so an ArithmeticException is thrown.
     *
     * @param a The acceleration.
     * @param d The distance in the rest frame.
     * @return The time in the accelerated frame.
     * @throws ArithmeticException When a = 0.
     */
    static public double dToTau(double a, double d)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(d)) {
                throw new ArithmeticException("The distance matches every point on the acceleration curve.");
            }
            throw new ArithmeticException("The distance matches no points on the acceleration curve.");
        }

        // If a > 0, tau = xToTau(a,  |d|, d >= 0)
        // If a < 0, tau = xToTau(a, -|d|, d >= 0);

        return (a > 0) ? xToTau(a, Math.abs(d), d >= 0) : xToTau(a, -Math.abs(d), d >= 0);
    }

    /**
     * Given d, return gamma.
     * <p>
     * If the acceleration is 0, then since the initial velocity is 0, gamma is
     * always 1.
     * <p>
     *
     * @param a The acceleration.
     * @param d The distance in the rest frame.
     * @return Gamma.
     * @throws ArithmeticException When a = 0 and d != 0.
     */
    static public double dToGamma(double a, double d)
    {
        if (Util.fuzzyZero(a)) {
            if (Util.fuzzyZero(d)) return 1;
            throw new ArithmeticException("The distance matches no points on the acceleration curve.");
        }
        return xToGamma(a, d);
    }

    // **********************************************************
    // *
    // * Source is t
    // *
    // **********************************************************

    /**
     * Given t, return v.
     *
     * @param a The acceleration.
     * @param t The time in the rest frame.
     *
     * @return The velocity.
     */
    static public double tToV(double a, double t)
    {
        if (Util.fuzzyZero(a)) {
            return 0;
        }

        // This formula works with positive and negative acceleration and with
        // positive and negative t's.

        if (Util.fuzzyZero(t)) t = 0.0;
        if (Double.isInfinite(t)) return Util.sign(a) * Util.sign(t);      // Inf t means v = 1 or -1
        return (a * t) / Math.sqrt(1 + (a * a * t * t));
    }

    /**
     * Given t, return x.
     *
     * @param a The acceleration.
     * @param t The time in the rest frame.
     *
     * @return The position in the rest frame.
     */
    static public double tToX(double a, double t)
    {
        if (Util.fuzzyZero(a)) {
            return 0;
        }

        // With positive acceleration, the x position will always be positive.
        // With a negative acceleration, the x position will always be negative.
        // The formula gives the right answer in either case.
        //
        // The formula also works fine with positive or negative t's.

        if (Util.fuzzyZero(t)) t = 0.0;
        return (Math.sqrt(1 + (a * a * t * t)) - 1) / a;
    }

    /**
     * Given t, return d.
     *
     * @param a The acceleration.
     * @param t The time in the rest frame.
     * @return The distance in the rest frame.
     */
    static public double tToD(double a, double t)
    {
        double d = Math.abs(tToX(a, t));
        if (Util.fuzzyZero(d)) d = 0.0;
        return (Util.fuzzyLT(t, 0)) ? -d : d;
    }

    /**
     * Given t, calculate tau.
     *
     * @param a The acceleration.
     * @param t The time in the rest frame.
     *
     * @return The time in the accelerated frame.
     */
    static public double tToTau(double a, double t)
    {
        if (Util.fuzzyZero(a)) {
            return t;
        }
        // This formula works with positive and negative acceleration. The time
        // dilation is not affected by the direction of motion.
        //
        // The formula also works with positive and negative t's.

        if (Util.fuzzyZero(t)) t = 0.0;
        return Util.asinh(a * t) / a;
    }

    /**
     * Given t, return gamma.
     *
     * @param a The acceleration.
     * @param t The time in the rest frame.
     * @return Gamma.
     */
    static public double tToGamma(double a, double t)
    {
        if (Util.fuzzyZero(a)) {
            return 1;
        }

        // This formula works with positive and negative acceleration and with
        // positive and negative t's.

        if (Util.fuzzyZero(t)) t = 0.0;
        return Math.sqrt(1 + (a * a * t * t));
    }

    // **********************************************************
    // *
    // * Source is tau
    // *
    // **********************************************************

    /**
     * Given tau, return v.
     *
     * @param a The acceleration.
     * @param tau The time in the accelerated frame.
     *
     * @return The velocity.
     */
    static public double tauToV(double a, double tau)
    {
        if (Util.fuzzyZero(a)) {
            return 0;
        }

        // This formula works with positive and negative acceleration and with
        // positive and negative taus.

        if (Util.fuzzyZero(tau)) tau = 0.0;
        return Math.tanh(a * tau);
    }

    /**
     * Given tau, return x.
     *
     * @param a The acceleration.
     * @param tau The time in the accelerated frame.
     *
     * @return The position in the rest frame.
     */
    static public double tauToX(double a, double tau)
    {
        if (Util.fuzzyZero(a)) {
            return 0;
        }

        // With positive acceleration, the x position will always be positive.
        // With a negative acceleration, the x position will always be negative.
        // The formula gives the right answer in either case.
        //
        // The formula also works fine with positive or negative taus.

        if (Util.fuzzyZero(tau)) tau = 0.0;
        return (Math.cosh(a * tau) - 1) / a;
    }

    /**
     * Given tau, return d.
     *
     * @param a The acceleration.
     * @param tau The time in the accelerated frame.
     *
     * @return The distance in the rest frame.
     */
    static public double tauToD(double a, double tau)
    {
        if (Util.fuzzyZero(a)) {
            return 0;
        }
        double d = Math.abs(tauToX(a, tau));
        if (Util.fuzzyZero(d)) d = 0.0;
        return (tau < 0) ? -d : d;
    }

    /**
     * Given tau, return t.
     *
     * @param a The acceleration.
     * @param tau The time in the accelerated frame.
     *
     * @return The time in the rest frame.
     */
    static public double tauToT(double a, double tau)
    {
        if (Util.fuzzyZero(a)) {
            return tau;
        }

        // This formula works with positive and negative acceleration. The time
        // dilation is not affected by the direction of motion.
        //
        // The formula also works with positive and negative taus.

        if (Util.fuzzyZero(tau)) tau = 0.0;
        return Math.sinh(a * tau) / a;
    }

   /**
     * Given tau, return gamma.
       *
     * @param a The acceleration.
     * @param tau The time in the accelerated frame.
     * @return Gamma.
     */
    static public double tauToGamma(double a, double tau)
    {
        if (Util.fuzzyZero(a)) {
            return 1;
        }

        // This formula works with positive and negative acceleration and with
        // positive and negative taus.

        if (Util.fuzzyZero(tau)) tau = 0.0;
        return Math.cosh(a * tau);
    }

    // **********************************************************
    // *
    // * Intersections
    // *
    // **********************************************************

    /**
     * Find the intersection of a standard acceleration curve with a line. If
     * there are an infinite number of matches, we return the earliest, which
     * might include one coordinate value located at infinity. Lines sloped at
     * multiples of 45 degrees can intersect with both values are infinity.
     *
     * @param a The acceleration.
     * @param line The line to intersect with.
     *
     * @return The intersections. There can be zero, one, or two intersections.
     * If there are zero intersections, the returned value is null.
     */
    static public Coordinate[] intersect(double a, Line line)
    {
        // Basics:
        // The line formula is t = mx + k.
        // The acceleration formula is t = sqrt(x^2 + 2x/a)

        // Get m and k for the line

        double angle = line.getAngle();
        double m = line.getSlope();
        double k = line.getConstantOffset();
        Coordinate coord = line.getCoordinate();
        boolean zeroAcceleration = Util.fuzzyZero(a);


        // Special cases when the line is vertical

        if (Util.fuzzyEQ(angle, 90.0))  {
            double minT = line.getBounds().min.t;

            // Is the acceleration also 0 (meaning that the acceleration
            // curve is also a vertical line)? If so, it's formula is x = 0

            if (zeroAcceleration) {

                // If the line also has the formula x = 0, then return the
                // earliest point that intersects the line

                if (Util.fuzzyZero(coord.x)) {
                    return new Coordinate[]{ new Coordinate(coord.x, minT) };
                }

                return null;
            }

            // The acceleration is not zero

            if (Util.fuzzyZero(coord.x)) {
                if (Util.fuzzyLE(minT, 0)) {
                    return new Coordinate[]{ new Coordinate(coord.x, 0) };
                }
                return null;
            }
            else if (coord.x < 0.0 && a > 0.0) {
                return null;
            }
            else if (coord.x > 0.0 & a < 0.0) {
                return null;
            }

            // We drop through for other vertical line cases

        }

        // Special cases when the line is horizontal

        else if (Util.fuzzyZero(angle))  {

            // Is the acceleration 0 (meaning that the acceleration
            // curve is a vertical line)? If so, it's formula is x = 0 and
            // there is always one intersection

            if (zeroAcceleration) {
                return new Coordinate[]{ new Coordinate(0, coord.t) };
            }

            // We drop through for other horizontal cases

        }

        // Special cases when the line is +/- 45 degrees

        else if (Util.fuzzyEQ(Math.abs(angle), 45.0)) {
            double ak = a * k;
            double x;
            double t;
            if (angle > 0.0) {                  // +45
                if (Util.fuzzyEQ(ak, 1.0)) {
                    if (Util.fuzzyGT(a, 0) && line.isInfinitePlus()) {
                        x = Double.POSITIVE_INFINITY;
                        t = Double.POSITIVE_INFINITY;
                    }
                    else if (Util.fuzzyLT(a, 0) && line.isInfiniteMinus()) {
                        x = Double.NEGATIVE_INFINITY;
                        t = Double.NEGATIVE_INFINITY;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    x = -(ak * k) / (2 * (ak - 1));
                    t = m * x + k;
                }
            }
            else /* if (angle < 0.0) */{        // -45
                if (Util.fuzzyEQ(ak, 1.0)) {
                    if (Util.fuzzyGT(a, 0) && line.isInfinitePlus()) {
                        x = Double.POSITIVE_INFINITY;
                        t = Double.NEGATIVE_INFINITY;
                    }
                    else if (Util.fuzzyLT(a, 0) && line.isInfiniteMinus()) {
                        x = Double.NEGATIVE_INFINITY;
                        t = Double.POSITIVE_INFINITY;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    x = (ak * k) / (2 * (ak + 1));
                    t = m * x + k;
                }
            }
            return new Coordinate[]{ new Coordinate(x, t) };
        }

        // Handle all remaining cases:
        //
        // * Vertical line crossing an accelerating curve at two points
        // * Horizontal line crossing an accelerating curve at one point
        // * All other lines, except ones sloped +/- 45 degrees

        double x1;
        double t1;
        double x2;
        double t2;

        // Check to see if we have a vertical line

        if (Double.isInfinite(m)) {

            // The formula for a vertical line is x = x0, where x0 is the
            // point through which the line goes.
            //
            // We plug this into the acceleration formula to calculate t

            x1 = x2 = line.getCoordinate().x;
            t1 = xToT(a, x1, false);
            t2 = xToT(a, x2, true);
        }

        else {
            // mx + k = sqrt(x^2 + 2x/a)
            // x = (+/-sqrt(a^2^k2 - 2*a*k*m + 1)/a + 1/a - k*m) / (m^2 - 1)
            // x = ((+/-sqrt(a*k*(a*k - 2*m) + 1) + 1)/a - k*m) / (m^2 - 1)
            // Solve for t and we have the solutions

            double ak = a * k;
            double km = k * m;
            double m21 = m *m - 1;

            // If we are going to take the square root of a negative number,
            // we don't have an intersection

            double root = (ak * (ak - 2 * m) + 1);
            if (root < 0) {
                return null;
            }

            root = Math.sqrt(root);
            x1 = ((-root + 1)/a - km) / m21;
            x2 = ((+root + 1)/a - km) / m21;

            t1 = m * x1 + k;
            t2 = m * x2 + k;
        }

        // Discard intersections not within the lines bounds

        Coordinate result1 = new Coordinate(x1, t1);
        Coordinate result2 = new Coordinate(x2, t2);
        boolean inside1 = line.getBounds().inside(result1);
        boolean inside2 = line.getBounds().inside(result2);

        if (inside1 && inside2) {
            // Sort by time

            if (result1.t <= result2.t) {
                return new Coordinate[]{ result1, result2 };
            }
            else {
                return new Coordinate[]{ result2, result1 };
            }
        }
        else if (inside1) {
            return new Coordinate[]{ result1 };
        }
        else if (inside2) {
            return new Coordinate[]{ result2 };
        }
        else {
            return null;
        }
    }

}
