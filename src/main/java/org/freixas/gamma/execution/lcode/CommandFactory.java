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
package org.freixas.gamma.execution.lcode;

import org.freixas.gamma.ProgrammingException;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.execution.HCodeEngine;
import org.freixas.gamma.value.PropertyList;
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
        commandMap.put("worldline", new WorldlineCommandExec());
        commandMap.put("path", new PathCommandExec());
        commandMap.put("label", new LabelCommandExec());
    }

    static public Command createCommand(HCodeEngine engine, String name, PropertyList properties)
    {
        CommandExec exec = commandMap.get(name);
        if (exec == null) throw new ProgrammingException("Unexpected command name '" + name + ";");

        // Place the properties into a structure

        Struct cmdStruct = Struct.createNewStruct(engine, name, properties);
        StyleStruct styleStruct = engine.getStylesheet().createStyleStruct(engine.getTokenContext().getURLFile(), name, cmdStruct.id, cmdStruct.cls, cmdStruct.style);

        return new Command(cmdStruct, styleStruct, exec);
    }

}
