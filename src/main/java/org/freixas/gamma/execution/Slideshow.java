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
import org.freixas.gamma.file.URLFile;
import org.freixas.gamma.parser.Parser;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

public class Slideshow
{
    // **********************************************************************
    // *
    // * Inner Classes
    // *
    // **********************************************************************

    static public final class Slide
    {
        private final URLFile urlFile;
        private final Parser parser;
        private final double pause;

        /**
         * Create a slide.
         *
         * @param urlFile The URLFile that contains the script.
         * @param parser The parser that has completed parsing the script that
         * represents this slide.
         * @param pause The number of seconds to pause after the last user
         * interaction with this slide before proceeding with the next.
         */
        public Slide(URLFile urlFile, Parser parser, double pause)
        {
            this.urlFile = urlFile;
            this.parser = parser;
            this.pause = pause;
        }

        /**
         * Get the URLFile that contains the script.
         *
         * @return The URLFile that contains the script.
         */
        public URLFile getUrlFile()
        {
            return urlFile;
        }

        /**
         * Get the parser that has completed parsing the script that represents
         * this slide.
         *
         * @return The parser that has completed parsing the script that
         * represents this slide.
         */
        public Parser getParser()
        {
            return parser;
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
    private final double defaultAnimationPause;
    private final double defaultInteractivePause;

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

    /**
     * Get the URLFile of the slideshow.
     *
     * @return The URLFile of the slideshow.
     */
    public URLFile getSlideshowURLFile()
    {
        return slideshowURLFile;
    }

    /**
     * Return true if autoplay is on. If enabled, the slideshow begins in
     * PLAY mode; otherwise, it is in PAUSE mode.
     *
     * @return True if autoplay is on.
     */
    public boolean isAutoPlay()
    {
        return autoPlay;
    }

    /**
     * Enable or disable autoplay. If enabled, the slideshow begins in
     * PLAY mode; otherwise, it is in PAUSE mode.
     *
     * @param autoPlay True if autoplay should be enabled.
     */
    public void setAutoPlay(boolean autoPlay)
    {
        this.autoPlay = autoPlay;
    }

    /**
     * Set the default diagram pause in seconds. This is the default amount of
     * time to pause after a non-animated, non-interactive diagram.
     *
     * @param defaultDiagramPause The default diagram pause time
     */
    public void setDefaultDiagramPause(double defaultDiagramPause)
    {
        this.defaultDiagramPause = defaultDiagramPause;
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
     * Add a slide to the end of the slideshow. If the slideshow is from a local
     * file, then slides can come from anywhere. If the slideshow is from a URL,
     * then all slides must come from the same domain.
     *
     * @param urlFile The URLFile that contains the script which will become the slide.
     * @param pause The number of seconds to pause after the last user
     * interaction with this slide before proceeding with the next. If it's
     * NaN, we will use a default value
     *
     * @throws Exception Thrown if there is a problem reading or parsing the
     * file.
     */
    public void addSlide(URLFile urlFile, double pause)  throws Exception
    {
        try {
            String script = urlFile.readString();
            Parser parser = new Parser(urlFile, script);
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
                slideshow.add(new Slide(urlFile, parser, pause));
            }
        }
        catch (Exception e) {
            Exception exception = e;
            if (exception instanceof NoSuchFileException) {
                exception = new Exception("File '" + urlFile.getPath() + "' does not exist.", exception);
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
        mainWindow.runSlide(slide);
    }
}
