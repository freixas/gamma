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
 *
 * @author Antonio Freixas
 */
public class Rule
{
    private final ArrayList<Selector> selectors;
    private final ArrayList<StyleProperty> properties;

    public Rule()
    {
        selectors = new ArrayList<>();
        properties = new ArrayList<>();
    }

    public ArrayList<Selector> getSelectors()
    {
        return selectors;
    }

    public ArrayList<StyleProperty> getProperties()
    {
        return properties;
    }

    public void addSelector(Selector selector)
    {
        selectors.add(selector);
    }

    public void addStyleProperty(StyleProperty styleProperty)
    {
        properties.add(styleProperty);
    }

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
        for (int i = 0; i < selectors.size(); i++) {
            int selectorScore = selectors.get(i).getMatchScore(commandName, id, classes);
            if (selectorScore > - 1) {
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
        Iterator<StyleProperty> iter = properties.iterator();
        while (iter.hasNext()) {
            iter.next().setStyleStructValues(styles);
        }
    }

}
