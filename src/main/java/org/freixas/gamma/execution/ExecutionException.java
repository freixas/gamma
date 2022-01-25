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
package gamma.execution;

import gamma.GammaRuntimeException;
import gamma.parser.ParseException;

/**
 *
 * @author Antonio Freixas
 */
@SuppressWarnings("serial")
public class ExecutionException extends GammaRuntimeException
{
    public ExecutionException(String message)
    {
        super("Runtime error: " + message);
    }

    public ExecutionException(String message, Throwable cause)
    {
        super(
            message == null && cause != null && cause instanceof ParseException ?
                ("Stylesheet parsing error: " + cause.getLocalizedMessage()) :
                ("Runtime error: " + message +
                    (cause != null ?
                    (cause.getLocalizedMessage() != null ?
                        "\nCaused by " + cause.getLocalizedMessage() :
                        "") :
                    "")
                ),
            cause);
    }

//    @Override
//    public String toString()
//    {
//        String str = getLocalizedMessage() == null ? "" : getLocalizedMessage();
//        Throwable cause = getCause();
//        while (cause != null) {
//            if (cause.getLocalizedMessage() != null) {
//                str += "\nCaused by: " + cause.getLocalizedMessage();
//            }
//            else {
//                str += "\nCaused by: " + cause.getClass();
//            }
//            cause = cause.getCause();
//        }
//        return str;
//    }

}