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
package org.freixas.gamma.drawing;

import javafx.scene.canvas.GraphicsContext;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.execution.lcode.HypergridStruct;
import org.freixas.gamma.math.OffsetAcceleration;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.Bounds;
import org.freixas.gamma.value.Coordinate;
import org.freixas.gamma.value.HyperbolicSegment;

/**
 * Draw a hypergird.
 *
 * @author Antonio Freixas
 */
public class Hypergrid
{
    static public final double MIN_GRID_SIZE = 20;

    /**
     * Draw the hypergrid.
     *
     * @param context The drawing structure.
     * @param struct The hypergrid properties.
     * @param styles The style properties.
     */
    static public void draw(Context context, HypergridStruct struct,
                            StyleStruct styles)
    {
        GraphicsContext gc = context.gc;

        // Save the current graphics context

        gc.save();

        Bounds bounds = context.bounds;

        // We don't want to crowd things. We've chosen a certain minimum size in
        // screen coordinates. Since our minimum size is in screen coordinates,
        // we need to find the equivalent size in our transformed space

        double minSpacing = MIN_GRID_SIZE * context.invScale;

        // We want to use the largest power of 10 that is larger than the
        // spacing we've calculated. For instance, if the spacing is 30, we
        // want to use 100, not 10

        double spacing = Math.pow(10, Math.ceil(Math.log10(minSpacing)));

        // Set up the gc

        Line.setupLineGc(context, styles);

        if (styles.leftQuadrant || styles.rightQuadrant) {
            double[] range = calculateXRange(bounds);

            if (range != null) {
                 double startX = range[0] - (range[0] % spacing);

                // Draw the left  and right quadrants

                if (styles.leftQuadrant || styles.rightQuadrant) {
                    double x;
                    for (x = startX; x <= range[1]; x += spacing) {
                        if (x < 0 && !styles.leftQuadrant) continue;
                        if (x > 0 && !styles.rightQuadrant) continue;
                        if (Util.fuzzyZero(x)) continue;
                        double a = 1/x;
                        OffsetAcceleration curve = new OffsetAcceleration(a, 0, new Coordinate(x, 0.0), 0.0, 0.0);
                        HyperbolicSegment segment = new HyperbolicSegment(a, bounds.min.t, bounds.max.t, curve);
                        HyperbolicSegment segment2 = segment.intersect(bounds);
                        if (segment2 != null) {
                            Hyperbola.drawRaw(context, segment2);
                        }
                    }
                }
            }
        }

        // Draw the top and bottom quadrants

        if (styles.bottomQuadrant || styles.topQuadrant) {
            gc.rotate(90);
            bounds = context.getCurrentCanvasBounds();

            double[] range = calculateXRange(bounds);

            if (range != null) {
                double startX = range[0] - (range[0] % spacing);
                if (styles.bottomQuadrant || styles.topQuadrant) {
                    double x;
                    for (x = startX; x <= bounds.max.x; x += spacing) {
                        if (x < 0 && !styles.bottomQuadrant) continue;
                        if (x > 0 && !styles.topQuadrant) continue;
                        if (Util.fuzzyZero(x)) continue;
                        double a = 1/x;
                        OffsetAcceleration curve = new OffsetAcceleration(a, 0, new Coordinate(x, 0.0), 0.0, 0.0);
                        HyperbolicSegment segment = new HyperbolicSegment(a, bounds.min.t, bounds.max.t, curve);
                        HyperbolicSegment segment2 = segment.intersect(bounds);
                        if (segment2 != null) {
                            Hyperbola.drawRaw(context, segment2);
                        }
                    }
                }
            }
        }

        // Restore the original graphics context

        gc.restore();
    }

