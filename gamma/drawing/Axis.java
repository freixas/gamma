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
package gamma.drawing;

import gamma.execution.lcode.StyleStruct;
import gamma.value.Bounds;
import gamma.value.Coordinate;

/**
 *
 * @author Antonio Freixas
 */
public class Axis
{
    static final double MIN_MINOR_TICK_MARK_LENGTH = 10.0;
    static final double MIN_MAJOR_TICK_MARK_LENGTH = 20.0;

    public static void draw(Context context, gamma.value.Line line,
                            boolean positiveOnly, String xLabel,
                            StyleStruct styles)
    {
        Coordinate origin = line.getCoordinate();

        // Intersect the axis with the viewport.
        // A line has some thickness. If we have tick marks, we can think of
        // the line is being thicker, so we can apply the same algorithm
        // whether a line has tick marks or not.

        // We reverse rotate the line to horizontal around its origin,
        // which just means that the line is at x = origin.x for any t.

        double height = styles.lineThickness;

        // If we have tick marks, the height increases

        if (styles.ticks) {
            height += MIN_MAJOR_TICK_MARK_LENGTH;
        }

        // The thickness of the line is evenly distributed to either side

        double halfHeight = height / 2.0;

        // Create a bounding box for the line

        Bounds lineBounds = new Bounds(
            origin.x - halfHeight,
            Double.NEGATIVE_INFINITY,
            origin.x + halfHeight,
            Double.POSITIVE_INFINITY);

        // Reverse rotate the viewport around the line's origin

        double angle = line.getAngle();
        Bounds rotatedBounds = context.t.getViewportBounds().rotate(-angle, origin);

        // Check whether to draw the line

        Bounds intersection = rotatedBounds.intersect(lineBounds);

        // If there is no intersection, we're done
        if (intersection == null) {
            return;
        }

        // Now we know approximately where the line lies. 



	    // We draw tick marks as though the axis were horizontal
	    // and the ticks vertical and then rotate them into place.
	    // Axes (and their tick marks) are infinite, so we need to
	    // limit our drawing just to what intersects the viewport.
	    // The easiest way to do that is to reverse rotate the
            // clipping area and intersect that with a bounding box for
            // the tick marks to find out the area we need to draw.

            // Create a bounding box for the horizontal line

            double minorTickMarkLength = MIN_MINOR_TICK_MARK_LENGTH + styles.lineThickness;
            double majorTickMarkLength = MIN_MAJOR_TICK_MARK_LENGTH + styles.lineThickness;
            double halfMaxHeight = majorTickMarkLength / 2;

            Bounds lineBounds = new Bounds(
                origin.x - halfMaxHeight,
                Double.NEGATIVE_INFINITY,
                origin.x + halfMaxHeight,
                Double.POSITIVE_INFINITY);

            Bounds intersection = rotatedBounds.intersect(lineBounds);

            // If there is no intersection, we're done

            if (intersection == null) return;

            // Otherwise, figure out the tick marks to draw
        }
    }
}
