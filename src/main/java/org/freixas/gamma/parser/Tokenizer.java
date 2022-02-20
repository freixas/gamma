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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;

/**
 * The common code used by all tokenizers.
 *
 * @author Antonio Freixas
 */
abstract public class Tokenizer
{
    /**
     * A final marker character for the end of the script.
     */
    static protected final char EOF = '\1';
    static private final String HEX_DIGITS = "01234567890ABCDEFabcdef";

    protected ArrayList<Token<?>> list = null;      // Collect all tokens here
    protected final File file;                      // The source file for the script
    protected final String code;                    // The script code

    protected int cPtr;             // The pos of next char to read
    protected int charPos;          // The pos of the cur char read
    protected char c;               // The cur char
    protected char cNext;           // The next char

    protected int lineNumber;       // The line number for a char
    protected int lineNumberStart;  // The char pos at start of line
    protected int charNumber;       // The char number rel to start of line

    protected final String operators;   // The list of allowed chars in operators
    protected final String delimiters;  // The list of allowed chars in delimiters

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a Tokenizer.
     *
     * @param file The file from which the code was read. This file is only
     *      * used to store in a TokenContext. It could potentially be null.
     * @param code The code to tokenize.
     * @param operators The list of permitted operator characters.
     * @param delimiters This list of permitted delimiter characters.
     */
    public Tokenizer(File file, String code, String operators, String delimiters)
    {
        this.file = file;

        // Replace any line ending with a newline
        // \R also catches Form Feed and some other odd line separators
        // Add a null to the end to make it easier to know when we're done.

        this.code = code.replaceAll("\\R", "\n") + EOF;

        this.operators = operators;
        this.delimiters = delimiters;
    }

    // **********************************************************************
    // *
    // * Tokenize
    // *
    // **********************************************************************

    /**
     * Tokenize the code. While this method can be called more than once,
     * it will operate on the same code string and thus return the same
     * tokens. Generally, one creates a new tokenizer for each new input.
     *
     * @return A list of Tokens.
     * @throws ParseException When an invalid token is found.
     */
    abstract public ArrayList<Token<?>> tokenize() throws ParseException;

    /**
     * Initialize the tokenizer. This resets the scanning position to the
     * start of the code and creates a new, empty list of tokens, which will
     * be filled as we go.
     */
    protected final void initialize()
    {
        lineNumber = 1;
        lineNumberStart = 0;
        charNumber = 0;
        charPos = 0;

        cPtr = 0;
        list = new ArrayList<>();
    }

    /**
     * Get the next character from the code string. On return,
     * <ul>
     *     <li>'c' will hold the next character (but never beyond the terminating
     *     EOF)
     *     <li>'cNext' will hold the one after that(but never beyond the terminating
     *     EOF)
     *     <li>'charPos' will hold the position of 'c' (but never beyond the
     *     terminating EOF)
     *     <li>'cPtr' will hold the position of 'cNext' (but never beyond the
     *     terminating EOF)
     *     <li>'lineNumnber' will hold the number of the current line (the first
     *     line is line 1)
     *     <li>'lineNumberStart' will hold the character position at which the
     *     line starts
     *     <li>'charNumber' will hold the character position relative to the
     *     start of the line (the first character is 1)
     * </ul>
     */
    protected final void next()
    {
        // Start a new line every time we find a newline. We don't need to
        // worry about any other type of line ending

        if (cPtr > 0 && c == '\n') {
            lineNumber++;
            lineNumberStart = cPtr;
            charNumber = 0;
        }

        // Get the next character

        c = code.charAt(cPtr);
        charNumber++;
        charPos = cPtr;

        // And the character after that, but make sure to never go beyond the
        // EOF

        cNext = c == EOF ? EOF : code.charAt(cPtr + 1);
        if (c != EOF) cPtr++;
    }

    /**
     *  Grab the current token context. This will be associated with a token.
     *
     * @return The current token context.
     */
    protected final TokenContext captureContext()
    {
        return new TokenContext(file, code, lineNumber, charNumber, charPos, charPos);
    }

