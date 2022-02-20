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
import java.util.Iterator;

/**
 * A stylesheet Rule. A stylesheet contains a sequence of Rules, each of
 * which consist of a set of selectors and a sequence of style properties.
 *
 * @author Antonio Freixas
 */
public final class Rule
{
    /**
     * The list of selectors.
     */
    private final ArrayList<Selector> selectors;

    /**
     * The list of style properties
     */
    private final ArrayList<StyleProperty> properties;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create an empty rule.
     */
    public Rule()
    {
        selectors = new ArrayList<>();
        properties = new ArrayList<>();
    }

    // **********************************************************************
    // *
    // * Getters / Setters
    // *
    // **********************************************************************

    /**
     * Get the list of selectors.
     *
     * @return The list of selectors.
     */
    public ArrayList<Selector> getSelectors()
    {
        return selectors;
    }

    /**
     * Add a selector to the Rule.
     *
     * @param selector The selector to add.
     */
    public void addSelector(Selector selector)
    {
        selectors.add(selector);
    }

    /**
     * Get the list of style properties.
     *
     * @return The list of style properties.
     */
    public ArrayList<StyleProperty> getProperties()
    {
        return properties;
    }

    /**
     * Add a style property to the Rule.
     *
     * @param styleProperty The style property to add.
     */
    public void addStyleProperty(StyleProperty styleProperty)
    {
        properties.add(styleProperty);
    }

    // **********************************************************************
    // *
    // * Features
    // *
    // **********************************************************************

    /**
     * Get the score for the selector with the best match to a command's
     * name, id and class list.
     *
     * @param commandName The name of the command. This should never be null.
     * @param id The command's id. This can be null.
     * @param classes An array of class names. This can be null. If not null,
     * the classes must be sorted in natural order.
     *
     * @return The highest matching score. -1 means nothing matched.
     */
    public int getMatchScore(String commandName, String id, String[] classes)
    {
        // If this rule has no selectors, the rule matches with a score of 0

        if (selectors.isEmpty()) return 0;

        // Get the score for each selector and return the largest score. Note
        // that if there are no matches, the score remains -1.

        int score = -1;

        // System.err.println("Matching rule");
        for (Selector selector : selectors) {
            int selectorScore = selector.getMatchScore(commandName, id, classes);
            if (selectorScore > -1) {
                score = Math.max(score, selectorScore);
                // System.err.println("Score " + selectorScore + " for " + selectors.get(i));
            }
        }

        return score;
    }

    /**
     * Use the styles in this rule to set the values in a StyleStruct. Apply the
     * values in order, from first to last.
     *
     * @param styles The StylesStruct to set.
     */
    public void setStyleStructValues(StyleStruct styles)
    {
        for (StyleProperty property : properties) {
            property.setStyleStructValues(styles);
        }
    }

}
