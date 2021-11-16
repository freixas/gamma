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
package gamma.value;

import gamma.execution.lcode.Color;
import java.util.HashMap;

/**
 *
 * @author Antonio Freixas
 */
public class Property implements PropertyElement
{
    private final String name;
    private final Object value;

    public Property(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public boolean isStyleProperty()
    {
        switch (name) {
            case "color"                -> { return true; }
            case "backgroundColor"      -> { return true; }
            case "lineThickness"        -> { return true; }
            case "lineStyle"            -> { return true; }
            case "arrow"                -> { return true; }
            case "eventDiameter"        -> { return true; }
            case "eventShape"           -> { return true; }
            case "fontFamily"           -> { return true; }
            case "fontWeight"           -> { return true; }
            case "fontStyle"            -> { return true; }
            case "fontSize"             -> { return true; }
            case "textPadding"          -> { return true; }
            case "textAnchor"           -> { return true; }
            case "ticks"                -> { return true; }
            case "tickLabels"           -> { return true; }
            case "tickThickness"        -> { return true; }
            case "majorTickThickness"   -> { return true; }
            default                     -> { return false; }
        }
    }
}
