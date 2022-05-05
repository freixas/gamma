/*
 * Copyright (c) 2022 Antonio Freixas
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
package org.freixas.gamma.execution;

import org.freixas.gamma.MainWindow;
import org.freixas.gamma.css.value.Stylesheet;
import org.freixas.gamma.execution.hcode.SetStatement;
import org.freixas.gamma.file.URLFile;
import org.freixas.gamma.parser.Parser;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Slideshow
{
    // **********************************************************************
    // *
    // * Inner Classes
    // *
    // **********************************************************************

    public final class Slide
    {
        private final String name;
        private final URLFile URLFile;
        private final LinkedList<Object> hCodes;
        private final boolean isAnimated;
        private final SetStatement setStatement;
        private final Stylesheet stylesheet;

        private double pause;

        /**
         * Create a slide.
         *
         * @param parser The parser that has completed parsing the script that
         * represents this slide.
         * @param name The name of the script.
         * @param pause The number of seconds to pause after the last user
         * interaction with this slide before proceeding with the next.
         */
        public Slide(Parser parser, String name, double pause)
        {
            this.name = name;
            this.URLFile = parser.getScriptURL();
            this.hCodes = parser.getHCodes();
            this.isAnimated = parser.isAnimated();
            this.setStatement = parser.getSetStatement();
            this.stylesheet = parser.getStylesheet();

            this.pause = pause;
        }

        /**
         * Get the number of seconds to pause after the last user interaction
         * with this slide before proceeding with the next.
         *
         * @return The number of seconds to pause after the last user interaction
         * with this slide.
         */

        public double getPause()
        {
            return pause;
        }
    }

    // **********************************************************************
    // *
    // * Main Class
    // *
    // **********************************************************************

    private final URLFile slideshowURLFile;
    private final ArrayList<Slide> slideshow;

    private boolean autoPlay;
    private double defaultDiagramPause;
    private double defaultAnimationPause;
    private double defaultInteractivePause;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    public Slideshow(URLFile slideshowURLFile)
    {
        this.slideshowURLFile = slideshowURLFile;
        this.slideshow = new ArrayList<>();

        this.autoPlay = false;
        this.defaultDiagramPause = 5.0;
        this.defaultAnimationPause = 0.0;
        this.defaultInteractivePause = Double.POSITIVE_INFINITY;
    }

    // **********************************************************************
    // *
    // * Getters and Setters
    // *
    // **********************************************************************

    public boolean isAutoPlay()
    {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay)
    {
        this.autoPlay = autoPlay;
    }

    public double getDefaultDiagramPause()
    {
        return defaultDiagramPause;
    }

    public void setDefaultDiagramPause(double defaultDiagramPause)
    {
        this.defaultDiagramPause = defaultDiagramPause;
    }

    public double getDefaultAnimationPause()
    {
        return defaultAnimationPause;
    }

    public void setDefaultAnimationPause(double defaultAnimationPause)
    {
        this.defaultAnimationPause = defaultAnimationPause;
    }

    public double getDefaultInteractivePause()
    {
        return defaultInteractivePause;
    }

    public void setDefaultInteractivePause(double defaultInteractivePause)
    {
        this.defaultInteractivePause = defaultInteractivePause;
    }

    /**
     * Get the number of slides in this slideshow.
     *
     * @return The number of slides in this slideshow.
     */
    public int size()
    {
        return slideshow.size();
    }

    /**
     * Get the slide at the given index.
     *
     * @param index The index of the slide to get.
     *
     * @return The slide at the indexed position.
     */
    public Slide getSlide(int index)
    {
        return slideshow.get(index);
    }

    /**
     * Get the entire collection of slides.
     *
     * @return The entire collection of slides.
     */
    public ArrayList<Slide> getAllSlides()
    {
        return slideshow;
    }

    /**
     * Add a slide to the end of the slideshow/
     *
     * @param name The name of the script file which will become the slide.
     * @param pause The number of seconds to pause after the last user
     * interaction with this slide before proceeding with the next. If it's
     * NaN, we will use a default value
     *
     * @throws Exception Thrown if there is a problem reading or parsing the
     * file.
     */
    public void addSlide(String name, double pause)  throws Exception
    {
        try {
            URLFile URLFile = slideshowURLFile.getDependentScriptURL(name);
            String script = URLFile.readString();
            Parser parser = new Parser(URLFile, script);
            parser.parse();

            if (parser.isSlideshow()) {
                slideshow.addAll(parser.getSlideshow().getAllSlides());
            }
            else {
                if (Double.isNaN(pause)) {
                    pause = defaultDiagramPause;
                    if (parser.isAnimated()) pause = defaultAnimationPause;
                    if (parser.hasDisplayVariables()) pause = defaultInteractivePause;
                }

                slideshow.add(new Slide(parser, name, pause));
            }
        }
        catch (Exception e) {
            Exception exception = e;
            if (exception instanceof NoSuchFileException) {
                exception = new Exception("File '" + name + "' does not exist.", exception);
            }
            throw exception;
        }

    }

    /**
     * Show a slide.
     *
     * @param mainWindow The window in which the slide will be shown.
     * @param index The index of the slide to show.
     */
    public void showSlide(MainWindow mainWindow, int index)
    {
        Slide slide = getSlide(index);
        DiagramEngine engine = new DiagramEngine(mainWindow, slide.hCodes, slide.isAnimated, slide.setStatement, slide.stylesheet);
        engine.execute();
    }
}
