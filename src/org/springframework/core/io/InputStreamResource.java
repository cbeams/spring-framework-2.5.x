package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource implementation for a given InputStream. Should only
 * be used if no specific Resource implementation is applicable.
 *
 * <p>In contrast to other Resource implementations, this is a descriptor
 * for an <i>already opened</i> resource - therefore returning true on
 * isOpen(). Do not use it if you need to keep the resource descriptor
 * somewhere, or if you need to read a stream multiple times.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 */
public class InputStreamResource extends AbstractResource {

	private InputStream inputStream;

	private final String description;

	/**
	 * Create a new InputStreamResource.
	 * @param inputStream the InputStream to use
	 * @param description where the InputStream comes from
	 */
	public InputStreamResource(InputStream inputStream, String description) {
		this.inputStream = inputStream;
		this.description = description;
	}

	public boolean exists() {
		return true;
	}

	public boolean isOpen() {
		return true;
	}

	/**
	 * This implementation throws IllegalStateException if attempting to
	 * read the underlying stream multiple times.
	 */
	public InputStream getInputStream() throws IOException, IllegalStateException {
		if (this.inputStream == null) {
			throw new IllegalStateException("InputStream has already been read - " +
			                                "do not use InputStreamResource if a stream needs to be read multiple times");
		}
		InputStream result = this.inputStream;
		this.inputStream = null;
		return result;
	}

	public String getDescription() {
		return description;
	}

}
