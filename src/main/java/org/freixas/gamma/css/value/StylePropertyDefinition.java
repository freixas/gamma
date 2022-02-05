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

import org.freixas.gamma.ProgrammingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import javafx.scene.text.Font;

/**
 * This class defines all the style properties, with their external names, field
 * names (the name of the field in the StyleStruct, if any), data types,
 * minimums and maximums, and any dependent properties (a property with
 * dependencies should, when set, also set all its dependent properties to the
 * same value).
 *
 * @author Antonio Freixas
 */
public class StylePropertyDefinition
{
    public enum Min { NONE, EQ0, GT0 }
    public enum Max { NONE, EQ1 }

    // **********************************************************************
    // *
    // * All Definitions
    // *
    // **********************************************************************

    static final HashMap<String, Field> fieldMap = new HashMap<>();
    static final HashMap<String, StylePropertyDefinition> defMap = new HashMap<>();
    static final Field[] fields;

    static {

        // Put all the fields in the StyleStruct into a HashMap

        fields = StyleStruct.class.getFields();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
    }

    // Colors

    static final StylePropertyDefinition xColor = new StylePropertyDefinition("x-color", fieldMap.get("xColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition tColor = new StylePropertyDefinition("t-color", fieldMap.get("tColor"), StyleProperty.Type.COLOR);

    static final StylePropertyDefinition xMajorDivColor = new StylePropertyDefinition("x-major-div-color", fieldMap.get("xMajorDivColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition tMajorDivColor = new StylePropertyDefinition("t-major-div-color", fieldMap.get("tMajorDivColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition[] majorDivColorDep = { xMajorDivColor, tMajorDivColor };
    static final StylePropertyDefinition majorDivColor = new StylePropertyDefinition("major-div-color", null, StyleProperty.Type.COLOR, majorDivColorDep);

    static final StylePropertyDefinition xDivColor = new StylePropertyDefinition("x-div-color", fieldMap.get("xDivColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition tDivColor = new StylePropertyDefinition("t-div-color", fieldMap.get("tDivColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition[] divColorDep = { xDivColor, tDivColor, majorDivColor };
    static final StylePropertyDefinition divColor = new StylePropertyDefinition("div-color", null, StyleProperty.Type.COLOR, divColorDep);

    static final StylePropertyDefinition xTextColor = new StylePropertyDefinition("x-text-color", fieldMap.get("xTextColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition tTextColor = new StylePropertyDefinition("t-text-color", fieldMap.get("tTextColor"), StyleProperty.Type.COLOR);
    static final StylePropertyDefinition[] textColorDep = { xTextColor, tTextColor };
    static final StylePropertyDefinition textColor = new StylePropertyDefinition("text-color", fieldMap.get("textColor"), StyleProperty.Type.COLOR, textColorDep);

    static final StylePropertyDefinition[] colorDep = { xColor, tColor, divColor, textColor };
    static final StylePropertyDefinition color = new StylePropertyDefinition("color", fieldMap.get("color"), StyleProperty.Type.COLOR, colorDep);

    static final StylePropertyDefinition backgroundColor = new StylePropertyDefinition("background-color", fieldMap.get("backgroundColor"), StyleProperty.Type.COLOR);

    // Lines

    static final StylePropertyDefinition xLineThickness = new StylePropertyDefinition("x-line-thickness", fieldMap.get("xLineThickness"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition tLineThickness = new StylePropertyDefinition("t-line-thickness", fieldMap.get("tLineThickness"), StyleProperty.Type.FLOAT, Min.GT0);

    static final StylePropertyDefinition xMajorDivLineThickness = new StylePropertyDefinition("x-major-div-line-thickness", fieldMap.get("xMajorDivLineThickness"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition tMajorDivLineThickness = new StylePropertyDefinition("t-major-div-line-thickness", fieldMap.get("tMajorDivLineThickness"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition[] majorDivLineThicknessDep = { xMajorDivLineThickness, tMajorDivLineThickness };
    static final StylePropertyDefinition majorDivLineThickness = new StylePropertyDefinition("major-div-line-thickness", null, StyleProperty.Type.FLOAT, Min.GT0, majorDivLineThicknessDep);

    static final StylePropertyDefinition xDivLineThickness = new StylePropertyDefinition("x-div-line-thickness", fieldMap.get("xDivLineThickness"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition tDivLineThickness = new StylePropertyDefinition("t-div-line-thickness", fieldMap.get("tDivLineThickness"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition[] divLineThicknessDep = { xDivLineThickness, tDivLineThickness, majorDivLineThickness };
    static final StylePropertyDefinition divLineThickness = new StylePropertyDefinition("div-line-thickness", null, StyleProperty.Type.FLOAT, Min.GT0, divLineThicknessDep);

    static final StylePropertyDefinition[] lineThicknessDep = { xLineThickness, tLineThickness, divLineThickness };
    static final StylePropertyDefinition lineThickness = new StylePropertyDefinition("line-thickness", fieldMap.get("lineThickness"), StyleProperty.Type.FLOAT, Min.GT0, lineThicknessDep);


    static final StylePropertyDefinition xLineStyle = new StylePropertyDefinition("x-line-style", fieldMap.get("xLineStyle"), StyleProperty.Type.LINE_STYLE);
    static final StylePropertyDefinition tLineStyle = new StylePropertyDefinition("t-line-style", fieldMap.get("tLineStyle"), StyleProperty.Type.LINE_STYLE);

    static final StylePropertyDefinition xMajorDivLineStyle = new StylePropertyDefinition("x-major-div-line-style", fieldMap.get("xMajorDivLineStyle"), StyleProperty.Type.LINE_STYLE);
    static final StylePropertyDefinition tMajorDivLineStyle = new StylePropertyDefinition("t-major-div-line-style", fieldMap.get("tMajorDivLineStyle"), StyleProperty.Type.LINE_STYLE);
    static final StylePropertyDefinition[] majorDivLineStyleDep = { xMajorDivLineStyle, tMajorDivLineStyle };
    static final StylePropertyDefinition majorDivLineStyle = new StylePropertyDefinition("major-div-line-style", null, StyleProperty.Type.LINE_STYLE, majorDivLineStyleDep);

    static final StylePropertyDefinition xDivLineStyle = new StylePropertyDefinition("x-div-line-style", fieldMap.get("xDivLineStyle"), StyleProperty.Type.LINE_STYLE);
    static final StylePropertyDefinition tDivLineStyle = new StylePropertyDefinition("t-div-line-style", fieldMap.get("tDivLineStyle"), StyleProperty.Type.LINE_STYLE);
    static final StylePropertyDefinition[] divLineStyleDep = { xDivLineStyle, tDivLineStyle, majorDivLineStyle };
    static final StylePropertyDefinition divLineStyle = new StylePropertyDefinition("div-line-style", null, StyleProperty.Type.LINE_STYLE, divLineStyleDep);

    static final StylePropertyDefinition[] lineStyleDep = { xLineStyle, tLineStyle, divLineStyle };
    static final StylePropertyDefinition lineStyle = new StylePropertyDefinition("line-style", fieldMap.get("lineStyle"), StyleProperty.Type.LINE_STYLE, lineStyleDep);

    // Font components

    static final StylePropertyDefinition xTickFontFamily = new StylePropertyDefinition("x-tick-font-family", fieldMap.get("xTickFontFamily"), StyleProperty.Type.STRING);
    static final StylePropertyDefinition tTickFontFamily = new StylePropertyDefinition("t-tick-font-family", fieldMap.get("tTickFontFamily"), StyleProperty.Type.STRING);
    static final StylePropertyDefinition[] tickFontFamilyDep = { xTickFontFamily, tTickFontFamily };
    static final StylePropertyDefinition tickFontFamily = new StylePropertyDefinition("tick-font-family", null, StyleProperty.Type.STRING, tickFontFamilyDep);
    static final StylePropertyDefinition[] fontFamilyDep = { tickFontFamily };
    static final StylePropertyDefinition fontFamily = new StylePropertyDefinition("font-family", fieldMap.get("fontFamily"), StyleProperty.Type.STRING, fontFamilyDep);

    static final StylePropertyDefinition xTickFontWeight = new StylePropertyDefinition("x-tick-font-weight", fieldMap.get("xTickFontWeight"), StyleProperty.Type.FONT_WEIGHT);
    static final StylePropertyDefinition tTickFontWeight = new StylePropertyDefinition("t-tick-font-weight", fieldMap.get("tTickFontWeight"), StyleProperty.Type.FONT_WEIGHT);
    static final StylePropertyDefinition[] tickFontWeightDep = { xTickFontWeight, tTickFontWeight };
    static final StylePropertyDefinition tickFontWeight = new StylePropertyDefinition("tick-font-weight", null, StyleProperty.Type.FONT_WEIGHT, tickFontWeightDep);
    static final StylePropertyDefinition[] fontWeightDep = { tickFontWeight };
    static final StylePropertyDefinition fontWeight = new StylePropertyDefinition("font-weight", fieldMap.get("fontWeight"), StyleProperty.Type.FONT_WEIGHT, fontWeightDep);

    static final StylePropertyDefinition xTickFontStyle = new StylePropertyDefinition("x-tick-font-style", fieldMap.get("xTickFontStyle"), StyleProperty.Type.FONT_STYLE);
    static final StylePropertyDefinition tTickFontStyle = new StylePropertyDefinition("t-tick-font-style", fieldMap.get("tTickFontStyle"), StyleProperty.Type.FONT_STYLE);
    static final StylePropertyDefinition[] tickFontStyleDep = { xTickFontStyle, tTickFontStyle };
    static final StylePropertyDefinition tickFontStyle = new StylePropertyDefinition("tick-font-style", null, StyleProperty.Type.FONT_STYLE, tickFontStyleDep);
    static final StylePropertyDefinition[] fontStyleDep = { tickFontStyle };
    static final StylePropertyDefinition fontStyle = new StylePropertyDefinition("font-style", fieldMap.get("fontStyle"), StyleProperty.Type.FONT_STYLE, fontStyleDep);

    static final StylePropertyDefinition xTickFontSize = new StylePropertyDefinition("x-tick-font-size", fieldMap.get("xTickFontSize"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition tTickFontSize = new StylePropertyDefinition("t-tick-font-size", fieldMap.get("tTickFontSize"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition[] tickFontSizeDep = { xTickFontSize, tTickFontSize };
    static final StylePropertyDefinition tickFontSize = new StylePropertyDefinition("tick-font-size", fieldMap.get("tickFontSize"), StyleProperty.Type.FLOAT, Min.GT0, tickFontSizeDep);
    static final StylePropertyDefinition[] fontSizeDep = { tickFontSize };
    static final StylePropertyDefinition fontSize = new StylePropertyDefinition("font-size", fieldMap.get("fontSize"), StyleProperty.Type.FLOAT, Min.GT0, fontSizeDep);

    // Fonts

    static final StylePropertyDefinition xTickFont = new StylePropertyDefinition("?x-tick-font", fieldMap.get("xTickFont"), StyleProperty.Type.FONT);
    static final StylePropertyDefinition tTickFont = new StylePropertyDefinition("?t-tick-font", fieldMap.get("tTickFont"), StyleProperty.Type.FONT);
    static final StylePropertyDefinition[] fontDep = { xTickFont, tTickFont };
    static final StylePropertyDefinition font = new StylePropertyDefinition("?font", fieldMap.get("font"), StyleProperty.Type.FONT, fontDep);

    static {
        xTickFontFamily.setfontDependency(xTickFont);
        xTickFontWeight.setfontDependency(xTickFont);
        xTickFontStyle.setfontDependency(xTickFont);
        xTickFontSize.setfontDependency(xTickFont);

        tTickFontFamily.setfontDependency(tTickFont);
        tTickFontWeight.setfontDependency(tTickFont);
        tTickFontStyle.setfontDependency(tTickFont);
        tTickFontSize.setfontDependency(tTickFont);

        fontFamily.setfontDependency(font);
        fontWeight.setfontDependency(font);
        fontStyle.setfontDependency(font);
        fontSize.setfontDependency(font);
    }

    // Text

    static final StylePropertyDefinition textPaddingTop = new StylePropertyDefinition("text-padding-top", fieldMap.get("textPaddingTop"), StyleProperty.Type.FLOAT, Min.EQ0);
    static final StylePropertyDefinition textPaddingBottom = new StylePropertyDefinition("text-padding-bottom", fieldMap.get("textPaddingBottom"), StyleProperty.Type.FLOAT, Min.EQ0);
    static final StylePropertyDefinition textPaddingLeft = new StylePropertyDefinition("text-padding-left", fieldMap.get("textPaddingLeft"), StyleProperty.Type.FLOAT, Min.EQ0);
    static final StylePropertyDefinition textPaddingRight = new StylePropertyDefinition("text-padding-right", fieldMap.get("textPaddingRight"), StyleProperty.Type.FLOAT, Min.EQ0);
    static final StylePropertyDefinition[] textPaddingDep = { textPaddingTop, textPaddingBottom, textPaddingLeft, textPaddingRight };
    static final StylePropertyDefinition textPadding = new StylePropertyDefinition("text-padding", null, StyleProperty.Type.FLOAT, Min.EQ0, textPaddingDep);

    static final StylePropertyDefinition textAnchor = new StylePropertyDefinition("text-anchor", fieldMap.get("textAnchor"), StyleProperty.Type.TEXT_ANCHOR);

    // Ticks

    static final StylePropertyDefinition xTicks = new StylePropertyDefinition("x-ticks", fieldMap.get("xTicks"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition tTicks = new StylePropertyDefinition("t-ticks", fieldMap.get("tTicks"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition[] ticksDep = { xTicks, tTicks };
    static final StylePropertyDefinition ticks = new StylePropertyDefinition("ticks", null, StyleProperty.Type.BOOLEAN, ticksDep);

    static final StylePropertyDefinition xTickLabels = new StylePropertyDefinition("x-tick-labels", fieldMap.get("xTickLabels"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition tTickLabels = new StylePropertyDefinition("t-tick-labels", fieldMap.get("tTickLabels"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition[] tickLabelsDep = { xTickLabels, tTickLabels };
    static final StylePropertyDefinition tickLabels = new StylePropertyDefinition("tick-labels", null, StyleProperty.Type.BOOLEAN, tickLabelsDep);

    static final StylePropertyDefinition tickLength = new StylePropertyDefinition("tick-length", fieldMap.get("tickLength"), StyleProperty.Type.FLOAT, Min.EQ0);
    static final StylePropertyDefinition majorTickLength = new StylePropertyDefinition("major-tick-length", fieldMap.get("majorTickLength"), StyleProperty.Type.FLOAT, Min.EQ0);

    // Hypergrid

    static final StylePropertyDefinition leftQuadrant = new StylePropertyDefinition("left-quadrant", fieldMap.get("leftQuadrant"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition rightQuadrant = new StylePropertyDefinition("right-quadrant", fieldMap.get("rightQuadrant"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition topQuadrant = new StylePropertyDefinition("top-quadrant", fieldMap.get("topQuadrant"), StyleProperty.Type.BOOLEAN);
    static final StylePropertyDefinition bottomQuadrant = new StylePropertyDefinition("bottom-quadrant", fieldMap.get("bottomQuadrant"), StyleProperty.Type.BOOLEAN);

    // Miscellaneous

    static final StylePropertyDefinition opacity = new StylePropertyDefinition("opacity", fieldMap.get("opacity"), StyleProperty.Type.FLOAT, Min.EQ0);

    static final StylePropertyDefinition arrow = new StylePropertyDefinition("arrow", fieldMap.get("arrow"), StyleProperty.Type.ARROW);
    static final StylePropertyDefinition arrowWidth = new StylePropertyDefinition("arrow-width", fieldMap.get("arrowWidth"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition arrowHeight = new StylePropertyDefinition("arrow-height", fieldMap.get("arrowHeight"), StyleProperty.Type.FLOAT, Min.GT0);

    static final StylePropertyDefinition eventDiameter = new StylePropertyDefinition("event-diameter", fieldMap.get("eventDiameter"), StyleProperty.Type.FLOAT, Min.GT0);
    static final StylePropertyDefinition eventShape = new StylePropertyDefinition("event-shape", fieldMap.get("eventShape"), StyleProperty.Type.EVENT_SHAPE);

    static {

        // Colors

        defMap.put("x-color", xColor);
        defMap.put("t-color", tColor);

        defMap.put("x-div-color", xDivColor);
        defMap.put("t-div-color", tDivColor);
        defMap.put("div-color", divColor);

        defMap.put("x-major-div-color", xMajorDivColor);
        defMap.put("t-major-div-color", tMajorDivColor);
        defMap.put("major-div-color", majorDivColor);

        defMap.put("x-text-color", xTextColor);
        defMap.put("t-text-color", tTextColor);
        defMap.put("text-color", textColor);

        defMap.put("color", color);

        defMap.put("background-color", backgroundColor);

        // Lines

        defMap.put("x-line-thickness", xLineThickness);
        defMap.put("t-line-thickness", tLineThickness);

        defMap.put("x-div-line-thickness", xDivLineThickness);
        defMap.put("t-div-line-thickness", tDivLineThickness);
        defMap.put("div-line-thickness", divLineThickness);

        defMap.put("x-major-div-line-thickness", xMajorDivLineThickness);
        defMap.put("t-major-div-line-thickness", tMajorDivLineThickness);
        defMap.put("major-div-line-thickness", majorDivLineThickness);

        defMap.put("line-thickness", lineThickness);

        defMap.put("x-line-style", xLineStyle);
        defMap.put("t-line-style", tLineStyle);

        defMap.put("x-div-line-style", xDivLineStyle);
        defMap.put("t-div-line-style", tDivLineStyle);
        defMap.put("div-line-style", divLineStyle);

        defMap.put("x-major-div-line-style", xMajorDivLineStyle);
        defMap.put("t-major-div-line-style", tMajorDivLineStyle);
        defMap.put("major-div-line-style", majorDivLineStyle);

        defMap.put("line-style", lineStyle);

        // Fonts

        defMap.put("x-tick-font-family", xTickFontFamily);
        defMap.put("t-tick-font-family", tTickFontFamily);
        defMap.put("tick-font-family", tickFontFamily);
        defMap.put("font-family", fontFamily);

        defMap.put("x-tick-font-weight", xTickFontWeight);
        defMap.put("t-tick-font-weight", tTickFontWeight);
        defMap.put("tick-font-weight", tickFontWeight);
        defMap.put("font-weight", fontWeight);

        defMap.put("x-tick-font-style", xTickFontStyle);
        defMap.put("t-tick-font-style", tTickFontStyle);
        defMap.put("tick-font-style", tickFontStyle);
        defMap.put("font-style", fontStyle);

        defMap.put("x-tick-font-size", xTickFontSize);
        defMap.put("t-tick-font-size", tTickFontSize);
        defMap.put("tick-font-size", tickFontSize);
        defMap.put("font-size", fontSize);

        // Text

        defMap.put("text-padding-top", textPaddingTop);
        defMap.put("text-padding-bottom", textPaddingBottom);
        defMap.put("text-padding-left", textPaddingLeft);
        defMap.put("text-padding-right", textPaddingRight);
        defMap.put("text-padding", textPadding);

        defMap.put("text-anchor", textAnchor);

        // Ticks

        defMap.put("x-ticks", xTicks);
        defMap.put("t-ticks", tTicks);
        defMap.put("ticks", ticks);

        defMap.put("x-tick-labels", xTickLabels);
        defMap.put("t-tick-labels", tTickLabels);
        defMap.put("tick-labels", tickLabels);

        defMap.put("tick-length", tickLength);
        defMap.put("major-tick-length", majorTickLength);

        // Hypergrid

        defMap.put("left-quadrant", leftQuadrant);
        defMap.put("right-quadrant", rightQuadrant);
        defMap.put("top-quadrant", topQuadrant);
        defMap.put("bottom-quadrant", bottomQuadrant);

        // Miscellaneous

        defMap.put("opacity", opacity);

        defMap.put("arrow", arrow);
        defMap.put("arrow-width", arrowWidth);
        defMap.put("arrow-height", arrowHeight);

        defMap.put("event-diameter", eventDiameter);
        defMap.put("event-shape", eventShape);
    }

    private final String propName;
    private final Field field;
    private final StyleProperty.Type type;
    private final Min min;
    private final Max max;
    private final StylePropertyDefinition[] dependencies;
    private StylePropertyDefinition fDependency;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    public StylePropertyDefinition(
        String propName, Field field, StyleProperty.Type type)
    {
        this(propName, field, type, Min.NONE, Max.NONE, null);
    }

    public StylePropertyDefinition(
        String propName, Field field, StyleProperty.Type type,
        StylePropertyDefinition[] dependencies)
    {
        this(propName, field, type, Min.NONE, Max.NONE, dependencies);
    }

    public StylePropertyDefinition(
        String propName, Field field, StyleProperty.Type type, Min min)
    {
        this(propName, field, type, min, Max.NONE, null);
    }

    public StylePropertyDefinition(
        String propName, Field field, StyleProperty.Type type, Min min, Max max)
    {
        this(propName, field, type, min, max, null);
    }

    public StylePropertyDefinition(
        String propName, Field field, StyleProperty.Type type, Min min,
        StylePropertyDefinition[] dependencies)
    {
        this(propName, field, type, min, Max.NONE, dependencies);
    }

    public StylePropertyDefinition(
        String propName, Field field, StyleProperty.Type type, Min min, Max max,
        StylePropertyDefinition[] dependencies)
    {
        this.propName = propName;
        this.field = field;
        this.type = type;
        this.min = min;
        this.max = max;
        this.dependencies = dependencies;
        this.fDependency = null;
    }

    public static StylePropertyDefinition toDefinition(String name)
    {
        return defMap.get(name);
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    public StyleProperty.Type getType()
    {
        return type;
    }

    public String getPropName()
    {
        return propName;
    }

    public Field getField()
    {
        return field;
    }

    public Min getMin()
    {
        return min;
    }

    public Max getMax()
    {
        return max;
    }

    public StylePropertyDefinition[] getDependencies()
    {
        return dependencies;
    }

    public StylePropertyDefinition getfontDependency()
    {
        return fDependency;
    }

    public void setfontDependency(StylePropertyDefinition fDependency)
    {
        this.fDependency = fDependency;
    }

    public void setStyleStructValue(StyleStruct styles, Object value)
    {
        try {

            // If we have a matching field in the StyleStruct, set it

            if (field != null) {
                if (value == null || field.getType().isAssignableFrom(value.getClass())) {
                    field.set(styles, value);
                }
                else if (field.getType().getName().equals("double") && value.getClass() == Double.class) {
                    field.set(styles, (double)value);
                }
                else if (field.getType().getName().equals("boolean") && value.getClass() == Boolean.class) {
                    field.set(styles, (boolean)value);
                }
                else {
                    throw new ProgrammingException("StylePropertyDefinition.setStyleStructValue: Can't assign value to field");
                }
            }

            // If we have dependent style properties, set their values as well

            if (dependencies != null && dependencies.length > 0) {
                for (StylePropertyDefinition dependency : dependencies) {
                    dependency.setStyleStructValue(styles, value);
                }
            }
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ProgrammingException("StylePropertyDefinition.setStyleStructValue", e);
        }
    }

    /**
     * Generate the final fonts. If any font component (name, weight, style, size)
     * has changed, we need to generate the corresponding font.
     *
     * @param styles The StylesStruct in which to generate the fonts
     */
    static public void generateFonts(StyleStruct styles)
    {
        if (styles.font == null) {
            styles.font = Font.font(styles.fontFamily, styles.fontWeight, styles.fontStyle, styles.fontSize);
        }
        if (styles.xTickFont == null) {
            styles.xTickFont = Font.font(styles.xTickFontFamily, styles.xTickFontWeight, styles.xTickFontStyle, styles.xTickFontSize);
        }
        if (styles.tTickFont == null) {
            styles.tTickFont = Font.font(styles.tTickFontFamily, styles.tTickFontWeight, styles.tTickFontStyle, styles.tTickFontSize);
        }
    }

}
