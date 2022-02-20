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

import javafx.util.Pair;
import org.freixas.gamma.css.parser.CSSParser;
import org.freixas.gamma.execution.ExecutionException;
import org.freixas.gamma.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The stylesheet, a list of Rules, each containing selectors and style properties,
 * that are matched with script commands to define the appearance of the things
 * that are drawn in the diagram.
 * <p>
 * Stylesheets are modelled after CSS (Cascading Style Sheets) from HTML.
 * 
 * @author Antonio Freixas
 */
public final class Stylesheet
{
    /**
     * The default stylesheet (as a string), the stylesheet with the lowest precedence.
     */
    static final String DEFAULT_STYLESHEET_STRING =
        "grid { color: #CCC; } hypergrid { color: #3D3; } worldline { color: #00F; } axes { font-size: 14; tick-font-size: 10; }";

    /**
     * The default stylesheet (as a Stylesheet), the stylesheet with the lowest precedence.
     */
    static public Stylesheet DEFAULT_STYLESHEET;
    static {
        try {
            DEFAULT_STYLESHEET = Stylesheet.createStylesheet(null, DEFAULT_STYLESHEET_STRING);
        }
        catch (ParseException ignored) { }
    }

    /**
     * The user's default stylesheet, next in precedence to the default stylesheet.
     */
    static public Stylesheet USER_STYLESHEET = null;

    // Used to compare match scores

    static private final Comparator<Pair<Integer, Rule>> scoreComparator = Comparator.comparingInt(Pair::getKey);

    // Patterns used to match stylesheet IDs and classes

    static private final Pattern ID_PATTERN = Pattern.compile("^[-a-zA-Z_][-a-zA-Z_0-9]*$");
    static private final Pattern CLASS_PATTERN = Pattern.compile("^([-a-zA-Z_][-a-zA-Z_0-9]*)(\\s*,\\s*[-a-zA-Z_][-a-zA-Z_0-9]*)*$");

    // A stylesheet is a list of Rules.

    private final ArrayList<Rule> rules;

    // When a stylesheet is matched to a command, we wind up with another
    // stylesheet. We cache and re-use these stylesheets when we can.

    private boolean cacheEnabled;
    private HashMap<String, StyleStruct>styleStructCache;
    private final HashMap<String, Stylesheet>stylesheetCache;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a new, empty stylesheet.
     */
    public Stylesheet()
    {
        rules = new ArrayList<>();
        cacheEnabled = false;
        styleStructCache = null;

        // We are going to take a chance and always cache stylesheets.
        // This could pose a problem if a user changes the id's or classes a
        // lot. It's not a problem if they change style property

        stylesheetCache = new HashMap<>();
    }

    // **********************************************************************
    // *
    // * Factory methods
    // *
    // **********************************************************************

    /**
     * Create a stylesheet from a file presumably containing valid styles.
     *
     * @param cssFile If null, this is equivalent to new Stylesheet(). Otherwise,
     * it is a file containing a styles.
     *
     * @return The created stylesheet.
     *
     * @throws StyleException If the styles have a syntax error.
     * @throws IOException If there is a problem reading the file.
     */
    static public Stylesheet createStylesheet(File cssFile) throws IOException, ParseException, StyleException
    {
        if (cssFile == null) return new Stylesheet();

        if (!cssFile.exists()) {
            throw new StyleException("File '" + cssFile + "' does not exist.");
        }
        if (cssFile.isDirectory()) {
            throw new StyleException("File '" + cssFile + "' is a directory.");
        }
        String css = Files.readString(cssFile.toPath());

        return createStylesheet(cssFile, css);
    }

    /**
     * Create a stylesheet from a string presumably containing valid styles.
     *
     * @param cssFile The parent file from which the styles were read, if any.
     * It may be null if the styles originated in a string.
     * @param css The string containing the styles.
     *
     * @return The created stylesheet.
     */
    static public Stylesheet createStylesheet(File cssFile, String css) throws ParseException
    {
        if (css == null || css.length() == 0) return new Stylesheet();

        CSSParser cssParser = new CSSParser(cssFile, css);
        return cssParser.parse();
    }

