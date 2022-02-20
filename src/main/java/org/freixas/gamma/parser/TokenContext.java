/*
 * Copyright (c) 2022 Antonio Freixas
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

public final class TokenContext {
    private final File file;
    private final String code;
    private final int lineNumber;
    private final int charNumber;
    private final int charStart;
    private int charEnd;

    /**
     * Capture information about a token for use in reporting errors.
     *
     * @param file The file from which the token came. May be null.
     * @param code The entire code string containing the token.
     * @param lineNumber The line number of the line on which the token starts.
     * The first line is line 1.
     * @param charNumber The character number of the first character of the token
     * relative to the line. The first character is character 1.
     * @param charStart The character position of the first character of the
     * token relative to the entire code string. The first possible code position
     * is 0.
     * @param charEnd The character position of the last character of the token
     * relative to the entire code string. For a one-character token, this will
     * be the same as charStart.
     */
    public TokenContext(File file, String code, int lineNumber, int charNumber, int charStart, int charEnd)
    {
        this.file = file;
        this.code = code;
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;
        this.charStart = charStart;
        this.charEnd = charEnd;
    }

    // **********************************************************************
    // *
    // * Getters / Setters
    // *
    // **********************************************************************

    /**
     * Get the file from which the token came. May be null.
     *
     * @return The file from which the token came. May be null.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the entire code string containing the token.
     *
     * @return The entire code string containing the token.
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the line number of the line on which the token starts. The first line
     * is line 1
     *
     * @return The line number of the line on which the token starts. * The
     * first line is line 1
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Get the character number of the first character of the token relative to
     * the line. The first character is character 1.
     *
     * @return The character number of the first character of the token relative
     * to the line. The first character is character 1.
     */
    public int getCharNumber() {
        return charNumber;
    }

    /**
     * Get the character position of the first character of the token relative
     * to the entire code string. The first possible code position is 0.
     *
     * @return The character position of the first character of the token
     * relative to the entire code string. The first possible code position is
     * 0.
     */
    public int getCharStart() {
        return charStart;
    }

    /**
     * Get the character position of the last character of the token relative to
     * the entire code string. For a one-character token, this will be the same
     * as charStart.
     *
     * @return The character position of the last character of the token
     * relative to the entire code string. For a one-character token, this will
     * be the same as charStart.
     */
    public int getCharEnd() {
        return charEnd;
    }

    /**
     * Set the character position of the last character of the token relative to
     * the entire code string. For a one-character token, this will be the same
     * as charStart.
     *
     * @param charEnd The character position of the last character of the token
     * relative to the entire code string. For a one-character token, this will
     * be the same as charStart.
     */
    public void setCharEnd(int charEnd)
    {
        this.charEnd = charEnd;
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString() {
        return " {" +
                "file=" + file.getName() +
                ", lineNumber=" + lineNumber +
                ", charNumber=" + charNumber +
                ", charStart=" + charStart +
                ", charEnd=" + charEnd +
                '}';
    }
}
