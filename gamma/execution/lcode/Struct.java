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
package gamma.execution.lcode;

import gamma.value.Color;
import gamma.ProgrammingException;
import gamma.math.Util;
import gamma.value.Frame;
import gamma.value.Observer;
import gamma.value.Property;
import gamma.value.PropertyList;
import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;
import gamma.value.Displayable;
import gamma.value.ExecutionMutable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Antonio Freixas
 */
public abstract class Struct
{
    static final HashMap<String, HashMap<String, Field>> allFields = new HashMap<>();
    static final HashMap<String, HashMap<String, Method>> allMethods = new HashMap<>();

    static public final int INT_NOT_SET = -999999999;

    static
    {
        allFields.put("DisplayStruct", getFields(DisplayStruct.class));
        allMethods.put("DisplayStruct", getMethods(DisplayStruct.class));

        allFields.put("FrameStruct", getFields(FrameStruct.class));
        allMethods.put("FrameStruct", getMethods(FrameStruct.class));

        allFields.put("AnimationStruct", getFields(AnimationStruct.class));
        allMethods.put("AnimationStruct", getMethods(AnimationStruct.class));

        allFields.put("AxesStruct", getFields(AxesStruct.class));
        allMethods.put("AxesStruct", getMethods(AxesStruct.class));

        allFields.put("GridStruct", getFields(GridStruct.class));
        allMethods.put("GridStruct", getMethods(GridStruct.class));

        allFields.put("HypergridStruct", getFields(HypergridStruct.class));
        allMethods.put("HypergridStruct", getMethods(HypergridStruct.class));

        allFields.put("EventStruct", getFields(EventStruct.class));
        allMethods.put("EventStruct", getMethods(EventStruct.class));

        allFields.put("LineStruct", getFields(LineStruct.class));
        allMethods.put("LineStruct", getMethods(LineStruct.class));

        allFields.put("WorldlineStruct", getFields(WorldlineStruct.class));
        allMethods.put("WorldlineStruct", getMethods(WorldlineStruct.class));

        allFields.put("PathStruct", getFields(PathStruct.class));
        allMethods.put("PathStruct", getMethods(PathStruct.class));

        allFields.put("LabelStruct", getFields(LabelStruct.class));
        allMethods.put("LabelStruct", getMethods(LabelStruct.class));

        allFields.put("StyleStruct", getFields(StyleStruct.class));
        allMethods.put("StyleStruct", getMethods(StyleStruct.class));
    }

    /**
     * Finalize any settings. Some values have defaults that depend on other
     * values, so this is called after everything has been set by the user.
     * If a class has a flexible default, it should override this class to
     * set it.
     */
    public void finalizeValues()
    {
        // Do nothing by default
    }

    /**
     * Make any coordinates, velocities, angles, distances, and durations
     * relative to the given frame instead of the rest frame.
     *
     * @param prime The frame to which things should be relative.
     */
    abstract public void relativeTo(Frame prime);

    /**
     * Get all the fields in a Struct subclass and put them into a HashMap,
     * indexed by the field name.
     *
     * @param cls The class of the Struct subclass.
     * @return The fields in a HashMap.
     */
    private static HashMap<String, Field> getFields(Class cls)
    {
        Field[] fields = cls.getFields();
        HashMap<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    /**
     * Get all the methods in a Struct subclass and put them into a HashMap,
     * indexed by the method name.
     *
     * @param cls The class of the Struct subclass.
     * @return The methods in a HashMap.
     */
    private static HashMap<String, Method> getMethods(Class cls)
    {
        Method[] methods = cls.getMethods();
        HashMap<String, Method> methodMap = new HashMap<>();
        for (Method method : methods) {
            methodMap.put(method.getName(), method);
        }
        return methodMap;
    }

    /**
     * Generic range checking for integer fields.This can be used as a helper
     * method for implementing range checks in subclasses.
     *
     * @param propertyName The name of the property being checked.
     * @param value The property's value.
     * @param min The allowed minimum (Double.NEGATIVE_INFINITY is allowed).
     * @param max The allowed maximum (Double.POSITIVE_INFINITY is allowed).
     */
    protected void rangeCheck(String propertyName, int value, int min, int max)
    {
        if (value < min || value > max) {
            throw new ExecutionException("The value used for property '" + propertyName + "' is out of range");
        }
    }

    /**
     * Generic range checking for double fields.This can be used as a helper
     * method for implementing range checks in subclasses.
     *
     * @param propertyName The name of the property being checked.
     * @param value The property's value.
     * @param min The allowed minimum (Double.NEGATIVE_INFINITY is allowed).
     * @param max The allowed maximum (Double.POSITIVE_INFINITY is allowed).
     */
    protected void rangeCheck(String propertyName, double value, double min, double max)
    {
        if (value < min || value > max) {
            throw new ExecutionException("The value used for property '" + propertyName + "' is out of range");
        }
    }

   /**
     * Create a new structure, one that is a subclass of Struct, and initialize
     * the structure using the values in the property list.
     * <p>
     * The structure should be defined with public fields all set to their
     * default values. Fields that have no default should be matched with a
     * boolean field with the same name followed by "Set". This field should be
     * set to false. After initialization, they will all be set to true.
     * <p>
     * This method should not be used for creating the style structure,
     * StyleStruct. The style structure should be a copy of the the master style
     * structure, which is held by the hcode engine and then initialized from
     * the property list using initializeStruct. *
     *
     * @param cmdName The name of the command.
     * @param list The property list used to initialize the structure.
     *
     * @return
     */
    public static Struct createNewStruct(HCodeEngine engine, String cmdName, PropertyList list)
    {
        try {

            // Get the Struct class for this command

            String capCmdName = cmdName.substring(0, 1).toUpperCase() + cmdName.substring(1);
            String cmdStructName = capCmdName + "Struct";
            Class<?> cmdStructClass = Class.forName("gamma.execution.lcode." + cmdStructName);

            // Call the constructor to create a new instance

            Constructor cmdStructConstructor = cmdStructClass.getConstructor();
            Struct cmdStruct = (Struct)cmdStructConstructor.newInstance();

            initializeStruct(engine, cmdStruct, cmdName, list);

            return cmdStruct;
        }
        catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new ProgrammingException("Struct.createNewStruct()", e);
        }
    }

