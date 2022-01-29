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

import java.io.File;
import javafx.scene.paint.Color;

/**
 * When a script is parsed, it is first tokenized.
 * This class stores information on each token.
 *
 * @param <T>
 * @author Antonio Freixas
 */
public class Token<T>
{
    public enum Type {
        NUMBER, COLOR, STRING, OPERATOR, SELECTOR, NAME, DELIMITER, EOF
    }

    private final Type type;
    private final T value;
    private TokenContext context;

    /**
     * Create a Token.
     *
     * @param type The token type.
     * @param value The token's value.
     * @param context The context for the token. This identifies the position of the token in the code.
     */
    public Token(Type type, T value, TokenContext context)
    {
        this.type = type;
        this.value = value;
        this.context = context;
    }

    public boolean isInstruction() { return false; }

    /**
     * Get the token's type.
     *
     * @return The token's type.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Get the token's value.
     *
     * @return The token's value.
     */
    public T getValue()
    {
        return value;
    }

    /**
     *  Get the token context, which provides the context in which the token was located.
     *
     * @return The token context.
     */
    public TokenContext getContext() {
        return context;
    }

    public boolean isNumber() { return type == Token.Type.NUMBER; }
    public boolean isColor() { return type == Token.Type.COLOR; }
    public boolean isString() { return type == Token.Type.STRING; }
    public boolean isOperator() { return type == Token.Type.OPERATOR; }
    public boolean isSelector() { return type == Token.Type.SELECTOR; }
    public boolean isName() { return type == Token.Type.NAME; }
    public boolean isDelimiter() { return type == Token.Type.DELIMITER; }
    public boolean isEOF() { return type == Token.Type.EOF; }

    public char getChar() { return (Character)getValue(); }
    public double getNumber() { return (Double)getValue(); }
    public Color getColor() { return (Color)getValue(); }
    public String getString() { return (String)getValue(); }

    @Override
    public String toString()
    {
        return "Token{" + "type=" + type + ", value=" + value + ", context=" + context + '}';
    }


}
