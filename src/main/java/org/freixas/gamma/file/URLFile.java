/*
 * Copyright (c) 2022 Antonio Freixas
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

import org.freixas.gamma.ProgrammingException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * Scripts can come from files or URLs. This class creates a single object
 * type which can be either a URL or a File.
 */
public class URLFile
{
    private final boolean isFile;
    private final URL url;
    private final File file;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a URLFile from a string. URLs are identified only if they contain
     * "//:".
     *
     * @param name The name of a file or URL.
     *
     * @throws MalformedURLException If the name represents a URL, but is not
     * a valid URL.
     */
    public URLFile(String name) throws MalformedURLException
    {
        this(name, false);
    }

    /**
     * Create a URLFile from a string. URLs are identified because they contain
     * "//:" or because isURL is true.
     *
     * @param name The name of a file or URL.
     * @param isURL True if we should assume the name represents a URL.
     *
     * @throws MalformedURLException If the name represents a URL, but is not
     * a valid URL.
     */

    public URLFile(String name, boolean isURL) throws MalformedURLException
    {
        this.isFile = !isURL && !name.contains("://");

        if (this.isFile) {
            this.url = null;
            this.file = new File(name).getAbsoluteFile();
        }
        else if (name.contains("://")) {
            this.url = new URL(name);
            this.file = null;
        }
        else {
            this.url = new URL("http://" + name);
            this.file = null;
        }
    }

    /**
     * Create a URLFile from a URL.
     *
     * @param url The URL from which to create the URLFile.
     */
    public URLFile(URL url)
    {
        this.isFile = false;
        this.url = url;
        this.file = null;
    }

    /**
     * Create a URLFile from a File.
     *
     * @param file The File from which to create the URLFile.
     */
    public URLFile(File file)
    {
        this.isFile = true;
        this.url = null;
        this.file = file!= null ? file.getAbsoluteFile() : null;
    }

    // **********************************************************************
    // *
    // * Getters
    // *
    // **********************************************************************

    /**
     * Return true if this URLFile represents a URL.
     *
     * @return True if this URLFile represents a URL.
     */
    public boolean isURL()
    {
        return !isFile;
    }

    /**
     * Return true if this URLFile represents a File.
     *
     * @return True if this URLFile represents a File.
     */
    public boolean isFile()
    {
        return isFile;
    }

    /**
     * Get the URL represented by this URLFile. This will be null if isURL()
     * is false.
     *
     * @return The URL represented by this URLFile.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Get the File represented by this URLFile. This will be null if isFile()
     * is false.
     *
     * @return The File represented by this URLFile.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Get the name the URLFile file. This is just the last name in the
     * pathname's name sequence. If the URLFile is a URL, the name is preceded
     * by the host name in parentheses. If the pathname's name sequence is
     * empty, then the empty string is returned.
     *
     * @return The name of the associated script file.
     */
    public String getName()
    {
        if (isFile) {
            return file.getName();
        }
        else {
            String host = url.getHost();
            String path = url.getPath();
            return "(" + host + ")" + new File(path).getName();
        }
    }

    /**
     * Get the path portion of the URLFile.
     *
     * @return The path portion of the URLFile.
     */
    public String getPath()
    {
        if (isFile) {
            if (file == null) return "";
            return file.getPath();
        }
        else {
            return url.getPath();
        }
    }

    // **********************************************************************
    // *
    // * Functionality
    // *
    // **********************************************************************

    /**
     * Read the contents of the URLFile into a string.
     *
     * @return The contents of the URLFile.
     *
     * @throws IOException If the file can't be read.
     */
    public String readString() throws IOException
    {
        String str;
        if (isFile) {
            str = Files.readString(file.toPath());
        }
        else {
            try (Scanner scanner = new Scanner(url.openStream(), StandardCharsets.UTF_8.toString())) {
                scanner.useDelimiter("\\A");
                str = scanner.hasNext() ? scanner.next() : "";
            }
        }
        return str;
    }

    /**
     * Given a string representing a File or URL dependent on this URLFile,
     * create a URLFile for the dependent. If the name designates a relative
     * path, it is relative to this URLFile.
     * <p>
     * If this URLFile represents a URL, then any dependent files must come from
     * the same domain.
     *
     * @param name The name of the dependent file.
     *
     * @return The dependent URLFile.
     *
     * @throws IOException If the file cannot be read for any reason.
     */
    public URLFile getDependentScriptURL(String name) throws IOException
    {
        // The cases we need to consider are:
        //
        // 1) The parent is a file and the dependent is an absolute file path
        // 2) The parent is a file and the dependent is an absolute URL
        // 3) The parent is a file and the dependent is a relative path
        // 4) The parent is a URL and the dependent is an absolute file path
        // 5) The parent is a URL and the dependent is an absolute URL
        // 6) The parent is a URL and the dependent is a relative path

        File dependentFile = new File(name);
        boolean isAbsoluteFile = dependentFile.isAbsolute();
        boolean isAbsoluteURL = !isAbsoluteFile && name.contains("://");

        if (isFile) {

            // Case 1: Use the dependent file as is

            if (isAbsoluteFile) {
                return new URLFile(dependentFile);
            }

            // Case 2: Use the dependent URL as is

            else if (isAbsoluteURL) {
                return new URLFile(new URL(name));
            }

            // Case 3: Convert a relative path to an absolute file path

            else {
                return new URLFile(new File(file.getParent(), name));
            }

        }

        // Handle these 3 cases:
        //
        // 4) The parent is a URL and the dependent is an absolute file path
        // 5) The parent is a URL and the dependent is an absolute URL
        // 6) The parent is a URL and the dependent is a relative path

        else {

            // Case 4: You cannot have a dependent file that is local when
            // the script file is null

            if (dependentFile.isAbsolute()) {
                throw new IOException("When a script file is remote, all dependent files must also be remote");
            }

            // Case 5: Use the dependent URL only if it is from the same
            // domain as the script

            else if (isAbsoluteURL) {
                URL dependentURL = new URL(name);
                if (!dependentURL.getHost().equals(url.getHost())) {
                    throw new IOException("Dependent files must be from the same domain ('" + url.getHost() + "')");
                }
                return new URLFile(dependentURL);
            }

            // Case 6:  Convert a relative path to an absolute URL

            else {
                return new URLFile(new URL(url, name));
            }
        }
    }

    // **********************************************************************
    // *
    // * Standard methods: toString, clone hashCode, equals
    // *
    // **********************************************************************

    @Override
    public String toString()
    {
        if (isFile) return file.toString();
        return url.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof URLFile other)) return false;

        if (this.isFile != other.isFile) return false;
        if (isFile) {
            return this.file.equals(other.file);
        }
        else {
            return this.url.equals(other.url);
        }
    }

}
