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

import gamma.math.Lorentz;
import gamma.math.Util;

/**
 *
 * @author Antonio Freixas
 */
public class Frame
{
    public enum AtType
    {
        T, TAU, D
    };

    private final Coordinate origin;
    private final double v;

    public Frame(Observer observer)
    {
        this(observer, AtType.TAU, 0.0);
    }

    public Frame(Observer observer, AtType type, double value)
    {
        double x;
        double t;
        double tau;
        double d;

        switch (type) {
            case T -> {
                t = value;
                v = observer.tToV(t);
                x = observer.tToX(t);
                tau = observer.tToTau(t);
            }
            case TAU -> {
                tau = value;
                v = observer.tauToV(tau);
                x = observer.tauToX(tau);
                t = observer.tauToT(tau);

            }
            case D -> {
                d = value;
                v = observer.dToV(d);
                x = observer.dToX(d);
                t = observer.dToT(d);
                tau = observer.dToTau(d);
            }

            default -> {
                v = x = t = tau = 0;
            }
        }

        // Determine the size of the axes (relative to 1 when v = 0)

        double vSquared = v * v;
        double scaling = Math.sqrt(1 + vSquared) / Math.sqrt(1 - vSquared);

        // Find the origin of the axes (where tau is 0)

        double distanceToOrigin = tau * scaling;
        double theta = Util.vToTAngle(v);
        double signTheta = Util.sign(theta);

        origin = new Coordinate(
            x - signTheta * Math.cos(theta) * distanceToOrigin,
            t - signTheta * Math.sin(theta) * distanceToOrigin);
    }

    /**
     * Get the frame's velocity.
     *
     * @return The frame's velocity.
     */
    public double getV()
    {
        return v;
    }

    /**
     * Get the frame's origin.
     *
     * @return The frame's origin.
     */
    public Coordinate getOrigin()
    {
        return new Coordinate(origin);
    }

    /**
     * Convert a coordinate relative to this frame to one relative to the rest
     * frame.
     *
     * @param c The coordinate.
     *
     * @return The transformed coordinate.
     */
    public Coordinate toRest(Coordinate c)
    {
        Coordinate tc = Lorentz.toRestFrame(c, v);
        tc.x -= origin.x;
        tc.t -= origin.t;
        return tc;
    }

    /**
     * Convert a coordinate relative to the default frame to one relative to
     * this frame.
     *
     * @param c The coordinate.
     *
     * @return The transformed coordinate.
     */
    public Coordinate toFrame(Coordinate c)
    {
        return Lorentz.toPrimeFrame(new Coordinate(c.x + origin.x, c.t + origin.t), v);
    }

}
