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

import gamma.ProgrammingException;
import gamma.execution.lcode.StyleStruct;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author Antonio Freixas
 */
public class Text
{
    private static javafx.scene.text.Text textNode = null;

    public static void draw(Context context, double x, double t, String text, double angle, StyleStruct styles)
    {
        draw(context, x, t, text, angle, styles.javaFXColor, styles.font, styles.textPadding, styles.textAnchor);
    }

    public static void draw(Context context, double x, double t, String text, double angle,
                            Color color, Font font, double padding, String anchor)
    {
        // NOTE: + angle is counterclockwise. - angle is clockwise
        try {
            Canvas canvas = context.canvas;
            GraphicsContext gc = context.gc;

            // Save the current graphics context

            gc.save();

            gc.setFont(font);

            // Set the drawing position
            // These need to be inverted from their logical settings

            char vAlign = anchor.charAt(0);
            char hAlign = anchor.charAt(1);
            double offsetX = 0;
            double offsetT = 0;

            switch (vAlign) {
                case 'T' -> {
                    gc.setTextBaseline(VPos.TOP);
                    offsetT = padding;
                }
                case 'M' -> gc.setTextBaseline(VPos.CENTER);
                case 'B' -> {
                    gc.setTextBaseline(VPos.BASELINE);
                    offsetT = -padding;
                }
            }

            switch (hAlign) {
                case 'R' -> {
                    gc.setTextAlign(TextAlignment.RIGHT);
                    offsetX = -padding;
                }
                case 'C' -> gc.setTextAlign(TextAlignment.CENTER);
                case 'L' -> {
                    gc.setTextAlign(TextAlignment.LEFT);
                    offsetX = padding;
                }
            }

            gc.setFill(color);

            Affine transform = gc.getTransform();

            // Convert sizes from screen units to world units

            Point2D size = transform.inverseDeltaTransform(1D, 0D);
            double scale;
            double xScale = size.getX();
            double yScale = size.getY();
            if (xScale == 0.0) {
                scale = yScale;
            }
            else if (yScale == 0.0) {
                scale = xScale;
            }
            else {
                scale = Math.sqrt(xScale * xScale + yScale * yScale);
            }

            // We scale the font so that the font size comes out constant
            // regardless of the current scale

            transform.appendScale(scale, -scale, x, t);

            // Point (x, t) hasn't changed position. We need to offset by the
            // padding, which is in screen coordinates, so let's repeat the
            // above steps

            size = transform.inverseDeltaTransform(1D, 0D);
            xScale = size.getX();
            yScale = size.getY();
            if (xScale == 0.0) {
                scale = yScale;
            }
            else if (yScale == 0.0) {
                scale = xScale;
            }
            else {
                scale = Math.sqrt(xScale * xScale + yScale * yScale);
            }

            // (x, t) is the position which we will scale and rotate
            // (xOffset, tOffset) is the position at which we will draw the
            // text

            double xOffset = x + (offsetX / scale);
            double tOffset = t + (offsetT / scale);

            // Rotate around the offset

            transform.appendRotation(-angle, x, t);

            // Now draw the text

            gc.setTransform(transform);
            gc.fillText(text, xOffset, tOffset);

//            gc.setStroke(Color.MAGENTA);
//            gc.strokeLine(x, t, x, t+10);
//            gc.strokeLine(x, t, x+10, t);
//            gc.setStroke(Color.CYAN);
//            gc.strokeLine(xOffset, tOffset, xOffset, tOffset+20);
//            gc.strokeLine(xOffset, tOffset, xOffset+20, tOffset);

            // Restore the original graphics context

            gc.restore();
        }
        catch (NonInvertibleTransformException e) {
            throw new ProgrammingException("Text.draw()", e);
        }

    }

    public static Bounds getTextBounds(String text, Font font)
    {
        if (textNode == null) {
            textNode = new javafx.scene.text.Text();
            textNode.setBoundsType(TextBoundsType.LOGICAL);
        }

        textNode.setFont(font);
        textNode.setText(text);
        return textNode.getLayoutBounds();
    }

    public static void test(Context context)
    {
        GraphicsContext gc = context.gc;

        gc.setFill(Color.BLACK);
        gc.fillOval(-1, -1, 2, 2);
        gc.fillOval(19, -1, 2, 2);
        gc.fillOval(-1, 19, 2, 2);
        gc.fillOval(-21, -1, 2, 2);
        gc.fillOval(-1, -21, 2, 2);

        Font font = Font.font(null, 50);
//        draw(context, 0, 0, "A", 0, Color.RED, font, 0, "MC");
//        draw(context, 20, 0, "B", 45, Color.GREEN, font, 0, "MC");
//        draw(context, 0, 20, "C", 90, Color.BLUE, font, 0, "MC");
//        draw(context, -20, 0, "D", -45, Color.CYAN, font, 0, "MC");
//        draw(context, 0, -20, "E", -90, Color.MAGENTA, font, 0, "MC");

//        draw(context, 0, 0,   "A", 0, Color.RED, font, 0, "MC");
//        draw(context, 20, 0,  "B", 0, Color.GREEN, font, 10, "MC");
//        draw(context, 0, 20,  "C", 0, Color.BLUE, font, 20, "MC");
//        draw(context, -20, 0, "D", 0, Color.CYAN, font, 30, "MC");
//        draw(context, 0, -20, "E", 0, Color.MAGENTA, font, 40, "MC");

        draw(context, 0, 0,   "123", 90, Color.RED, font, 10, "TL");
        draw(context, 20, 0,  "123", 90, Color.GREEN, font, 10, "TC");
        draw(context, 0, 20,  "123", 90, Color.BLUE, font, 10, "TR");
        draw(context, -20, 0, "123", 90, Color.CYAN, font, 10, "ML");
        draw(context, 0, -20, "123", 90, Color.MAGENTA, font, 10, "MR");
        draw(context, 0, 0,   "123", 90, Color.YELLOW, font, 10, "BL");
        draw(context, 20, 0,  "123", 90, Color.GRAY, font, 10, "BC");
        draw(context, 0, 20,  "123", 90, Color.PINK, font, 10, "BR");

//        draw(context, 0, 0,   "X", 0, Color.RED, font, 0, "BR");
//        draw(context, 20, 0,  "X", 45, Color.GREEN, font, 0, "BR");
//        draw(context, 0, 20,  "X", 90, Color.BLUE, font, 0, "BR");
//        draw(context, -20, 0, "X", -45, Color.CYAN, font, 0, "BR");
//        draw(context, 0, -20, "X", -90, Color.MAGENTA, font, 0, "BR");

//        draw(context, 0, 0,   "A", 0, Color.RED, font, 10, "TC");
//        draw(context, 20, 0,  "B", 0, Color.GREEN, font, 10, "BC");
//        draw(context, 0, 20,  "C", 0, Color.BLUE, font, 10, "MR");
//        draw(context, -20, 0, "D", 0, Color.CYAN, font, 10, "ML");
//        draw(context, 0, -20, "E", 0, Color.MAGENTA, font, 10, "BR");

//        draw(context, 0, 0, "XXXXX", -90, Color.RED, font, 20, "MR");
//        draw(context, 20, 0, "XXXXX", -90, Color.GREEN, font, 20, "MR");
//        draw(context, 0, 20, "XXXXX", -90, Color.BLUE, font, 20, "MR");
//        draw(context, -20, 0, "XXXXX", -90, Color.CYAN, font, 20, "MR");
//        draw(context, 0, -20, "XXXXX", -90, Color.MAGENTA, font, 20, "MR");
//
    }
}
