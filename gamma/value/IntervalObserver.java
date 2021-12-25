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
import gamma.execution.HCodeEngine;

/**
 * An observer has an initial origin, tau and distance. The observer then
 * travels through as series of periods of constant velocity or constant
 * acceleration.
 *
 * @author Antonio Freixas
 */
public class IntervalObserver extends Observer
{
    private final ConcreteObserver observer;
    private final Interval interval;

    private final WorldlineEndpoint min;
    private final WorldlineEndpoint max;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    public IntervalObserver(Observer observer, Interval interval)
    {
        if (observer == null) {
            throw new ProgrammingException("BoundedLine: Trying to attach an interval to a null observer");
        }

        if (observer instanceof IntervalObserver intervalObserver) {
            this.observer = intervalObserver.observer;
        }
        else if (observer instanceof ConcreteObserver concreteObserver) {
            this.observer = concreteObserver;
        }
        else {
            throw new ProgrammingException("IntervalLine: line is not a interval or concrete line");
        }

	this.interval = interval;

        double minT = 0;
        double maxT = 0;

        if (null != interval.getType()) switch (interval.getType()) {
            case T -> {
                minT = interval.getMin();
                maxT = interval.getMax();
            }
            case TAU -> {
                minT = this.observer.dToTau(interval.getMin());
                maxT = this.observer.dToTau(interval.getMax());
            }
            case D -> {
                minT = this.observer.dToT(interval.getMin());
                maxT = this.observer.dToT(interval.getMax());
            }
        }

        this.min = new WorldlineEndpoint(
            this.observer.tToV(minT),
            this.observer.tToX(minT),
            minT,
            this.observer.tToTau(minT),
            this.observer.tToD(minT));

        this.max = new WorldlineEndpoint(
            this.observer.tToV(maxT),
            this.observer.tToX(maxT),
            maxT,
            this.observer.tToTau(maxT),
            this.observer.tToD(maxT));
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Return the ConcreteObserver wrapped in this IntervalObserver.
     *
     * @return The ConcreteObserver wrapped in this IntervalObserver.
     */
    public ConcreteObserver getObserver()
    {
        return this.observer;
    }

    @Override
    public Worldline getWorldline()
    {
        return observer.getWorldline();
    }

    @Override
    public IntervalObserver relativeTo(Frame prime)
    {
        ConcreteObserver relObserver = (ConcreteObserver)observer.relativeTo(prime);
        ???;
        return new IntervalObserver(relObserver, relInterval);
    }

    // **********************************************************
    // *
    // * Source is v
    // *
    // **********************************************************

    @Override
    public double vToX(double v)
    {
	double x = observer.vToX(v);
    }

    @Override
    public double vToD(double v)
    {
	return observer.vToD(v);
    }

    @Override
    public double vToT(double v)
    {
	return observer.vToT(v);
    }

    @Override
    public double vToTau(double v)
    {
	return observer.vToTau(v);
    }

    // **********************************************************
    // *
    // * Source is d
    // *
    // **********************************************************

    @Override
    public double dToV(double d)
    {
	return observer.dToV(d);
    }

    @Override
    public double dToX(double d)
    {
	return observer.dToX(d);
    }

    @Override
    public double dToT(double d)
    {
	return observer.dToT(d);
    }

    @Override
    public double dToTau(double d)
    {
	return observer.dToTau(d);
    }

    // **********************************************************
    // *
    // * Source is t
    // *
    // **********************************************************

    @Override
    public double tToV(double t)
    {
	return observer.tToV(t);
    }

    @Override
    public double tToX(double t)
    {
	return observer.tToX(t);
    }

    @Override
    public double tToD(double t)
    {
	return observer.tToD(t);
    }

    @Override
    public double tToTau(double t)
    {
	return observer.tToTau(t);
    }

    // **********************************************************
    // *
    // * Source is tau
    // *
    // **********************************************************

    @Override
    public double tauToV(double tau)
    {
	return observer.tauToV(tau);
    }

    @Override
    public double tauToX(double tau)
    {
	return observer.tauToX(tau);
    }

    @Override
    public double tauToD(double tau)
    {
	return observer.tauToD(tau);
    }

    @Override
    public double tauToT(double tau)
    {
	return observer.tauToT(tau);
    }

    // **********************************************************
    // *
    // * Intersections
    // *
    // **********************************************************

    @Override
    public Coordinate intersect(Line line)
    {
	return observer.intersect(line);
    }

    @Override
    public Coordinate intersect(Observer other)
    {
	return observer.intersect(other);
    }

    // **********************************************************************
    // *
    // * Display support
    // *
    // **********************************************************************

    @Override
    public String toDisplayableString(HCodeEngine engine)
    {
        return "[ Interval Observer " + observer.toDisplayableString(engine) + " interval " +
               interval.toDisplayableString(engine) +
               "]";
    }

}
