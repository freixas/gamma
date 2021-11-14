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

import gamma.value.Bounds;
import gamma.value.Coordinate;

/**
 * This class handles the transformation of coordinates from
 * diagram space to screen space.
 * <p>
 *
 *
 * @author Antonio Freixas
 */
public class T
{
//    private final Coordinate origin;
//    private double scale;
//    private final double units;
//    private final Dimension viewport;
//    private final Bounds viewportBounds;
//    private final Coordinate originOffset;
//    private final Coordinate translate;
//    private Coordinate dragPoint;
//
//    /**
//     *
//     * @param origin The
//     * @param scale
//     * @param units
//     * @param width
//     * @param height
//     */
//    public T(Coordinate origin, double scale, double units, double width, double height)
//    {
//        this.origin = origin;
//        this.scale = scale;
//        this.units = units;
//        this.viewport = new Dimension(width, height);
//        this.originOffset = new Coordinate(0.0, 0.0);
//        this.translate = new Coordinate (0.0, 0.0);
//        this.viewportBounds = new Bounds(xInv(0.0), tInv(height), xInv(width), tInv(0.0));
//    }
//
//    public final Coordinate getOrigin()
//    {
//        return origin;
//    }
//
//    public void setOrigin(Coordinate origin)
//    {
//        this.origin.setTo(origin);
//        updatePosition();
//    }
//
//    public final double getScale()
//    {
//        return scale;
//    }
//
//    public final void setScale(double scale)
//    {
//        this.scale = scale;
//    }
//
//    public final Dimension getViewport()
//    {
//        return viewport;
//    }
//
//    public final void setViewport(Dimension viewport)
//    {
//        viewport.setTo(viewport);
//        originOffset.setTo(viewport.width / 2, viewport.height / 2);
//        viewportBounds
//    }
//
//    public Bounds getViewportBounds()
//    {
//        return viewportBounds;
//    }
//
//    public final double x(double x)
//    {
//        return x * scale + translate.x;
//    }
//
//    public final double xInv(double xp)
//    {
//        return (xp - translate.x) / scale;
//    }
//
//    public final double t(double t)
//    {
//	return -(t * scale) + translate.t;
//    }
//
//    public final double tInv(double tp)
//    {
//	return -(tp - translate.t) / scale;
//    }
//
//    public final void updatePosition()
//    {
//        translate.setTo(origin.x + originOffset.x, -origin.t + originOffset.t);
//        // DClip.i().setWorkingClip();
//    }
//
//    public final void setRelZoom(double zoom, Coordinate pix)
//    {
//        if (zoom == 0) return;
//
//	// Convert the pixel coordinates to world coordinates
//
//	Coordinate world = new Coordinate(xInv(pix.x), tInv(pix.t));
//
//	// Convert the zoom amount into a scaling factor
//	// and set the new scale
//
//	double zoomExp = 1 + (Math.abs(zoom) / 10);
//	double zoomIncr = Math.pow(scale, zoomExp) / 10;
//	if (zoom < 0) zoomIncr = -zoomIncr;
//	setScale(scale + zoomIncr);
//
//	// Calculate the offset such that the same pixel coordinate
//	// translates into the same world coordinate
//
//	setOrigin(
//	    new Coordinate(
//		-((world.x * scale) - pix.x + originOffset.x),
//		-((world.t * scale) + pix.t - originOffset.t)
//	));
//    }
//
//    public final void beginDrag(Coordinate pix)
//    {
//	dragPoint = pix;
//    }
//
//    public final void drag(Coordinate pix)
//    {
//	Coordinate delta = new Coordinate(
//	     (pix.x - dragPoint.x),
//	    -(pix.t - dragPoint.t)
//	);
//
//	dragPoint = pix;
//
//	origin.add(delta);
//	updatePosition();
//    }

}
