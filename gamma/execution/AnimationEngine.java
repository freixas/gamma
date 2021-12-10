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
import gamma.execution.lcode.DisplayCommandExec;
import gamma.execution.lcode.DisplayStruct;
import gamma.math.Util;
import gamma.value.AnimationVariable;
import java.util.Iterator;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author Antonio Freixas
 */
public class AnimationEngine
{
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
    private AnimationTimer timer;

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
    }

    public void execute()
    {
        // First execution

        hCodeEngine = new HCodeEngine(window, program);
        hCodeEngine.execute(true);

        // We don't have the animation statement settings or the variables until
        // after the first execution

        AnimationStruct animationStruct =
            (AnimationStruct)hCodeEngine.getLCodeEngine().getAnimationCommand().getCmdStruct();

        animationSymbolTable = hCodeEngine.getAnimationSymbolTable();

        boolean isLoop = animationStruct.control.equals("loop");
        int reps = animationStruct.reps;
        double speed = animationStruct.speed;

        // We calculate the maximum number of frames in a loop by checking the
        // limits of all the animation variables. If an animation variable doesn't
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
    }

    public void close()
    {
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        if (hCodeEngine != null) {
            hCodeEngine.close();
        }
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

    /**
     * Add all the listeners needed by this lcode engine. Every listener
     * added must be removed when this lcode engine is closed.
     */
    private void addListeners()
    {
        final AnimationEngine engine = this;

        // ************************************************************
        // *
        // * MOUSE FEEDBACK HANDLER
        // *
        // ************************************************************

        Label label = (Label)(window.getScene().lookup("#coordinateArea"));

        canvas.setOnMouseMoved(event -> {
            displayCoordinates(label, event.getX(), event.getY());
            engine.setMouseInside(true);
        });

        canvas.setOnMouseEntered(event -> {
            displayCoordinates(label, event.getX(), event.getY());
            engine.setMouseInside(true);
        });

        canvas.setOnMouseExited(event -> {
            label.setText("");
            engine.setMouseInside(false);
        });

        // ************************************************************
        // *
        // * PAN HANDLER
        // *
        // ************************************************************

        canvas.setOnMousePressed(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });

        canvas.setOnMouseDragged(event -> {
            try {
                double deltaX = event.getX() - mouseX;
                double deltaY = event.getY() - mouseY;

                GraphicsContext gc = context.gc;
                Affine transform = gc.getTransform();
                Point2D point = transform.inverseDeltaTransform(deltaX, deltaY);
                gc.translate(point.getX(), point.getY());
                context.invScale = context.getCurrentInvScale();
                context.bounds = context.getCurrentCanvasBounds();

                engine.execute();

                mouseX = event.getX();
                mouseY = event.getY();
            }
            catch (NonInvertibleTransformException e) {
                throw new ProgrammingException("LCodeEngine.setOnMouseDragged()", e);
            }
        });

        // ************************************************************
        // *
        // * MOUSE ZOOM HANDLER
        // *
        // ************************************************************

        canvas.setOnScroll(event -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            double delta = event.getDeltaY();
            if (delta == 0.0) return;

            Point2D center = new Point2D(event.getX(), event.getY());
            zoom(center, delta);

            engine.execute();
        });

        // ************************************************************
        // *
        // * KEYBOARD ZOOM HANDLER
        // *
        // ************************************************************

        canvas.setOnKeyPressed(event -> {

            // The Ctrl key must be used

            if (!event.isControlDown()) return;
            if (event.getCode() != KeyCode.PLUS &&
                event.getCode() != KeyCode.EQUALS &&
                event.getCode() != KeyCode.MINUS &&
                event.getCode() != KeyCode.DIGIT0) return;

            // Reset zoom/pan (Ctrl + 0)

            if (event.getCode() == KeyCode.DIGIT0) {
                ((DisplayCommandExec)displayCommand.getCmdExec()).
                    setInitialZoomPan(context, ((DisplayStruct)displayCommand.getCmdStruct()));
            }

            // Zoom in/out (Ctrl + +/-)

            else {
                Point2D center = new Point2D(canvas.getWidth() / 2.0, canvas.getHeight() / 2.0);
                zoom(center, event.getCode() == KeyCode.MINUS ? +100.0 : -100.0);
           }

            engine.execute();
            if (engine.isMouseInside()) {
                // displayCoordinates(label, event.getX(), event.getY());
            }
        });
    }

    private void displayCoordinates(Label label, double x, double y)
    {
        try {
            GraphicsContext gc = context.gc;
            Point2D point
                = gc.getTransform().inverseTransform(x, y);
            label.setText(
                "(" +
                String.format("%g", point.getX()) +
                ", " +
                String.format("%g", point.getY()) +
                ")");
        }
        catch (NonInvertibleTransformException e) {
            throw new ProgrammingException("LCodeEngine.displayCoordinates()", e);
        }
    }

    /**
     * Common zoom code.
     *
     * @param center The point to zoom around in screen coordinates.
     * @param delta  The zoom amount (+ for zoom in, - for zoom out).
     */
    private void zoom(Point2D center, double delta)
    {
        try {
            GraphicsContext gc = context.gc;

            if (delta == 0.0) return;

             Affine transform = gc.getTransform();

             double curInvScale = context.getCurrentInvScale();

             // Scale using this rather magic formula

             double zoomExp = 1 + (Math.abs(delta) / 1000.0);
             double zoomIncr = Math.pow(curInvScale, zoomExp) / 10.0;
             if (delta < 0) zoomIncr = -zoomIncr;
             double newScale = curInvScale + zoomIncr;

             // Limit the scaling

             if (newScale < MIN_ZOOM_SCALE) {
                 newScale = MIN_ZOOM_SCALE;
             }
             else if (newScale > MAX_ZOOM_SCALE) {
                 newScale = MAX_ZOOM_SCALE;
             }

             // Get the relative scaling to use

             newScale = 1 / newScale;

             // Convert the zoom center to world coordinates

             Point2D worldCenter = transform.inverseTransform(center);

             // Scale around the center point

             transform.appendScale(curInvScale, curInvScale, worldCenter.getX(), worldCenter.getY());
             transform.appendScale(newScale, newScale, worldCenter.getX(), worldCenter.getY());
             gc.setTransform(transform);

            context.invScale = context.getCurrentInvScale();
            context.bounds = context.getCurrentCanvasBounds();
         }
         catch (NonInvertibleTransformException e) {
             throw new ProgrammingException("LCodeEngine.zoom()", e);
         }
    }

    /**
     * Remove all the listeners attached to this lcode engine
     */
    private void removeListeners()
    {
        canvasParent.widthProperty().removeListener(widthListener);
        canvasParent.widthProperty().removeListener(heightListener);
        canvas.setOnMouseMoved(null);
        canvas.setOnMouseExited(null);
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnScroll(null);
        canvas.setOnKeyPressed(null);
    }

}
