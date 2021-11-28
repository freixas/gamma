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
package gamma.parser;

import java.io.File;

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
        NUMBER, STRING, OPERATOR, NAME, DELIMITER, EOF
    }

    private final Type type;
    private final T value;
    private final File file;
    private final int lineNumber;
    private final int charNumber;

    /**
     * Create a Token.
     *
     * @param type The token type.
     * @param value The token's value.
     * @param file The file in which the token was found.
     * @param lineNumber The line number of the line on which the token was
     * found. The first line is number 1.
     * @param charNumber The character number (relative to the start of a line)
     * of the character which begins the token. The first character is number 1.
     */
    public Token(Type type, T value, File file, int lineNumber, int charNumber)
    {
        this.type = type;
        this.value = value;
        this.file = file;
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;
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
     * Get the file from which the script came.
     *
     * @return The file from which the script came.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Get the number of the line on which the token was found. The first line
     * is number 1.
     *
     * @return The number of the line on which the token was found.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Get the character number (relative to the start of a line) of the
     * character which begins the token. The first character is number 1.
     *
     * @return The character number of the character which begins the token
     */
    public int getCharNumber()
    {
        return charNumber;
    }

    public boolean isNumber() { return type == Token.Type.NUMBER; }
    public boolean isString() { return type == Token.Type.STRING; }
    public boolean isOperator() { return type == Token.Type.OPERATOR; }
    public boolean isName() { return type == Token.Type.NAME; }
    public boolean isDelimiter() { return type == Token.Type.DELIMITER; }
    public boolean isEOF() { return type == Token.Type.EOF; }

    public char getChar() { return (Character)getValue(); }
    public double getNumber() { return (Double)getValue(); }
    public String getString() { return (String)getValue(); }

    @Override
    public String toString()
    {
        return "Token{" + "type=" + type + ", value=" + value + ", file=" + file.getName() + ", lineNumber=" + lineNumber + ", charNumber=" + charNumber + '}';
    }


}
