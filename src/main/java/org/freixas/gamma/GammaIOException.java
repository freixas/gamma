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
    @Serial
    private static final long serialVersionUID = 1L;

    // **********************************************************************
    // *
    // * Constructors
    // *
    // **********************************************************************

    /**
     * Create a Gamma I/O exception from a Java I/O Exception.
     *
     * @param cause The exception which caused the problem.
     */
    public GammaIOException(IOException cause)
    {
        this(null, cause);
    }

    /**
     * Create a Gamma I/O exception from a Java I/O Exception.
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
    @SuppressWarnings("unused")
    public String getLocalizedMessage()
    {
        Throwable cause = getCause();
        String causeMessage = cause == null || cause.getLocalizedMessage() == null ? "" :  cause.getLocalizedMessage();

        // We skip some I/O errors we think are unlikely to occur in this
        // application

        return switch (cause) {
            case AttachOperationFailedException attachOperationFailedException ->
                "Attach operation failed - " + causeMessage;
            case ChangedCharSetException changedCharSetException ->
                "Changed character set error - " + causeMessage;
            case CharacterCodingException characterCodingException ->
                "Character coding error - " + causeMessage;
            case CharConversionException charConversionException ->
                "Character conversion error - " + causeMessage;
            case ClosedChannelException closedChannelException ->
                "The channel was closed - " + causeMessage;
            case ClosedConnectionException closedConnectionException ->
                "The connection was closed - " + causeMessage;
            case EOFException eofException -> "End of file - " + causeMessage;
            case FileLockInterruptionException fileLockInterruptionException ->
                "The file lock was interrupted - " + causeMessage;
            case FileNotFoundException fileNotFoundException ->
                "The file was not found - " + causeMessage;

            // 	case FilerException ->
            // 	    "Filer - " + causeMessage;

            case FileSystemException fileSystemException ->
                "File system error - " + causeMessage;
            case HttpRetryException httpRetryException ->
                "HTTP retry error - " + causeMessage;
            case HttpTimeoutException httpTimeoutException ->
                "HTTP timeout error - " + causeMessage;
            case IIOException iioException ->
                "Error while reading or writing - " + causeMessage;
            case InterruptedByTimeoutException interruptedByTimeoutException ->
                "An I/O operation was interrupted by a timeout - " + causeMessage;
            case InterruptedIOException interruptedIOException ->
                "An I/O operation was interrupted - " + causeMessage;

            // 	case instanceof InvalidPropertiesFormatException ->
            // 	    "InvalidPropertiesFormat - " + causeMessage;
            // case JMXProviderException ->
            // 	    "JMXProvider - " + causeMessage;
            // 	case JMXServerErrorException ->
            // 	    "JMXServerError - " + causeMessage;
            // 	case MalformedURLException ->
            // 	    "Malformed URL - " + causeMessage;
            // 	case ObjectStreamException ->
            // 	    "ObjectStream - " + causeMessage;

            case ProtocolException protocolException ->
                "An error occurred in the I/O protocol - " + causeMessage;

            // 	case RemoteException ->
            // 	    "Remote - " + causeMessage;
            // 	case SaslException ->
            // 	    "Sasl - " + causeMessage;
            // 	case SocketException ->
            // 	    "Socket - " + causeMessage;

            case SSLException sslException ->
                "SSL error - " + causeMessage;
            case SyncFailedException syncFailedException ->
                "Synchronization failed - " + causeMessage;

            // 	case TransportTimeoutException ->
            // 	    "TransportTimeout - " + causeMessage;

            case UnknownHostException unknownHostException ->
                "Unknown host - " + causeMessage;
            case UnknownServiceException unknownServiceException ->
                "Unknown service - " + causeMessage;
            case UnsupportedEncodingException unsupportedEncodingException ->
                "Unsupported encoding - " + causeMessage;

            // 	case UserPrincipalNotFoundException ->
            // 	    "User Principal Not Found - " + causeMessage;

            case UTFDataFormatException utfDataFormatException ->
                "Malformed UTF character encountered - " + causeMessage;
            case WebSocketHandshakeException webSocketHandshakeException ->
                "Web socket handshake failed - " + causeMessage;

            // 	case ZipException ->
            // 	    "Zip - " + causeMessage;
            // 	}

            case null, default -> causeMessage;
        };
    }


}
