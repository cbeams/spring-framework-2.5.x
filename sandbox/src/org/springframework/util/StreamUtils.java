/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Static utility functions for handling streams.
 * 
 * @author Keith Donald
 */
public final class StreamUtils {
    private static final int bufSize = 4096;

    /**
     * Read all of the data from a stream, writing it into another stream.
     * Reads data from the input stream and writes it to the output stream,
     * until no more data is available.
     * 
     * @param input
     *            The input stream.
     * @param output
     *            The output stream.
     * 
     * @exception java.io.IOException
     *                If an exception occurred while reading from the stream.
     */
    public static final OutputStream readStreamIntoStream(
        InputStream input,
        OutputStream output)
        throws IOException {
        Assert.notNull(input);
        Assert.notNull(output);
        byte buf[] = new byte[bufSize];
        int b;
        while ((b = input.read(buf)) > 0) {
            output.write(buf, 0, b);
        }
        return output;
    }

    /**
     * Read all of the data from a stream, returning the contents as a <code>byte</code>
     * array.
     * 
     * @param input
     *            The stream to read from.
     * @return The contents of the stream, as a <code>byte</code> array.
     * @exception java.io.IOException
     *                If an exception occurred while reading from the stream.
     */
    public static final byte[] readStreamIntoByteArray(InputStream input)
        throws IOException {
        Assert.notNull(input);
        return (writeByteArrayStreamFrom(input).toByteArray());
    }

    /*
     * Fully read an <code> InputStream </code> , writing the data read to a
     * <code> ByteArrayOutputStream. Returns a reference to the resulting
     * stream.
     */
    private static final ByteArrayOutputStream writeByteArrayStreamFrom(InputStream input)
        throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(bufSize);
        readStreamIntoStream(input, output);
        return (output);
    }
}
