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
package gamma.execution.hcode;

import gamma.ProgrammingException;
import gamma.execution.ExecutionException;
import gamma.execution.SymbolTable;
import gamma.execution.lcode.Command;
import gamma.execution.lcode.CommandFactory;
import gamma.math.Util;
import gamma.value.*;
import java.util.ArrayList;
import java.util.HashMap;
import jdk.jshell.spi.ExecutionControl;

/**
 * An HCode is a high-level instruction for an imaginary machine which we
 * emulate using the HCodeEngine.
 *
 * @author Antonio Freixas
 */
public abstract class HCode extends ExecutorContext
{
    public enum Type
    {
        PRINT, SET_STYLE,
        DYNAMIC_NAME, FETCH, FETCH_PROP, FETCH_ADDRESS, FETCH_PROP_ADDRESS,
        NOT, TO_BOOLEAN, OR, AND,
        EQ, NE, LT, GT, LE, GE,
        UNARY_MINUS, UNARY_PLUS, SUB, MULT, DIV, REMAINDER, EXP, LORENTZ, INV_LORENTZ,
        W_INITIALIZER, W_SEGMENT, PROPERTY, PROPERTY_LIST,
        FRAME, OBSERVER_FRAME, AXIS_LINE, ANGLE_LINE, ENDPOINT_LINE, PATH, BOUNDS, INTERVAL, STYLE, COORDINATE,
        COMMAND
    }

    static final HashMap<Type, LambdaFunction> map = new HashMap<>();

    // ****************************************
    // * Misc
    // ****************************************

    // PRINT
    static final FunctionalOneArgNoRet<Object> print = (engine, obj) -> {
        if (obj == null) {
            engine.print("null");
        }
        else {
            engine.print(engine.toDisplayableString(obj));
        }
    };
    //SET_STYLE
    static final FunctionalOneArgNoRet<PropertyList> setStyle = (engine, properties) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Style style = new Style();
        style.add(properties);

        // Use these properties as the defaults further on

