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
package org.freixas.gamma.parser;

import javafx.scene.paint.Color;

/**
 * When a script is parsed, it is first tokenized.
 * This class stores information about each token.
 *
 * @param <T>
 * @author Antonio Freixas
 */
public class Token<T>
{
    /**
     * The types of tokens.
     */
    public enum Type {
        NUMBER, COLOR, STRING, OPERATOR, SELECTOR, NAME, DELIMITER, EOF
    }

    private final Type type;
    private final T value;
    private final TokenContext context;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a Token.
     *
     * @param type The token type.
     * @param value The token's value.
     * @param context The context for the token. This identifies the position of
     * the token in the code.
     */
    public Token(Type type, T value, TokenContext context)
    {
        this.type = type;
        this.value = value;
        this.context = context;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the token's type. Alternatively, you can call any of the
     * various if...() methods to see if the token is of a specific type.
     *
     * @return The token's type.
     */
    public final Type getType()
    {
        return type;
    }

    /**
     * Get the token's value.
     *
     * @return The token's value.
     */
    public final  T getValue()
    {
        return value;
    }

    /**
     *  Get the token context, which provides the context in which the token was located.
     *
     * @return The token context.
     */
    public final TokenContext getContext() {
        return context;
    }

    /**
     * Return the value cast to a char. The Token should be checked to
     * see if it is a delimiter before calling this method.
     *
     * @return The value cast to a char.
     */
    public final char getChar() { return (char)getValue(); }

    /**
     * Return the value cast to a double. The Token should be checked to
     * see if it is a number before calling this method.
     *
     * @return The value cast to a double.
     */
    public final double getNumber() { return (Double)getValue(); }

    /**
     * Return the value cast to a Color. The Token should be checked to
     * see if it is a Color before calling this method.
     *
     * @return The value cast to a Color.
     */
    public final Color getColor() { return (Color)getValue(); }

    /**
     * Return the value cast to a String. The Token should be checked to
     * see if it is a string, selector, name, or delimiter before calling this
     * method.
     *
     * @return The value cast to a String.
     */
    public final String getString() { return (String)getValue(); }

    // **********************************************************************
    // *
    // * Informational
    // *
    // **********************************************************************

    public final boolean isNumber() { return type == Token.Type.NUMBER; }
    public final boolean isColor() { return type == Token.Type.COLOR; }
    public final boolean isString() { return type == Token.Type.STRING; }
    public final boolean isOperator() { return type == Token.Type.OPERATOR; }
    public final boolean isSelector() { return type == Token.Type.SELECTOR; }
    public final boolean isName() { return type == Token.Type.NAME; }
    public final boolean isDelimiter() { return type == Token.Type.DELIMITER; }
    public final boolean isEOF() { return type == Token.Type.EOF; }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        return "Token{" + "type=" + type + ", value=" + value + ", context=" + context + '}';
    }


}
