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
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.math.Relativity;
import org.freixas.gamma.math.Util;
import java.util.Objects;

/**
 * A Frame is the internal representation of an inertial frame. It is composed
 * of an origin and a velocity.
 *
 * @author Antonio Freixas
 */
public class Frame  extends ObjectContainer implements ExecutionMutable, Displayable
{
    static private final String[] PROPERTY_NAMES = { "v", "origin" };

    public enum AtType implements ExecutionImmutable
    {
        T, TAU, D, V
    }

    private Coordinate origin;
    private double v;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a frame from the instantaneous moving frame (IMF) of some point on
     * the observer's world line. Since no point on the worldline is given, we
     * pick the point where tau is 0.
     *
     * @param observer The observer from which to create the frame.
     */
    public Frame(Observer observer)
    {
        this(observer, AtType.TAU, 0.0);
    }

    /**
     * Create a frame from the instantaneous moving frame (IMF) of some point on
     * the observer's world line. The point to use is identified by some t, tau,
     * or d value on the worldline. If d is given, it is possible that no
     * point will be identified. In this case, a runtime exception will occur.
     *
     * @param observer The observer from which to create the frame.
     * @param type The type: AtType.T, AtType.TAU or AtType.D
     * @param value The value associated with the type.
     */
    public Frame(Observer observer, AtType type, double value)
    {
        super(PROPERTY_NAMES);

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

            case V -> {
                v = value;
                x = observer.vToX(v);
                t = observer.vToT(v);
                tau = observer.vToTau(v);
            }

            default -> v = x = t = tau = 0;
        }

        // Determine the size of the axes (relative to 1 when v = 0)

        double vSquared = v * v;
        double scaling = Math.sqrt(1 + vSquared) / Math.sqrt(1 - vSquared);

        // Find the origin of the axes (where tau is 0)

        double distanceToOrigin = tau * scaling;
        double theta = Relativity.vToTAngle(v);
        double signTheta = Util.sign(theta);

        origin = new Coordinate(
            x - signTheta * Math.cos(Math.toRadians(theta)) * distanceToOrigin,
            t - signTheta * Math.sin(Math.toRadians(theta)) * distanceToOrigin);
    }

    public Frame(Coordinate origin, double v)
    {
        super(PROPERTY_NAMES);
        this.origin = origin;
        this.v = v;
    }

    /**
     * Copy constructor.
     *
     * @param other The other frame to copy.
     */
    public Frame(Frame other)
    {
        super(PROPERTY_NAMES);
        if (other != null) {
            this.v = other.v;
            this.origin = new Coordinate(other.origin);
            }
        else {
            throw new ProgrammingException("Frame: Trying to copy a null object");
        }
    }

    // **********************************************************************
    // *
    // * ExecutionMutable Support
    // *
    // **********************************************************************

    @Override
    public Frame createCopy()
    {
        return new Frame(this);
    }

    // **********************************************************************
    // *
    // * Observer to Frame
    // *
    // **********************************************************************

    /**
     * This method promotes an Observer to a Frame, if the object is not
     * already a Frame.
     *
     * @param obj The object presumed to be either a Frame or Observer.
     *
     * @return A Frame, either the original object or the promoted Observer.
     * If the object is neither a Frame nor Observer, this method returns null.
     */
    static public Frame promote(Object obj)
    {
        if (obj instanceof Frame frame) return frame;
        if (obj instanceof Observer observer) return new Frame(observer);
        return null;
    }

    // **********************************************************************
    // *
    // * ObjectContainer Support
    // *
    // **********************************************************************

    @Override
    public Object getProperty(String name)
    {
        // The HCode that handles object properties will complain if an invalid
        // property name is used, so we don't need to re-check here

        switch (name) {
            case "v" -> { return v; }
            case "origin" -> { return origin; }
        }
        return null;
    }

    @Override
    public void setProperty(String name, Object value)
    {
        // The HCode that handles object properties will complain if an invalid
        // property name is used, so we don't need to re-check here

        switch (name) {
            case "v" -> {
                if (!(value instanceof Double)) {
                    throw new ExecutionException("Frame property 'v' must be a floating point number");
                }
                v = (Double)value;
            }
            case "origin" -> {
                if (!(value instanceof Coordinate)) {
                    throw new ExecutionException("Frame property 'origin' must be a coordinate");
                }
                origin = (Coordinate)value;

            }
            default -> {
            }
        }
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

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

    // **********************************************************************
    // *
    // * Transformations
    // *
    // **********************************************************************

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
        return toRest(c.x, c.t);
    }

    /**
     * Convert a coordinate relative to this frame to one relative to the rest
     * frame.
     *
     * @param x The x coordinate.
     * @param t The t coordinate.
     *
     * @return The transformed coordinate.
     */
    public Coordinate toRest(double x, double t)
    {
        Coordinate tc = Relativity.toRestFrame(x, t, v);
        tc.x += origin.x;
        tc.t += origin.t;
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
        return toFrame(c.x, c.t);
    }

    /**
     * Convert a coordinate relative to the default frame to one relative to
     * this frame.
     *
     * @param x The x coordinate.
     * @param t The t coordinate.
     *
     * @return The transformed coordinate.
     */
    public Coordinate toFrame(double x, double t)
    {
        return Relativity.toPrimeFrame(x - origin.x, t - origin.t, v);
    }

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    /**
     * Create a new version of this frame that is relative to the given frame
     * rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new frame.
     */
    public Frame relativeTo(Frame prime)
    {
        return new Frame(prime.toFrame(origin), Relativity.vPrime(v, prime.getV()));
    }

    // **********************************************************************
    // *
    // * Display Support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Frame velocity " + engine.toDisplayableString(v) + ", origin " + origin.toDisplayableString(engine) + " ]";
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.origin);
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.v) ^ (Double.doubleToLongBits(this.v) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Frame other = (Frame)obj;
        if (Double.doubleToLongBits(this.v) != Double.doubleToLongBits(other.v)) {
            return false;
        }
        return Objects.equals(this.origin, other.origin);
    }

}
