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
        ENABLE, PRINT, SET_STYLE,
        FETCH, FETCH_PROP, FETCH_ADDRESS, FETCH_PROP_ADDRESS,
        UNARY_MINUS, UNARY_PLUS, SUB, MULT, DIV, EXP, LORENTZ, INV_LORENTZ,
        W_INITIALIZER, W_SEGMENT, PROPERTY, PROPERTY_LIST,
        FRAME, OBSERVER_FRAME, AXIS_LINE, ANGLE_LINE, ENDPOINT_LINE, PATH, BOUNDS, INTERVAL, STYLE, COORDINATE,
        COMMAND
    }

    static final HashMap<Type, LambdaFunction> map = new HashMap<>();

    // ****************************************
    // * Misc
    // ****************************************

    // ENABLE
    static final FunctionalOneArgNoRet<Double> enable = (engine, dbl) -> {
        engine.setExecutionEnabled(!Util.fuzzyZero(dbl));
    };
    // PRINT
    static final FunctionalOneArgNoRet<Object> print = (engine, obj) -> {
        if (engine.isExecutionEnabled()) {
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

    // FETCH
    static final FunctionalOneArg<String, Object> fetch = (engine, symbol) -> {
        SymbolTable table = engine.getSymbolTable();
        if (!table.contains(symbol)) throw new ExecutionException("Variable '" + symbol + "' is not defined");
        return table.get(symbol);
    };
    // FETCH_PROP
    static final FunctionalTwoArg<ObjectContainer, String, Object> fetchProp = (engine, container, propName) -> {
        if (!container.hasProperty(propName)) throw new ExecutionException("'" + propName + " is not a valid property");
        return container.getProperty(propName);
    };
    // FETCH_ADDRESS
    static final FunctionalOneArg<String, SymbolTableAddress> fetchAddress = (engine, symbol) -> new SymbolTableAddress(engine.getSymbolTable(), symbol);
    // FETCH_PROP_ADDRESS
    static final FunctionalTwoArg<Address, String, ObjectPropertyAddress> fetchPropAddress = (engine, address, propName) -> {
        if (!address.exists()) throw new ExecutionException("Invalid address");
        Object value = address.getValue();

        // Make sure the value has the given property

        if (!(value instanceof ObjectContainer) || !((ObjectContainer)value).hasProperty(propName)) {
            throw new ExecutionException("'" + propName + "' is not a valid property");
        }

        return new ObjectPropertyAddress((ObjectContainer)value, propName);
    };

    // ****************************************
    // * Operators
    // ****************************************

    // UNARY_MINUS
    static final FunctionalOneArg<Double, Double> unaryMinus = (engine, arg1) -> -arg1;
     // UNARY_PLUS
    static final FunctionalOneArg<Double, Double> unaryPlus = (engine, arg1) -> +arg1;
    // SUB
    static final FunctionalTwoArg<Double, Double, Double> sub = (engine, arg1, arg2) -> arg1 - arg2;
    // MULT
    static final FunctionalTwoArg<Double, Double, Double> mult = (engine, arg1, arg2) -> arg1 * arg2;
    // DIV
    static final FunctionalTwoArg<Double, Double, Double> div = (engine, arg1, arg2) -> arg1 / arg2;
    // EXP
    static final FunctionalTwoArg<Double, Double, Double> exp = (engine, arg1, arg2) -> Math.pow(arg1, arg2);
    // LORENTZ
    static final FunctionalTwoArg<Coordinate, Object, Coordinate> lorentz = (engine, coord, obj) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Frame frame = Frame.promote(obj);
        if (obj == null) throw new ExecutionException("The '>' operator expects a frame or an observer");

        return frame.toFrame(coord);
    };
    // INV_LORENTZ
    static final FunctionalTwoArg<Coordinate, Object, Coordinate> invLorentz = (engine, coord, obj) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Frame frame = Frame.promote(obj);
        if (obj == null) throw new ExecutionException("The '>' operator expects a frame or an observer");
        return frame.toRest(coord);
    };

    // ****************************************
    // * Supporting Object Creation
    // ****************************************

    // W_INITIALIZER
    static final FunctionalThreeArg<Coordinate, Double, Double, WInitializer> wInitializer = (engine, coord, tau, d) -> new WInitializer(coord, tau, d);
    // W_SEGMENT
    static final FunctionalFourArg<Double, Double, WorldlineSegment.LimitType, Double, WSegment> wSegment = (engine, v, a, limitType, delta) -> {
        if (v <= -1.0 || v >= 1.0) throw new ExecutionException("The velocity must be within -1 and +1.");

         // Convert user acceleration (in g's) to the correct value ((ly/y^2) * user units)

        a =     a * 1.032295276 * engine.getSetStatement().getUnits();
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
                throw new ProgrammingException("Trying to create a PropertyList from elements that are not PropertyElements");
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
        if (v <= -1.0 || v >= 1.0) throw new ExecutionException("The velocity must be within -1 and +1.");
        return new Frame(origin, v);
    };
    // OBSERVER_FRAME
    static final FunctionalThreeArg<Observer, Frame.AtType, Double, Frame> observerFrame = (engine, observer, atType, atValue) -> {
        if (atType == Frame.AtType.V && (atValue <= -1.0 || atValue >= 1.0)) {
            throw new ExecutionException("The velocity must be within -1 and +1.");
        }

        return new Frame(observer, atType, atValue);
    };
    // AXIS_LINE
    static final FunctionalThreeArg<Line.AxisType, Object, Double, Line> axisLine = (engine, type, obj, offset) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Frame frame = Frame.promote(obj);
        if (obj == null) throw new ExecutionException("The line object expects a frame or an observer");
        return new ConcreteLine(type, frame, offset);
    };
    // ANGLE_LINE
    static final FunctionalTwoArg<Double, Coordinate, Line> angleLine = (engine, angle, coord) -> new ConcreteLine(angle, coord);
    // ENDPOINT_LINE
    static final FunctionalTwoArg<Coordinate, Coordinate, Line> endpointLine = (engine, coord1, coord2) -> new ConcreteLine(coord1, coord2);
    // PATH
    static final VariableArg<Path> path = (engine, data) -> {
        int numOfCoordinates = data.length;
        ArrayList<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < numOfCoordinates; i++) {
            if (!(data[i] instanceof Coordinate)) {
                throw new ProgrammingException("Trying to create a Path from elements that are not all Coordinates");
            }
            coords.add((Coordinate)data[i]);
        }
        return new Path(coords);
    };
    // BOUNDS
    static final FunctionalTwoArg<Coordinate, Coordinate, Bounds> bounds = (engine, min, max) -> new Bounds(min, max);
    // INTERVAL
    static final FunctionalThreeArg<Interval.Type, Double, Double, Interval> interval = (engine, type, min, max) -> new Interval(type, min, max);
    // STYLE
    static final FunctionalOneArg<PropertyList, Style> style = (engine, properties) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Style style = new Style();
        style.add(properties);
        return style;
    };
    // COORDINATE
    static final FunctionalTwoArg<Double, Double, Coordinate> coordinate = (engine, x, t) -> new Coordinate(x, t);

    // ****************************************
    // * COMMANDS
    // ****************************************

    static final FunctionalTwoArgNoRet<PropertyList, String> command = (engine, properties, name) -> {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Command command = CommandFactory.createCommand(engine, name, properties);
        engine.addCommand(command);
    };
    static {
        map.put(Type.ENABLE, enable);
        map.put(Type.PRINT, print);
        map.put(Type.SET_STYLE, setStyle);

        map.put(Type.FETCH, fetch);
        map.put(Type.FETCH_PROP, fetchProp);
        map.put(Type.FETCH_ADDRESS, fetchAddress);
        map.put(Type.FETCH_PROP_ADDRESS, fetchPropAddress);

        map.put(Type.UNARY_MINUS, unaryMinus);
        map.put(Type.UNARY_PLUS, unaryPlus);
        map.put(Type.SUB, sub);
        map.put(Type.MULT, mult);
        map.put(Type.DIV, div);
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
