package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Convenience base class for Resource implementations,
 * pre-implementing typical behavior.
 *
 * <p>The "exists" method will check whether an InputStream can be opened;
 * "isOpen" will always return false; "getFile" throws an exception;
 * and "toString" will return the description.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 */
public abstract class AbstractResource implements Resource {

	/**
	 * This implementations checks whether an InputStream can be opened.
	 */
	public boolean exists() {
		try {
			InputStream is = getInputStream();
			is.close();
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	/**
	 * This implementations always returns false.
	 */
	public boolean isOpen() {
		return false;
	}

	/**
	 * This implementations throws a FileNotFoundException, assuming
	 * that the resource cannot be resolved to an absolute file path.
	 */
	public File getFile() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
	}

	/**
	 * This implementations returns the description of this resource.
	 * @see #getDescription
	 */
	public String toString() {
		return getDescription();
	}

}
