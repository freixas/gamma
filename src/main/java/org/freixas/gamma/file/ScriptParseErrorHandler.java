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
package org.freixas.gamma.file;

import org.freixas.gamma.MainWindow;
import java.util.List;
import java.util.ListIterator;
import javafx.scene.control.Alert.AlertType;
import org.freixas.gamma.parser.ParseException;

/**
 * This class handles errors that occur while reading or parsing the script
 * file.
 *
 * @author Antonio Freixas
 */
public class ScriptParseErrorHandler implements Runnable
{
    private final MainWindow window;
    private final List<Exception> list;


    public ScriptParseErrorHandler(MainWindow window, List<Exception> list)
    {
        this.window = window;
        this.list = list;
    }

    @Override
    public void run()
    {
        // Iterations throug the list must be in a synchronized block
        // List<Exception> list = fileWatcher.getExceptionList();
        // synchronized (list) {
        //     Iterator iter = list.iterator();
        //     ... etc. ...
        // }
        //
        // Single operations are OK.

        StringBuilder str = new StringBuilder();

        synchronized (list) {
            ListIterator<Exception> iter = list.listIterator();
            while (iter.hasNext()) {
                Exception e = iter.next();
                if (e instanceof ParseException parseException) {
                    window.showParseException(parseException);
                }
                else {
                    window.showTextAreaAlert(AlertType.ERROR, "Error", "Error", e.getLocalizedMessage(), true);
                }
            }
            list.clear();
        }


    }
}
