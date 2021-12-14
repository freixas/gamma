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
package gamma.file;

import gamma.MainWindow;
import gamma.parser.ParseException;
import gamma.parser.Parser;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;

/**
 *
 * @author Antonio Freixas
 */
public class FileWatcher extends Thread
{
    private final File file;
    private final MainWindow window;
    private final List<Exception> list;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public FileWatcher(File file, MainWindow window)
    {
        this.file = file;
        this.window = window;
        this.list = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Get the list of exceptions. While the list is thread-safe for atomic
     * operations, iterators must be in a synchronized block.
     * <code>
     * List&lt;Exception&gt; list = fileWatcher.getExceptionList();
     * synchronized (list) {
     *     Iterator iter = list.iterator();
     *     ... etc. ...
     * }
     * </code>
     * @return
     */
    public List<Exception> getExceptionList()
    {
        return list;
    }

    public boolean isStopped()
    {
        return stop.get();
    }

    public void stopThread()
    {
        stop.set(true);
    }

    private void doOnChange()
    {
        try {
            String script = Files.readString(file.toPath());
            Parser parser = new Parser(file, script);
            parser.parse();
            Platform.runLater(new ScriptParseCompleteHandler(window, parser));
        }
        catch (IOException | ParseException e) {
            list.add(e);
            e.printStackTrace();
            Platform.runLater(new ScriptParseErrorHandler(window, list));
        }
        catch (Exception e) {
            list.add(e);
            e.printStackTrace();
            Platform.runLater(new ScriptParseErrorHandler(window, list));
        }
    }

    @Override
    public void run()
    {
        // When we first execute this thread, we should treat the file as though
        // we've just noticed that it changed

        doOnChange();

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            Path path = file.toPath().getParent();
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            boolean poll = true;
            long lastModTime = 0L;

            while (poll) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    System.err.println("Event kind : " + kind + " - File : " + event.context());

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE ||
                        kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        @SuppressWarnings("unchecked")
                        Path filename = ((WatchEvent<Path>)event).context();
                        System.err.print("Filename is " + filename + "\n");

                        if (filename.toString().equals(file.getName())) {
                            Long modTime = file.lastModified();
                            if (modTime > lastModTime + 1000) {
                                System.err.println("doOnChange()\n");
                                doOnChange();
                                lastModTime = modTime;
                            }
                        }
                    }

                    else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        @SuppressWarnings("unchecked")
                        Path filename = ((WatchEvent<Path>)event).context();
                        System.err.print("Filename is " + filename + "\n");

                        if (filename.toString().equals(file.getName())) {
                            // ???
                        }
                    }

                }
                poll = key.reset();
            }
        }
        catch (IOException e) {
            list.add(e);
            Platform.runLater(new ScriptParseErrorHandler(window, list));
        }
        catch (InterruptedException e) {
            list.add(e);
            Platform.runLater(new ScriptParseErrorHandler(window, list));
        }
        catch (Exception e) {
            list.add(e);
            Platform.runLater(new ScriptParseErrorHandler(window, list));
        }
    }

}