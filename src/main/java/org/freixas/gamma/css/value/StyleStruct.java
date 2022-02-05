/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.css.value;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * This class holds all the final style properties used by the Gamma
 * commands. Values are in their final forms:
 * <ul>
 * <li>Float are doubles.
 * <li>Colors are javafx.scene.paint.Colors.
 * <li>Booleans are booleans.
 * <li>Strings are Strings.
 * <li>Font weights are StyleProperties.FontWeightTypes.
 * <li>Font families are Strings.
 * <li>Font styles are StylePropertoes.FontStyleTypes.
 * <li>The above 3 items plus the font size become a javafx.scene.text.Font.
 * <li>Text anchors are StyleProperties.TextAnchorTypes.
 * <li>Arrows are StyleProperties.ArrowTypes.
 * <li>Event shapes are StyleProperties.EventShapeTypes.
 * </ul>
 *
 * @author Antonio Freixas
 */
public class StyleStruct
{
    public Color color = Color.BLACK;
    public Color xColor = color;
    public Color tColor = color;
    public Color xDivColor = color;
    public Color tDivColor = color;
    public Color xMajorDivColor = color;
    public Color tMajorDivColor = color;
    public Color textColor = color;
    public Color xTextColor = textColor;
    public Color tTextColor = textColor;
    public Color backgroundColor = Color.WHITE;

    public double lineThickness = 1.0;
    public double xLineThickness = lineThickness;
    public double tLineThickness = lineThickness;
    public double xDivLineThickness = lineThickness;
    public double tDivLineThickness = lineThickness;
    public double xMajorDivLineThickness = lineThickness;
    public double tMajorDivLineThickness = lineThickness;

    public StyleProperties.LineStyle lineStyle = StyleProperties.LineStyle.SOLID;
    public StyleProperties.LineStyle xLineStyle = lineStyle;
    public StyleProperties.LineStyle tLineStyle = lineStyle;
    public StyleProperties.LineStyle xDivLineStyle = lineStyle;
    public StyleProperties.LineStyle tDivLineStyle = lineStyle;
    public StyleProperties.LineStyle xMajorDivLineStyle = lineStyle;
    public StyleProperties.LineStyle tMajorDivLineStyle = lineStyle;

    public String fontFamily = "System";
    public String xTickFontFamily = fontFamily;
    public String tTickFontFamily = fontFamily;
    public FontWeight fontWeight = FontWeight.MEDIUM;
    public FontWeight  xTickFontWeight = FontWeight.MEDIUM;
    public FontWeight  tTickFontWeight = FontWeight.MEDIUM;
    public FontPosture fontStyle = FontPosture.REGULAR;
    public FontPosture xTickFontStyle = FontPosture.REGULAR;
    public FontPosture tTickFontStyle = FontPosture.REGULAR;
    public double fontSize = 10.0;
    public double xTickFontSize = fontSize;
    public double tTickFontSize = fontSize;

    public Font font = null;
    public Font xTickFont = null;
    public Font tTickFont = null;

    public double textPaddingTop = 0.0;
    public double textPaddingBottom = 0.0;
    public double textPaddingLeft = 0.0;
    public double textPaddingRight = 0.0;
    public StyleProperties.TextAnchor textAnchor = StyleProperties.TextAnchor.TC;

    public boolean xTicks = true;
    public boolean tTicks = true;
    public boolean xTickLabels = true;
    public boolean tTickLabels = true;
    public double tickLength = 3.0;
    public double majorTickLength = 10.0;

    public boolean leftQuadrant = true;
    public boolean rightQuadrant = true;
    public boolean topQuadrant = true;
    public boolean bottomQuadrant = true;

    public double opacity = 1.0;
    public StyleProperties.Arrow arrow = StyleProperties.Arrow.NONE;
    public double arrowWidth = 10.0;
    public double arrowHeight = 8.0;
    public double eventDiameter = 5.0;
    public StyleProperties.EventShape eventShape = StyleProperties.EventShape.CIRCLE;
}
