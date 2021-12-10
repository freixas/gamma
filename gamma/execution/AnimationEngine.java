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
import gamma.execution.lcode.AnimationStruct;
import gamma.math.Util;
import gamma.value.AnimationVariable;
import java.util.Iterator;
import java.util.Set;
import javafx.animation.AnimationTimer;

/**
 *
 * @author Antonio Freixas
 */
public class AnimationEngine
{
    // Maximum of 10 hours of animation at 30 FPS

    private static final int MAX_FRAMES = 10 * 60 * 60 * 30;

    private final MainWindow window;
    private final HCodeProgram program;

    private AnimationSymbolTable animationSymbolTable;
    private Set<String> symbolNames;

    private HCodeEngine hCodeEngine;

    private int framesPerLoop;
    private int framesPerRep;
    private int absFrame;
    private int absMaxFrame;

    public AnimationEngine(MainWindow window, HCodeProgram program)
    {
        this.window = window;
        this.program = program;
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

        AnimationEngine me = this;
        AnimationTimer timer = new AnimationTimer() {
            private int lastFrame = -1;

            @Override
            public void handle(long now) {
                int frame = me.getNextFrame(1);
                if (frame == lastFrame) stop();
                me.executeFrame(frame);
                lastFrame = frame;
            }

        };
        timer.start();
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

}
