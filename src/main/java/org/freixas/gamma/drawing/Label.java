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

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.execution.lcode.LabelStruct;
import org.freixas.gamma.css.value.StyleStruct;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 * Draw a label.
 *
 * @author Antonio Freixas
 */
public class Label
{
    static private javafx.scene.text.Text textNode = null;

    /**
     *  Draw a label.
     *
     * @param context The drawing context.
     * @param struct The label properties.
     * @param styles The style properties.
     */
    static public void draw(Context context, LabelStruct struct, StyleStruct styles)
    {
        draw(context, struct, styles.textColor, styles.font, styles);
    }

    /**
     *  Draw a label.
     *
     * @param context The drawing context.
     * @param struct The label properties.
     * @param color The label color (overrides the textColor style).
     * @param font The font (overrides the font style).
     * @param styles The style properties.
     */
    static public void draw(Context context, LabelStruct struct, Color color, Font font, StyleStruct styles)
    {
        // NOTE: + angle is counterclockwise. - angle is clockwise

        try {
            GraphicsContext gc = context.gc;

            // Save the current graphics context

            gc.save();

            gc.setFont(font);

            // Set the text anchor. The T and B values need to be inverted
            // since we have inverted the screen units in the y direction

            char vAlign = styles.textAnchor.toString().charAt(0);
            char hAlign = styles.textAnchor.toString().charAt(1);
            double offsetX = 0;
            double offsetT = 0;

            switch (vAlign) {
                case 'T' -> {
                    gc.setTextBaseline(VPos.TOP);
                    offsetT = styles.textPaddingTop;
                }
                case 'M' -> gc.setTextBaseline(VPos.CENTER);
                case 'B' -> {
                    gc.setTextBaseline(VPos.BASELINE);
                    offsetT = -styles.textPaddingBottom;
                }
            }

            switch (hAlign) {
                case 'R' -> {
                    gc.setTextAlign(TextAlignment.RIGHT);
                    offsetX = -styles.textPaddingRight;
                }
                case 'C' -> gc.setTextAlign(TextAlignment.CENTER);
                case 'L' -> {
                    gc.setTextAlign(TextAlignment.LEFT);
                    offsetX = styles.textPaddingLeft;
                }
            }

            gc.setFill(color);

            Affine transform = gc.getTransform();

            // Convert sizes from screen units to world units

            double scale = context.getCurrentInvScale();

            // We invScale the font so that the font size comes out constant
            // regardless of the current invScale

            transform.appendScale(scale, -scale, struct.location.x, struct.location.t);

            // Point (x, t) hasn't changed position. We need to offset by the
            // padding, which is in screen coordinates, so let's repeat the
            // above steps

            Point2D size = transform.inverseDeltaTransform(1D, 0D);
            scale = size.getX();

            // (x, t) is the position which we will invScale and rotate
            // (xOffset, tOffset) is the position at which we will draw the
            // text

            double xOffset = struct.location.x + (offsetX / scale);
            double tOffset = struct.location.t + (offsetT / scale);

            // Rotate around the offset

            transform.appendRotation(-struct.rotation, struct.location.x, struct.location.t);

            // Now draw the text

            gc.setTransform(transform);
            gc.fillText(struct.text, xOffset, tOffset);

            // Debug code - places an "X" centered exactly on the coordinate
            // at which to draw the text

            //gc.setStroke(Color.RED);
            //gc.strokeLine(
            //    xOffset - 5, tOffset - 5,
            //    xOffset + 5, tOffset + 5);
            //gc.strokeLine(
            //    xOffset - 5, tOffset + 5,
            //    xOffset + 5, tOffset - 5);

            // Restore the original graphics context

            gc.restore();
        }
        catch (NonInvertibleTransformException e) {
            throw new ProgrammingException("Text.draw()", e);
        }
    }

    /**
     * Given a text string and a font, determine the bounds of the text
     * in screen units.
     *
     * @param text The text string.
     * @param font The font.
     *
     * @return The bounds of the text string in screen units.
     */
    static public Bounds getTextBounds(String text, Font font)
    {
        if (textNode == null) {
            textNode = new javafx.scene.text.Text();
            textNode.setBoundsType(TextBoundsType.LOGICAL);
        }

        textNode.setFont(font);
        textNode.setText(text);
        return textNode.getLayoutBounds();
    }

}
