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

    private int rep;
    private int reps;
    private int frame;
    private int maxFrames;
    private int absFrame;
    private int absMaxFrame;
    private boolean isLoop;
    private boolean countingDown;



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

        AnimationStruct animationStruct =
            (AnimationStruct)hCodeEngine.getLCodeEngine().getAnimationCommand().getCmdStruct();

        animationSymbolTable = hCodeEngine.getAnimationSymbolTable();


        isLoop = animationStruct.control.equals("loop");
        reps = animationStruct.reps;
        double speed = animationStruct.speed;

        framesPerLoop = getMaxFrames(hCodeEngine);
        framesPerRep = (isLoop ? maxFramesPerLoop : maxFramesPerLoop * 2 - 2);

        AnimationEngine me = this;

        // This is a 0-based absolute frame number

        absFrame = 0;

        // This is the 0-based absolute frame number of the largest possible
        // absolute frame

        absMaxFrame = (reps * framesPerRep) - 1;

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                int frame = me.getNextFrame();
                if (frame == -1) stop();
                me.executeFrame(frame);
            }

        };
        timer.start();
    }

    private int getNextFrame(int step)
    {
        // Calculate the next absolute frame number

        absFrame += step;
        if (absFrame < 0) {
            absFrame = 0;
        }
        else if (absFrame > absMaxFrame) {
            absFrame = absMaxFrame;
        }

        // Calculate the frame position within a rep (0-based)

        int frameInRep = absFrame % framesPerRep;

        // Calculate the frame position within a cycle, if we have one

        if (frameInRep >= framesPerLoop) {
            frame = framesPerRep - frameInRep;
        }

        // Frame is 1-based

        frame++;


        // Normal, counting down

        if (countingDown) {
            if (frame >= 2) {
                return frame--;
            }

            // Finished counting down

            else if (rep < reps) {
                reps++;
                countingDown = false;
                frame = 1;
                return frame++;
            }

            // If we're done with reps, return -1

            else {
                return -1;
            }
        }

        // Normal, counting up

        else if (frame <= maxFrames) {
            return frame++;
        }

        // Finished counting up
        // If we're cycling, we need to count down

        else if (!isLoop) {
            frame = maxFrames - 1;
            countingDown = true;
            return frame--;
        }

        // If we're looping, we do another rep, counting up

        else if (rep < reps) {
            reps++;
            frame = 1;
            return frame++;
        }

        // If we're done with reps, return -1

        else {
            return -1;
        }
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
