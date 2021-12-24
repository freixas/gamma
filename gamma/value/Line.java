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

/**
 * A line is defined by an angle (in degrees) and a point through which the line
 * crosses. A line can be created using other methods, but all are converted to
 * an angle and a point.
 * <p>
 * The angle is between -90 (exclusive) and 90 (inclusive) degrees. Lines
 * between 0 and 90 degrees (inclusive) can be thought of as being drawn with
 * increasing x and t coordinates. Lines between 0 and -90 degrees (exclusive)
 * can be thought of as drawn with decreasing x and increasing t coordinates.
 * <p>
 * Lines are generally infinite, but can be capped off on one or both ends. If
 * capped off on one end, the line starts or ends on the point. If capped off on
 * both ends, the line effectively becomes a single point.
 * <p>
 *
 *
 * @author Antonio Freixas
 */
abstract public class Line extends CurveSegment implements ExecutionImmutable, Displayable
{
    public enum AxisType implements ExecutionImmutable
    {
        X, T
    };

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the point through which the line crosses.
     *
     * @return The point through which the line crosses.
     */
    abstract public Coordinate getCoordinate();

    /**
     * Get the angle of the line in degrees.
     *
     * @return The angle of the line in degrees.
     */
    abstract public double getAngle();

    /**
     * Get the slope of a line. If the line is vertical, return +infinity.
     *
     * @return The slope of the line.
     */
    abstract public double getSlope();

    /**
     * Get the line's offset. Given the standard formula for a line,
     * t = mx + k, where m is the slope, k is the offset.
     *
     * @return The line's offset.
     */
    abstract public double getConstantOffset();

    /**
     * If true, the line starts at some infinite distance. If not true, the
     * line starts at the point.
     *
     * @return True if the line starts at some infinite distance.
     */
    abstract public boolean isInfiniteMinus();

    /**
     * If true, the line ends at some infinite distance. If not true, the
     * line ends at the point.
     *
     * @return True if the line ends at some infinite distance.
     */
    abstract public boolean isInfinitePlus();

    /**
     * Get the bounds of a line. For an infinite line, the bounds go from
     * negative to positive infinity. A line can be marked as finite on one or
     * bother ends. The finite end is the point through which the line
     * traverses.
     * <p>
     * This is used mainly by worldlines where the initial segment or the final
     * segment has zero acceleration.
     *
     * @return The bounding box.
     */
    @Override
    abstract public Bounds getBounds();

    // **********************************************************************
    // *
    // * Drawing frame support
    // *
    // **********************************************************************

    /**
     * Create a new version of this line that is relative to the given frame
     * rather than relative to the rest frame.
     *
     * @param prime The frame to be relative to.
     * @return The new line.
     */
    abstract public Line relativeTo(Frame prime);

    // **********************************************************************
    // *
    // * Intersections
    // *
    // **********************************************************************

    /**
     * Intersect this line with another.
     *
     * @param other The other line to intersect with.
     *
     * @return The intersection or null if none.
     */
    abstract public Coordinate intersect(Line other);

    /**
     * Intersect this line with a bounding box. Return the line segment
     * that intersects or null if none.
     * <p>
     * The given bounding box cannot have corners at infinity.
     *
     * @param extBounds The bounding box.
     *
     * @return The line segment that intersects or null if none.
     */
    abstract public LineSegment intersect(Bounds extBounds);
}
