package org.springframework.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of resource, like file or classpath resource. Can also represent
 * a resource handle with an open stream.
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a File handle can just be returned for resources
 * in the file system.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 */
public interface Resource {

	/**
	 * Return whether this resource actually exists in physical form.
	 */
	boolean exists();

	/**
	 * Return whether this resource represents a handle with an open
	 * stream. If true, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * <p>Will be false for all usual resource descriptors.
	 */
	boolean isOpen();

	/**
	 * Return an InputStream for this resource.
	 * @throws IOException if the resource could not be opened
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Return a File handle for this resource.
	 * @throws IOException if the resource cannot be resolved as absolute
	 * file path, i.e. if the resource is not available in a file system
	 */
	File getFile() throws IOException;

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * <p>Implementations are also encouraged to return this value
	 * from their toString method.
	 * @see java.lang.Object#toString
	 */
	String getDescription();

}
