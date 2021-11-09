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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Antonio Freixas
 */
public class PropertyInfo
{
    public enum Type {
        DOUBLE, STRING, STRING_SET, OBSERVER, FRAME,  LINE, COORDINATE, POLYLINE
    }
    static private final HashMap<String, PropertyInfo> infoTable = new HashMap<>();
    static
    {
        // Style command and style objects

        infoTable.put("style-color",              new PropertyInfo(Type.DOUBLE,       Color.black, Double.NaN, Double.NaN));
        infoTable.put("style-backgroundColor",    new PropertyInfo(Type.DOUBLE,       Color.white, Double.NaN, Double.NaN));
        infoTable.put("style-lineThickness",          new PropertyInfo(Type.DOUBLE,       2,           1,          20));
        infoTable.put("style-lineStyle",          new PropertyInfo(Type.STRING_SET,   "solid",     Double.NaN, Double.NaN, "solid, dashed, dotted"));
        infoTable.put("style-arrow",              new PropertyInfo(Type.STRING_SET,   "none",      Double.NaN, Double.NaN, "none, both, start, end"));
        infoTable.put("style-diameter",           new PropertyInfo(Type.DOUBLE,       2,           1,          20));
        infoTable.put("style-eventShape",         new PropertyInfo(Type.DOUBLE,       "circle",    Double.NaN, Double.NaN, "circle, square, diamond, star"));
        infoTable.put("style-font",               new PropertyInfo(Type.STRING,       "Arial",     Double.NaN, Double.NaN));
        infoTable.put("style-weight",             new PropertyInfo(Type.STRING_SET,   "normal",    Double.NaN, Double.NaN, "normal, bold"));
        infoTable.put("style-slant",              new PropertyInfo(Type.STRING_SET,   "normal",    Double.NaN, Double.NaN, "normal, italic"));
        infoTable.put("style-size",               new PropertyInfo(Type.DOUBLE,       10,          Double.NaN, Double.NaN));
        infoTable.put("style-padding",            new PropertyInfo(Type.DOUBLE,       2,           Double.NaN, Double.NaN));
        infoTable.put("style-anchor",             new PropertyInfo(Type.STRING_SET,   "MC",        Double.NaN, Double.NaN, "UL,UC,UR, ML,MC,MR, BL,BC,BR"));
        infoTable.put("style-ticks",              new PropertyInfo(Type.DOUBLE,       1,           Double.NaN, Double.NaN));
        infoTable.put("style-tickLabels",         new PropertyInfo(Type.DOUBLE,       1,           Double.NaN, Double.NaN));
        infoTable.put("style-tickThickness",      new PropertyInfo(Type.DOUBLE,       2,           1,          20));
        infoTable.put("style-majorTickThickness", new PropertyInfo(Type.DOUBLE,       2,           1,          20));

    }

    private final Type type;
    private final Object defaultValue;
    private final double minValue;
    private final double maxValue;
    private final ArrayList<String> commands;
    private List<String> stringSet = null;


    public PropertyInfo(Type type, Object defaultValue,
                        double minValue, double maxValue)
    {
        this.type = type;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.commands = new ArrayList<>();
    }

    public PropertyInfo(Type type, Object defaultValue,
                        double minValue, double maxValue,
                        String stringSet)
    {
        this(type, defaultValue, minValue, maxValue);

        String[] sets = stringSet.split(", *");
        this.stringSet = Arrays.asList(sets);
    }

    public Type getType()
    {
        return type;
    }

    public Object getDefault()
    {
        return defaultValue;
    }

    public double getMinValue()
    {
        return minValue;
    }

    public double getMaxValue()
    {
        return maxValue;
    }

    public ArrayList<String> getCommands()
    {
        return commands;
    }

    public List<String> getStringSet()
    {
        return stringSet;
    }

    public boolean isRequired()
    {
        return defaultValue == null;
    }

    static public boolean exists(String command, String propertyName)
    {
        String name = command + "-" + propertyName;
        return infoTable.containsKey(name);
    }

    static public boolean isStyleProperty(String propertyName)
    {
        return exists("style", propertyName);
    }

    static public PropertyInfo getPropertyInfo(String command, String propertyName)
    {
        String name = command + "-" + propertyName;
        return infoTable.get(name);
    }

}
