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
package gamma.execution;

import gamma.MainWindow;
import gamma.execution.hcode.SetStatement;
import gamma.execution.lcode.AnimationStruct;
import gamma.math.Util;
import gamma.value.AnimationVariable;
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
        RUNNING, STOPPED, NOT_SET
    }

    class DiagramAnimationTimer extends AnimationTimer
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

        public final void stop()
        {
            super.stop();
            System.err.println("Timer stopped");
        }

        @Override
        public final void start()
        {
            firstCallTime = -1;
            totalFrameCount = 0;

            super.start();
            System.err.println("Timer started");
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
                    Platform.runLater(() -> {
                        animationEngine.window.getDiagramEngine().handleExeception(e);
                    });
                }
            }
        }
    }

    // Maximum of 10 hours of animation at 30 FPS

    private static final int MAX_FRAMES = 10 * 60 * 60 * 30;

    private final MainWindow window;
    private final SetStatement setStatement;
    private final HCodeProgram program;

    private AnimationSymbolTable animationSymbolTable;
    private Set<String> symbolNames;

    private HCodeEngine hCodeEngine;
    private DiagramAnimationTimer timer;
    private double speed;
    private State state;

    private Canvas canvas;
    private HBox animationControls;
    private Button buttonAnimStart;
    private Button buttonAnimEnd;
    private Button buttonAnimStepBackward;
    private Button buttonAnimStepForward;
    private Button buttonAnimPlayPause;

    EventHandler<ActionEvent> animStartEventHandler;
    EventHandler<ActionEvent> animEndEventHandler;
    EventHandler<ActionEvent> animStepBackwardEventHandler;
    EventHandler<ActionEvent> animStepForwardEventHandler;
    EventHandler<ActionEvent> animPlayPauseEventHandler;
    EventHandler<KeyEvent> keyTypedEventHandler;

    private ImageView playImage;
    private ImageView stopImage;

    private int framesPerLoop;
    private int framesPerRep;
    private int absFrame;
    private int absMaxFrame;

    public AnimationEngine(MainWindow window, SetStatement setStatement, HCodeProgram program)
    {
        this.window = window;
        this.setStatement = setStatement;
        this.program = program;

        this.hCodeEngine = null;
        this.timer = null;

        this.state = State.NOT_SET;

        setup();
    }

    public final void setup()
    {
        // Get the drawing area

        canvas = window.getCanvas();

        animationControls       = (HBox)  window.getScene().lookup("#animationControls");
        buttonAnimStart         = (Button)window.getScene().lookup("#animStart");
        buttonAnimEnd           = (Button)window.getScene().lookup("#animEnd");
        buttonAnimStepBackward  = (Button)window.getScene().lookup("#animStepBackward");
        buttonAnimStepForward   = (Button)window.getScene().lookup("#animStepForward");
        buttonAnimPlayPause     = (Button)window.getScene().lookup("#animPlay");

        playImage = new ImageView(new Image(window.getClass().getResourceAsStream("resources/player_play.png")));
        stopImage = new ImageView(new Image(window.getClass().getResourceAsStream("resources/player_stop.png")));

        // Enable the button area

        animationControls.setDisable(false);

        // Set up our change listeners

        addListeners();

        setState(State.STOPPED);
    }

    /**
     * Add all the listeners needed by the animation engine. Every listener
     * added must be removed when this engine is closed.
     */
    private void addListeners()
    {
        final AnimationEngine engine = this;

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
	buttonAnimStepBackward.addEventHandler(ActionEvent.ANY, animStepBackwardEventHandler);

        animStepForwardEventHandler = event -> {
            stepForward();
            canvas.requestFocus();
	};
	buttonAnimStepForward.addEventHandler(ActionEvent.ANY, animStepForwardEventHandler);

        animPlayPauseEventHandler = event -> {
            togglePlay();
            canvas.requestFocus();
	};
	buttonAnimPlayPause.addEventHandler(ActionEvent.ANY, animPlayPauseEventHandler);

        // ************************************************************
        // *
        // * KEYBOARDHANDLER
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
        setState(State.STOPPED);
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
        setState(State.STOPPED);
        if (timer != null) timer.stop();
    }

    private void stepBackward()
    {
        // Only if stopped

        if (state != State.STOPPED) return;
        int frame = getNextFrame(-1);
        executeFrame(frame);
        setState(State.STOPPED);
    }

    private void stepForward()
    {
        // Only if stopped

        if (state != State.STOPPED) return;
        int frame = getNextFrame(1);
        executeFrame(frame);
        setState(State.STOPPED);
    }

    private void play()
    {
        setState(State.RUNNING);
        if (timer != null) timer.start();
    }

    private void togglePlay()
    {
        if (state == State.STOPPED) {
            play();
        }
        else if (state == State.RUNNING) {
            stop();
        }
    }

    private synchronized void stop()
    {
        setState(State.STOPPED);
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
            buttonAnimStepBackward.setDisable(true);
            buttonAnimStepForward.setDisable(true);

            buttonAnimPlayPause.setGraphic(stopImage);
        }
        else if (state == State.STOPPED) {
            buttonAnimStart.setDisable(false || absFrame == 0);
            buttonAnimEnd.setDisable(false || absFrame == absMaxFrame);
            buttonAnimPlayPause.setDisable(false || absFrame == absMaxFrame);
            buttonAnimStepBackward.setDisable(false || absFrame == 0);
            buttonAnimStepForward.setDisable(false || absFrame == absMaxFrame);

            buttonAnimPlayPause.setGraphic(playImage);
        }
    }

    public void execute()
    {
        // First execution

        hCodeEngine = new HCodeEngine(window, setStatement, program);
        hCodeEngine.execute(true);

        // We don"t have the animation statement settings or the variables until
        // after the first execution

        AnimationStruct animationStruct =
            (AnimationStruct)hCodeEngine.getLCodeEngine().getAnimationCommand().getCmdStruct();

        animationSymbolTable = hCodeEngine.getAnimationSymbolTable();

        boolean isLoop = animationStruct.control.equals("loop");
        int reps = animationStruct.reps;
        speed = animationStruct.speed;

        // We calculate the maximum number of frames in a loop by checking the
        // limits of all the animation variables. If an animation variable doesn"t
        // have a limit, we still impose one

        framesPerLoop = getMaxFrames(hCodeEngine);

        // If we cycle, we repeat the loop backwards, but without the first
        // and last frames

        framesPerRep = (isLoop ? framesPerLoop : framesPerLoop * 2 - 2);

        // This is the 0-based absolute frame number of the largest possible
        // absolute frame

        absMaxFrame = (reps * framesPerRep) - 1;

        // This is a 0-based absolute frame number. We've already drawn the
        // first frame, so we start with 1, the second frame

        absFrame = 1;

        timer = new DiagramAnimationTimer(this, speed, 1);
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

        if (frame >= framesPerLoop) {
            frame = framesPerRep - frame;
        }

        // Frame is 1-based

        return frame + 1;

    }

    private synchronized void executeFrame(int frame)
    {
        Iterator<String> iter = symbolNames.iterator();

        // Tell all the animation variables to update to match the current
        // frame value

        while (iter.hasNext()) {
            AnimationVariable var = animationSymbolTable.getAnimationVariable(iter.next());
            var.setCurrentValue(frame);
        }

        // Execute the HCode and LCode again

        hCodeEngine.execute(true);

        // Try to see if we can keep the focus while the animation is running

        canvas.requestFocus();
    }

    private synchronized boolean atEnd()
    {
        return absFrame >= absMaxFrame;
    }

    // TO DO

    // * If the script file changes while we're running, we need to stop
    // * Need to hook up buttons
    // * Need to maintain running state

    // If we're not optimizing
    // Serialize the hCodes, an empty symbol table, and an empty container
    // for lCodes.

    // In either case, initialize the frame counts and create a new
    // AnimationSymbolTable.
    // Deserialize the saved hCodes, symbol table and lCodes.
    // Pass all these plus the animation symbol table to the HCodeEngine
    // (we may need an alternate execute() method) for execution

    // After the first execution, get the AnimationSymbolTable and
    // compute the maximum number of frames (which could be +infinity).
    // Also, get the animation command parameters: reps, loop/cycle, and
    // speed

    // Set up a callback to generate the next frame in x seconds, where x
    // is determined by the playback speed
    // Enable the video controls and set up their callbacks

    // For each frame we draw, we'll update the animation symbol table values
    // for each animation variable (or rather, we will give each one the
    // appropriate frame number and let it calculate its own value).
    // Then we'll deserialize the saved hCodes, symbol table and lCodes and
    // pass them again to the HCodeEngine (along with the AnimationTable)

    private int getMaxFrames(HCodeEngine hCodeEngine)
    {
        symbolNames = animationSymbolTable.getSymbolNames();

        Iterator<String> iter = symbolNames.iterator();

        int maxFrames = 1;
        while (iter.hasNext()) {
            AnimationVariable var = animationSymbolTable.getAnimationVariable(iter.next());
            if (Double.isNaN(var.getFinalValue())) {
                maxFrames = MAX_FRAMES;
                break;
            }

            double start = var.getInitialValue();
            double step  = var.getStepSize();
            double end   = var.getFinalValue();

            int numSteps;

            if (start < end) {
                numSteps = Util.toInt((end - start) / step) + 1;
            }
            else {
                numSteps = Util.toInt((start - end) / -step) + 1;
            }
            maxFrames = Math.max(maxFrames, numSteps);
        }
        return maxFrames;
    }

    public void close()
    {
        stop();
        removeListeners();

        if (hCodeEngine != null) {
            hCodeEngine.close();
        }
    }

    /**
     * Remove all the listeners attached to this lcode engine
     */
    private void removeListeners()
    {
        buttonAnimStart.removeEventHandler(ActionEvent.ACTION, animStartEventHandler);
        buttonAnimEnd.removeEventHandler(ActionEvent.ACTION, animEndEventHandler);
        buttonAnimStepBackward.removeEventHandler(ActionEvent.ACTION, animStepBackwardEventHandler);
        buttonAnimStepForward.removeEventHandler(ActionEvent.ACTION, animStepForwardEventHandler);
        buttonAnimPlayPause.removeEventHandler(ActionEvent.ACTION, animPlayPauseEventHandler);
        canvas.removeEventFilter(KeyEvent.KEY_TYPED, keyTypedEventHandler);
        window.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, keyTypedEventHandler);
    }

}
