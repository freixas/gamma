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

import gamma.execution.hcode.HCode;

/**
 * This is a worldline segment definition structure. It is used to 
 * create worldline segments in a worldline.
 * 
 * @author Antonio Freixas
 */
public class WSegment
{
    private final double v;
    private final double a;
    private final WorldlineSegment.LimitType type;
    private final double delta;

    /**
     * Create a worldline segment based on information from the parser.This worldline is incompletely defined.
     * It is only fully defined when it is added to a worldline.
     * 
     * @param v The initial velocity. If none was given, use NaN.
     * @param a The acceleration. If none was given, use 0.
     * @param type The limit type.
     * @param delta The limit delta. If no limit was given use NaN.
     */
    public WSegment(double v, double a, WorldlineSegment.LimitType type, double delta)
    {
        this.v = v;
        this.a = a;
        this.type = type;
        this.delta = delta;
    }

    public double getV()
    {
        return v;
    }

    public double getA()
    {
        return a;
    }

    public WorldlineSegment.LimitType getType()
    {
        return type;
    }
    
    public double getDelta()
    {
        return delta;
    }
    
}
