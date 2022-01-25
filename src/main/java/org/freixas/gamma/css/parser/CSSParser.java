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
package gamma.css.parser;

import gamma.css.value.Rule;
import gamma.css.value.Selector;
import gamma.css.value.StyleException;
import gamma.css.value.StyleProperties;
import gamma.css.value.StyleProperty;
import gamma.css.value.StylePropertyDefinition;
import gamma.css.value.Stylesheet;
import gamma.parser.ParseException;
import gamma.parser.Token;
import java.io.File;
import java.util.ArrayList;
import javafx.scene.paint.Color;

/**
 *
 * @author Antonio Freixas
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class CSSParser
{
    private final File file;
    private final String css;
    private ArrayList<Token<?>> tokens;

    private final Token<?> dummyToken = new Token<>(Token.Type.DELIMITER, '~', null, 0, 0);

    private int tokenPtr;
    private Token<?> curToken;
    private Token<?> peek;

    private Stylesheet stylesheet;

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public CSSParser(File file, String css)
    {
        this.file = file;
        this.css = css;
    }

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

    /**
     * Parse the css.
     *
     * @return The stylesheet produced by parsing.
     * @throws ParseException
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

    // Generally, each parse method should assume it should start processing
    // the current token. Each parse statement should return with the current
    // token set to the token after whatever syntax it covers.

    private Stylesheet parseCSS() throws ParseException
    {
        stylesheet = new Stylesheet();

        nextToken();
        while (!isEOF()) {
            stylesheet.addRule(parseRule());
        }

        return stylesheet;
    }

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

    private StyleProperty parseStyleProperty() throws ParseException
    {
        // We should be point to the style property name or to a ';' if we
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
                Double value = parseFloat();
                if (!Double.isNaN(value)) {
                    StylePropertyDefinition definition = StylePropertyDefinition.toDefinition(name);
                    return new StyleProperty(name, value, definition);
                }
            }

//            if (isName() && getString().equals("rgb")) {
//                Color color = parseRGB();
//                StylePropertyDefinition definition = StylePropertyDefinition.toDefinition(name);
//                return new StyleProperty(name, color, definition);
//            }
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

//    private Color parseRGB() throws ParseException
//    {
//        // The current token is "rgb"
//
//        nextToken();
//
//        // Look for a '('
//
//        if (!isDelimiter() || getChar() != '(') {
//            throwParseException("Expected '('");
//        }
//        nextToken();
//
//        // Look for 3 numbers
//
//        double colors[] = new double[3];
//        for (int i = 0; i < 3; i++) {
//            Double value = parseFloat();
//            if (!Double.isNaN(value)) {
//                colors[i] = Math.min(Math.max(0, value), 255.0);
//            }
//            else {
//                throwParseException("Expected a number");
//            }
//            nextToken();
//
//            if (i < 2) {
//                if (isDelimiter() && getChar() == ',') {
//                    nextToken();
//                }
//                else {
//                    throwParseException("Expected a ','");
//                }
//            }
//        }
//
//        // Look for an optional 4th number
//
//        double alpha = 1.0;
//
//        if (isDelimiter() && getChar() == ',') {
//            nextToken();
//
//            Double value = parseFloat();
//            if (!Double.isNaN(value)) {
//                alpha = Math.min(Math.max(0, getNumber()), 1.0);
//            }
//            else {
//                throwParseException("Expected a number");
//            }
//        }
//        return new Color(colors[0], colors[1], colors[2], alpha);
//    }
//
    private double parseFloat() throws ParseException
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

    private void returnToken()
    {
        // When we return first token, we want to set the tokens to what they
        // were before the last nextToken() call.
        // The current token become the peek token.

        peek = curToken;

        // If we can't decrement the tokenPtr, point curToken to the dummy
        // token

        if (tokenPtr < 0) {
            curToken = dummyToken;
        }

        // Otherwise, decrement the tokenPtr and get the token there

        else {
            tokenPtr--;
            curToken = tokens.get(tokenPtr);
        }
    }

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

    private boolean isNumber() { return curToken.isNumber(); }
    private boolean isColor() { return curToken.isColor(); }
    private boolean isString() { return curToken.isString(); }
    private boolean isSelector() { return curToken.isSelector(); }
    private boolean isName() { return curToken.isName(); }
    private boolean isDelimiter() { return curToken.isDelimiter(); }
    private boolean isEOF() { return curToken.isEOF(); }
    private double getNumber() { return curToken.getNumber(); }
    private char getChar() { return curToken.getChar(); }
    private String getString() { return curToken.getString(); }

    private void throwParseException(String message) throws ParseException
    {
        throw new ParseException(
                curToken.getFile(),
                curToken.getLineNumber(),
                curToken.getCharNumber(),
                message);
    }

}
