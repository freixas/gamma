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
package org.freixas.gamma.execution;

import org.freixas.gamma.MainWindow;
import org.freixas.gamma.css.value.Stylesheet;
import org.freixas.gamma.execution.hcode.SetStatement;
import org.freixas.gamma.execution.lcode.AnimationStruct;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.value.AnimationVariable;
import org.freixas.gamma.value.DynamicVariable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

/**
 *
 * @author Antonio Freixas
 */
public class AnimationEngine
{
    enum State {
        RUNNING, PAUSED, NOT_SET
    }

    static class DiagramAnimationTimer extends AnimationTimer
    {
        private final AnimationEngine animationEngine;
        private double FPS;
        private int direction;

        private long firstCallTime;
        private long totalFrameCount;

        DiagramAnimationTimer(AnimationEngine animationEngine, double speed, int direction)
        {
            this.animationEngine = animationEngine;
            this.setSpeed(speed);
            this.setDirection(direction);
        }

        public final void setSpeed(double speed)
        {
            FPS = speed * 30;
        }

        public final void setDirection(int direction)
        {
            this.direction = (int)Util.sign(direction);
        }

        @Override
        public final void stop()
        {
            super.stop();
        }

        @Override
        public final void start()
        {
            firstCallTime = -1;
            totalFrameCount = 0;

            super.start();
        }

        @Override
        public void handle(long now)
        {
            int frameSkipSize;
            int frameStepSize;

            if (firstCallTime == -1) {
                frameSkipSize = 1;
                frameStepSize = 1;
                firstCallTime = now;
            }
            else {
                totalFrameCount++;
                double totalTime = now - firstCallTime;
                double avgNsPerFrame = totalTime / totalFrameCount;
                double avgSecPerFrame = avgNsPerFrame / 1_000_000_000.0;
                double avgFPS = 1 / avgSecPerFrame;

                double stepSize = FPS / avgFPS;
                if (stepSize >= 1.0) {
                    frameStepSize = Util.toInt(stepSize);
                    frameSkipSize = 1;
                }
                else {
                    frameStepSize = 1;
                    frameSkipSize = Util.toInt(1.0 / stepSize);
                }
            }

            if (totalFrameCount % frameSkipSize == 0) {
                int frame = animationEngine.getNextFrame(direction * frameStepSize);
                if (animationEngine.atEnd()) {
                    animationEngine.stop();
                }

                try {
                    animationEngine.executeFrame(frame);
                }
                catch (Throwable e) {
                    animationEngine.stop();
                    Platform.runLater(() -> animationEngine.window.getDiagramEngine().handleException(e));
                }
            }
        }
    }

    // Maximum of 10 hours of animation at 30 FPS

    static private final int MAX_FRAMES = 10 * 60 * 60 * 30;

    private final MainWindow window;
    private final SetStatement setStatement;
    private final Stylesheet stylesheet;
    private final HCodeProgram program;

    private DynamicSymbolTable dynamicSymbolTable;
    private Set<String> symbolNames;

    private HCodeEngine hCodeEngine;
    private DiagramAnimationTimer timer;
    private double speed;
    private State state;

    private final Canvas canvas;
    private final HBox animationControls;
    private final Button buttonAnimStart;
    private final Button buttonAnimEnd;
    private final Button buttonAnimPrevious;
    private final Button buttonAnimNext;
    private final Button buttonAnimPlayPause;

    EventHandler<ActionEvent> animStartEventHandler;
    EventHandler<ActionEvent> animEndEventHandler;
    EventHandler<ActionEvent> animStepBackwardEventHandler;
    EventHandler<ActionEvent> animStepForwardEventHandler;
    EventHandler<ActionEvent> animPlayPauseEventHandler;
    EventHandler<KeyEvent> keyTypedEventHandler;

    private final ImageView playImage;
    private final ImageView pauseImage;

    private int framesPerAnimation;
    private int framesPerRep;
    private int absFrame;
    private int absMaxFrame;

    private boolean isClosed;

