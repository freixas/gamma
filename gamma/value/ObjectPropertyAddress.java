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

/**
 * This class provides indirect access to property in an object.
 *
 * @author Antonio Freixas
 */
public class ObjectPropertyAddress extends Address
{
    private final ObjectContainer container;
    private final String name;

    public ObjectPropertyAddress(ObjectContainer container, String name)
    {
        this.container = container;
        this.name = name;
    }

    /**
     * Get the name of the symbol for which this object is an address.
     *
     * @return The name of the symbol for which this object is an address.
     */
    public String getName()
    {
        return name;
    }

    @Override
    public boolean exists()
    {
        return container.hasProperty(name);
    }

    @Override
    public Object getValue()
    {
        return container.getProperty(name);
    }

    @Override
    public void setValue(Object value)
    {
        container.setProperty(name, value);
    }
}
