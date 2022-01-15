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
package gamma.css.parser;

import gamma.parser.ParseException;
import gamma.parser.Token;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import javafx.scene.paint.Color;

/**
 *
 * @author Antonio Freixas
 */
public class CSSTokenizer
{
    static private final char EOF = '\1';
    private ArrayList<Token<?>> list = null;
    private final File file;
    private final String css;

    private int cPtr;
    char c;
    char cNext;

    private int lineNumber;
    private int lineNumberStart;
    private int charNumber;

    private final String delimiters = "+,:;(){}";
    private final String hexDigits = "01234567890ABCDEFabcdef";

    public CSSTokenizer(File file, String css)
    {
        this.file = file;

        // Replace any line ending with a newline
        // \R also catches Form Feed and some other odd line separators
        // Add a null to the end to make it easier to know when we're done.

        this.css = css.replaceAll("\\R", "\n") + EOF;
    }

    /**
     * Tokenize the css.
     *
     * @return A list of Tokens.
     * @throws ParseException When an invalid token is found.
     */
    public ArrayList<Token<?>> tokenize() throws ParseException
    {
        lineNumber = 1;
        lineNumberStart = 0;
        charNumber = 1;

        int insideBraces = 0;

        cPtr = 0;
        list = new ArrayList<>();

        while ((c = css.charAt(cPtr)) != EOF) {

            cNext = css.charAt(cPtr + 1);
            charNumber = cPtr - lineNumberStart + 1;

            // Strip comments

            if (c == '/' && cNext == '*') {
                cPtr += 2;
                stripComment();
                if (c == EOF) break;
                c = css.charAt(cPtr);
                cNext = css.charAt(cPtr + 1);
                charNumber = cPtr - lineNumberStart + 1;
            }

            // Count newlines and discard them as whitespace

            if (c == '\n') {
                lineNumber++;
                cPtr++;
                lineNumberStart = cPtr;
                continue;
            }

            // Handle delimiters

            if (c == '-' && !Character.isLetter(cNext) || delimiters.indexOf(c) != -1) {
                if (c == '{') insideBraces++;
                else if (c == '}') insideBraces--;
                if (insideBraces < 0) {
                    throw new ParseException(
                        file, lineNumber, charNumber, "Unbalanced braces");
                }
                list.add(new Token<>(Token.Type.DELIMITER, c, file, lineNumber, charNumber));
                cPtr++;
            }

            // Handle strings

            else if (c == '\'' || c == '"') {
                list.add(new Token<>(Token.Type.STRING, getString(), file, lineNumber, charNumber));
            }

            // Handle selectors

            else if (insideBraces == 0 && (c == '#' || c == '.' || Character.isLetter(c))) {
                list.add(new Token<>(Token.Type.SELECTOR, getSelector(), file, lineNumber, charNumber));
            }

            // Handle names and possibly colors

            else if (c == '_' || c == '-' || Character.isLetter(c)) {
                String name = getName();
                Color color = getColorByName(name);
                if (color == null) {
                    list.add(new Token<>(Token.Type.NAME, name, file, lineNumber, charNumber));
                }
                else {
                    list.add(new Token<>(Token.Type.COLOR, color, file, lineNumber, charNumber));
                }
            }

            // Handle colors given with '#' notation

            else if ((c == '#' && isHexDigit(cNext))) {
                list.add(new Token<>(Token.Type.COLOR, getColor(), file, lineNumber, charNumber));
            }

            // Handle numbers

            else if ((c == '.' && Character.isDigit(cNext)) ||
                     Character.isDigit(c)) {
                list.add(new Token<>(Token.Type.NUMBER, getNumber(), file, lineNumber, charNumber));
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

        debugTokens(list);
        return list;
    }

    /**
     * Strip a comment.
     * <p>
     * When called, cPtr should point to the first character after a "/*". On
     * return, cPtr points to the first character just past the comment.
     */
    private void stripComment()
    {
        while (((c = css.charAt(cPtr)) != '*' || (cNext = css.charAt(cPtr + 1)) != '/') && c != EOF) {
            cPtr++;
        }
        if (c != EOF) cPtr += 2;
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

        char delimiter = css.charAt(cPtr);
        cPtr++;

        while ((c = css.charAt(cPtr)) != delimiter) {

            // Check for quoting

            switch (c) {
                case '\\' -> {
                    cPtr++;
                    c = css.charAt(cPtr);

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
     * Get a selector token. '
     * <p>
     * When called, cPtr should point to the first character of the selector.
     * On return, cPtr will point to the first character after the
     * selector.
     *
     * @return A string.
     */
    private String getSelector()
    {
        StringBuilder selector = new StringBuilder();

        while (Character.isLetterOrDigit(c = css.charAt(cPtr)) ||
               c == '_' || c == '-' || c == '#' || c == '.') {
            selector.append(c);
            cPtr++;
        }

        return selector.toString();
    }

    /**
     * Get a name token.
     * <p>
     * When called, cPtr should point to the first character of the selector,
     * which will be a letter of an underscore or a hyphen. On return, cPtr will
     * point to the first character after the selector.
     *
     * @return A string.
     */
    private String getName()
    {
        StringBuilder name = new StringBuilder();

        while (Character.isLetterOrDigit(c = css.charAt(cPtr)) || c == '_' || c == '-') {
            name.append(c);
            cPtr++;
        }
        return name.toString();
    }

    /**
     * Get a color token.
     * <p>
     * When called, cPtr should point to the first character of the color, a '#'.
     * On return, cPtr points to the first non-hex digit.
     *
     * @return A number.
     * @throws ParseException When the color number is improperly formed.
     */
    private Color getColor() throws ParseException
    {
        // Check for color numbers

        StringBuilder number = new StringBuilder();

        try {
            cPtr++;
            while (isHexDigit(c = css.charAt(cPtr))) {
                number.append(c);
                cPtr++;
            }

            // cPtr should now be pointing to the first non-hex digit

            return Color.web(number.toString());
        }
        catch (IllegalArgumentException e) {
            throw new ParseException(
                file, lineNumber, charNumber, "Invalid color value: '" + number.toString() + "'");
        }
    }

    /**
     * Get a color token by name.
     * <p>
     * When called, cPtr should point to the first character after the name
     * passed in. On return, cPtr points to the first character after the color.
     *
     * @return A number.
     * @throws ParseException When the color number is improperly formed.
     */
    private Color getColorByName(String name) throws ParseException
    {
        int startLineNumber = lineNumber;
        int startCharNumber = charNumber;

        // If the name is rgb, hsl, or hsla, we should have a color function

        if (name.equals("rgb") || name.equals("hsl") || name.equals("hsla")) {

            StringBuilder builder = new StringBuilder(name);
            cPtr++;

            while ((c = css.charAt(cPtr)) != ')' && c != EOF) {
                builder.append(c);
                cPtr++;
            }

            // We didn't find a ')'

            if (c == EOF) {
                throw new ParseException(
                    file, startLineNumber, startCharNumber, "Expected a color function");
            }

            // We found a ')'. Let's see if we have a valid color function

            else {
                try {
                    return Color.web(name);
                }
                catch (IllegalArgumentException e) {
                    throw new ParseException(
                        file, startLineNumber, startCharNumber, "Expected a color function");
                }
            }
        }

        // In this case, we either have a color name or we don't. There are a
        // lot of color names, but I don't believe any of them clash with other
        // possible values, so if we have a color name, it really is supposed to
        // be a color value
        
        else {
            try {
                return Color.web(name);
            }
            catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    /**
     * Get a number token.
     * <p>
     * When called, cPtr should point to the first character of the number. On
     * return, cPtr points to the first non-digit.
     *
     * @return A number.
     * @throws ParseException When the color number is improperly formed.
     */
    private double getNumber() throws ParseException
    {
        // Check for color numbers

        StringBuilder number = new StringBuilder();

        try {
            while (Character.isDigit(c = css.charAt(cPtr)) || c == '.') {
                number.append(c);
                cPtr++;
            }

            // cPtr should now be pointing to the first non-number digit or period
            // Check to make sure we don't have two periods

            int p1 = number.indexOf(".");
            int p2 = number.lastIndexOf(".");
            if (p1 != p2) {
                throw new ParseException(
                    file, lineNumber, charNumber, "Invalid number: '" + number.toString() + "'");
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

    private void debugTokens(ArrayList<Token<?>> tokens)
    {
        try {
            File logDir  = new File("D:/users/tony/Documents/Gamma/Logs");
            logDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            File log = new File(logDir, timestamp +" Log.csv");
            log.createNewFile();
            FileOutputStream out = new FileOutputStream(log);
            try (PrintWriter writer = new PrintWriter(out)) {
                ListIterator<Token<?>> iter = tokens.listIterator();
                writer.write("Line Number,Char Number,Type,Value\n");

                while (iter.hasNext()) {
                    Token<?> token = iter.next();

                    String value = valueToString(token.getValue());

                    writer.write(
                            token.getLineNumber() + "," +
                        token.getCharNumber() + "," +
                        token.getType() + "," +
                        value + "\n");
                }
            }
        }
        catch (IOException e) {

        }
    }

    private String valueToString(Object value)
    {
        if (value instanceof Character character) {
            return quoteForCsv(character.toString());
        }
        else if (value instanceof Double double1) {
            return double1.toString();
        }
        else if (value instanceof String string) {
            return(quoteForCsv(string));
        }
        else if (value instanceof Integer integer1) {
            return integer1.toString();
        }
        else {
            return "";
        }
    }

    private String quoteForCsv(String str)
    {
        if (str.matches(".*[,\r\n\"].*")) {
            str = str.replaceAll("\"", "\"\"");
            str = "\"" + str + "\"";
        }
        return str;
    }


}
