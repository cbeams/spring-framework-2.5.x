package org.springframework.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Resource implementation for java.io.File handles.
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see java.io.File
 */
public class FileResource extends AbstractResource {

	private final File file;

	/**
	 * Create a new FileResource.
	 * @param file a File handle
	 */
	public FileResource(File file) {
		this.file = file;
	}

	/**
	 * Create a new FileResource.
	 * @param path a file path
	 */
	public FileResource(String path) {
		this.file = new File(path);
	}

	public boolean exists() {
		return this.file.exists();
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	public File getFile() {
		return file;
	}

	public String getDescription() {
		return "file path [" + this.file.getAbsolutePath() + "]";
	}

}
