package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple interface for objects that are sources for java.io.InputStreams.
 * Base interface for Spring's Resource interface.
 * @author Juergen Hoeller
 * @since 20.01.2004
 * @see Resource
 */
public interface InputStreamSource {

	/**
	 * Return an InputStream.
	 * It is expected that each call creates a <i>fresh</i> stream.
	 * @throws IOException if the stream could not be opened
	 */
	InputStream getInputStream() throws IOException;

}