    public AnimationEngine(MainWindow window, SetStatement setStatement, Stylesheet stylesheet, HCodeProgram program)
    {
        this.window = window;
        this.setStatement = setStatement;
        this.stylesheet = stylesheet;
        this.program = program;

        this.hCodeEngine = null;
        this.timer = null;

        this.state = State.NOT_SET;
        this.isClosed = false;

        // Get the drawing area

        canvas = window.getCanvas();

        animationControls   = (HBox)  window.getScene().lookup("#animation-controls");
        buttonAnimStart     = (Button)window.getScene().lookup("#anim-start");
        buttonAnimEnd       = (Button)window.getScene().lookup("#anim-end");
        buttonAnimPrevious  = (Button)window.getScene().lookup("#anim-previous");
        buttonAnimNext      = (Button)window.getScene().lookup("#anim-next");
        buttonAnimPlayPause = (Button)window.getScene().lookup("#anim-play-pause");

        InputStream playIS  = window.getClass().getResourceAsStream("/anim_play.png");
        InputStream pauseIS = window.getClass().getResourceAsStream("/anim_pause.png");

        playImage  = playIS == null ? null : new ImageView(new Image(playIS));
        pauseImage = pauseIS == null ? null : new ImageView(new Image(pauseIS));
        if (playImage != null) {
            playImage.setFitHeight(16);
            playImage.setFitWidth(16);
        }
        if (pauseImage != null) {
            pauseImage.setFitHeight(16);
            pauseImage.setFitWidth(16);
        }

        // Enable the button area

        animationControls.setDisable(false);

        // Set up our change listeners

        addListeners();

        setState(State.PAUSED);
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

        animStartEventHandler = event -> {
            toStart();
            canvas.requestFocus();
        };
        buttonAnimStart.addEventHandler(ActionEvent.ANY, animStartEventHandler);

        animEndEventHandler = event -> {
            toEnd();
            canvas.requestFocus();
        };
        buttonAnimEnd.addEventHandler(ActionEvent.ANY, animEndEventHandler);

        animStepBackwardEventHandler = event -> {
            stepBackward();
            canvas.requestFocus();
        };
        buttonAnimPrevious.addEventHandler(ActionEvent.ANY, animStepBackwardEventHandler);

        animStepForwardEventHandler = event -> {
            stepForward();
            canvas.requestFocus();
        };
        buttonAnimNext.addEventHandler(ActionEvent.ANY, animStepForwardEventHandler);

        animPlayPauseEventHandler = event -> {
            togglePlay();
            canvas.requestFocus();
        };
        buttonAnimPlayPause.addEventHandler(ActionEvent.ANY, animPlayPauseEventHandler);

        // ************************************************************
        // *
        // * KEYBOARD HANDLER
        // *
        // ************************************************************

        keyTypedEventHandler = event -> {
            boolean foundKey = true;
            switch (event.getCharacter()) {
                case " " -> togglePlay();
                case "1" -> playNormal();
                case "{" -> toStart();
                case "}" -> toEnd();
                default -> foundKey = false;
            }
            if (!foundKey) {
                foundKey = true;
                if (null != event.getCode()) {
                    switch (event.getCode()) {
                        case ESCAPE -> stop();
                        case LEFT -> stepBackward();
                        case RIGHT -> stepForward();
                        case UP -> playFaster();
                        case DOWN -> playSlower();
                        default -> foundKey = false;
                    }
                }
            }
            if (foundKey) event.consume();
        };
        canvas.addEventFilter(KeyEvent.KEY_TYPED, keyTypedEventHandler);
        window.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyTypedEventHandler);
    }

    private void toStart()
    {
        // We can always go to the first frame. This stops the animation

        if (absFrame != 0) {
            absFrame = 0;
            int frame = absoluteToLogicalFrame(absFrame);
            executeFrame(frame);
        }
        setState(State.PAUSED);
        if (timer != null) timer.stop();
    }

    private void toEnd()
    {
        // We can always go to the last frame. This stops the animation

        if (absFrame != absMaxFrame) {
            absFrame = absMaxFrame;
            int frame = absoluteToLogicalFrame(absFrame);
            executeFrame(frame);
        }
        setState(State.PAUSED);
        if (timer != null) timer.stop();
    }

    private void stepBackward()
    {
        // Only if stopped

        if (state != State.PAUSED) return;
        int frame = getNextFrame(-1);
        executeFrame(frame);
        setState(State.PAUSED);
    }

    private void stepForward()
    {
        // Only if stopped

        if (state != State.PAUSED) return;
        int frame = getNextFrame(1);
        executeFrame(frame);
        setState(State.PAUSED);
    }

    private void play()
    {
        setState(State.RUNNING);
        if (timer != null) timer.start();
    }

    private void togglePlay()
    {
        if (state == State.PAUSED) {
            play();
        }
        else if (state == State.RUNNING) {
            stop();
        }
    }

    private synchronized void stop()
    {
        setState(State.PAUSED);
        if (timer != null) timer.stop();
    }

    private void playFaster()
    {
        speed *= 2;
        if (speed > 10.0) speed = 10.0;
        if (timer != null) timer.setSpeed(speed);
    }

    private void playSlower()
    {
        speed /= 2;
        if (speed < .1) speed = .1;
        if (timer != null) timer.setSpeed(speed);
    }

    private void playNormal()
    {
        if (timer != null) timer.setSpeed(1.0);
    }

    private void setState(State newState)
    {
        state = newState;
        if (state == State.RUNNING) {
            buttonAnimStart.setDisable(false);
            buttonAnimEnd.setDisable(false);
            buttonAnimPlayPause.setDisable(false);
            buttonAnimPrevious.setDisable(true);
            buttonAnimNext.setDisable(true);

            if (pauseImage != null) buttonAnimPlayPause.setGraphic(pauseImage);
        }
        else if (state == State.PAUSED) {
            buttonAnimStart.setDisable(absFrame == 0);
            buttonAnimEnd.setDisable( absFrame == absMaxFrame);
            buttonAnimPlayPause.setDisable(absFrame == absMaxFrame);
            buttonAnimPrevious.setDisable(absFrame == 0);
            buttonAnimNext.setDisable(absFrame == absMaxFrame);

            if (playImage != null) buttonAnimPlayPause.setGraphic(playImage);
        }
    }

    /**
     * Run the animation.
     */
    public void execute()
    {
        execute(true);
    }

    /**
     * Run the animation.
     *
     * @param firstTime If true, we are restarting the animation. We need to
     * remove all animation variables from the dynamic symbol table, recalculate
     * the maximum frame and restart from frame 1.
     */
    public void execute(boolean firstTime)
    {
        if (isClosed) return;

        if (firstTime) {
            // First execution

            hCodeEngine = new HCodeEngine(window, setStatement, stylesheet, program);
            hCodeEngine.execute();

            // We don't have the animation statement settings or dynamic variables until
            // after the first execution

            dynamicSymbolTable = hCodeEngine.getDynamicSymbolTable();
            symbolNames = dynamicSymbolTable.getSymbolNames();
        }

        else {
            // Remove all animation variables from the dynamic symbol table

            Iterator<String> iter = symbolNames.iterator();
            ArrayList<String> names = new ArrayList<>();

            while (iter.hasNext()) {
                String name = iter.next();
                DynamicVariable dynamicVariable = dynamicSymbolTable.getDynamicVariable(name);
                if (dynamicVariable instanceof AnimationVariable) {
                    names.add(name);
                }
            }

            for (String name : names) {
                dynamicSymbolTable.remove(name);
            }

            // Execute the h-code once to add in any new/changes animation variables

            hCodeEngine.execute();
        }

        // Handle the animation statement and animation variables

        AnimationStruct animationStruct =
            (AnimationStruct)hCodeEngine.getLCodeEngine().getAnimationCommand().getCmdStruct();

        int reps = animationStruct.reps;
        speed = animationStruct.speed;

        // We calculate the maximum number of frames in a straight repetition by
        // checking the limits of all the animation variables. If an animation
        // variable doesn"t have a limit, we still impose one

        framesPerAnimation = getMaxFrames();

        // If we cycle, we repeat the animation backwards, but without the first
        // and last frames

        framesPerRep = (animationStruct.cycle ? framesPerAnimation * 2 - 2 : framesPerAnimation);

        // This is the 0-based absolute frame number of the largest possible
        // absolute frame. If we cycle, we need to add one more frame so that the
        // last cycle ends exactly on the first logical frame

        absMaxFrame = (reps * framesPerRep) - 1 + (animationStruct.cycle ? 1 : 0);

        // This is a 0-based absolute frame number. We've already drawn the
        // first frame, so we start with 1, the second frame

        absFrame = 1;

        if (firstTime) timer = new DiagramAnimationTimer(this, speed, 1);
        timer.start();

        setState(State.RUNNING);
    }

    private synchronized int getNextFrame(int step)
    {
        // Calculate the next absolute frame number

        absFrame += step;

        // We can't go below 0

        if (absFrame < 0) {
            absFrame = 0;
        }

        // And we can't go over the absMaxFrame

        else if (absFrame > absMaxFrame) {
            absFrame = absMaxFrame;
        }

        return absoluteToLogicalFrame(absFrame);
    }

    private int absoluteToLogicalFrame(int absFrame)
    {
        // Calculate the frame position within a rep (0-based)

        int frame = absFrame % framesPerRep;

        // Calculate the frame position within a cycle, if we have one

        if (frame >= framesPerAnimation) {
            frame = framesPerRep - frame;
        }

        // Frame is 1-based

        return frame + 1;

    }

    private synchronized void executeFrame(int frame)
    {
        if (isClosed) return;

        // Tell all the animation variables to update to match the current
        // frame value

        for (String symbolName : symbolNames) {
            DynamicVariable dynamicVariable = dynamicSymbolTable.getDynamicVariable(symbolName);
            if (dynamicVariable instanceof AnimationVariable var) {
                var.setCurrentValue(frame);
            }
        }

        // Execute the h-code and l-code again

        hCodeEngine.execute();

        // If this is the last frame, report that we're done

        if (atEnd()) window.diagramCompleted();

        // Try to see if we can keep the focus while the animation is running

        canvas.requestFocus();
    }

    private synchronized boolean atEnd()
    {
        return absFrame >= absMaxFrame;
    }

    /**
     * Get the maximum number of frames for one repetition of the animation,
     * ignoring cycles.

     *
     * @return The maximum number of frames for one repetition of the animation.
     */
    private int getMaxFrames()
    {
        int maxFrames = -1;

        for (String symbolName : symbolNames) {
            DynamicVariable dynamicVariable = dynamicSymbolTable.getDynamicVariable(symbolName);
            if (dynamicVariable instanceof AnimationVariable var) {
                if (Double.isNaN(var.getFinalValue())) continue;

                double start = var.getInitialValue();
                double step = var.getStepSize();
                double end = var.getFinalValue();

                int numSteps;

                if (start < end) {
                    numSteps = Util.toInt((end - start) / step) + 1;
                }
                else {
                    numSteps = Util.toInt((start - end) / -step) + 1;
                }
                maxFrames = Math.max(maxFrames, numSteps);
            }
        }

        // If we didn't find any animation variable with a limit, then this
        // single repetition goes for the maximum allowed time

        if (maxFrames == -1) {
            maxFrames = MAX_FRAMES;
        }

        return maxFrames;
    }

    public void close()
    {
        stop();
        removeListeners();
        isClosed = true;

        // Disable the button area

        animationControls.setDisable(true);

        if (hCodeEngine != null) {
            hCodeEngine.close();
            hCodeEngine = null;
        }
    }

    /**
     * This is called when a display variable is changed. If the animation
     * is running, we don't need to do anything--the next frame will get the
     * latest display variable value. If the animation is not running, we
     * need to redisplay the current frame.
     * <p>
     * If a restart is requested, we need to stop any running animation and
     * restart the animation from the beginning (recalculating frames, etc.).
     *
     * @param restart True if the animation should be restarted when a display
     * variable changes.
     */
    public void updateForDisplayVariable(boolean restart)
    {
        if (restart) {
            stop();
            execute(false);
        }
        else if (state != State.RUNNING) {
            hCodeEngine.execute();
        }
    }
    /**
     * Remove all the listeners attached to this lcode engine
     */
    private void removeListeners()
    {
        buttonAnimStart.removeEventHandler(ActionEvent.ACTION, animStartEventHandler);
        buttonAnimEnd.removeEventHandler(ActionEvent.ACTION, animEndEventHandler);
        buttonAnimPrevious.removeEventHandler(ActionEvent.ACTION, animStepBackwardEventHandler);
        buttonAnimNext.removeEventHandler(ActionEvent.ACTION, animStepForwardEventHandler);
        buttonAnimPlayPause.removeEventHandler(ActionEvent.ACTION, animPlayPauseEventHandler);
        canvas.removeEventFilter(KeyEvent.KEY_TYPED, keyTypedEventHandler);
        window.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, keyTypedEventHandler);
    }

}
