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

import gamma.math.Util;

/**
 * Gamma's version of a color. This class is immutable.
 *
 * @author Antonio Freixas
 */
public class Color implements ExecutionImmutable
{
    // Colors as doubles. These go into the symbol table for use by the
    // users

    static public double red =     Color.toColorDouble(0xFF, 0,    0   );
    static public double green =   Color.toColorDouble(0,    0xFF, 0   );
    static public double blue =    Color.toColorDouble(0,    0,    0xFF);

    static public double yellow =  Color.toColorDouble(0xFF, 0xFF, 0   );
    static public double magenta = Color.toColorDouble(0xFF, 0,    0xFF);
    static public double cyan =    Color.toColorDouble(0,    0xFF, 0xFF);

    static public double black =   Color.toColorDouble(0,    0,    0   );
    static public double gray =    Color.toColorDouble(0x88, 0x88, 0x88);
    static public double white =   Color.toColorDouble(0xFF, 0xFF, 0xFF);

    // Colors as Colors. These are for the program's own use

    static public Color redColor =     new Color(Color.red);
    static public Color greenColor =   new Color(Color.green);
    static public Color blueColor =    new Color(Color.blue);

    static public Color yellowColor =  new Color(Color.yellow);
    static public Color magentaColor = new Color(Color.magenta);
    static public Color cyanColor =    new Color(Color.cyan);

    static public Color blackColor =   new Color(Color.black);
    static public Color grayColor =    new Color(Color.gray);
    static public Color whiteColor =   new Color(Color.white);

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    private final javafx.scene.paint.Color javaFXColor;

    public Color(double dColor)
    {
        long iColor = Util.toLong(dColor);
        this.r = (int)((iColor >> 24) & 0xFF);
        this.g = (int)((iColor >> 16) & 0xFF);
        this.b = (int)((iColor >> 8) & 0xFF);
        this.a = (int)(iColor & 0xFF);

        javaFXColor = javafx.scene.paint.Color.rgb(this.r, this.g, this.b, this.a / 255.0);
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

        javaFXColor = javafx.scene.paint.Color.rgb(this.r, this.b, this.g, this.a / 255.0);
    }

    public Color(Color other)
    {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
        this.javaFXColor = other.javaFXColor;
    }

    public javafx.scene.paint.Color getJavaFXColor()
    {
        return javaFXColor;
    }

    static public double toColorDouble(long red, long green, long blue)
    {
        return (double) ((red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | 0xFF);
    }

    static public double toColorDouble(long red, long green, long blue, long alpha)
    {
        return (double) ((red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | (alpha & 0xFF));
    }

    static public javafx.scene.paint.Color toJavaFXColor(double color)
    {
        return new Color(color).getJavaFXColor();
    }

}
