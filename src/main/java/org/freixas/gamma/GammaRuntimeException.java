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
package org.freixas.gamma;

import org.freixas.gamma.parser.TokenContext;

/**
 * Most exceptions eventually become GammaRuntimeExceptions. Most exceptions are either syntax errors or script
 * runtime errors. These get displayed in a nice dialog, where we try to show the user exactly where the error
 * occurred.
 *
 * There are also possible programming problems, which have their own display.
 *
 * @author Antonio Freixas
 */
public class GammaRuntimeException extends RuntimeException
{
    public enum Type
    {
        /**
         * Execution errors: either syntax errors or script runtime errors
         */
        EXECUTION,

        /**
         * Programming errors: things that should never occur
         */
        PROGRAMMING,

        /**
         * Any other kind of exception
         */
        OTHER
    }

    private final Type type;
    TokenContext context;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a GammaRuntimeException.
     *
     * @param type The exception type: EXECUTION, PROGRAMMING, or OTHER.
     * @param context The Token context of the error. This helps us display the
     * relevant portion of the script to the user.
     * @param message The error message.
     */
    public GammaRuntimeException(Type type, TokenContext context, String message)
    {
        this(type, context, message, null);
    }

    /**
     * Create a GammaRuntimeException.
     *
     * @param type The exception type: EXECUTION, PROGRAMMING, or OTHER.
     * @param context The Token context of the error. This helps us display the relevant portion of the
     *                script to the user.
     * @param cause The exception which caused the problem.
     */
    public GammaRuntimeException(Type type, TokenContext context, Throwable cause)
    {
        this(type, context, cause != null ? cause.getLocalizedMessage() : null, cause);
    }

    /**
     * Create a GammaRuntimeException.
     *
     * @param type The exception type: EXECUTION, PROGRAMMING, or OTHER.
     * @param context The Token context of the error. This helps us display the relevant portion of the
     *                script to the user.
     * @param message The error message.
     * @param cause The exception which caused the problem.
     */
    public GammaRuntimeException(Type type, TokenContext context, String message, Throwable cause)
    {
        super(message, cause);
        this.type = type;
        this.context = context;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    public Type getType()
    {
        return type;
    }

    public TokenContext getTokenContext()
    {
        return context;
    }

}