    /**
     * Initialize a structure, one that is a subclass of Struct, using values in
     * the property list.
     *
     * @param cmdStruct The structure to initialize.
     * @param cmdName The name of the command.
     * @param list The property list used to initialize the structure.
     */
    public static void initializeStruct(HCodeEngine engine, Struct cmdStruct, String cmdName, PropertyList list)
    {
        try {
            String capCmdName = cmdName.substring(0, 1).toUpperCase() + cmdName.substring(1);
            String cmdStructName = capCmdName + "Struct";

            // Get all the command struct's fields and methods

            HashMap<String, Field> fieldMap = allFields.get(cmdStructName);
            HashMap<String, Method> methodMap = allMethods.get(cmdStructName);

            // Get all the style struct's fields

            HashMap<String, Field> styleFieldMap = allFields.get("StyleStruct");

            // Go through the property list

            for (int i = 0; i < list.size(); i++) {
                Property property = list.getProperty(i);
                String propertyName = property.getName();

                // This is a command property, set it

                if (fieldMap.containsKey(propertyName)) {
                    Field field = fieldMap.get(propertyName);
                    setValue(engine, cmdStruct, fieldMap, field, property, methodMap);
                }

                // This is not a command property or a style property
                // If the command is "style", ignore checks as we will have properties for
                // the command being passed in.
                //
                // The only way to avoid this would be to remove cmd properties
                // before working on the style. Then if any properties were left,
                // we would know there were some inappropriate properties given

                else if (!cmdName.equals("style") && !styleFieldMap.containsKey(propertyName)) {
                    throw new ExecutionException("Invalid property '" + propertyName + "' for command '" + cmdName + "'");
                }
            }

            // Check for missing required properties
            // We have a missing property when a field ending with "Set" is false

            Set<String> fieldNames = fieldMap.keySet();
            Iterator<String> iter = fieldNames.iterator();
            while (iter.hasNext()) {
                String fieldName = iter.next();
                if (fieldName.matches(".*Set$")) {
                    Field field = fieldMap.get(fieldName);
                    if (!field.getBoolean(cmdStruct)) {
                        String realFieldName = fieldName.substring(0, fieldName.length() - 3);
                        throw new ExecutionException("Required property '" + realFieldName + "' is missing");
                    }
                }
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ProgrammingException("Struct.initializeStruct()", e);
        }
    }

    private static void setValue(
        HCodeEngine engine,
        Object instance,
        HashMap<String, Field> fieldMap, Field field,
        Property property,
        HashMap<String, Method> methodMap)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        String propertyName = property.getName();
        Object propertyValue = property.getValue();

        // The Field and Property are of the same type

        if (propertyValue.getClass() == field.getType()) {

            // If the property value can be changed, we need to store a copy

            if (propertyValue instanceof ExecutionMutable executionMutable) {
                field.set(instance, executionMutable.createCopy());
            }

            // If it is immutable (or a primitive), we can store a reference

            else {
                field.set(instance, propertyValue);
            }
        }

        // Field is a string
        // Property value can be a Double or any Displayable

        else if (field.getType() == String.class) {
            if (propertyValue instanceof Double dbl) {
                field.set(instance, engine.toDisplayableString(dbl));
            }
            else if (propertyValue instanceof Displayable displayable) {
                field.set(instance, displayable.toDisplayableString(engine));
            }
            else {
                throw new ExecutionException("Property " + propertyName + "'s value is of the wrong type");
            }
        }

        // Property value is a Double
        // Field can be a double, int, color, or boolean

        else if (propertyValue instanceof Double dbl) {
            if (field.getType() == double.class) {
                field.setDouble(instance, dbl);
            }
            else if (field.getType() == int.class) {
                field.setInt(instance, Util.toInt(dbl));
            }
            else if (field.getType() == Color.class) {
                field.set(instance, new Color(dbl));
            }
            else if (field.getType() == boolean.class) {
                field.setBoolean(instance, dbl != 0);
            }
            else {
                throw new ExecutionException("Property " + propertyName + "'s value is of the wrong type");
            }
        }

        // Property is an Observer
        // Field can be a Frame

        else if (propertyValue instanceof Observer observer) {
            if (field.getType() == Frame.class) {
                field.set(instance, new Frame(observer));
            }
            else {
                throw new ExecutionException("Property " + propertyName + "'s value is of the wrong type");
            }
        }

        else {
           throw new ExecutionException("Property " + propertyName + "'s value is of the wrong type");
        }

        // Check to see if this is a required field
        // If so, mark it off as having been set

        String fieldSetName = propertyName + "Set";
        if (fieldMap.containsKey(fieldSetName)) {
            Field fieldSet = fieldMap.get(fieldSetName);
            fieldSet.setBoolean(instance, true);
        }

        // For fields of type Integer, Double, we may have to handle
        // min/max values. Look for methods with the same name as the property
        // and ending with "CheckRange".

        String methodRangeCheckName = propertyName + "RangeCheck";
        if (methodMap.containsKey(methodRangeCheckName)) {
            Method methodRangeCheck = methodMap.get(methodRangeCheckName);
            methodRangeCheck.invoke(instance);
        }
    }
}
