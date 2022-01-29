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
 *
 * @author Antonio Freixas
 */
@SuppressWarnings("serial")
public class GammaRuntimeException extends RuntimeException
{
    public enum Type { EXECUTION, PROGRAMMING, OTHER };

    private final Type type;
    TokenContext context;

    public GammaRuntimeException(Type type, TokenContext context, String message)
    {
        this(type, context, message, null);
    }

    public GammaRuntimeException(Type type, TokenContext context, Throwable cause)
    {
        this(type, context, cause != null ? cause.getLocalizedMessage() : null, cause);
    }

    public GammaRuntimeException(Type type, TokenContext context, String message, Throwable cause)
    {
        super(message, cause);
        this.type = type;
        this.context = context;

    }

    public Type getType()
    {
        return type;
    }

    public TokenContext getTokenContext()
    {
        return context;
    }

}
