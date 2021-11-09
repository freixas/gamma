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

import customfx.ResizableCanvas;
import gamma.MainWindow;
import gamma.drawing.Context;
import gamma.drawing.T;
import gamma.execution.lcode.*;
import gamma.value.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;

/**
 *
 * @author Antonio Freixas
 */
public class LCodeEngine
{
    class ListenerStruct
    {

    }

    private final ArrayList<Command> commands;
    private final MainWindow window;
    private Command animationCommand;
    private Command displayCommand;
    private Command frameCommand;

    private ResizableCanvas canvas;
    private double lastWidth;
    private double lastHeight;

    private T transform;

    private Context context;

    ChangeListener<Number> sizeListener;

    /**
     * Create the lcode engine.
     *
     * @param window The window into which we will draw.
     */
    public LCodeEngine(MainWindow window)
    {
        this.window = window;
        this.commands = new ArrayList<>();

        this.displayCommand = new Command(new DisplayStruct(), new StyleStruct(), new DisplayCommand());
        this.frameCommand = new Command(new FrameStruct(), new StyleStruct(), new FrameCommand());

        lastWidth = -1.0;
        lastHeight = -1.0;
    }

    /**
     * Get the window associated with the diagram we are going to draw.
     *
     * @return The associated window.
     */
    public MainWindow getWindow()
    {
        return window;
    }

    /**
     * Get the animation command (which is not stored on the lcode list).
     *
     * @return The animation command.
     */
    public Command getAnimationCommand()
    {
        return animationCommand;
    }

    /**
     * Get the display command  (which is not stored on the lcode list).
     * @return
     */
    public Command getDisplayCommand()
    {
        return displayCommand;
    }

    /**
     * Get the frame command (which is not stored on the lcode list).
     *
     * @return The frame command.
     */
    public Command getFrameCommand()
    {
        return frameCommand;
    }

    /**
     * Get the canvas on which we will draww. This is only valid after setup()
     * is called.
     *
     * @return The drawing canvas.
     */
    public ResizableCanvas getCanvas()
    {
        return canvas;
    }

    /**
     * Get the graphics transform. This is only valid after setup() is
     * called.
     *
     * @return The graphics transform.
     */
    public T getTransform()
    {
        return transform;
    }

    /**
     * Get the graphics context. This is only valid after setup() is
     * called.
     *
     * @return The graphics context.
     */
    public Context getContext()
    {
        return context;
    }

    /**
     * Get all the commands on the lcode list. This excludes the animation,
     * display, and frame commands.
     * <p>
     * This is a live list. Changes to this list will affect the lcode
     * execution.
     *
     * @return The list of all commands on the lcode list.
     */
    public ArrayList<Command> getCommands()
    {
        return commands;
    }

    /**
     * Add a  command to the command list. If the command is animation,
     * display, or frame, it is stored in the lcode engine, but not on the
     * command list.
     *
     * @param command The command to add.
     */

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
        // Tell the window we are starting it. It will shut down any prior
        // lcode engine

        window.setLCodeEngine(this);

        // Get the drawing area

        canvas = window.getCanvas();

        // Set up our change listeners

        final LCodeEngine engine = this;

        sizeListener = (ObservableValue<? extends Number> ov, Number oldValue, Number newValue) -> {
            if (engine.lastWidth != canvas.getWidth() || engine.lastHeight != canvas.getHeight()) {
                engine.execute();
                engine.lastWidth = canvas.getWidth();
                engine.lastHeight = canvas.getWidth();
            }
        };
        canvas.widthProperty().addListener(sizeListener);

        // Observer for mouse events
        // Click and drag for pan
        // Ctrl + scrollwheel to zoom
        // Keyboard events
        // Ctrl +/-  to zoom
        // Ctrl + 0 to reset zoom and pan

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

        // Set up the drawing transforms. The transform is set up here,
        // but is modified whenever the zoom/pan changes or when the canvas
        // is resized

        DisplayStruct dStruct = (DisplayStruct)displayCommand.getCmdStruct();

        transform = new T(dStruct.origin, dStruct.scale, dStruct.units, canvas.getWidth(), canvas.getHeight());

        // Create the graphics context

        context = new Context(transform, canvas);

        // Draw the initial display

        execute();
    }

    /**
     * Execute the lCode.
     * <p>
     * This is called after setup() and the after any zoom/pan changes or after
     * the drawing area is resized.
     */
    public void execute()
    {
        // Run the display command

        displayCommand.execute(this);

        // Execute normal commands

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
        if (canvas != null) {
            canvas.widthProperty().removeListener(sizeListener);
        }
    }
}
