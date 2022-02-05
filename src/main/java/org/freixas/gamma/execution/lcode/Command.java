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

import javafx.scene.canvas.GraphicsContext;
import org.freixas.gamma.css.value.StyleStruct;
import org.freixas.gamma.drawing.Context;

/**
 *
 * @author Antonio Freixas
 */
public class Command
{
    private final Struct cmdStruct;
    private final StyleStruct styles;
    private final CommandExec cmdExec;

    public Command(Struct cmdStruct, StyleStruct styles, CommandExec cmdExec)
    {
        this.cmdStruct = cmdStruct;
        this.cmdStruct.finalizeValues();
        this.styles = styles;
        this.cmdExec = cmdExec;
    }

    public Struct getCmdStruct()
    {
        return cmdStruct;
    }

    public StyleStruct getStyles()
    {
        return styles;
    }

    public CommandExec getCmdExec()
    {
        return cmdExec;
    }

    public void execute(Context context)
    {
        // Handle the global opacity style at this level

        GraphicsContext gc = context.gc;
        gc.save();
        gc.setGlobalAlpha(styles.opacity);
        
        try {
            cmdExec.execute(context, cmdStruct, styles);
        }
        finally {
            gc.restore();
        }
    }
}
