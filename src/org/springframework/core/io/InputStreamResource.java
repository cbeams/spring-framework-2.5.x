package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource implementation for a given InputStream. Should only
 * be used if no specific Resource implementation is applicable.
 * @author Juergen Hoeller
 * @since 28.12.2003
 */
public class InputStreamResource extends AbstractResource {

	private final InputStream inputStream;

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

	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	public String getDescription() {
		return description;
	}

}
