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
 *
 * @author Antonio Freixas
 */
public class GammaRuntimeException extends RuntimeException
{
    public GammaRuntimeException(String message)
    {
        super(message);
    }

    public GammaRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GammaRuntimeException(Throwable cause)
    {
        super(cause != null ? cause.getLocalizedMessage() : null, cause);
    }


}
