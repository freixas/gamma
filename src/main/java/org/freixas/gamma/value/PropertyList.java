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
package org.freixas.gamma.value;

import org.freixas.gamma.ProgrammingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains lists of properties.
 *
 * @author Antonio Freixas
 */
public class PropertyList implements PropertyElement, ExecutionMutable
{
    private final ArrayList<Property> properties;
    private final HashMap<String, Integer> index;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create an empty property list.
     */
    public PropertyList()
    {
        this.properties = new ArrayList<>();
        this.index = new HashMap<>();
    }

    // **********************************************************************
    // *
    // * Getters/Setters
    // *
    // **********************************************************************

    @SuppressWarnings("unused")
    public boolean hasProperty(String name)
    {
        return index.containsKey(name);
    }

    public Property getProperty(int index)
    {
        return properties.get(index);
    }

    /**
     * Get the property's value.This will return null if the property does not
     * exist, although null might be a valid value for the property. To avoid
     * this problem, call hasProperty() first.
     *
     * @param name The name of the property to get.
     * @return The property's value.
     */
    @SuppressWarnings("unused")
    public Object getProperty(String name)
    {
        Integer i = index.get(name);
        if (i == null) return null;
        return properties.get(i).getValue();
    }

    /**
     * Add a property element to the end of the property list. If the
     * element is a property list, add all the properties in that list to this
     * list.
     * <p>
     * If the element is or contains a property that is already in the list,
     * the existing element is removed and the new one is added at the end.
     *
     * @param element The element to add.
     */
    public void add(PropertyElement element)
    {
        if (element instanceof Property property) {
            addProperty(property);
        }
        else if (element instanceof PropertyList propertyList) {
            addList(propertyList);
        }
    }

    protected void addProperty(Property property)
    {
        String name = property.getName();

        // If the property exists, remove it

        Integer i = index.get(name);
        if (i != null) {
            index.remove(name);
            properties.remove((int)i);
        }

        // Add the property at the end of the list

        index.put(property.getName(), properties.size());
        properties.add(property);
    }

    protected void addList(PropertyList list)
    {
        for (Property property : list.properties) {
            addProperty(property);
        }
    }

    /**
     * Get the number of properties in the container.
     *
     * @return The number of properties in the container.
     */
    public int size()
    {
        return properties.size();
    }

    // **********************************************************************
    // *
    // * ExecutionMutable Support
    // *
    // **********************************************************************

    @Override
    public PropertyList createCopy()
    {
        try {
            Class<?> cls = this.getClass();
            Constructor<?> constructor = cls.getConstructor();
            PropertyList newPropertyList = (PropertyList)constructor.newInstance();

            for (int i = 0; i < size(); i++) {
                PropertyElement element = getProperty(i);
                newPropertyList.add((PropertyElement)ExecutionMutableSupport.copy(element));
            }
            return newPropertyList;
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new ProgrammingException("PropertyList.createCopy()", e);
        }
    }

}
