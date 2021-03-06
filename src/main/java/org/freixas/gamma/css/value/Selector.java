/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.css.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A stylesheet Selector. Selectors are parts of a Rule. These can be matched to
 * command names, IDs and classes to determine a match with a Rule.
 *
 * @author Antonio Freixas
 */
public final class Selector
{
    /**
     * This pattern is used to identify all possible command names. If a new
     * command is added, this string should be updated, so a future refactoring
     * should probably try to centralize the location of all command names
     */
    static private final Pattern COMMAND_NAME_PATTERN = Pattern.compile("^(display|frame|animation|axes|grid|hypergrid|event|line|worldline|path|label)$");

    private String commandName; // The command name in this selector
    private String id;          // The ID in this selector
    private String[] classes;   // The set of classes in this selector

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     *  Create a Selector from a name. The pattern for a selector is:
     *  <p>
     *  command-name # id . class1 . class2 ...
     *  <p>
     *  Each part is optional.
     *
     * @param name The selector name
     * @throws StyleException If a syntax error occurs.
     */
    public Selector(String name) throws StyleException
    {
        if (name == null || name.length() == 0) {
            throw new StyleException("Invalid stylesheet selector");
        }
        // We know the name is syntactically correct, but we don't know that it's
        // valid. We need to split it into its component parts

        commandName = null;
        id = null;
        ArrayList<String> cls = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        boolean isCmdName = true;
        boolean isId = false;
        boolean isClass = false;

        String originalName = name;
        name += '$';            // Add an EOF marker

        // The following algorithm relies on the selector name already having
        // been syntax-checked. It might still not be valid

        for (int i = 0; i < name.length(); i++ ) {
            char c = name.charAt(i);

            // Starting a new type

            if (c == '#' || c == '.' || c == '$') {
                String str = builder.toString();

                // Last item was a command name. There can only be one and, if
                // present, it will always be the first thing

                if (isCmdName && str.length() > 0) {
                    Matcher cmdMatcher = COMMAND_NAME_PATTERN.matcher(str);
                    if (cmdMatcher.matches()) {
                        commandName = str;
                    }
                    else {
                        throw new StyleException("Stylesheet selector '" + originalName +  "'contains an invalid command name");
                    }
                }

                // Last item was an id. Only one id is allowed

                else if (isId) {
                    if (str.length() < 1) {
                        throw new StyleException("Stylesheet selector '" + originalName +  "'contains an invalid id name");
                    }
                    if (id == null) {
                        id = str;
                    }
                    else {
                        throw new StyleException("Stylesheet selector '" + originalName +  "'contains more than one id");
                    }
                }

                // Last item was a class. We can have as many as we want and
                // we're going to allow them to repeat

                else if (isClass) {
                    if(str.length() < 1) {
                        throw new StyleException("Stylesheet selector '" + originalName +  "'contains an invalid class name");
                    }
                    if (cls.contains(str)) continue;
                    cls.add(str);
                }

            }

            if (c == '#') {
                isId = true; isCmdName = isClass = false;
                builder = new StringBuilder();
            }

            else if (c == '.') {
                isClass = true; isCmdName = isId = false;
                builder = new StringBuilder();
            }

            else {
                builder.append(c);
            }
        }

        // If the class list is empty, make it null

        if (cls.isEmpty()) {
            classes = null;
        }

        // If the class list has more than one element, sort it

        else {
            if (cls.size() > 1) cls.sort(Comparator.naturalOrder());
            classes = new String[cls.size()];
            classes = cls.toArray(classes);
        }
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Get the command name part of the selector. Null if none.
     *
     * @return The command name part of the selector.
     */
    public String getCommandName()
    {
        return commandName;
    }

    /**
     * Get the id part of the selector. Null if none.
     *
     * @return The id part of the selector. Null if none.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Get the classes in the selector. Null if none.
     *
     * @return The classes in the selector.
     */
    public String[] getClasses()
    {
        return classes;
    }

    // **********************************************************************
    // *
    // * Features
    // *
    // **********************************************************************

    /**
     * Get the match score for this selector and a command's name, id, or
     * class list.
     *
     * @param commandName The name of the command. This should never be null.
     * @param id The command's id. This can be null.
     * @param classes An array of class names. This can be null. If not null,
     * the classes must be sorted in natural order.
     *
     * @return The match score. -1 means nothing matched.
     */
    public int getMatchScore(String commandName, String id, String[] classes)
    {
        int score = 0;

        // If the selector has a command name, they must match

        if (this.commandName != null) {
            if (this.commandName.equals(commandName)) {
                score += 1;
            }
            else {
                return -1;
            }
        }

        // If the selector has an id, it must match

        if (this.id != null) {
            if (this.id.equals(id)) {
                score += 1000;
            }
            else {
                return -1;
            }
        }

        // Every class in the selector must be matched by a class in the command's
        // class list.

        if (this.classes != null) {
            if (classes != null) {
                for (String cls : this.classes) {
                    int index = Arrays.binarySearch(classes, cls);
                    if (index < 0) return -1;
                    score += 10;
                }
            }
            else {
                return -1;
            }
        }

        // Since a selector will always have at least one command name, id or
        // class, we've either already returned a -1 or else we have our final
        // match score, which should always be > 0

        return score;
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        return "Selector { " +
            (commandName != null ? commandName + " " : "") +
            (id != null ? "#" + id + " " : "") +
            (classes != null ? "." + String.join(", .", classes) : "") +
        "}";
    }

}