        engine.setStyleDefaults(style);
    };

    // ****************************************
    // * Fetching
    // ****************************************

    // DYNAMIC_NAME
    static final FunctionalTwoArg<Object, String, String> dynamicName = (engine, obj, baseName) -> {
        if (obj instanceof Double dbl) {
            int index = Util.toInt(dbl);
            return baseName + "$" + index;
        }
        else if (obj instanceof String str) {
            return baseName + "$" + str;
        }
        else {
            throw new ExecutionException("Array index must be a number or a string");
        }
    };
    // FETCH
    static final FunctionalOneArg<String, Object> fetch = (engine, symbol) -> {
        SymbolTable table = engine.getSymbolTable();
        if (!table.contains(symbol)) throw new ExecutionException("Variable '" + symbol + "' is not defined");
        return table.get(symbol);
    };
    // FETCH_PROP
    static final FunctionalTwoArg<ObjectContainer, String, Object> fetchProp = (engine, container, propName) -> {
        if (container == null) throw new ExecutionException("can't dereference a null value");
        if (!container.hasProperty(propName)) throw new ExecutionException("'" + propName + " is not a valid property");
        return container.getProperty(propName);
    };
    // FETCH_ADDRESS
    static final FunctionalOneArg<String, SymbolTableAddress> fetchAddress = (engine, symbol) -> new SymbolTableAddress(engine.getSymbolTable(), symbol);
    // FETCH_PROP_ADDRESS
    static final FunctionalTwoArg<Address, String, ObjectPropertyAddress> fetchPropAddress = (engine, address, propName) -> {
        if (!address.exists()) throw new ProgrammingException("FETCH_PROP_ADDRESS: Invalid address");
        Object value = address.getValue();
        if (value == null) throw new ExecutionException("can't dereference a null value");

        // Make sure the value has the given property

        if (!(value instanceof ObjectContainer) || !((ObjectContainer)value).hasProperty(propName)) {
            throw new ExecutionException("'" + propName + "' is not a valid property");
        }

        return new ObjectPropertyAddress((ObjectContainer)value, propName);
    };

    // ****************************************
    // * Operators
    // ****************************************

    // NOT
    static final FunctionalOneArg<Double, Double> not = (engine, arg1) -> (arg1 != null && Util.fuzzyZero(arg1)) ? 1.0 : 0.0;
    // TO_BOOLEAN
    static final FunctionalOneArg<Double, Double> toBoolean = (engine, arg1) -> (arg1 == null || Util.fuzzyZero(arg1)) ? 0.0 : 1.0;
    // OR
    static final FunctionalTwoArg<Double, Double, Double> or = (engine, arg1, arg2) -> !Util.fuzzyZero(arg1) || !Util.fuzzyZero(arg2) ? 1.0 : 0.0;
    //AND
    static final FunctionalTwoArg<Double, Double, Double> and = (engine, arg1, arg2) -> !Util.fuzzyZero(arg1) && !Util.fuzzyZero(arg2) ? 1.0 : 0.0;

    // EQ
    static final FunctionalTwoArg<Object, Object, Double> eq = (engine, arg1, arg2) -> Util.fuzzyEQ(arg1, arg2) ? 1.0 : 0.0;
    // NE
    static final FunctionalTwoArg<Object, Object, Double> ne = (engine, arg1, arg2) -> Util.fuzzyNE(arg1, arg2) ? 1.0 : 0.0;
    // LT
    static final FunctionalTwoArg<Double, Double, Double> lt = (engine, arg1, arg2) -> Util.fuzzyLT(arg1, arg2) ? 1.0 : 0.0;
    // GT
    static final FunctionalTwoArg<Double, Double, Double> gt = (engine, arg1, arg2) -> Util.fuzzyGT(arg1, arg2) ? 1.0 : 0.0;
    // LE
    static final FunctionalTwoArg<Double, Double, Double> le = (engine, arg1, arg2) -> Util.fuzzyLE(arg1, arg2) ? 1.0 : 0.0;
    // GE
    static final FunctionalTwoArg<Double, Double, Double> ge = (engine, arg1, arg2) -> Util.fuzzyGE(arg1, arg2) ? 1.0 : 0.0;

    // UNARY_MINUS
    static final FunctionalOneArg<Double, Double> unaryMinus = (engine, arg1) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't negate a null value");
        }
        return -arg1;
    };
     // UNARY_PLUS
    static final FunctionalOneArg<Double, Double> unaryPlus = (engine, arg1) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't operate on a null value");
        }
        return +arg1;
    };
    // SUB
    static final FunctionalTwoArg<Double, Double, Double> sub = (engine, arg1, arg2) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't use math on a null value");
        }
        return arg1 - arg2;
    };
    // MULT
    static final FunctionalTwoArg<Double, Double, Double> mult = (engine, arg1, arg2) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't use math on a null value");
        }
        return arg1 * arg2;
    };
    // DIV
    static final FunctionalTwoArg<Double, Double, Double> div = (engine, arg1, arg2) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't use math on a null value");
        }
        return arg1 / arg2;
    };
    // REMAINDER
    static final FunctionalTwoArg<Double, Double, Double> remainder = (engine, arg1, arg2) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't use math on a null value");
        }
        return arg1 % arg2;
    };
    // EXP
    static final FunctionalTwoArg<Double, Double, Double> exp = (engine, arg1, arg2) -> {
        if (arg1 == null) {
            throw new ExecutionException("Can't use math on a null value");
        }
        return Math.pow(arg1, arg2);
    };
    // LORENTZ
    static final FunctionalTwoArg<Coordinate, Object, Coordinate> lorentz = (engine, coord, obj) -> {
        if (coord == null) throw new ExecutionException("The coordinate can't be null");
        if (obj == null) throw new ExecutionException("The frame is null");
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Frame frame = Frame.promote(obj);
        if (frame == null) throw new ExecutionException("The '>' operator expects a frame or an observer");

        return frame.toFrame(coord);
    };
    // INV_LORENTZ
    static final FunctionalTwoArg<Coordinate, Object, Coordinate> invLorentz = (engine, coord, obj) -> {
        if (coord == null) throw new ExecutionException("The coordinate can't be null");
        if (obj == null) throw new ExecutionException("The frame is null");
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Frame frame = Frame.promote(obj);
        if (frame == null) throw new ExecutionException("The '<-' operator expects a frame or an observer");
        return frame.toRest(coord);
    };

    // ****************************************
    // * Supporting Object Creation
    // ****************************************

    // W_INITIALIZER
    static final FunctionalThreeArg<Coordinate, Double, Double, WInitializer> wInitializer = (engine, coord, tau, d) -> new WInitializer(coord, tau, d);
    // W_SEGMENT
    static final FunctionalFourArg<Double, Double, WorldlineSegment.LimitType, Double, WSegment> wSegment = (engine, v, a, limitType, delta) -> {
        if (v == null) throw new ExecutionException("The velocity is null");
        if (v <= -1.0 || v >= 1.0) throw new ExecutionException("The velocity must be within -1 and +1.");

         // Convert user acceleration (in g's) to the correct value ((ly/y^2) * user units)

        if (a == null) throw new ExecutionException("The acceleration is null");
        a =  a * 1.032295276 * engine.getSetStatement().getUnits();
        if (delta == null) throw new ExecutionException("The limit's value is null");
        if (limitType == WorldlineSegment.LimitType.V && (delta <= -1.0 || delta >= 1.0)) {
            throw new ExecutionException("The velocity must be within -1 and +1.");
        }
        return new WSegment(v, a, limitType, delta);
    };
    // PROPERTY
    static final FunctionalTwoArg<String, Object, Property> property = (engine, name, value) -> new Property(name, value);
    // PROPERTY_LIST
    static final VariableArg<PropertyList> propertyList = (engine, data) -> {
        int numOfProperties = data.length;
        PropertyList properties = new PropertyList();
        for (int i = 0; i < numOfProperties; i++) {
            if (!(data[i] instanceof PropertyElement)) {
                throw new ExecutionException("One of the elements in the list of properties is not a property");
            }
            PropertyElement element = (PropertyElement)data[i];
            properties.add(element);
        }
        return properties;
    };

    // ****************************************
    // * Object Creation
    // ****************************************

    // FRAME
    static final FunctionalTwoArg<Coordinate, Double, Frame> frame = (engine, origin, v) -> {
        if (origin == null) throw new ExecutionException("The origin is null");
        if (v == null) throw new ExecutionException("The velocity is null");
        if (v <= -1.0 || v >= 1.0) throw new ExecutionException("The velocity must be within -1 and +1.");
        return new Frame(origin, v);
    };
    // OBSERVER_FRAME
    static final FunctionalThreeArg<Observer, Frame.AtType, Double, Frame> observerFrame = (engine, observer, atType, atValue) -> {
        if (observer == null) throw new ExecutionException("The observer is null");
        if (atValue == null) throw new ExecutionException("The 'at' value is null");
        if (atType == Frame.AtType.V && (atValue <= -1.0 || atValue >= 1.0)) {
            throw new ExecutionException("The velocity must be within -1 and +1.");
        }

        return new Frame(observer, atType, atValue);
    };
    // AXIS_LINE
    static final FunctionalThreeArg<Line.AxisType, Object, Double, Line> axisLine = (engine, type, obj, offset) -> {
        if (obj == null) throw new ExecutionException("The frame is null");
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Frame frame = Frame.promote(obj);
        if (frame == null) throw new ExecutionException("The line object expects a frame or an observer");
        return new ConcreteLine(type, frame, offset);
    };
    // ANGLE_LINE
    static final FunctionalTwoArg<Double, Coordinate, Line> angleLine = (engine, angle, coord) -> {
        if (angle == null) throw new ExecutionException("The angle is null");
        if (coord == null) throw new ExecutionException("The coordinate is null");
        return new ConcreteLine(angle, coord);
    };
    // ENDPOINT_LINE
    static final FunctionalTwoArg<Coordinate, Coordinate, Line> endpointLine = (engine, coord1, coord2) -> {
        if (coord1 == null) throw new ExecutionException("The first coordinate is null");
        if (coord2 == null) throw new ExecutionException("The second coordinate is null");
        return new ConcreteLine(coord1, coord2);
    };
    // PATH
    static final VariableArg<Path> path = (engine, data) -> {
        int numOfCoordinates = data.length;
        ArrayList<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < numOfCoordinates; i++) {
            if (data[i] == null) throw new ExecutionException("Coordinate '" + (i + 1) +"' is null");
            if (!(data[i] instanceof Coordinate)) {
                throw new ProgrammingException("Trying to create a Path from elements that are not all Coordinates");
            }
            coords.add((Coordinate)data[i]);
        }
        return new Path(coords);
    };
    // BOUNDS
    static final FunctionalTwoArg<Coordinate, Coordinate, Bounds> bounds = (engine, min, max) -> {
        if (min == null) throw new ExecutionException("The minimum coordinate is null");
        if (max == null) throw new ExecutionException("The maximum coordinate is null");
        return new Bounds(min, max);
    };
    // INTERVAL
    static final FunctionalThreeArg<Interval.Type, Double, Double, Interval> interval = (engine, type, min, max) -> {
        if (min == null) throw new ExecutionException("The minimum value is null");
        if (max == null) throw new ExecutionException("The maximum value is null");
        return new Interval(type, min, max);
    };
    // STYLE
    static final FunctionalOneArg<PropertyList, Style> style = (engine, properties) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Style style = new Style();
        style.add(properties);
        return style;
    };
    // COORDINATE
    static final FunctionalTwoArg<Double, Double, Coordinate> coordinate = (engine, x, t) -> {
        if (x == null) throw new ExecutionException("The x coordinate is null");
        if (t == null) throw new ExecutionException("The t coordinate is null");
        return new Coordinate(x, t);
    };

    // ****************************************
    // * COMMANDS
    // ****************************************

    static final FunctionalTwoArgNoRet<PropertyList, String> command = (engine, properties, name) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Command command = CommandFactory.createCommand(engine, name, properties);
        engine.addCommand(command);
    };
    static {
        map.put(Type.PRINT, print);
        map.put(Type.SET_STYLE, setStyle);

        map.put(Type.DYNAMIC_NAME, dynamicName);
        map.put(Type.FETCH, fetch);
        map.put(Type.FETCH_PROP, fetchProp);
        map.put(Type.FETCH_ADDRESS, fetchAddress);
        map.put(Type.FETCH_PROP_ADDRESS, fetchPropAddress);

        map.put(Type.NOT, not);
        map.put(Type.TO_BOOLEAN, toBoolean);
        map.put(Type.OR, or);
        map.put(Type.AND, and);

        map.put(Type.EQ, eq);
        map.put(Type.NE, ne);
        map.put(Type.LT, lt);
        map.put(Type.GT, gt);
        map.put(Type.LE, le);
        map.put(Type.GE, ge);

        map.put(Type.NOT, not);
        map.put(Type.UNARY_MINUS, unaryMinus);
        map.put(Type.UNARY_PLUS, unaryPlus);
        map.put(Type.SUB, sub);
        map.put(Type.MULT, mult);
        map.put(Type.DIV, div);
        map.put(Type.REMAINDER, remainder);
        map.put(Type.EXP, exp);
        map.put(Type.LORENTZ, lorentz);
        map.put(Type.INV_LORENTZ, invLorentz);

        map.put(Type.W_INITIALIZER, wInitializer);
        map.put(Type.W_SEGMENT, wSegment);
        map.put(Type.PROPERTY, property);
        map.put(Type.PROPERTY_LIST, propertyList);

        map.put(Type.FRAME, frame);
        map.put(Type.OBSERVER_FRAME, observerFrame);
        map.put(Type.ANGLE_LINE, angleLine);
        map.put(Type.AXIS_LINE, axisLine);
        map.put(Type.ENDPOINT_LINE, endpointLine);
        map.put(Type.PATH, path);
        map.put(Type.BOUNDS, bounds);
        map.put(Type.INTERVAL, interval);
        map.put(Type.STYLE, style);
        map.put(Type.COORDINATE, coordinate);

        map.put(Type.COMMAND, command);
    }

}
