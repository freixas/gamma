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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 *
 * @author Antonio Freixas
 */
public class PropertyList implements PropertyElement, ExecutionMutable
{
    private final ArrayList<Property> properties;
    private final HashMap<String, Integer> index;

    /**
     * Create an empty property list.
     */
    public PropertyList()
    {
        this.properties = new ArrayList<>();
        this.index = new HashMap<>();
    }

    @Override
    public Object createCopy()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
     * @param element
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
        ListIterator<Property> iter = list.properties.listIterator();
        while (iter.hasNext()) {
            addProperty(iter.next());
        }
    }

    /**
     * Get the number of properties in the container.
     *
     * @return
     */
    public int size()
    {
        return properties.size();
    }

}
