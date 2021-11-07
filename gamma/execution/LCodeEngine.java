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
import gamma.drawing.T;
import gamma.execution.lcode.*;
import gamma.value.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import javafx.scene.canvas.Canvas;

/**
 *
 * @author Antonio Freixas
 */
public class LCodeEngine
{
    private final ArrayList<Command> commands;
    private final MainWindow window;
    private Command animationCommand;
    private Command displayCommand;
    private Command frameCommand;

    private boolean resize = false;
    private Canvas canvas;

    public LCodeEngine(MainWindow window)
    {
        this.window = window;
        this.commands = new ArrayList<>();

        this.displayCommand = new Command(new DisplayStruct(), new StyleStruct(), new DisplayCommand());
        this.displayCommand = new Command(new FrameStruct(), new StyleStruct(), new FrameCommand());
    }

    public MainWindow getWindow()
    {
        return window;
    }

    public Command getAnimationCommand()
    {
        return animationCommand;
    }

    public Command getDisplayCommand()
    {
        return displayCommand;
    }

    public Command getFrameCommand()
    {
        return frameCommand;
    }

    public ArrayList<Command> getCommands()
    {
        return commands;
    }

    public void addCommand(Command command)
    {
        Struct struct = command.getCmdStruct();

        if (struct instanceof AnimationStruct) {
            animationCommand = command;
        }
        if (struct instanceof DisplayStruct) {
            displayCommand = command;
        }
        else if (struct instanceof FrameStruct) {
            frameCommand = command;
        }
        else {
            commands.add(command);
        }
    }

    /**
     * Set up for the first run of the lcode.
     */
    public void setup()
    {

        window.setLCodeEngine(this);

        DisplayStruct dStruct = (DisplayStruct)displayCommand.getCmdStruct();
        int width = dStruct.width;
        int height = dStruct.height;

        // If width and height are ommitted, always resize to the maximum
        // available space

        if (width == Struct.INT_NOT_SET && height == Struct.INT_NOT_SET) {
            resize = true;
            width = height = -1;
        }
        else if (width == Struct.INT_NOT_SET) {
            width = -1;
        }
        else if (height == Struct.INT_NOT_SET) {
            height = -1;
        }

        // Add the canvas

        canvas = window.setupCanvas(width, height);

        // Use the frame command to revise all the coordinates in the structures.
        // Optimize if we have the default frame

        FrameStruct fStruct = (FrameStruct)frameCommand.getCmdStruct();
        if (!fStruct.frame.equals(HCodeEngine.getDefFrame())) {
            final Frame prime = fStruct.frame;
            Iterator<Command> iter = (Iterator<Command>)commands.iterator();
            commands.forEach((Command command) -> {
                command.getCmdStruct().relativeTo(prime);
            });
        }

        // Set up the drawing transforms

        T t = new T(dStruct.origin, dStruct.scale, dStruct.units, canvas.getWidth(), canvas.getHeight());
    }

    public void execute()
    {
        ListIterator<Command> iter = commands.listIterator();
        while (iter.hasNext()) {
            iter.next().execute(this);
        }
    }

    /**
     * Close this LCodeEngine by shutting down any observables and performing
     * any other cleanup.
     *
     */
    public void close()
    {
        // Remove all observables
    }
}