    /**
     * Strip a "//" comment.
     * <p>
     * When called, the current character should be the character after a "//". On
     * return, the next character will be either the first character after a newline or an EOF.
     */
    protected final void stripComment()
    {
        while (c != EOF) {
            if (c == '\n') {
                next();
                return;
            }
            next();
        }
    }

    /**
     * Strip an alternate comment, ones starting with "/*".
     * <p>
     * When called, the current character should be the character after a "/*". On
     * return, the next character will be the first character just past the comment.
     */
    protected final void stripAlternateComment()
    {
        while ((c != '*' || cNext != '/') && c != EOF) {
            next();
        }
        if (c != EOF) {
             next();
             next();
        }
    }

    /**
     * Get a string token.
     * <p>
     * When called, the current character should be the string delimiter. On return, the next character
     * will be the first character after the delimiter.
     *
     * @return A string.
     */
    protected final String getString()
    {
        StringBuilder name = new StringBuilder();

        // Determine if the delimiter is a single-quote or a double-quote

        char delimiter = c;
        next();

        while (c != delimiter) {

            // Check for quoting

            switch (c) {
                case '\\' -> {
                    next();

                    switch (c) {

                        // Check for EOF. If found, just terminate the string

                        case EOF -> {
                            return name.toString();
                        }

                        // Convert \n to newline

                        case 'n' -> {
                            name.append('\n');
                            next();
                        }

                        // Quote everything else literally

                        default -> {
                            name.append(c);
                            next();
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
                    next();
                }
            }
        }

        next();
        return name.toString();
    }

     /**
     * Get a number token.
     * <p>
     * When called, the current character should be the first character of the number. On
     * return, the current character will be the first non-digit after the number.
     *
     * @return A number.
      *
     * @throws ParseException When the color number is improperly formed.
     */
    protected final double getNumber() throws ParseException
    {
        TokenContext context = captureContext();

        // Check for color numbers

        StringBuilder number = new StringBuilder();

        try {
            while (Character.isDigit(c) || c == '.') {
                number.append(c);
                next();
            }

            // The current character should now be the first non-number digit or period
            // Check to make sure we don't have two periods

            int p1 = number.indexOf(".");
            int p2 = number.lastIndexOf(".");
            if (p1 != p2) {
                context.setCharEnd(charPos);
                throw new ParseException(
                    new Token<>(Token.Type.STRING, number.toString(), context),
                    "Invalid number: '" + number + "'");
            }
            return Double.parseDouble(number.toString());
        }
        catch (NumberFormatException e) {
            context.setCharEnd(charPos);
            throw new ParseException(
                new Token<>(Token.Type.STRING, number.toString(), context),
                "Invalid number: '" + number + "'");
        }
    }

    /**
     * Determine if the given character is a hex digit.
     *
     * @param c The character to check.
     *
     * @return True if the character is a hex digit.
     */
    protected final boolean isHexDigit(char c)
    {
        return HEX_DIGITS.indexOf(c) != -1;
    }

    // **********************************************************************
    // *
    // * Tokenize
    // *
    // **********************************************************************

    /**
     * For debugging. The method is given a list of tokens and writes them out
     * to a log file in CSV format. The log file's name is a timestamp.
     *
     * @param pathName The directory in which a log file should be written.
     * @param tokens The token list of log.
     */
    protected final void debugTokens(String pathName, ArrayList<Token<?>> tokens)
    {
        try {
            File logDir  = new File(pathName);
            //noinspection ResultOfMethodCallIgnored
            logDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            File log = new File(logDir, timestamp +" Log.csv");
            //noinspection ResultOfMethodCallIgnored
            log.createNewFile();
            FileOutputStream out = new FileOutputStream(log);
            try (PrintWriter writer = new PrintWriter(out)) {
                ListIterator<Token<?>> iter = tokens.listIterator();
                writer.write("Line Number,Char Number,Type,Value\n");

                while (iter.hasNext()) {
                    Token<?> token = iter.next();

                    String value = valueToString(token.getValue());

                    writer.write(
                        token.getContext().getLineNumber() + "," +
                        token.getContext().getCharNumber() + "," +
                        token.getType() + "," +
                        value + "\n");
                }
            }
        }
        catch (IOException ignored) {   }
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
