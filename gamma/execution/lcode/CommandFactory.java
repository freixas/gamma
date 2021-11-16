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
package gamma.execution.lcode;

import gamma.ProgrammingException;
import gamma.execution.HCodeEngine;
import gamma.value.PropertyList;
import java.util.HashMap;

/**
 *
 * @author Antonio Freixas
 */
public class CommandFactory
{
    static final HashMap<String, CommandExec> commandMap = new HashMap<>();

    static {
        commandMap.put("display", new DisplayCommandExec());
        commandMap.put("frame", new FrameCommandExec());
        commandMap.put("animation", new AnimationCommandExec());
        commandMap.put("axes", new AxesCommandExec());
        commandMap.put("grid", new GridCommandExec());
        commandMap.put("hypergrid", new HypergridCommandExec());
        commandMap.put("event", new EventCommandExec());
        commandMap.put("line", new LineCommandExec());
        commandMap.put("world", new WorldlineCommandExec());
        commandMap.put("path", new PathCommandExec());
        commandMap.put("label", new LabelCommandExec());
    }

    public static Command createCommand(HCodeEngine engine, String name, PropertyList properties)
    {
        CommandExec exec = commandMap.get(name);
        if (exec == null) throw new ProgrammingException("Unexpected command name '" + name + ";");

        Struct cmdStruct = Struct.createNewStruct(name, properties);
        StyleStruct styleStruct = new StyleStruct(engine.getStyleDefaults());
        Struct.initializeStruct(styleStruct, "style", properties);

        return new Command(cmdStruct, styleStruct, exec);
    }

}