    // **********************************************************************
    // *
    // * Getters / Setters
    // *
    // **********************************************************************

    @SuppressWarnings("unused")
    public boolean isCacheEnabled()
    {
        return cacheEnabled;
    }

    /**
     * Enable or disable the cache.
     *
     * @param cacheEnabled If true, the cache is enabled.
     */
    public void setCacheEnabled(boolean cacheEnabled)
    {
        this.cacheEnabled = cacheEnabled;
        if (cacheEnabled && styleStructCache == null) {
            styleStructCache = new HashMap<>();
        }
    }

    // **********************************************************************
    // *
    // * Add to the stylesheet
    // *
    // **********************************************************************

    /**
     * Add a Rule to the stylesheet.
     *
     * @param rule The Rule to add.
     */
    public void addRule(Rule rule)
    {
        rules.add(rule);
    }

    /**
     * Merge two stylesheets. The other stylesheet's rules are always added before
     * all the rules in this stylesheet.
     *
     * @param other The other stylesheet.
     */
    public void prefixStylesheet(Stylesheet other)
    {
        if (other == null) return;
        rules.addAll(0, other.rules);
    }

    /**
     * Merge two stylesheets. The other stylesheet's rules are always added after
     * all the rules in this stylesheet.
     *
     * @param other The other stylesheet.
     */
    public void addStylesheet(Stylesheet other)
    {
        if (other == null) return;
        rules.addAll(other.rules);
    }

    // **********************************************************************
    // *
    // * StyleStruct creation
    // *
    // **********************************************************************

    /**
     * Give the command name, and the id, class, and style properties, create
     * a StyleStruct for the command.
     *
     * @param file The file used for reporting parsing errors in the style property.
     * @param commandName The name of the command.
     * @param id The id string.
     * @param cls The class string.
     * @param style The style string.
     *
     * @return The created StyleStruct
     */
    public StyleStruct createStyleStruct(File file, String commandName, String id, String cls, String style)
    {
        // We have two caches:
        //
        // The stylesheet cache looks for a match with the command name, id (if
        // any), and class (if any).
        //
        // The StyleStruct cache looks for a match with the command name, id (if
        // any), class (if any), and style (if any)
        //
        // We check the StyleStruct cache first, because it's optimal

        String stylesheetCacheId =
            commandName +
            (id != null ? "$" + id : "") +
            (cls != null ? "$" + cls : "");

        String styleStructCacheId =  stylesheetCacheId +  (style != null ? "$" + style : "");

        if (styleStructCache != null) {
            StyleStruct styleStruct = styleStructCache.get(styleStructCacheId);
            if (styleStruct != null) {
                // System.err.println("Found styleStruct in cache using ID " + styleStructCacheId);
                return styleStruct;
            }
        }

        // Validate the id

        if (id != null) {
            id = id.trim();
            Matcher matcher = ID_PATTERN.matcher(id);
            if (!matcher.matches()) {
                throw new ExecutionException("Invalid id value for command " + commandName);
            }
        }

        // Validate the class string, split out the classes and sort them

        String[] classes = null;
        if (cls != null) {
            Matcher matcher = CLASS_PATTERN.matcher(cls.trim());
            if (matcher.matches()) {
                classes = cls.split("\\s*,\\s*");
            }
            else {
                throw new ExecutionException("Invalid class value for command " + commandName);
            }
            for (int i = 0; i < classes.length; i++) {
                classes[i] = classes[i].trim();
            }
            Arrays.sort(classes);
        }

        // Compile the styles

        try {
            // Create a new ordered stylesheet from all the matching rules
            // in this stylesheet

//            System.err.println("Creating a new stylesheet for matches for " +
//                (commandName != null ? commandName + " " : "") +
//                (id != null ? "#" + id + " " : "") +
//                (classes != null ? "." + String.join(", .", classes) : ""));

            // Check the stylesheet cache. We need to make sure we only cache
            // or retrieve a copy of the ordered stylesheet

            Stylesheet orderedStylesheet;
            Stylesheet cachedStylesheet = stylesheetCache.get(stylesheetCacheId);

            if (cachedStylesheet != null) {
                orderedStylesheet = new Stylesheet();
                orderedStylesheet.addStylesheet(cachedStylesheet);
//                System.err.println("Found stylesheet in cache using ID " + stylesheetCacheId);
            }
            else {
                orderedStylesheet = match(commandName, id, classes);
                cachedStylesheet = new Stylesheet();
                cachedStylesheet.addStylesheet(orderedStylesheet);
                stylesheetCache.put(stylesheetCacheId, cachedStylesheet);
            }

            if (style != null) {
                CSSParser cssParser = new CSSParser(file, "{" + style + ";}");
                Stylesheet localStylesheet = cssParser.parse();

                // Add the local stylesheet at the very bottom (the highest priority)

                orderedStylesheet.addStylesheet(localStylesheet);
            }

            // Apply all the rules

            StyleStruct styles = new StyleStruct();
            orderedStylesheet.setStyleStructValues(styles);

            // Save the styleStruct in the cache
            // We're counting on StyleStructs never being permanently changed,
            // so we don't need to save a copy, just a reference

            if (cacheEnabled) {
                styleStructCache.put(styleStructCacheId, styles);
            }
            return styles;
        }
        catch (ParseException e) {
            throw new ExecutionException(null, e);
        }
    }

