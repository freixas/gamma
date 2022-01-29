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
package org.freixas.gamma.css.parser;

import org.freixas.gamma.parser.TokenContext;
import javafx.scene.paint.Color;
import org.freixas.gamma.parser.ParseException;
import org.freixas.gamma.parser.Token;
import org.freixas.gamma.parser.Tokenizer;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Antonio Freixas
 */
public class CSSTokenizer extends Tokenizer
{
    public CSSTokenizer(File file, String css)
    {
        super(file, css, "", "+,:;(){}");
    }

    /**
     * Tokenize the css.
     *
     * @return A list of Tokens.
     * @throws ParseException When an invalid token is found.
     */
    @Override
    public ArrayList<Token<?>> tokenize() throws ParseException
    {
        TokenContext context;
        Token token;

        initialize();
        next();

        int insideBraces = 0;

        while (c != EOF) {

            // Strip comments

            while (c == '/' && cNext == '*') {
                next(); next();
                stripAlternateComment();
                if (c == EOF) break;
            }

            // Count newlines and discard them as whitespace

            if (c == '\n') {
                next();
                continue;
            }

            // Handle delimiters

            if (c == '-' && !Character.isLetter(cNext) || delimiters.indexOf(c) != -1) {
                if (c == '{') insideBraces++;
                else if (c == '}') insideBraces--;
                if (insideBraces < 0) {
                    context = captureContext();
                    throw new ParseException(new Token<>(Token.Type.DELIMITER, c, context), "Unbalanced braces");
                }
                context = captureContext();
                list.add(new Token<>(Token.Type.DELIMITER, c, context));
                next();
            }

            // Handle strings

            else if (c == '\'' || c == '"') {
                context = captureContext();
                list.add(new Token<>(Token.Type.STRING, getString(), context));
                context.setCharEnd(cPtr - 2);
            }

            // Handle selectors

            else if (insideBraces == 0 && (c == '#' || c == '.' || Character.isLetter(c))) {
                context = captureContext();
                list.add(new Token<>(Token.Type.SELECTOR, getSelector(), context));
                context.setCharEnd(cPtr - 2);
            }

            // Handle names and possibly colors

            else if (c == '_' || c == '-' || Character.isLetter(c)) {
                String name = getName();
                Color color = getColorByName(name);
                context = captureContext();
                if (color == null) {
                    list.add(new Token<>(Token.Type.NAME, name, context));
                }
                else {
                    list.add(new Token<>(Token.Type.COLOR, color, context));
                }
                context.setCharEnd(cPtr - 2);
            }

            // Handle colors given with '#' notation

            else if ((c == '#' && isHexDigit(cNext))) {
                context = captureContext();
                list.add(new Token<>(Token.Type.COLOR, getColor(), context));
                context.setCharEnd(cPtr - 2);
            }

            // Handle numbers

            else if ((c == '.' && Character.isDigit(cNext)) ||
                     Character.isDigit(c)) {
                context = captureContext();
                list.add(new Token<>(Token.Type.NUMBER, getNumber(), context));
                context.setCharEnd(cPtr - 2);
            }

            // Skip whitespace

            else if (Character.isWhitespace(c)) {
                next();
            }

            // Invalid character

            else {
                context = captureContext();
                throw new ParseException(new Token<>(Token.Type.STRING, Character.toString(c), context), "Invalid character : '" + c + "'");
            }
        }

        context = captureContext();
        list.add(new Token<>(Token.Type.EOF, EOF, context));

        debugTokens(list);
        return list;
    }

    /**
     * Get a selector token. '
     * <p>
     * When called, the current character should be the first character of the selector.
     * On return, current character will be the first character after the selector.
     *
     * @return A string.
     */
    private String getSelector()
    {
        StringBuilder selector = new StringBuilder();

        while (Character.isLetterOrDigit(c) ||
               c == '_' || c == '-' || c == '#' || c == '.') {
            selector.append(c);
            next();
        }

        return selector.toString();
    }

    /**
     * Get a name token.
     * <p>
     * When called, the current character should be the first character of the name, which
     * will be a letter of an underscore. On return, the current character will be the
     * first character after the name.
     *
     * @return A string.
     */
    private String getName()
    {
        StringBuilder name = new StringBuilder();

        while (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
            name.append(c);
            next();
        }
        return name.toString();
    }

    /**
     * Get a color token.
     * <p>
     * When called, the current character will be the first character of the color, a '#'.
     * On return, the current character will be the first non-hex digit following the color.
     *
     * @return A number.
     *
     * @throws ParseException When the color number is improperly formed.
     */
    private Color getColor() throws ParseException
    {
        TokenContext context = captureContext();

        // Check for color numbers

        StringBuilder number = new StringBuilder();

        try {
            next();
            while (isHexDigit(c)) {
                number.append(c);
                next();
            }

            return Color.web(number.toString());
        }
        catch (IllegalArgumentException e) {
            context.setCharEnd(charPos);
            throw new ParseException(
                new Token(Token.Type.STRING, number.toString(), context), "Invalid color value: '" + number + "'");
        }
    }

    /**
     * Get a color token by name.
     * <p>
     * When called, the current character should be the first character after the name
     * passed in. On return, the current character will be the first character after the color.
     *
     * @return A number.
     *
     * @throws ParseException When the color number is improperly formed.
     */
    private Color getColorByName(String name) throws ParseException
    {
        TokenContext context = captureContext();

        // If the name is rgb, hsl, or hsla, we should have a color function

        if (name.equals("rgb") || name.equals("hsl") || name.equals("hsla")) {

            StringBuilder builder = new StringBuilder(name);
            next();

            while (c != ')' && c != EOF) {
                builder.append(c);
                next();
            }

            // We didn't find a ')'

            if (c == EOF) {
                context.setCharEnd(charPos);
                throw new ParseException(new Token(Token.Type.STRING, builder.toString(), context), "Expected a color function");
            }

            // We found a ')'. Let's see if we have a valid color function

            else {
                try {
                    return Color.web(builder.toString());
                }
                catch (IllegalArgumentException e) {
                    context.setCharEnd(charPos);
                    throw new ParseException(new Token(Token.Type.STRING, builder.toString(), context), "Expected a color function");
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

}
