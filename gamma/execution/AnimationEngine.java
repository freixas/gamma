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
import java.util.LinkedList;

/**
 *
 * @author Antonio Freixas
 */
public class AnimationEngine
{
    private final MainWindow window;
    private final LinkedList<Object> hCodes;

    public AnimationEngine(MainWindow window, LinkedList<Object> hCodes)
    {
        this.window = window;
        this.hCodes = hCodes;
    }

    public void execute()
    {
        // Do an optimization run of the hCodes
        // Call the HCodeEngine to optimize.
        // The output should be the optimized hCodes, the symbol table and the
        // lCodes
        // Copy these using serialization

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
    }

}
