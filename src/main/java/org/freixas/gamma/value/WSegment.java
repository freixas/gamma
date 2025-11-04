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

import org.freixas.gamma.math.Relativity;

/**
 * This is a worldline segment definition structure. A WSegment stores the
 * information provided by a script writer to define a segment. A
 * WorldlineSegment, uses this information to create the actual segment.
 *
 * @param v The initial velocity. If none was given, use NaN.
 * @param a The acceleration. If none was given, use 0.
 * @param type The limit type.
 * @param limit The limit. If no limit was given use NaN.
 *
 * @author Antonio Freixas
 */
public record WSegment(double v, double a, WorldlineSegment.LimitType type, double limit)
    implements ExecutionImmutable
{
    // **********************************************************************
    // *
    // * Drawing Frame Support
    // *
    // **********************************************************************

    /**
     * Create a new version of this WSegment that is relative to the given frame
     * rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     *
     * @return The new segment.
     */
    public WSegment relativeTo(Frame prime)
    {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        double v = this.v;

        if (!Double.isNaN(v)) {
            v = Relativity.vPrime(v, prime.getV());
        }

        @SuppressWarnings("LocalVariableHidesMemberVariable")
        double limit = this.limit;

        if (type != WorldlineSegment.LimitType.NONE && !Double.isNaN(limit)) {
            if (type == WorldlineSegment.LimitType.D) {
                limit = Relativity.lengthContraction(limit, prime.getV());
            }
            else if (type == WorldlineSegment.LimitType.T) {
                limit = Relativity.timeDilation(limit, prime.getV());
            }
        }

        return new WSegment(v, a, type, limit);
    }

}
