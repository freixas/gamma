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
import java.util.ArrayList;

/**
 *
 * @author Antonio Freixas
 */
public class ScriptTokenizer extends Tokenizer
{
    public ScriptTokenizer(File file, String code)
    {
        super(file, code, "+-*/^.<>!&|%=", ";,:=[](){}");
    }

    /**
     * Tokenize the script.
     *
     * @return A list of Tokens.
     *
     * @throws ParseException When an invalid token is found.
     */
    @Override
    public ArrayList<Token<?>> tokenize() throws ParseException
    {
        TokenContext context;

        initialize();
        next();

        while (c != EOF) {

            // Strip comments

            while (c == '/' && cNext == '/') {
                next(); next();
                stripComment();
                if (c == EOF) break;
            }

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

            // Handle operators

            if (operators.indexOf(c) != -1) {
                String operator = getOperator();
                if (operator != null) {
                    context = captureContext();
                    list.add(new Token<>(Token.Type.OPERATOR, operator, context));
                    continue;
                }
            }

            // Handle delimiters

            if (delimiters.indexOf(c) != -1) {
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

            // Handle names

            else if (c == '_' || Character.isLetter(c)) {
                context = captureContext();
                list.add(new Token<>(Token.Type.NAME, getName(), context));
                context.setCharEnd(cPtr - 2);
            }

            // Handle numbers

            else if ((c == '.' && Character.isDigit(cNext))  ||
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

        return list;
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

        while (Character.isLetterOrDigit(c) || c == '_') {
            name.append(c);
            next();
        }
        return name.toString();
    }

    /**
     *  When called, the current character is an operator character. Operators can be one- or two-characters long.
     *  Some single-character operator characters aren't legal operators. On return, the current character is the
     *  character after the operator if we found a valid operator; otherwise, the current character remains unchanged.
     *
     * @return The operator string if we found a valid operator; null otherwise.
     */
    private String getOperator()
    {
        // Check for two-character operators
        // Boolean: &&, ||, ==, !=, <=, >=
        // Lorentz transform: <-, ->

        // We know the first character is an operator

        char op = c;

        if (op == '&' && cNext == '&') { next(); next(); return "&&"; }
        if (op == '|' && cNext == '|') { next(); next(); return "||"; }
        if (op == '!' && cNext == '=') { next(); next(); return "!="; }
        if (op == '=' && cNext == '=') { next(); next(); return "=="; }
        if (op == '<' && cNext == '=') { next(); next(); return "<="; }
        if (op == '>' && cNext == '=') { next(); next(); return ">="; }
        if (op == '<' && cNext == '-') { next(); next(); return "<-"; }
        if (op == '-' && cNext == '>') { next(); next(); return "->"; }

        // Some single characters aren't operators

        if ( op == '&' || op == '|' || op == '=' || (op == '.' && Character.isDigit(cNext))) {
            return null;
        }

        next();
        return Character.toString(op);
    }

}
