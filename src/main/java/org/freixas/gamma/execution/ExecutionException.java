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
package org.freixas.gamma.execution;

import org.freixas.gamma.parser.ParseException;

import java.io.Serial;

/**
 *
 * @author Antonio Freixas
 */

public class ExecutionException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = 1L;

    public ExecutionException(String message)
    {
        super(message);
    }

    public ExecutionException(String message, Throwable cause)
    {
        super(
            message == null && cause instanceof ParseException ?
                ("Stylesheet parsing error: " + cause.getLocalizedMessage()) :
                (message +
                    (cause != null ?
                    (cause.getLocalizedMessage() != null ?
                        "\nCaused by " + cause.getLocalizedMessage() :
                        "") :
                    "")
                ),
            cause);
    }

}
