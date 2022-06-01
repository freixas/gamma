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

import javafx.application.Platform;
import org.freixas.gamma.MainWindow;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * This class runs in a separate thread and is used to monitor script files and
 * any dependent files, such as include files and stylesheet files. If the
 * script file or any dependent file is modified, this class reloads and parses
 * it. It then hands off the h-code back to the GUI thread for execution and
 * display.
 *
 * @author Antonio Freixas
 */
public class FileWatcher extends Thread
{
    private final int ID;
    private final File file;
    private final ArrayList<URLFile> dependentFiles;
    private final MainWindow window;
    private final List<Exception> list;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     *  Create a file watcher.
     *
     * @param ID The ID assigned to this watcher.
     * @param file The script file to monitor.
     * @param dependentFiles A list of dependent files (may be empty).
     * @param window The window associated with the script.
     */
    public FileWatcher(int ID, File file, ArrayList<URLFile> dependentFiles, MainWindow window)
    {
        this.ID = ID;
        this.file = file;
        this.dependentFiles = dependentFiles;
        this.window = window;
        this.list = Collections.synchronizedList(new ArrayList<>());
    }

    // **********************************************************************
    // *
    // * Getter
    // *
    // **********************************************************************

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
     *
     * @return A list of exceptions.
     */
    @SuppressWarnings("unused")
    public List<Exception> getExceptionList()
    {
        return list;
    }

    // **********************************************************************
    // *
    // * Informational
    // *
    // **********************************************************************

    /**
     * Determine if the give file list is the same as the existing file
     * list.
     *
     * @param file The main script file.
     * @param dependentFiles A list of dependent files.
     *
     * @return True if the files are the same.
     */
    public boolean hasSameFiles(File file, ArrayList<URLFile> dependentFiles)
    {
        // We're going to do a very literal comparison. Some comparisons are
        // unlikely to ever be needed

        // We'll treat a null dependent list the same as an empty dependent list

        boolean emptyExistingDependents = this.dependentFiles == null || this.dependentFiles.isEmpty();
        boolean emptyNewDependents = dependentFiles == null || dependentFiles.isEmpty();

        // Check if the main script files are the same:
        // Either both are null or both are not null and are the same

        if ((this.file == null && file == null) ||
            (this.file != null && this.file.equals(file))) {

            // Check if the dependent files are the same:
            // Either both are null (or empty) or both are not null and
            // contain the same files

            return
                (emptyExistingDependents && emptyNewDependents) ||
                (this.dependentFiles != null && this.dependentFiles.equals(dependentFiles));
        }

        // The main script files aren't the same, so we don't need to bother with
        // the dependent files

        return false;
    }

    // **********************************************************************
    // *
    // * Thread control
    // *
    // **********************************************************************

    /**
     * True if this thread is no longer running.
     *
     * @return True if this thread is no longer running.
     */
    public boolean isStopped()
    {
        return stop.get();
    }

    /**
     * Request that the thread stop.
     */
    public void stopThread()
    {
        stop.set(true);
    }

    @Override
    public void run()
    {
        // We monitor several files, one of which is the main script file and the
        // rest of which are dependent files. The dependent files may change, forcing
        // the creation of a new file watcher

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            HashMap<Path, WatchKey> parentMap = new HashMap<>();
            HashMap<WatchKey, ArrayList<String>> watchKeyMap = new HashMap<>();

            // Create a watch key for the main script's parent

            Path path = file.toPath();
            Path parent = path.getParent();
            WatchKey key = parent.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            // Save the parent folder and the key -> filename link
            // When we get an event, we only get a relative path. We can convert
            // this to a filename to compare against what we are looking for

            parentMap.put(parent, key);
            ArrayList<String> filenames = new ArrayList<>();
            filenames.add(path.getFileName().toString());
            watchKeyMap.put(key, filenames);

            // Create a watch key for each unique parent of each dependent file

            if (dependentFiles != null) {
                for (URLFile dependentFile : dependentFiles) {

                    // Dependent files can be files or URLs.  We only monitor
                    // changes in files

                    if (dependentFile.isFile()) {
                        Path dependentPath = dependentFile.getFile().toPath();
                        Path dependentParent = dependentPath.getParent();

                        // If a watch key exists for the parent directory, just add
                        // the new file to list of files associated with the key

                        if ((key = parentMap.get(dependentParent)) != null) {
                            filenames = watchKeyMap.get(key);
                            filenames.add(dependentPath.getFileName().toString());
                        }

                        // Otherwise, we need a new watch key

                        else {
                            key = dependentParent.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                            parentMap.put(dependentParent, key);
                            filenames = new ArrayList<>();
                            filenames.add(dependentPath.getFileName().toString());
                            watchKeyMap.put(key, filenames);
                        }
                    }
                }
            }

            boolean poll = true;
            long lastModTime = 0L;

            while (poll) {

                // Wait up to 5 seconds. This means that it can take up to
                // 5 seconds to kill the thread

                key = watchService.poll(5, TimeUnit.SECONDS);

                // Check to see if we should terminate

                if (isStopped()) {
                    // System.err.println("Stop request detected. Terminating file watcher for " + file);
                    return;
                }

                // Check for any events that occurred

                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {

                        Object context = event.context();
                        if (context instanceof Path detectedPath) //noinspection CommentedOutCode
                        {
                            // System.err.print("File " + detectedPath + "\n");

                            WatchEvent.Kind<?> kind = event.kind();
                            // System.err.println("Event kind : " + kind + " - File : " + event.context());

                            if (kind == StandardWatchEventKinds.ENTRY_CREATE ||
                                kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                                // Get the list of filenames related to this key

                                filenames = watchKeyMap.get(key);
                                if (filenames == null) continue;

                                // Get the path to the file associated with the
                                // event

                                // See if the file detected is on our list

                                if (filenames.contains(detectedPath.getFileName().toString())) {

                                    // Convert the relative path we received to an
                                    // absolute one

                                    Path parentDirectory = (Path) key.watchable();
                                    Path fullDetectedPath = parentDirectory.resolve(detectedPath);

                                    File detectedFile = fullDetectedPath.toFile();
                                    long modTime = detectedFile.lastModified();

                                    // If we detect a change, and it's been more than
                                    // one second since the last change (we sometimes get more
                                    // than one event for a single update), then go ahead and
                                    // process the script

                                    if (modTime > lastModTime + 1000) {
                                        // System.err.println("Modified file detected\n");
                                        Platform.runLater(window::reloadModifiedMainScript);
                                        lastModTime = modTime;
                                    }
                                }
                            }

                            // This commented out section would be executed if
                            // the script file was deleted. We could take some
                            // action (such as clearing the diagram in the main
                            // window), but decided that this would have no
                            // benefit

//                            else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
//                                //noinspection StatementWithEmptyBody
//                                if (detectedPath.toString().equals(file.getName())) {
//                                    // ???
//                                }
//                            }
                        }
                        poll = key.reset();
                    }
                }
            }
        }
        catch (Exception e) {
            if (!isStopped()) Platform.runLater(() -> window.fileWatcherException(ID, e));
        }

        if (!isStopped()) Platform.runLater(() -> window.fileWatcherDone(ID));
    }

}