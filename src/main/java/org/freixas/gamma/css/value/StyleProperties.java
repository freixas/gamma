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

import org.freixas.gamma.math.Util;
import org.freixas.gamma.parser.Token;
import java.util.HashMap;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * This class contains various methods used to manipulate style properties.
 *
 * @author Antonio Freixas
 */
public final class StyleProperties
{
    // **********************************************************************
    // *
    // * Support for style property value enums.
    // *
    // **********************************************************************

    interface StylePropertyValueEnum
    {
        /**
         * Gets the string name associated with an enum (which may not be
         * exactly the same as the name of the enum).
         *
         * @return The string name associated with an enum.
         */
        String getName();
    }

    /**
     * Creates a map where the keys are the string names of an enum (what a user
     * would enter as a style property value) and the enum.
     *
     * @param values A list of enums.
     * @param <T> The enum type.
     *
     * @return A map of string names to enums.
     */
    static public <T extends StylePropertyValueEnum> HashMap<String, T> toMap(T[] values)
    {
        HashMap<String, T> map = new HashMap<>();
        for (T value : values) {
            map.put(value.getName(), value);
        }
        return map;
    }

    // **********************************************************************
    // *
    // * Style Property Value Enums
    // *
    // **********************************************************************

    /**
     *  The Boolean types: true / false.
     */
    public enum BooleanType implements StylePropertyValueEnum
    {
        TRUE("true"), FALSE("false");

        static private final HashMap<String, BooleanType> map = toMap(BooleanType.values());
        private final String name;

        BooleanType(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        static public BooleanType toEnum(String name) { return map.get(name.toLowerCase()); }
    }

    /**
     * The LineStyle types: solid, dashed, and dotted.
     */
    public enum LineStyle implements StylePropertyValueEnum
    {
        SOLID("solid"), DASHED("dashed"), DOTTED("dotted");

        static private final HashMap<String, LineStyle> map = toMap(LineStyle.values());
        private final String name;

        LineStyle(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        static public LineStyle toEnum(String name) { return map.get(name.toLowerCase()); }
    }

    /**
     * The TextAnchor types.
     */
    public enum TextAnchor implements StylePropertyValueEnum
    {
        TL("TL"), TC("TC"), TR("TR"),
        ML("ML"), MC("MC"), MR("MR"),
        BL("BL"), BC("BC"), BR("BR");

        static private final HashMap<String, TextAnchor> map = toMap(TextAnchor.values());
        private final String name;

        TextAnchor(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        static public  TextAnchor toEnum(String name)  { return map.get(name.toUpperCase()); }
    }

    /**
     * The Arrow types: none, both, start, and end.
     */
    public enum Arrow implements StylePropertyValueEnum
    {
        NONE("none"), BOTH("both"), START("start"), END("end");

        static private final HashMap<String, Arrow> map = toMap(Arrow.values());
        private final String name;

        Arrow(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        static public Arrow toEnum(String name)  { return map.get(name.toLowerCase()); }
    }

    /**
     * The EventShape types: circle, square, diamond, and star
     */
    public enum EventShape implements StylePropertyValueEnum
    {
        CIRCLE("circle"), SQUARE("square"), DIAMOND("diamond"), STAR("star");

        static private final HashMap<String, EventShape> map = toMap(EventShape.values());
        private final String name;

        EventShape(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        static public EventShape toEnum(String name) { return map.get(name.toLowerCase()); }
    }

    // **********************************************************************
    // *
    // * Factory
    // *
    // **********************************************************************

