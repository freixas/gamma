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
public record Command(Struct cmdStruct, StyleStruct styles, CommandExec cmdExec)
{
    public Command(Struct cmdStruct, StyleStruct styles, CommandExec cmdExec)
    {
        this.cmdStruct = cmdStruct;
        this.cmdStruct.finalizeValues();
        this.styles = styles;
        this.cmdExec = cmdExec;
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

    public void executeDisplay(Context context)
    {
        // For the display command, we actually want it to affect
        // the global graphics context, so we call this version of
        // execute

        // Handle the global opacity style at this level

        GraphicsContext gc = context.gc;
        gc.setGlobalAlpha(styles.opacity);
        cmdExec.execute(context, cmdStruct, styles);
    }

}
