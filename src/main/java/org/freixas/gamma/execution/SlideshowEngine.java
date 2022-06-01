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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.freixas.gamma.MainWindow;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowEngine
{
    enum State {
        PLAYING, PAUSED
    }


    private final MainWindow window;
    private final Slideshow slideshow;
    private final boolean isEmpty;
    private final int lastSlideIndex;

    private int curSlideIndex;
    private Timer timer;
    private State state;

    private final Button buttonSlideshowStart;
    private final Button buttonSlideshowEnd;
    private final Button buttonSlideshowPrevious;
    private final Button buttonSlideshowNext;
    private final Button buttonSlideshowPlayPause;

    private final ImageView playImage;
    private final ImageView pauseImage;

    EventHandler<ActionEvent> ssStartEventHandler;
    EventHandler<ActionEvent> ssEndEventHandler;
    EventHandler<ActionEvent> ssPreviousEventHandler;
    EventHandler<ActionEvent> ssNextEventHandler;
    EventHandler<ActionEvent> ssPlayPauseEventHandler;

    public SlideshowEngine(MainWindow window, Slideshow slideshow)
    {
        this.window = window;
        this.slideshow = slideshow;
        this.isEmpty = slideshow.size() == 0;
        this.lastSlideIndex = slideshow.size() - 1;

        this.curSlideIndex = -1;
        window.setSlideshowEngine(this);

        this.timer = null;
        this.state = slideshow.isAutoPlay() ? State.PLAYING : State.PAUSED;

        // Get the slideshow controls

        buttonSlideshowStart     = (Button)window.getScene().lookup("#toolbar-slideshow-start");
        buttonSlideshowEnd       = (Button)window.getScene().lookup("#toolbar-slideshow-end");
        buttonSlideshowPrevious  = (Button)window.getScene().lookup("#toolbar-slideshow-previous");
        buttonSlideshowNext      = (Button)window.getScene().lookup("#toolbar-slideshow-next");
        buttonSlideshowPlayPause = (Button)window.getScene().lookup("#toolbar-slideshow-play-pause");

        InputStream playIS = window.getClass().getResourceAsStream("/ss_play.png");
        InputStream stopIS = window.getClass().getResourceAsStream("/ss_pause.png");

        playImage  = playIS == null ? null : new ImageView(new Image(playIS));
        pauseImage = stopIS == null ? null : new ImageView(new Image(stopIS));
        if (playImage != null) {
            playImage.setFitHeight(16);
            playImage.setFitWidth(16);
        }
        if (pauseImage != null) {
            pauseImage.setFitHeight(16);
            pauseImage.setFitWidth(16);
        }

        // Set up our change listeners

        addListeners();

        // Update the state of the buttons

        updateButtonStates();
    }

    /**
     * Add all the listeners needed by the animation engine. Every listener
     * added must be removed when this engine is closed.
     */
    private void addListeners()
    {
        // ************************************************************
        // *
        // * BUTTON HANDLERS
        // *
        // ************************************************************

        ssStartEventHandler = event -> firstSlide();
        buttonSlideshowStart.addEventHandler(ActionEvent.ANY, ssStartEventHandler);

        ssEndEventHandler = event -> lastSlide();
        buttonSlideshowEnd.addEventHandler(ActionEvent.ANY, ssEndEventHandler);

        ssPreviousEventHandler = event -> previousSlide();
        buttonSlideshowPrevious.addEventHandler(ActionEvent.ANY, ssPreviousEventHandler);

        ssNextEventHandler = event -> nextSlide();
        buttonSlideshowNext.addEventHandler(ActionEvent.ANY, ssNextEventHandler);

        ssPlayPauseEventHandler = event -> togglePlay();
        buttonSlideshowPlayPause.addEventHandler(ActionEvent.ANY, ssPlayPauseEventHandler);
    }

    /**
     * Update which buttons are enabled and whether the play/pause button
     * displays play or pause.
     */
    private void updateButtonStates()
    {
        boolean atStart = curSlideIndex == 0 || isEmpty;
        boolean atEnd = curSlideIndex == lastSlideIndex || isEmpty;

        buttonSlideshowStart.setDisable(atStart);
        buttonSlideshowPrevious.setDisable(atStart);

        buttonSlideshowEnd.setDisable(atEnd);
        buttonSlideshowNext.setDisable(atEnd);

        buttonSlideshowPlayPause.setDisable(isEmpty);

        if (state == State.PLAYING) {
            if (pauseImage != null) buttonSlideshowPlayPause.setGraphic(pauseImage);
        }
        else if (state == State.PAUSED) {
            if (playImage != null) buttonSlideshowPlayPause.setGraphic(playImage);
        }
    }

    /**
     * Start the execution of the slideshow by running the first slide.
     */
    public void execute()
    {
        firstSlide();
    }

    /**
     * Get the current slide. If the slideshow is empty, this returns null.
     *
     * @return The current slide.
     */
    public Slideshow.Slide getCurrentSlide()
    {
        if (isEmpty) return null;
        return slideshow.getSlide(curSlideIndex);
    }

    /**
     * Display the first slide.
     */
    private void firstSlide()
    {
        if (!isEmpty && curSlideIndex != 0) {
            curSlideIndex = 0;
            showSlide();
        }
    }

    /**
     * Display the last slide.
     */
    private void lastSlide()
    {
        if (!isEmpty && curSlideIndex != lastSlideIndex) {
            curSlideIndex = lastSlideIndex;
            showSlide();
        }
    }

    /**
     * Display the previous slide.
     */
    private void previousSlide()
    {
        if (curSlideIndex > 0) {
            curSlideIndex--;
            showSlide();
        }
    }

    /**
     * Display the next slide.
     */
    private void nextSlide()
    {
        if (curSlideIndex < lastSlideIndex) {
            curSlideIndex++;
            showSlide();
        }
    }

    /**
     * Execute (show) the current slide.
     */
    private void showSlide()
    {
        killTimer();
        updateButtonStates();
        slideshow.showSlide(window, curSlideIndex);
    }

    /**
     * Change from play to pause or vice-versa.
     */
    private void togglePlay()
    {
        if (state == State.PAUSED) {
            play();
        }
        else if (state == State.PLAYING) {
            pause();
        }
    }

    /**
     * This is called after a slide has been fully displayed. This means:
     * <ul>
     *     <li>The slide is static and has been drawn.
     *     <li>The slide is interactive and not animated and has been drawn.
     *     <li>The slide is animated and the last frame has been drawn.
     *  </ul>
     * If the slideshow is running and the slide's pause time is 0, the next
     * slide is displayed. If the slideshow is running and the slide's pause
     * time is > 0, then a timer is created that will display the next slide
     * after the pause time expires. Finally, if the slideshow is not running,
     * this does nothing.
     */
    public void slideDone()
    {
        // Kill any existing timer

        killTimer();

        // Start a new timer if we need to

        Slideshow.Slide slide = slideshow.getSlide(curSlideIndex);
        double pause = slide.getPause();

        // If we are not running, we don't need a timer
        // If the current slide is the last one, we don't need a timer

        if (state != State.PLAYING || curSlideIndex >= lastSlideIndex) return;

        // If the pause time is 0, we don't need a timer -- we just move
        // immediately to the next slide

        if (pause <= 0) {
            nextSlide();
            return;
        }

        // If the pause time is infinite, we don't need a timer -- we just
        //   stay on the current slide

        if (Double.isInfinite(pause)) return;

        // Start a timer

        startTimer();
    }

    /**
     * Change the play/pause state to PLAYING.
     */
    private void play()
    {
        state = State.PLAYING;
        updateButtonStates();
        nextSlide();
    }

    /**
     * Change the play/pause state to PAUSE.
     */
    private void pause()
    {
        state = State.PAUSED;
        killTimer();
        updateButtonStates();
    }

    /**
     * This is called each time the user interacts with the slide and lets us
     * extend a running timer. An interaction occurs when:
     * <ul>
     *     <li>The user zooms or pans.
     *     <li>The user reloads the slide.
     *     <li>The user manipulates a GUI control.
     * </ul>
     * If the slideshow is running and a timer is running, we kill any pending
     * timer tasks and restart the timer.
     * <p>
     * If the slideshow is not running, there should be no timer running.
     * <p>
     * If there is no timer running, we don't need to extend it.
     */
    public void userInteraction()
    {
        if (state == State.PLAYING && timer != null) {
            startTimer();
        }
    }

    /**
     * Close the slideshow.
     */
    public void close()
    {
        killTimer();

        buttonSlideshowStart.removeEventHandler(ActionEvent.ANY, ssStartEventHandler);
        buttonSlideshowEnd.removeEventHandler(ActionEvent.ANY, ssEndEventHandler);
        buttonSlideshowPrevious.removeEventHandler(ActionEvent.ANY, ssPreviousEventHandler);
        buttonSlideshowNext.removeEventHandler(ActionEvent.ANY, ssNextEventHandler);
        buttonSlideshowPlayPause.removeEventHandler(ActionEvent.ANY, ssPlayPauseEventHandler);
    }

    /**
     * Start a pause timer for the current slide. This should only be called if
     * we know we need to pause for a fixed time.
     */
    private void startTimer()
    {
        // Kill any existing timer

        killTimer();

        timer = new Timer("Slideshow");

        // Create a new TimerTask

        TimerTask timerTask = new TimerTask()
        {
            public void run()
            {
                Platform.runLater(() -> nextSlide());
            }
        };

        Slideshow.Slide slide = slideshow.getSlide(curSlideIndex);
        double pause = slide.getPause() * 1000.0;
        long pauseLong = pause > Long.MAX_VALUE ? Long.MAX_VALUE : (long)pause;
        timer.schedule(timerTask, pauseLong);
    }

    /**
     * Kill any running timer.
     */
    private void killTimer()
    {
        // Kill any existing timer

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

}
