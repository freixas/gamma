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

import gamma.value.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.Property;

/**
 *
 * @author Antonio Freixas
 */
public class ArgInfo
{
    public enum Type
    {
        INTEGER, DOUBLE, STRING, DOUBLE_OR_STRING, W_INITIALIZER, W_SEGMENT, LIMIT_TYPE,
        OBSERVER, OBSERVER_OR_FRAME, AT_TYPE, LINE, AXIS_TYPE, PATH, PROPERTY, PROPERTY_LIST, STYLE,
        PROPERTY_ELEMENT, COORDINATE, ADDRESS, OBJECT_CONTAINER, LINE_OR_OBSERVER, ANY
    }

    private final int numberOfArgs;
    private final List<Type> argTypes;
    private final int numberOfReturnedValues;

    /**
     * Define the valid arguments for an HCode instruction or a Function.
     * <p>
     * This constructor sets the number of returned values to 1.
     *
     * @param numberOfArgs The number of arguments. If -1, then the number is
     * variable.
     * @param argTypes A list of the allowed argument types. Arguments and types
     * are matched 1-to-1. If the number of arguments is variable, the last type
     * on the list is repeatedly applied to any remaining arguments.
     */
    public ArgInfo(int numberOfArgs, List<Type> argTypes)
    {
        this(numberOfArgs, argTypes, 1);
    }

    /**
     * Define the valid arguments for an HCode instruction or a Function.
     *
     * @param numberOfArgs The number of arguments. If -1, then the number is
     * variable.
     * @param argTypes A list of the allowed argument types. Arguments and types
     * are matched 1-to-1. If the number of arguments is variable, the last type
     * on the list is repeatedly applied to any remaining arguments.
     * @param numberOfReturnedValues The number of values returned by the
     * associated HCode or Function.
     */
    public ArgInfo(int numberOfArgs, List<Type> argTypes, int numberOfReturnedValues)
    {
        this.numberOfArgs = numberOfArgs;
        this.argTypes = argTypes;
        this.numberOfReturnedValues = numberOfReturnedValues;
    }

    /**
     * Return the number of arguments required by an HCode instruction or
     * Function. A value of -1 means that we get the number of arguments from
     * the data stack.
     *
     * @return The number of arguments required by an HCode instruction or
     * Function.
     */
    public int getNumberOfArgs()
    {
        return numberOfArgs;
    }

    /**
     * Get the number of values pushed back onto the data stack. Currently,
     * this will be either 1 or 0.
     *
     * @return The number of values pushed back onto the data stack.
     */
    public int getNumberOfReturnedValues()
    {
        return numberOfReturnedValues;
    }

    public void checkTypes(List<Object> code)
    {
        Iterator<Object> iter = code.listIterator();
        int argTypePtr = 0;

        while (iter.hasNext()) {
            Object obj = iter.next();

            // If this is the last object to check and the number of args is -1, then the
            // last object is an integer which has already been checked, so we're done

            if (numberOfArgs == -1 && !iter.hasNext()) return;
            
            switch (argTypes.get(argTypePtr)) {
                case INTEGER ->             { if (!(obj instanceof Integer))                                throwTypeError("an integer"); }
                case DOUBLE ->              { if (!(obj instanceof Double))                                 throwTypeError("a number"); }
                case STRING ->              { if (!(obj instanceof String))                                 throwTypeError("a string"); }
                case DOUBLE_OR_STRING ->    { if (!(obj instanceof Double) && !(obj instanceof String))     throwTypeError("a number or string"); }
                case W_INITIALIZER ->       { if (!(obj instanceof WInitializer))                           throwTypeError("a worldline initializer"); }
                case W_SEGMENT ->           { if (!(obj instanceof WSegment))                               throwTypeError("a worldline segment"); }
                case LIMIT_TYPE ->          { if (!(obj instanceof WorldlineSegment.LimitType))             throwTypeError("a worldline segment limit type"); }
                case OBSERVER ->            { if (!(obj instanceof Observer))                               throwTypeError("an observer"); }
                case OBSERVER_OR_FRAME ->   { if (!(obj instanceof Observer) && !(obj instanceof Frame))    throwTypeError("a frame or observer"); }
                case AT_TYPE ->             { if (!(obj instanceof Frame.AtType))                           throwTypeError("an 'at' type"); }
                case LINE ->                { if (!(obj instanceof Line))                                   throwTypeError("a line"); }
                case AXIS_TYPE ->           { if (!(obj instanceof Line.AxisType))                          throwTypeError("an axis type"); }
                case PATH ->                { if (!(obj instanceof Path))                                   throwTypeError("a path"); }
                case PROPERTY ->            { if (!(obj instanceof Property))                               throwTypeError("a property"); }
                case PROPERTY_LIST ->       { if (!(obj instanceof PropertyList))                           throwTypeError("a property list"); }
                case STYLE ->               { if (!(obj instanceof Style))                                  throwTypeError("a style"); }
                case PROPERTY_ELEMENT ->    { if (!(obj instanceof PropertyElement))                        throwTypeError("a property element"); }
                case COORDINATE ->          { if (!(obj instanceof Coordinate))                             throwTypeError("a coordinate"); }
                case ADDRESS ->             { if (!(obj instanceof Address))                                throwTypeError("an address"); }
                case OBJECT_CONTAINER ->    { if (!(obj instanceof ObjectContainer))                        throwTypeError("an object with an accessible property"); }
                case LINE_OR_OBSERVER ->    { if (!(obj instanceof Line) && !(obj instanceof Observer))     throwTypeError("a line or an observer"); }
                case ANY -> { /* DO NOTHING */ }
                default -> { /* DO NOTHING */ }
            }

            if (argTypePtr < argTypes.size() - 1) argTypePtr++;
        }
    }

    private void throwTypeError(String message)
    {
        throw new ExecutionException("Expected " + message);
    }

}