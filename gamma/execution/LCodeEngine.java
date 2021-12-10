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
import gamma.drawing.Context;
import gamma.execution.lcode.*;
import gamma.value.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author Antonio Freixas
 */
public class LCodeEngine
{
    static private final double MIN_ZOOM_SCALE =      0.000001;
    static private final double MAX_ZOOM_SCALE = 100.0;

    private final ArrayList<Command> commands;
    private final MainWindow window;
    private Command animationCommand;
    private Command displayCommand;
    private Command frameCommand;

    private Canvas canvas;
    private Region canvasParent;
    private double lastWidth;
    private double lastHeight;

    private Context context;

    ChangeListener<Number> widthListener;
    ChangeListener<Number> heightListener;

    private double mouseX;
    private double mouseY;
    private boolean mouseInside;

    /**
     * Create the lcode engine.
     *
     * @param window The window into which we will draw.
     */
    public LCodeEngine(MainWindow window)
    {
        this.window = window;
        this.commands = new ArrayList<>();

        this.displayCommand = new Command(new DisplayStruct(), new StyleStruct(), new DisplayCommandExec());
        this.frameCommand = new Command(new FrameStruct(), new StyleStruct(), new FrameCommandExec());

        lastWidth = -1.0;
        lastHeight = -1.0;

        // Not really correct - the mouse might be inside the canvas at
        // the point the lcode engine is created

        mouseInside = false;
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
     * Get the canvas on which we will draw. This is only valid after setup()
     * is called.
     *
     * @return The drawing canvas.
     */
    public Canvas getCanvas()
    {
        return canvas;
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
     * Return true if the mouse is inside the canvas.
     *
     * @return True if the mouse is inside the canvas.
     */
    public boolean isMouseInside()
    {
        return mouseInside;
    }

    public void setMouseInside(boolean mouseInside)
    {
        this.mouseInside = mouseInside;
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

        // Get the drawing area

        canvas = window.getCanvas();
        canvasParent = (Region)canvas.getParent();

        // Set up our change listeners

        addListeners();

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

        // Create the graphics context

        context = new Context(this, canvas);

        // Run the display command

        ((DisplayCommandExec)displayCommand.getCmdExec()).initializeCanvas(
            context,
            (DisplayStruct)displayCommand.getCmdStruct(),
            displayCommand.getStyles());

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
       // Execute the display command

        displayCommand.execute(context);

        // Execute normal commands

        ListIterator<Command> iter = commands.listIterator();
        while (iter.hasNext()) {
            iter.next().execute(context);
        }
    }

    /**
     * Close this LCodeEngine by shutting down any observables and performing
     * any other cleanup.
     *
     */
    public void close()
    {
        removeListeners();
    }

    /**
     * Add all the listeners needed by this lcode engine. Every listener
     * added must be removed when this lcode engine is closed.
     */
    private void addListeners()
    {
        final LCodeEngine engine = this;

        // ************************************************************
        // *
        // * RESIZE HANDLER
        // *
        // ************************************************************

        widthListener = (ObservableValue<? extends Number> ov, Number oldValue, Number newValue) -> {
            if (engine.lastWidth != canvasParent.getWidth()) {
                engine.execute();
                engine.lastWidth = canvasParent.getWidth();
            }
        };
        canvasParent.widthProperty().addListener(widthListener);

        heightListener = (ObservableValue<? extends Number> ov, Number oldValue, Number newValue) -> {
            if (engine.lastHeight != canvasParent.getHeight()) {
                engine.execute();
                engine.lastHeight = canvasParent.getHeight();
            }
        };
        canvasParent.heightProperty().addListener(heightListener);

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
