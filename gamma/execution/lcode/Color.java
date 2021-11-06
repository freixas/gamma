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
package gamma.execution.lcode;

import gamma.math.Util;

/**
 *
 * @author Antonio Freixas
 */
public class Color
{
    static public double red =     Color.toColorDouble(0xFF, 0,    0   );
    static public double green =   Color.toColorDouble(0,    0xFF, 0   );
    static public double blue =    Color.toColorDouble(0,    0,    0xFF);

    static public double yellow =  Color.toColorDouble(0xFF, 0xFF, 0   );
    static public double magenta = Color.toColorDouble(0xFF, 0,    0xFF);
    static public double cyan =    Color.toColorDouble(0,    0xFF, 0xFF);

    static public double black =   Color.toColorDouble(0,    0,    0   );
    static public double gray =    Color.toColorDouble(0x88, 0x88, 0x88);
    static public double white =   Color.toColorDouble(0xFF, 0xFF, 0xFF);

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public Color(double dColor)
    {
        int iColor = Util.toInt(dColor);
        this.r = (iColor >> 24) & 0xFF;
        this.g = (iColor >> 16) & 0xFF;
        this.b = (iColor >> 8) & 0xFF;
        this.a = iColor & 0xFF;
    }

    public Color(double red, double green, double blue)
    {
        this(red, green, blue, 255.0);
    }

    public Color(double red, double green, double blue, double alpha)
    {
        this.r = Util.toInt(red);
        this.g = Util.toInt(green);
        this.b = Util.toInt(blue);
        this.a = Util.toInt(alpha);
    }

    public Color(Color other)
    {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
    }

    static public double toColorDouble(int red, int green, int blue)
    {
        return (double) ((red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | 0xFF);
    }

    static public double toColorDouble(int red, int green, int blue, int alpha)
    {
        return (double) ((red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | (alpha & 0xFF));
    }

}
