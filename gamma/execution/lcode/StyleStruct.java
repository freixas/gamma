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

import gamma.value.Color;
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import gamma.math.Lorentz;
import gamma.math.Util;
import gamma.value.Frame;
import gamma.value.Property;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 *
 * @author Antonio Freixas
 */
public class StyleStruct extends Struct
{
    public Color color = Color.blackColor;
    public Color backgroundColor = Color.whiteColor;
    public double lineThickness = 2.0;
    public String lineStyle = "solid";
    public String arrow = "none";
    public double eventDiameter = 5.0;
    public String eventShape = "circle";
    public String fontFamily = null;
    public String fontWeigth = "normal";
    public String fontStyle = "normal";
    public double fontSize = 10.0;
    public double textPadding = 2.0;
    public String textAnchor = "TC";
    public double textRotation = 0.0;
    public boolean ticks = true;
    public boolean tickLabels = true;
    public Color divColor = null;
    public Color majorDivColor = null;
    public double divThickness = 1.0;
    public double majorDivThickness = 2.0;

    public javafx.scene.paint.Color javaFXColor;
    public javafx.scene.paint.Color javaFXBackgroundColor;
    public javafx.scene.paint.Color javaFXDivColor;
    public javafx.scene.paint.Color javaFXMajorDivColor;
    public Font font;

    public static boolean isStyleProperty(Property property)
    {
        switch (property.getName()) {
            case "color" -> 		{ return true; }
            case "backgroundColor" ->	{ return true; }
            case "lineThickness" ->	{ return true; }
            case "lineStyle" -> 	{ return true; }
            case "arrow" -> 		{ return true; }
            case "eventDiameter" -> 	{ return true; }
            case "eventShape" -> 	{ return true; }
            case "fontFamily" -> 	{ return true; }
            case "fontWeight" -> 	{ return true; }
            case "fontStyle" -> 	{ return true; }
            case "fontSize" -> 		{ return true; }
            case "textPadding" ->	{ return true; }
            case "textAnchor" -> 	{ return true; }
            case "textRotation" ->	{ return true; }
            case "ticks" -> 		{ return true; }
            case "tickLabels" -> 	{ return true; }
            case "divThickness" -> 	{ return true; }
            case "majorDivThickness" -> { return true; }
            default -> 			{ return false; }
        }
    }

    public StyleStruct()
    {
    }

    public StyleStruct(StyleStruct other)
    {
        this.color = other.color;
        this.backgroundColor = other.backgroundColor;
        this.lineThickness = other.lineThickness;
        this.lineStyle = other.lineStyle;
        this.arrow = other.arrow;
        this.eventDiameter = other.eventDiameter;
        this.eventShape = other.eventShape;
        this.fontFamily = other.fontFamily;
        this.fontWeigth = other.fontWeigth;
        this.fontStyle = other.fontStyle;
        this.fontSize = other.fontSize;
        this.textPadding = other.textPadding;
        this.textAnchor = other.textAnchor;
        this.ticks = other.ticks;
        this.tickLabels = other.tickLabels;
        this.divThickness = other.divThickness;
        this.majorDivThickness = other.majorDivThickness;
    }

    @Override
    public void finalizeValues()
    {
        javaFXColor = color.getJavaFXColor();
        javaFXBackgroundColor = backgroundColor.getJavaFXColor();

        FontWeight weight;
        switch (fontWeigth) {

            case "bold" -> weight = FontWeight.BOLD;
            default -> weight = FontWeight.NORMAL;
        }
        FontPosture posture;
        switch (fontStyle) {

            case "italic" -> posture = FontPosture.ITALIC;
            default -> posture = FontPosture.REGULAR;
        }

        // I'm not sure that the font() method ever fails

        font = Font.font​(fontFamily, weight, posture, fontSize);
        if (font == null) {
            font = Font.font​(null, weight, posture, fontSize);
            if (font == null) {
                font = Font.getDefault();
            }
        }

        textRotation = Util.normalizeAngle90(textRotation);

        javaFXDivColor = divColor == null ? javaFXColor : divColor.getJavaFXColor();
        javaFXMajorDivColor = majorDivColor == null ? javaFXDivColor : majorDivColor.getJavaFXColor();
    }

    public void lineThicknessRangeCheck()
    {
        if (lineThickness <= 0.0) {
            throw new ExecutionException("'lineThickness' must be greater than 0");
        }
    }

    public void lineStyleRangeCheck()
    {
        if (!lineStyle.equals("solid") &&
            !lineStyle.equals("dashed") &&
            !lineStyle.equals("dotted")) {
            throw new ExecutionException("The value used for property 'lineStyle' is invalid");
        }
    }

    public void arrowRangeCheck()
    {
        if (!arrow.equals("none") &&
            !arrow.equals("both") &&
            !arrow.equals("start") &&
            !arrow.equals("end")) {
            throw new ExecutionException("The value used for property 'arrow' is invalid");
        }
    }

    public void eventDiameterRangeCheck()
    {
        if (eventDiameter <= 0.0) {
            throw new ExecutionException("'eventDiameter' must be greater than 0");
        }
    }

    public void eventShapeRangeCheck()
    {
        if (!eventShape.equals("circle") &&
            !eventShape.equals("square") &&
            !eventShape.equals("diamond") &&
            !eventShape.equals("star")) {
            throw new ExecutionException("The value used for property 'eventShape' is invalid");
        }
    }

    public void weightRangeCheck()
    {
        if (!fontWeigth.equals("normal") &&
            !fontWeigth.equals("bold")) {
            throw new ExecutionException("The value used for property 'weight' is invalid");
        }
    }

    public void slantRangeCheck()
    {
        if (!fontStyle.equals("normal") &&
            !fontStyle.equals("italic")) {
            throw new ExecutionException("The value used for property 'slant' is invalid");
        }
    }

    public void fontSizeRangeCheck()
    {
        if (fontSize <= 0.0) {
            throw new ExecutionException("'fontSize' must be greater than 0");
        }
    }

    public void textPaddingRangeCheck()
    {
        if (textPadding < 0.0) {
            throw new ExecutionException("'lineThickness' must not be negative");
        }
    }

    public void anchorRangeCheck()
    {
        if (!textAnchor.equals("TL") &&
            !textAnchor.equals("TC") &&
            !textAnchor.equals("TR") &&
            !textAnchor.equals("ML") &&
            !textAnchor.equals("MC") &&
            !textAnchor.equals("MR") &&
            !textAnchor.equals("BL") &&
            !textAnchor.equals("BC") &&
            !textAnchor.equals("BR")) {
            throw new ExecutionException("The value used for property 'anchor' is invalid");
        }
    }

    public void tickThicknessRangeCheck()
    {
        if (divThickness <= 0.0) {
            throw new ExecutionException("'divThickness' must be greater than 0");
        }
    }

    public void majorTickThicknessRangeCheck()
    {
        if (majorDivThickness <= 0.0) {
            throw new ExecutionException("'majorDivThickness' must be greater than 0");
        }
    }

    @Override
    public void relativeTo(Frame prime)
    {
        textRotation = Lorentz.toPrimeAngle(textRotation, prime.getV());
    }

}