    /**
     * Calculate which hyperbolas we should draw. We only want the ones that
     * fit inside the bounding box.
     *
     * @param bounds The box inside which the hyperbolas should fit.
     *
     * @return Two values or null. If two values are returned, they represent
     * a range of points on the x-axis. Any hypergrid hyperbola drawn through
     * that range of points will cross the bounding box.
     */
    static private double[] calculateXRange(Bounds bounds)
    {
        // The formula for our hyperbolas is t = sqrt(x^2 + 2x / a).
        //
        // Because the hypergrid hyperbolas are offset, the formula is actually
        // t = sqrt((x - x0)^2 + 2(x - x0) / a), where x0 is a coordinate on the
        // x-axis
        //
        // For the hypergrid hyperbolas, a = 1 / x0.
        //
        // So, if we have a point (x,t), then the x0 for the curve that goes
        // through x0 is -sqrt(x^2 - t^2) if x < 0 and +sqrt(x^2 - t^2) if x > 0.
        //
        // This formula is valid only when |x| > |t|. (x,t) points which fail
        // this condition have no hyperbolas going through them (at least,
        // not in the left or right quadrants, which is all we're concerned
        // with here).

        double absMinX = Math.abs(bounds.min.x);
        double absMaxX = Math.abs(bounds.max.x);
        double absMinT = Math.abs(bounds.min.t);
        double absMaxT = Math.abs(bounds.max.t);

        // Completely outside, return null

        if ((bounds.max.t < 0 && absMinX < absMaxT && absMaxX < absMaxT) ||
            (bounds.min.t > 0 && absMinX < absMinT && absMaxX < absMinT)) {
            return null;
        }

        // Convert our bounds to a valid region. The bounds won't necessarily
        // be rectangular when we're done, so we'll need four separate points

        Coordinate ll = new Coordinate(bounds.min.x, bounds.min.t);
        Coordinate ul = new Coordinate(bounds.min.x, bounds.max.t);
        Coordinate lr = new Coordinate(bounds.max.x, bounds.min.t);
        Coordinate ur = new Coordinate(bounds.max.x, bounds.max.t);

        // Completely inside, the bounds need no adjustment

        //noinspection StatementWithEmptyBody
        if ((bounds.max.x < 0 && absMaxX > absMinT && absMaxX > absMaxT) ||
            (bounds.min.x > 0 && absMinX > absMinT && absMinX > absMaxT)) {
            // Do nothing
        }

        // Not completely inside

        else {
            if (absMinX < absMinT) ll.t = ll.x;
            if (absMinX < absMaxT) ul.t = ul.x;
            if (absMaxX < absMinT) lr.t = lr.x;
            if (absMaxX < absMaxT) ur.t = ur.x;
        }

        // We want to find the minimum and maximum x0 values.

        // If the bounds are completely above or below the x-axis, we can use
        // the minimum and maximum values for all four corners

        if (ll.t > 0 || bounds.max.t < 0) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            double x0;

            x0 = Util.sign(ll.x) * Math.sqrt(ll.x * ll.x - ll.t * ll.t);
            min = Math.min(x0, min);
            max = Math.max(x0, max);

            x0 = Util.sign(lr.x) * Math.sqrt(lr.x * lr.x - lr.t * lr.t);
            min = Math.min(x0, min);
            max = Math.max(x0, max);

            x0 = Util.sign(ul.x) * Math.sqrt(ul.x * ul.x - ul.t * ul.t);
            min = Math.min(x0, min);
            max = Math.max(x0, max);

            x0 = Util.sign(ur.x) * Math.sqrt(ur.x * ur.x - ur.t * ur.t);
            min = Math.min(x0, min);
            max = Math.max(x0, max);

            return new double[]{ min, max };
        }

        // Below this point, we know that the bounds include the x-axis

        // If the bounds include the x-axis but are to the left of the t axis,
        // the lower range is the minimum x. The maximum range is either the
        // top right or bottom right corner, but not more than 0

        else if (bounds.max.x < 0) {
            double min = ll.x;
            double max = Double.NEGATIVE_INFINITY;

            double x0;

            x0 = Util.sign(ll.x) * Math.sqrt(ll.x * ll.x - ll.t * ll.t);
            max = Math.max(x0, max);

            x0 = Util.sign(lr.x) * Math.sqrt(lr.x * lr.x - lr.t * lr.t);
            max = Math.max(x0, max);

            x0 = Util.sign(ul.x) * Math.sqrt(ul.x * ul.x - ul.t * ul.t);
            max = Math.max(x0, max);

            x0 = Util.sign(ur.x) * Math.sqrt(ur.x * ur.x - ur.t * ur.t);
            max = Math.max(x0, max);

            return new double[]{ min, Math.min(0, max) };
        }

        // If the bounds include the x-axis but are to the right of the t axis,
        // the maximum range is the maximum x. The minimum range is either the
        // top left or bottom left corner, but not less than 0

        else if (bounds.min.x > 0) {
            double min = Double.POSITIVE_INFINITY;
            double max = lr.x;

            double x0;

            x0 = Util.sign(ll.x) * Math.sqrt(ll.x * ll.x - ll.t * ll.t);
            min = Math.min(x0, min);

            x0 = Util.sign(lr.x) * Math.sqrt(lr.x * lr.x - lr.t * lr.t);
            min = Math.min(x0, min);

            x0 = Util.sign(ul.x) * Math.sqrt(ul.x * ul.x - ul.t * ul.t);
            min = Math.min(x0, min);

            x0 = Util.sign(ur.x) * Math.sqrt(ur.x * ur.x - ur.t * ur.t);
            min = Math.min(x0, min);

            return new double[]{ Math.max(0, min), max };
        }

        // If the bounds include the origin, then the range is from the minimum
        // x to the maximum x

        else {
            return new double[]{ bounds.min.x, bounds.max.x };
        }
    }

}
