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
package org.freixas.gamma;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import com.sun.tools.attach.AttachOperationFailedException;

import javax.imageio.IIOException;
import javax.net.ssl.SSLException;
import javax.swing.text.ChangedCharSetException;
import java.io.*;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.net.http.HttpTimeoutException;
import java.net.http.WebSocketHandshakeException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.FileSystemException;

/**
 * Java I/O exceptions don't include much error information in the message; it's
 * mostly in the name of the subclass. The GammaIOException wraps an IOException
 * and produces a better error message for the user.
 * <p>
 * Sample usage:
 * <code>
 *     catch (IOException e) {
 *         System.println(new GammaIOException(e).getLocalizedMessage());
 *     }
 * </code>
 *
 * @author Antonio Freixas
 */
public class GammaIOException extends Exception
{
    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a a Gamma I/O exception from a Java I/O Exception.
     *
     * @param cause The exception which caused the problem.
     */
    public GammaIOException(IOException cause)
    {
        this(null, cause);
    }

    /**
     * Create a a Gamma I/O exception from a Java I/O Exception.
     *
     * @param message The message to display.
     * @param cause The exception which caused the problem.
     */
    public GammaIOException(String message, IOException cause)
    {
        super(message, cause);
    }

    /**
     * Convert IOExceptions into a more readable form by looking at the
     * specific instance of the exception.
     *
     * @return The cause of this throwable or null if the cause is nonexistent or unknown.
     */
    public String getLocalizedMessage()
    {
        Throwable cause = getCause();
        String causeMessage = cause == null || cause.getLocalizedMessage() == null ? "" :  cause.getLocalizedMessage();

        // We skip some I/O errors we think are unlikely to occur in this
        // application
        
        if (cause instanceof AttachOperationFailedException) {
            return "Attach operation failed - " + causeMessage;
        }
        else if (cause instanceof ChangedCharSetException) {
            return "Changed character set error - " + causeMessage;
        }
        else if (cause instanceof CharacterCodingException) {
            return "Character coding error - " + causeMessage;
        }
        else if (cause instanceof CharConversionException) {
            return "Character conversion error - " + causeMessage;
        }
        else if (cause instanceof ClosedChannelException) {
            return "The channel was closed - " + causeMessage;
        }
        else if (cause instanceof ClosedConnectionException) {
            return "The connection was closed - " + causeMessage;
        }
        else if (cause instanceof EOFException) {
            return "End of file - " + causeMessage;
        }
        else if (cause instanceof FileLockInterruptionException) {
            return "The file lock was interrupted - " + causeMessage;
        }
        else if (cause instanceof FileNotFoundException) {
            return "The file was not found - " + causeMessage;
        }
        // 	else if (cause instanceof FilerException) {
        // 	    return "Filer - " + causeMessage;
        // 	}
        else if (cause instanceof FileSystemException) {
            return "File system error - " + causeMessage;
        }
        else if (cause instanceof HttpRetryException) {
            return "HTTP retry error - " + causeMessage;
        }
        else if (cause instanceof HttpTimeoutException) {
            return "HTTP timeout error - " + causeMessage;
        }
        else if (cause instanceof IIOException) {
            return "Error while reading or writing - " + causeMessage;
        }
        else if (cause instanceof InterruptedByTimeoutException) {
            return "An I/O operation was interrupted by a timeout - " + causeMessage;
        }
        else if (cause instanceof InterruptedIOException) {
            return "An I/O operation was interrupted - " + causeMessage;
        }
        // 	else if (cause instanceof InvalidPropertiesFormatException) {
        // 	    return "InvalidPropertiesFormat - " + causeMessage;
        // 	}
        // 	else if (cause instanceof JMXProviderException) {
        // 	    return "JMXProvider - " + causeMessage;
        // 	}
        // 	else if (cause instanceof JMXServerErrorException) {
        // 	    return "JMXServerError - " + causeMessage;
        // 	}
        // 	else if (cause instanceof MalformedURLException) {
        // 	    return "Malformed URL - " + causeMessage;
        // 	}
        // 	else if (cause instanceof ObjectStreamException) {
        // 	    return "ObjectStream - " + causeMessage;
        // 	}
        else if (cause instanceof ProtocolException) {
            return "An error occurred in the I/O protocol - " + causeMessage;
        }
        // 	else if (cause instanceof RemoteException) {
        // 	    return "Remote - " + causeMessage;
        // 	}
        // 	else if (cause instanceof SaslException) {
        // 	    return "Sasl - " + causeMessage;
        // 	}
        // 	else if (cause instanceof SocketException) {
        // 	    return "Socket - " + causeMessage;
        // 	}
        else if (cause instanceof SSLException) {
            return "SSL error - " + causeMessage;
        }
        else if (cause instanceof SyncFailedException) {
            return "Synchronization failed - " + causeMessage;
        }
        // 	else if (cause instanceof TransportTimeoutException) {
        // 	    return "TransportTimeout - " + causeMessage;
        // 	}
        else if (cause instanceof UnknownHostException) {
            return "Unknown host - " + causeMessage;
        }
        else if (cause instanceof UnknownServiceException) {
            return "Unknown service - " + causeMessage;
        }
        else if (cause instanceof UnsupportedEncodingException) {
            return "Unsupported encoding - " + causeMessage;
        }
        // 	else if (cause instanceof UserPrincipalNotFoundException) {
        // 	    return "User Principal Not Found - " + causeMessage;
        // 	}
        else if (cause instanceof UTFDataFormatException) {
            return "Malformed UTF character encountered - " + causeMessage;
        }
        else if (cause instanceof WebSocketHandshakeException) {
            return "Web socket handshake failed - " + causeMessage;
        }
        // 	else if (cause instanceof ZipException) {
        // 	    return "Zip - " + causeMessage;
        // 	}
        else {
            return causeMessage;
        }
    }


}
