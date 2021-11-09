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

import customfx.ResizableCanvas;

/**
 * We need to pass around the context for drawing, which consists of
 * various things, some of which may need to be added in the future. We
 * encapsulate all these values in this class.
 *
 * @author Antonio Freixas
 */
public class Context
{
    public final T t;
    public final ResizableCanvas canvas;

    public Context(T t, ResizableCanvas canvas)
    {
        this.t = t;
        this.canvas = canvas;
    }
    
}
