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

/**
 * Exceptions causes by syntax errors.
 *
 * @author Antonio Freixas
 */
public final class ParseException extends Exception
{
    private final Token<?> token;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Create a Parse exception. There is always an error message.
     *
     * @param token The token at which the error was found.
     * @param message The error message.
     */
    public ParseException(Token<?> token, String message)
    {
        super(message);
        this.token = token;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the token associated with this exception.
     *
     * @return The token associated with this exception.
     */
    public Token<?> getToken()
    {
        return token;
    }

}
