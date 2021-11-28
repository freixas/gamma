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
import java.util.ArrayList;

/**
 *
 * @author Antonio Freixas
 */
public class Tokenizer
{
    static private final char EOF = '\1';
    private ArrayList<Token> list = null;
    private final File file;
    private final String script;

    private int cPtr;
    char c;
    char cNext;

    private int lineNumber;
    private int lineNumberStart;
    private int charNumber;

    private final String operators = "+-*/^.<>";
    private final String delimiters = ";,:=[]()";
    private final String hexDigits = "01234567890ABCDEFabcdef";

    public Tokenizer(File file, String script)
    {
        this.file = file;

        // Replace any line ending with a newline
        // \R also catches Form Feed and some other odd line separators
        // Add a null to the end to make it easier to know when we're done.

        this.script = script.replaceAll("\\R", "\n") + EOF;
    }

    /**
     * Tokenize the script.
     *
     * @return A list of Tokens.
     * @throws ParseException When an invalid token is found.
     */
    public ArrayList<Token> tokenize() throws ParseException
    {
        lineNumber = 1;
        lineNumberStart = 0;
        charNumber = 1;

        cPtr = 0;
        list = new ArrayList<>();

        while ((c = script.charAt(cPtr)) != EOF) {

            cNext = script.charAt(cPtr + 1);
            charNumber = cPtr - lineNumberStart + 1;

            // Strip comments

            if (c == '/' && cNext == '/') {
                cPtr += 2;
                stripComment();
                if (c == EOF) break;
                cNext = script.charAt(cPtr + 1);
                charNumber = cPtr - lineNumberStart + 1;
            }

            // Count newlines and discard them as whitespace

            if (c == '\n') {
                lineNumber++;
                cPtr++;
                lineNumberStart = cPtr;
            }

            // Handle delimiters

            else if (delimiters.indexOf(c) != -1) {
                list.add(new Token<>(Token.Type.DELIMITER, c, file, lineNumber, charNumber));
                cPtr++;
            }

            // Handle strings

            else if (c == '\'' || c == '"') {
                list.add(new Token<>(Token.Type.STRING, getString(), file, lineNumber, charNumber));
            }

            // Handle names

            else if (c == '_' || Character.isLetter(c)) {
                list.add(new Token<>(Token.Type.NAME, getName(), file, lineNumber, charNumber));
            }

            // Handle numbers

            else if ((c == '#' && isHexDigit(cNext)) ||
                     (c == '.' && Character.isDigit(cNext))  ||
                     Character.isDigit(c)) {
                list.add(new Token<>(Token.Type.NUMBER, getNumber(), file, lineNumber, charNumber));
            }

            // Handle operators

            else if (operators.indexOf(c) != -1) {
                list.add(new Token<>(Token.Type.OPERATOR, c, file, lineNumber, charNumber));
                cPtr++;
            }

            // Skip whitespace

            else if (Character.isWhitespace(c)) {
                cPtr++;
            }

            // Invalid character

            else {
                throw new ParseException(file, lineNumber, cPtr - lineNumberStart + 1, "Invalid character : '" + c + "'");
            }
        }

        list.add(new Token<>(Token.Type.EOF, EOF, file, lineNumber, cPtr - lineNumberStart + 1));

        return list;
    }

    /**
     * Strip a comment.
     * <p>
     * When called, cPtr should point to the first character after a "//". On
     * return, cPtr points to a newline or past the end of the script.
     */
    private void stripComment()
    {
        while ((c = script.charAt(cPtr)) != EOF) {
            c = script.charAt(cPtr);
            if (c == '\n') return;
            cPtr++;
        }

    }

    /**
     * Get a string token.
     * <p>
     * When called, cPtr should point to the string delimiter. On return, cPtr
     * will point to the first character past the delimiter.
     *
     * @return A string.
     */
    private String getString()
    {
        StringBuilder name = new StringBuilder();

        // Determine if the delimiter is a single-quote or a double-quote

        char delimiter = script.charAt(cPtr);
        cPtr++;

        while ((c = script.charAt(cPtr)) != delimiter) {

            // Check for quoting

            switch (c) {
                case '\\' -> {
                    cPtr++;
                    c = script.charAt(cPtr);

                    switch (c) {

                        // Check for EOF. If found, just terminate the string

                        case EOF -> {
                            return name.toString();
                        }

                        // Convert \n to newline

                        case 'n' -> {
                            name.append('\n');
                            cPtr++;
                        }

                        // Quote everything else literally

                        default -> {
                            name.append(c);
                            cPtr++;
                        }
                    }
                }

                // Check for EOF. If found, just terminate the string

                case EOF -> {
                    return name.toString();
                }

                // Everything else is entered as is

                default -> {
                    name.append(c);
                    cPtr++;
                }
            }
        }

        cPtr++;
        return name.toString();
    }

    /**
     * Get a name token.
     * <p>
     * When called, cPtr should point to the first character of the name, which
     * will be a letter of an underscore. On return, cPtr will point to the
     * first character after the name.
     *
     * @return A string.
     */
    private String getName()
    {
        StringBuilder name = new StringBuilder();

        while (Character.isLetterOrDigit(c = script.charAt(cPtr)) || c == '_') {
            name.append(c);
            cPtr++;
        }
        return name.toString();
    }

    /**
     * Get a number token.
     * <p>
     * When called, cPtr should point to the first character of the number. A
     * hex color is considered a number, so the first character might be "#". On
     * return, cPtr points to the first non-hex digit.
     *
     * @return A number.
     * @throws ParseException When the color number is improperly formed.
     */
    private double getNumber()
            throws ParseException
    {
        int startCPtr = cPtr;

        // Check for color numbers

        StringBuilder number = new StringBuilder();

        try {
            if (c == '#') {
                cPtr++;
                while (isHexDigit(c = script.charAt(cPtr))) {
                    number.append(c);
                    cPtr++;
                }

                // cPtr should now be pointing to the first non-hex digit.
                // If we have three digits, convert to 6 by doubling each digit

                if (number.length() == 3) {
                    for (int i = 0; i < 3; i++) {
                        number.insert(i * 2, number.charAt(i * 2));
                    }
                }

                // If we have four digits, convert to 8 by doubling each digit

                if (number.length() == 4) {
                    for (int i = 0; i < 4; i++) {
                        number.insert(i * 2, number.charAt(i * 2));
                    }
                }

                // If we have six digits, add "FF"

                if (number.length() == 6) {
                    number.append("FF");
                }

                // If we have eight digits, convert to a double

                if (number.length() == 8) {
                    number.insert(0, "0x");
                    return Long.decode(number.toString());
                }

                throw new ParseException(
                    file, lineNumber, charNumber, "Invalid color value: '#'" + number.toString() + "'");
            }

            // Check for double

            while (Character.isDigit(c = script.charAt(cPtr)) || c == '.') {
                number.append(c);
                cPtr++;
            }

            // cPtr should now be pointing to the first non-number digit or period
            // Check to make sure we don't have two periods

            int p1 = number.indexOf(".");
            int p2 = number.lastIndexOf(".");
            if (p1 != p2) {
                throw new ParseException(
                    file, lineNumber, charNumber, "Invalid float value: '" + number.toString() + "'");
            }
            return Double.valueOf(number.toString());
        }
        catch (NumberFormatException e) {
            throw new ParseException(
                file, lineNumber, charNumber, "Invalid number: '" + number.toString() + "'");
        }

    }

    private boolean isHexDigit(char c)
    {
        return hexDigits.indexOf(c) != -1;
    }
}
