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
import gamma.execution.lcode.*;
import java.util.ArrayList;
import java.util.ListIterator;

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

    public void execute()
    {
        ListIterator<Command> iter = commands.listIterator();
        while (iter.hasNext()) {
            iter.next().execute(this);
        }
    }
}