    /**
     * Use the styles in each rule in this stylesheet to set the values in a
     * StyleStruct. Apply the values in order, from first to last.
     *
     * @param styles The StylesStruct to set.
     */
    public void setStyleStructValues(StyleStruct styles)
    {
        for (Rule rule : rules) {
            rule.setStyleStructValues(styles);
        }

        // Generate the fonts, if needed, at the very end

        StylePropertyDefinition.generateFonts(styles);
    }

    /**
     * Gather all of a command's matching rules into a new stylesheet, where
     * the rules are sorted by how well they match the command. Given two
     * rules with the same score, the one that appears later in this stylesheet
     * will appear later in the new stylesheet.
     *
     * @param commandName The name of the command. This should never be null.
     * @param id The command's id. This can be null.
     * @param classes An array of class names. This can be null. If not null,
     * the classes must be sorted in natural order.
     *
     * @return A stylesheet with all matching rules sorted appropriately.
     */
    public Stylesheet match(String commandName, String id, String[] classes)
    {
        LinkedList<Pair<Integer, Rule>> list = new LinkedList<>();

        for (Rule rule : rules) {
            int score = rule.getMatchScore(commandName, id, classes);
            if (score > -1) {
                addScoredRule(list, new Pair<>(score, rule));
            }
        }

        Stylesheet orderedSheet = new Stylesheet();
        for (Pair<Integer, Rule> integerRulePair : list) {
            orderedSheet.addRule(integerRulePair.getValue());
        }

        return orderedSheet;
    }

    /**
     * Add a rule to a linked list such that the rules remain in scored order,
     * with higher scores being closer to the end of the list.
     *
     * @param list A list of paired values.
     * @param pair A paired value, the first of which is the match score for
     * a rule and the second being the rule.
     */
    private void addScoredRule(LinkedList<Pair<Integer, Rule>> list, Pair<Integer, Rule> pair)
    {
        int index = Collections.binarySearch(list, pair, scoreComparator);

        // We found a match at the index. We need to add the new rule after all
        // elements with the same score

        if (index > -1) {
            while (index < list.size() && list.get(index).getKey() == (int)pair.getKey()) {
                index++;
            }
            list.add(index, pair);
        }

        // We didn't find a match, but we know where to insert the element

        else {
            index = -(index + 1);
            list.add(index, pair);
        }
    }

}
