/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
     * @throws java.io.IOException
     *             If an exception occurred while reading from the stream.
     * @throws IllegalArgumentException
     *             If the input or output stream parameters are null.
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
     * @throws java.io.IOException
     *             if an exception occurred while reading from the stream.
     * @throws IllegalArgumentException
     *             if the input stream parameter is null.
     */
    public static final byte[] readStreamIntoByteArray(InputStream input)
        throws IOException {
        Assert.notNull(input);
        return (writeByteArrayStreamFrom(input).toByteArray());
    }

    /*
     * Fully read an <code> InputStream </code> , writing the data read to a
     * <code> ByteArrayOutputStream </code> . Returns a reference to the
     * resulting stream.
     */
    private static final ByteArrayOutputStream writeByteArrayStreamFrom(InputStream input)
        throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(bufSize);
        readStreamIntoStream(input, output);
        return (output);
    }
}
