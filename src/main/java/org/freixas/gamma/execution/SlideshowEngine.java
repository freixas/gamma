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

public class SlideshowEngine
{
    enum State {
        RUNNING, PAUSED
    }

    private final MainWindow window;
    private final Slideshow slideshow;
    private final boolean isEmpty;
    private final int lastSlideIndex;

    private int curSlideIndex;
    private final State state;

    public SlideshowEngine(MainWindow window, Slideshow slideshow)
    {
        this.window = window;
        this.slideshow = slideshow;
        this.isEmpty = slideshow.size() == 0;
        this.lastSlideIndex = slideshow.size() - 1;

        this.curSlideIndex = 0;
        window.setSlideshowEngine(this);

        this.state = slideshow.isAutoPlay() ? State.RUNNING : State.PAUSED;
    }

    public void execute()
    {
        firstSlide();
    }

    public void firstSlide()
    {
        if (!isEmpty && curSlideIndex != 0) {
            curSlideIndex = 0;
            slideshow.showSlide(window, curSlideIndex);
        }
    }

    public void lastSlide()
    {
        if (!isEmpty &&curSlideIndex != lastSlideIndex) {
            curSlideIndex = lastSlideIndex;
            slideshow.showSlide(window, curSlideIndex);
        }
    }

    public void prevSlide()
    {
        if (curSlideIndex > 0) {
            curSlideIndex--;
            slideshow.showSlide(window, curSlideIndex);
        }
    }

    public void nextSlide()
    {
        if (curSlideIndex < lastSlideIndex) {
            curSlideIndex++;
            slideshow.showSlide(window, curSlideIndex);
        }
    }

    private void setCurSlide()
    {
        Slideshow.Slide slide = slideshow.getSlide(curSlideIndex);
        if (state == State.RUNNING) {
            double pause = slide.getPause();
            if (pause > 0 && !Double.isInfinite(pause)) {
                // Start timer
            }
        }
    }

    /**
     * This is called after a slide has been displayed.
     * <ul>
     *     <li>The slide is static and has been drawn.
     *     <li>The slide is interactive and not animated and has been drawn.
     *     <li>The slide is animated and the last slide has been drawn.
     *  </ul>
     */
    public void slideDone()
    {

    }

    /**
     * This is called each time the user interacts with the slide:
     * <ul>
     *     <li>The user zooms or pans.
     *     <li>The user reloads the slide.
     *     <li>The user manipulates a GUI control.
     * </ul>
     */
    public void userInteraction()
    {

    }

}
