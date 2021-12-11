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
import gamma.ProgrammingException;
import gamma.execution.lcode.AnimationStruct;
import gamma.math.Util;
import gamma.value.AnimationVariable;
import java.util.Iterator;
import java.util.Set;
import javafx.animation.AnimationTimer;
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
    enum STATE {
        RUNNING, PAUSED, STOPPED, NOT_SET
    }

    class DiagramAnimationTimer extends AnimationTimer
    {
        private final AnimationEngine animationEngine;
        private double FPS;
        private int direction;

        private int lastFrame;
        private long firstCallTime;
        private int totalFrameCount;

        DiagramAnimationTimer(AnimationEngine animationEngine, double speed, int direction)
        {
            this.animationEngine = animationEngine;
            this.setSpeed(speed);
            this.setDirection(direction);

            this.lastFrame = -1;
            this.firstCallTime = -1;
            this.totalFrameCount = 0;
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
        public void handle(long now)
        {
            int frameSkipSize;
            int frameStepSize;

            if (firstCallTime == -1) {
                frameSkipSize = 1;
                frameStepSize = 1;
                totalFrameCount = 0;
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
                if (frame == lastFrame) {
                    stop();
                    return;
                }
                lastFrame = frame;

                animationEngine.executeFrame(frame);
            }
        }

    }

    // Maximum of 10 hours of animation at 30 FPS

    private static final int MAX_FRAMES = 10 * 60 * 60 * 30;

    private final MainWindow window;
    private final HCodeProgram program;

    private AnimationSymbolTable animationSymbolTable;
    private Set<String> symbolNames;

    private HCodeEngine hCodeEngine;
    private DiagramAnimationTimer timer;
    private STATE state;

    private Canvas canvas;
    private HBox animationControls;
    private Button buttonAnimStart;
    private Button buttonAnimEnd;
    private Button buttonAnimStepBackward;
    private Button buttonAnimStepForward;
    private Button buttonAnimPlayPause;
    private Button buttonAnimStop;

    EventHandler<ActionEvent> animStartEventHandler;
    EventHandler<ActionEvent> animEndEventHandler;
    EventHandler<ActionEvent> animStepBackwardEventHandler;
    EventHandler<ActionEvent> animStepForwardEventHandler;
    EventHandler<ActionEvent> animPlayPauseEventHandler;
    EventHandler<ActionEvent> animStopEventHandler;
    EventHandler<KeyEvent> keyTypedEventHandler;

    private ImageView playImage;
    private ImageView pauseImage;

    private int framesPerLoop;
    private int framesPerRep;
    private int absFrame;
    private int absMaxFrame;

    public AnimationEngine(MainWindow window, HCodeProgram program)
    {
        this.window = window;
        this.program = program;

        this.hCodeEngine = null;
        this.timer = null;

        this.state = STATE.NOT_SET;

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
        buttonAnimStop          = (Button)window.getScene().lookup("#animStop");

        playImage = new ImageView(new Image(window.getClass().getResourceAsStream("resources/player_play.png")));
        pauseImage = new ImageView(new Image(window.getClass().getResourceAsStream("resources/player_pause.png")));

        // Enable the button area

        animationControls.setDisable(false);

        // Set up our change listeners

        addListeners();

        setState(STATE.STOPPED);
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

        animStopEventHandler = event -> {
            stop();
            canvas.requestFocus();
	};
	buttonAnimStop.addEventHandler(ActionEvent.ANY, animStopEventHandler);

        // ************************************************************
        // *
        // * KEYBOARDHANDLER
        // *
        // ************************************************************

        keyTypedEventHandler = event -> {
            boolean foundKey = true;
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case SPACE -> togglePlay();
                    case ESCAPE -> stop();
                    case LEFT -> stepBackward();
                    case RIGHT -> stepForward();
                    case UP -> playFaster();
                    case DOWN -> playSlower();
                    case DIGIT1 -> playNormal();
                    case BRACELEFT -> toStart();
                    case BRACERIGHT -> toEnd();
                    default -> foundKey = false;
                }
                if (foundKey) event.consume();
            }
        };
        canvas.addEventHandler(KeyEvent.KEY_TYPED, keyTypedEventHandler);
    }

    private void toStart()
    {
        // We can always go to the first frame. This stops the animation

        setState(STATE.STOPPED);
        timer.stop();

        if (absFrame != 0) {
            absFrame = 0;
            int frame = absoluteToLogicalFrame(absFrame);
            executeFrame(frame);
        }
    }

    private void toEnd()
    {
        // We can always go to the last frame. This stops the animation

        setState(STATE.STOPPED);
        timer.stop();

        if (absFrame != absMaxFrame) {
            absFrame = absMaxFrame;
            int frame = absoluteToLogicalFrame(absFrame);
            executeFrame(frame);
        }
    }

    private void stepBackward()
    {
        // Only if paused

        if (state != STATE.PAUSED) return;
        int frame = getNextFrame(-1);
        executeFrame(frame);

    }

    private void stepForward()
    {
        if (state != STATE.PAUSED) return;
        int frame = getNextFrame(1);
        executeFrame(frame);
    }

    private void play()
    {
        if (state == STATE.RUNNING) return;
        setState(STATE.RUNNING);
        timer.start();
    }

    private void pause()
    {
        if (state == STATE.PAUSED) return;
        setState(STATE.PAUSED);
        timer.stop();
    }

    private void togglePlay()
    {
        if (state == STATE.STOPPED || state == STATE.PAUSED) {
            play();
        }
        else if (state == STATE.RUNNING) {
            pause();
        }
    }

    private void stop()
    {
        setState(STATE.STOPPED);
        timer.stop();
    }

    private void playFaster()
    {

    }

    private void playSlower()
    {

    }

    private void playNormal()
    {
        timer.setSpeed(1.0);
    }

    private void setState(STATE newState)
    {
        if (newState == state) return;
        if (state == STATE.STOPPED && newState == STATE.PAUSED) {
            throw new ProgrammingException("AnimationEngine.setState() stopped -> paused");
        }
        state = newState;
        if (state == STATE.RUNNING) {
            buttonAnimStart.setDisable(false);
            buttonAnimEnd.setDisable(false);
            buttonAnimPlayPause.setDisable(false);
            buttonAnimStop.setDisable(false);
            buttonAnimStepBackward.setDisable(true);
            buttonAnimStepForward.setDisable(true);

            buttonAnimPlayPause.setGraphic(pauseImage);
        }
        else if (state == STATE.PAUSED) {
            buttonAnimStart.setDisable(false);
            buttonAnimEnd.setDisable(false);
            buttonAnimPlayPause.setDisable(false);
            buttonAnimStop.setDisable(false);
            buttonAnimStepBackward.setDisable(false || absFrame == 0);
            buttonAnimStepForward.setDisable(false || absFrame == absMaxFrame);

            buttonAnimPlayPause.setGraphic(playImage);
        }
        else if (state == STATE.STOPPED) {
            buttonAnimStart.setDisable(false);
            buttonAnimEnd.setDisable(false);
            buttonAnimPlayPause.setDisable(false);
            buttonAnimStop.setDisable(true);
            buttonAnimStepBackward.setDisable(true);
            buttonAnimStepForward.setDisable(true);

            buttonAnimPlayPause.setGraphic(playImage);
        }

    }

    public void execute()
    {
        // First execution

        hCodeEngine = new HCodeEngine(window, program);
        hCodeEngine.execute(true);

        // We don"t have the animation statement settings or the variables until
        // after the first execution

        AnimationStruct animationStruct =
            (AnimationStruct)hCodeEngine.getLCodeEngine().getAnimationCommand().getCmdStruct();

        animationSymbolTable = hCodeEngine.getAnimationSymbolTable();

        boolean isLoop = animationStruct.control.equals("loop");
        int reps = animationStruct.reps;
        double speed = animationStruct.speed;

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

        setState(STATE.RUNNING);
    }

    private int getNextFrame(int step)
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

    private void executeFrame(int frame)
    {
        // Close down the last lCodeEngine

        hCodeEngine.getLCodeEngine().close();

        Iterator<String> iter = symbolNames.iterator();

        // Tell all the animation variables to update to match the current
        // frame value

        while (iter.hasNext()) {
            AnimationVariable var = animationSymbolTable.getAnimationVariable(iter.next());
            var.setCurrentValue(frame);
        }

        // Execute the HCode and LCode again

        hCodeEngine.execute(true);
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
        buttonAnimStop.removeEventHandler(ActionEvent.ACTION, animStopEventHandler);
        canvas.removeEventHandler(KeyEvent.KEY_TYPED, keyTypedEventHandler);
    }

}