    /**
     * This is a factory method for creating style properties.
     *
     * @param name The style property name.
     * @param value The style property's value's token.
     *
     * @return The created style property.
     * @throws StyleException If a syntax error occurs.
     */
    static public StyleProperty createStyleProperty(String name, Token<?> value) throws StyleException
    {
        StylePropertyDefinition definition = StylePropertyDefinition.toDefinition(name);

        // An error occurs if we can't find the property name

        if (definition == null) {
            throw new StyleException("Invalid style property '" + name + "'");
        }

        // The definition tells us what type of data to expect. We attempt to
        // convert the given value to the expected type. The various methods we
        // use return a null if the data type is incorrect. When the StyleProperty
        // constructor is called, it will throw a StyleException

        switch (definition.getType()) {
            case FLOAT -> {
                return new StyleProperty(name, toDouble(value), definition);
            }
            case STRING -> {
                return new StyleProperty(name, toStr(value), definition);
            }
            case COLOR -> {
                return new StyleProperty(name, toColor(value), definition);
            }
            case BOOLEAN -> {
                return new StyleProperty(name, toBoolean(value), definition);
            }
            case LINE_STYLE -> {
                return new StyleProperty(name, toLineStyle(value), definition);
            }
            case FONT_WEIGHT -> {
                return new StyleProperty(name, toFontWeight(value), definition);
            }
            case FONT_STYLE -> {
                return new StyleProperty(name, toFontStyle(value), definition);
            }
            case TEXT_ANCHOR -> {
                return new StyleProperty(name, toTextAnchor(value), definition);
            }
            case ARROW -> {
                return new StyleProperty(name, toArrow(value), definition);
            }
            case EVENT_SHAPE -> {
                return new StyleProperty(name, toEventShape(value), definition);
            }
        }

        throw new StyleException("Invalid style property '" + name + "'");
    }

    // **********************************************************************
    // *
    // * Convert Tokens to Stylesheet Property Values
    // *
    // **********************************************************************

    /**
     * Convert a token value to a LineStyle.
     *
     * @param value The token value.
     *
     * @return The corresponding LineStyle.
     */
    static public LineStyle toLineStyle(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.LineStyle.toEnum(value.getString());
        }
        return null;
    }

    /**
     * Convert a token value to a FontWeight.
     *
     * @param value The token value.
     *
     * @return The corresponding FontWeight.
     */
    static public FontWeight toFontWeight(Token<?> value)
    {
        if (value.isString()) {
            return FontWeight.findByName(value.getString());
        }
        if (value.isName()) {
            return FontWeight.findByName(value.getString());
        }
        else if (value.isNumber()) {
            return FontWeight.findByWeight(Util.toInt(value.getNumber()));
        }
        return null;
    }

    /**
     * Convert a token value to an Arrow type.
     *
     * @param value The token value.
     *
     * @return The corresponding Arrow type.
     */
    static public StyleProperties.Arrow toArrow(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.Arrow.toEnum(value.getString());
        }
        return null;
    }

    /**
     * Convert a token value to an FontStyle (FontPosture).
     *
     * @param value The token value.
     *
     * @return The corresponding FontStyle (FontPosture).
     */
    static public FontPosture toFontStyle(Token<?> value)
    {
        if (value.isName()) {
            return FontPosture.findByName(value.getString());
        }
        return null;
    }

    /**
     * Convert a token value to a Color.
     *
     * @param value The token value.
     *
     * @return The corresponding Color.
     */
    static public Color toColor(Token<?> value)
    {
        if (value.getType() == Token.Type.COLOR) {
            return value.getColor();
        }
        return null;
    }

    /**
     * Convert a token value to a Boolean.
     *
     * @param value The token value.
     *
     * @return The corresponding Boolean.
     */
    static public Boolean toBoolean(Token<?> value)
    {
        if (value.isName()) {
            StyleProperties.BooleanType booleanType = StyleProperties.BooleanType.toEnum(value.getString());
            if (booleanType == StyleProperties.BooleanType.TRUE) {
                return true;
            }
            if (booleanType == StyleProperties.BooleanType.FALSE) {
                return false;
            }
        }
        return null;
    }

    /**
     * Convert a token value to an EventShape.
     *
     * @param value The token value.
     *
     * @return The corresponding EventShape.
     */
    static public StyleProperties.EventShape toEventShape(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.EventShape.toEnum(value.getString());
        }
        return null;
    }

    /**
     * Convert a token value to a TextAnchor.
     *
     * @param value The token value.
     *
     * @return The corresponding TextAnchor.
     */
    static public StyleProperties.TextAnchor toTextAnchor(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.TextAnchor.toEnum(value.getString());
        }
        return null;
    }

    /**
     * Convert a token value to a String.
     *
     * @param value The token value.
     *
     * @return The corresponding String.
     */
    static public String toStr(Token<?> value)
    {
        // If a string is needed and a name is given, we'll use the name as the
        // string's value
        
        if (value.isString() || value.isName()) {
            return value.getString();
        }
        return null;
    }

    /**
     * Convert a token value to a Double.
     *
     * @param value The token value.
     *
     * @return The corresponding Double.
     */
    static public Double toDouble(Token<?> value)
    {
        if (value.isNumber()) {
            return value.getNumber();
        }
        return null;
    }
}
