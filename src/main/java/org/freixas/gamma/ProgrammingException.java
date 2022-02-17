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

/**
 * Exceptions caused by programming errors.
 *
 * @author Antonio Freixas
 */
public class ProgrammingException extends RuntimeException
{
    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Create a Programming exception. Include an error message.
     *
     * @param message The error message.
     */
    public ProgrammingException(String message)
    {
        this(message, null);
    }

    /**
     * Create a Programming exception. Include the exception that caused it.
     *
     * @param cause The exception which caused the problem.
     */
    public ProgrammingException(Throwable cause)
    {
        this(null, cause);
    }
    /**
     *  Create a Programming exception. Include the error message and the
     *  exception that caused this exception
     *
     * @param message The error message.
     * @param cause The exception which caused the problem.
     */

    public ProgrammingException(String message, Throwable cause)
    {
        super(
            message != null ?
                (message + (cause != null ?
                    (cause.getLocalizedMessage() != null ? "\nCaused by " + cause.getLocalizedMessage() : "") :
                     "")) :
                (cause.getLocalizedMessage() != null ? "\nCaused by " + cause.getLocalizedMessage() : ""),
            cause);
    }

}
