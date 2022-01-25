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

/**
 *
 * @author Antonio Freixas
 */
@SuppressWarnings("serial")
public class ParseException extends Exception
{
    public ParseException(File file, int lineNumber, int charNumber, String message)
    {
        super((file != null? file.getName() + ", " : "") + "line " + lineNumber + " character " + charNumber + ": " + message);
    }

}
