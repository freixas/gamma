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

/**
 * A style property. A style property looks much like script properties: a name,
 * followed by a colon and a value. Each Rule in a stylesheet contains a set
 * of style properties.
 *
 * @author Antonio Freixas
 */
public final class StyleProperty
{
    /**
     * The types of style property values.
     */
    public enum Type
    {
        FLOAT, STRING, COLOR, BOOLEAN,
        LINE_STYLE,
        FONT_WEIGHT, FONT_STYLE, TEXT_ANCHOR,
        ARROW, EVENT_SHAPE,
        FONT
    }

    private final Type type;                            // The type of this property
    private final Object value;                         // The value of this property
    private final StylePropertyDefinition definition;   // The associated definition
    private final Field field;                          // The matching field in the StyleStruct

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    public StyleProperty(String name, Object value, StylePropertyDefinition definition) throws StyleException
    {
        this.type = definition.getType();
        this.value = value;
        this.definition = definition;
        this.field = definition.getField();

        // If the value is null, the caller wasn't given the right type of value

        if (value == null) {
            throw new StyleException("Invalid value for style property '" + name + "'");
        }
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the style property's value's type.
     *
     * @return The style property's value's type.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Get the style property's value.
     *
     * @return The style property's value.
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Get the style property definition associated with this style property.
     *
     * @return The style property definition associated with this style property.
     */
    public StylePropertyDefinition getDefinition()
    {
        return definition;
    }

    // **********************************************************************
    // *
    // * Features
    // *
    // **********************************************************************

    /**
     * Set one or more values in a StyleStruct. We set the value in the
     * associated StyleStruct field and then in any dependent fields.
     *
     * @param styles The StylesStruct to set.
     */
    public void setStyleStructValues(StyleStruct styles)
    {
        try {
            // If we have a matching field in the StyleStruct, set it

            if (field != null) {
                if (field.getType().isAssignableFrom(value.getClass())) {
                    field.set(styles, value);
                }
                else if (field.getType().getName().equals("double") && value.getClass() == Double.class) {
                    field.set(styles, value);
                }
                else if (field.getType().getName().equals("boolean") && value.getClass() == Boolean.class) {
                    field.set(styles, value);
                }
                else {
                    throw new ProgrammingException("StyleProperty.setStyleStructValue: Can't assign value to field");
                }
            }

            // If we have dependent style properties, set their values as well

            StylePropertyDefinition[] dependencies = definition.getDependencies();
            if (dependencies != null && dependencies.length > 0) {
                for (StylePropertyDefinition dependency : dependencies) {
                    dependency.setStyleStructValue(styles, value);
                }
            }

            // If this property is a font component property (family, weight, style, size),
            // set the matching font to null

            StylePropertyDefinition fontDependency;
            if ((fontDependency = definition.getFontDependency()) != null) {
                fontDependency.setStyleStructValue(styles, null);
            }
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ProgrammingException("StyleProperty.setStyleStructValue", e);
        }
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        return definition.getPropName() + ":" + value;
    }

}
