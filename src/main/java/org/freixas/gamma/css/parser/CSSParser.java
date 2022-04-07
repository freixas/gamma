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
 * You should have received first copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.css.parser;

import org.freixas.gamma.css.value.*;
import org.freixas.gamma.file.URLFile;
import org.freixas.gamma.parser.ParseException;
import org.freixas.gamma.parser.Token;
import org.freixas.gamma.parser.TokenContext;

import java.io.File;
import java.util.ArrayList;

/**
 * This class handles parsing stylesheets.
 *
 * @author Antonio Freixas
 */
public final class CSSParser
{
    private final URLFile file;                // The file associated with the stylesheet
    private final String css;               // The code to parse
    private ArrayList<Token<?>> tokens;     // The tokens from tokenizing the code

    private final Token<?> dummyToken = new Token<>(Token.Type.DELIMITER, '~', new TokenContext(null, "", 0, 0, 0, 0));

    private int tokenPtr;                   // The pointer to the current token
    private Token<?> curToken;              // The current token
    private Token<?> peek;                  // The token after the current token

    private Stylesheet stylesheet;          // The stylesheet produced by parsing

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    public CSSParser(URLFile file, String css)
    {
        this.file = file;
        this.css = css;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the tokens produced by parsing.
     *
     * @return The tokens produced by parsing.
     */
    public ArrayList<Token<?>> getTokens()
    {
        return this.tokens;
    }

    /**
     * Get the stylesheet produced by parsing.
     *
     * @return The stylesheet produced by parsing.
     */
    public Stylesheet getStylesheet()
    {
        return this.stylesheet;
    }

    // **********************************************************************
    // *
    // * Parse
    // *
    // **********************************************************************

    /**
     * Parse the css.
     *
     * @return The stylesheet produced by parsing.
     * @throws ParseException When a syntax error occurs.
     */
    public Stylesheet parse() throws ParseException
    {
        CSSTokenizer tokenizer = new CSSTokenizer(file, css);
        tokens = tokenizer.tokenize();

        // tokenPtr points to the current token.
        // When we start, it points one before the start of the token list.
        // The current token is first dummy token.
        // Peek is the token after the current one.
        // It starts out as the first item on the token list.
        // The token list always has at least one item. The last token is
        // always the EOF token.

        setCurrentTokenTo(-1);

        stylesheet = parseCSS();
        return stylesheet;
    }

    // **********************************************************************
    // *
    // * Recursive Descent Parsing
    // *
    // **********************************************************************

    // Recursive descent parsing was chosen because it is simple, flexible,
    // and maintainable. In the case of left variables, it allows us an infinite
    // look-ahead that lets us avoid having keywords.
    //
    // To fully understand the methods here, one should have access to the
    // syntax definition in the specification.

    // Generally, each parse method should assume it should start processing
    // the current token. Each parse statement should return with the current
    // token set to the token after whatever syntax it covers.

    /**
     * Parse the stylesheet.
     *
     * @return A stylesheet.
     * @throws ParseException If a syntax error occurs.
     */
    private Stylesheet parseCSS() throws ParseException
    {
        stylesheet = new Stylesheet();

        nextToken();
        while (!isEOF()) {
            stylesheet.addRule(parseRule());
        }

        return stylesheet;
    }

    /**
     * Parse a rule.
     *
     * @return A stylesheet rule.
     * @throws ParseException If a syntax error occurs.
     */
    private Rule parseRule() throws ParseException
    {
        Rule rule = new Rule();

        // Get all the selectors. We might have none

        while (isSelector()) {
            try {
                Selector selector = new Selector(getString());
                rule.addSelector(selector);
                nextToken();
            }
            catch (StyleException e) {
                throwParseException(e.getLocalizedMessage());
            }

            // If we find a comma, we have another selector to parse

            if (isDelimiter() && getChar() == ',') {
                nextToken();
            }
            else {
                break;
            }
        }

        // We must have a '{'

        if (isDelimiter() && getChar() == '{') {
            nextToken();
        }
        else {
            throwParseException("Expected a '{'");
        }

        // Get all the properties

        while (!isDelimiter() || getChar() != '}') {
            StyleProperty property = parseStyleProperty();
            if (property != null) {
                rule.addStyleProperty(property);
            }

            // We must have a ';'

            if (isDelimiter() && getChar() == ';') {
                nextToken();
            }
            else {
                throwParseException("Expected a ';'");
            }
        }

        // Skip the '}'

        nextToken();

        return rule;
    }

    /**
     * Parse a style property.
     *
     * @return A style property.
     * @throws ParseException If a syntax error occurs.
     */
    private StyleProperty parseStyleProperty() throws ParseException
    {
        // We should be pointed to the style property name or to a ';' if we
        // have an empty property

        String name = null;

        // Empty property

        if (isDelimiter() && getChar() == ';') {
            return null;
        }

        if (isName()) {
            name = getString();
            nextToken();
        }


        else {
            throwParseException("Expected a property name");
        }

        // We must have a ':'

        if (isDelimiter() && getChar() == ':') {
            nextToken();
        }
        else {
            throwParseException("Expected a ';'");
        }

        // We may have a negative number
        // We may have a single other value (of various types)

        try {
            if (isDelimiter() && getChar() == '-') {
                double value = parseFloat();
                if (!Double.isNaN(value)) {
                    StylePropertyDefinition definition = StylePropertyDefinition.toDefinition(name);
                    return new StyleProperty(name, value, definition);
                }
            }
            else {
                StyleProperty property = StyleProperties.createStyleProperty(name, curToken);
                nextToken();
                return property;
            }
        }
        catch (StyleException e) {
            throwParseException(e.getLocalizedMessage());
        }

        return null;        // Should never be reached
    }

    /**
     * Parse a floating point number.
     *
     * @return A floating point number.
     */
    private double parseFloat()
    {
        double sign = 1;

        if (isDelimiter() && getChar() == '-' && peek.isNumber()) {
            sign = -1;
            nextToken();
        }

        if (isNumber()) {
            double number = getNumber() * sign;
            nextToken();
            return number;
        }

        return Double.NaN;
    }

    /**
     * Get the next token. The token is available in the global variable
     * 'curToken'.
     */
    private void nextToken()
    {
        // If the current token is EOF, we can't move forward.
        // The peek token will also  be EOF

        if (isEOF()) return;

        // If the current token isn't EOF, then we are guaranteed to be able
        // to get another token

        tokenPtr++;
        curToken = tokens.get(tokenPtr);

        // If the current token is now EOF, the peek token should also be EOF.
        // If the current token is not EOF, the next token is first valid one,
        // so grab it (it might be EOF).

        peek = curToken;
        if (!isEOF()) peek = tokens.get(tokenPtr + 1);
    }

    /**
     * Set the current token to the token pointed to.
     *
     * @param ptr The pointer to the token we should now point to.
     */
    @SuppressWarnings("SameParameterValue")
    private void setCurrentTokenTo(int ptr)
    {
        if (ptr < 0) {
            tokenPtr = -1;
            curToken = dummyToken;
            peek = tokens.get(0);
        }
        else if (ptr >= tokens.size()) {
            tokenPtr = tokens.size() - 1;
            curToken = tokens.get(tokenPtr);    // Should be EOF
            peek = curToken;
        }
        else {
            tokenPtr = ptr;
            curToken = tokens.get(tokenPtr);
            peek = curToken;
            if (!isEOF()) peek = tokens.get(tokenPtr + 1);
        }
    }

    // Various shortcut methods

    private boolean isNumber() { return curToken.isNumber(); }
    private boolean isSelector() { return curToken.isSelector(); }
    private boolean isName() { return curToken.isName(); }
    private boolean isDelimiter() { return curToken.isDelimiter(); }
    private boolean isEOF() { return curToken.isEOF(); }
    private double getNumber() { return curToken.getNumber(); }
    private char getChar() { return curToken.getChar(); }
    private String getString() { return curToken.getString(); }

    private void throwParseException(String message) throws ParseException
    {
        throw new ParseException(curToken, message);
    }

}
