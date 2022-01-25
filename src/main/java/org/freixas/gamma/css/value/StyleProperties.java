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
 *
 * @author Antonio Freixas
 */
public class StyleProperties
{
    // **********************************************************************
    // *
    // * Support for style property value enums.
    // *
    // **********************************************************************

    interface StylePropertyValueEnum
    {
        public String getName();
    }

    public static <T extends StylePropertyValueEnum> HashMap<String, T> toMap(T[] values)
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

    public enum BooleanType implements StylePropertyValueEnum
    {
        TRUE("true"), FALSE("false");

        private static final HashMap<String, BooleanType> map = toMap(BooleanType.values());
        private final String name;

        BooleanType(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        public static final BooleanType toEnum(String name) { return map.get(name.toLowerCase()); }
    }

    public enum LineStyle implements StylePropertyValueEnum
    {
        SOLID("solid"), DASHED("dashed"), DOTTED("dotted");

        private static final HashMap<String, LineStyle> map = toMap(LineStyle.values());
        private final String name;

        LineStyle(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        public static final LineStyle toEnum(String name) { return map.get(name.toLowerCase()); }
    }

    public enum TextAnchor implements StylePropertyValueEnum
    {
        TL("TL"), TC("TC"), TR("TR"),
        ML("ML"), MC("MC"), MR("MR"),
        BL("BL"), BC("BC"), BR("BR");

        private static final HashMap<String, TextAnchor> map = toMap(TextAnchor.values());
        private final String name;

        TextAnchor(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        public static final TextAnchor toEnum(String name)  { return map.get(name.toUpperCase()); }
    }

    public enum Arrow implements StylePropertyValueEnum
    {
        NONE("none"), BOTH("both"), START("start"), END("end");

        private static final HashMap<String, Arrow> map = toMap(Arrow.values());
        private final String name;

        Arrow(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        public static final Arrow toEnum(String name)  { return map.get(name.toLowerCase()); }
    }

    public enum EventShape implements StylePropertyValueEnum
    {
        CIRCLE("circle"), SQUARE("square"), DIAMOND("diamond"), STAR("star");

        private static final HashMap<String, EventShape> map = toMap(EventShape.values());
        private final String name;

        EventShape(String name) { this.name = name; }
        @Override
        public final String getName() { return name; }
        public static final EventShape toEnum(String name) { return map.get(name.toLowerCase()); }
    }

    // **********************************************************************
    // *
    // * Factory
    // *
    // **********************************************************************

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

    public static FontWeight toFontWeight(Token<?> value)
    {
        if (value.isName()) {
            return FontWeight.findByName(value.getString());
        }
        else if (value.isNumber()) {
            return FontWeight.findByWeight(Util.toInt(value.getNumber()));
        }
        return null;
    }

    public static StyleProperties.Arrow toArrow(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.Arrow.toEnum(value.getString());
        }
        return null;
    }

    public static FontPosture toFontStyle(Token<?> value)
    {
        if (value.isName()) {
            return FontPosture.findByName(value.getString());
        }
        return null;
    }

    public static Color toColor(Token<?> value)
    {
        if (value.getType() == Token.Type.COLOR) {
            return value.getColor();
        }
        return null;
    }

    public static Boolean toBoolean(Token<?> value)
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

    public static StyleProperties.EventShape toEventShape(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.EventShape.toEnum(value.getString());
        }
        return null;
    }

    public static StyleProperties.TextAnchor toTextAnchor(Token<?> value)
    {
        if (value.isName()) {
            return StyleProperties.TextAnchor.toEnum(value.getString());
        }
        return null;
    }

    public static String toStr(Token<?> value)
    {
        if (value.isString()) {
            return value.getString();
        }
        return null;
    }

    public static Double toDouble(Token<?> value)
    {
        if (value.isNumber()) {
            return value.getNumber();
        }
        return null;
    }
}
