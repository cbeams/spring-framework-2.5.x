package org.springframework.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Resource implementation for java.io.File handles.
 *
 * <p>Obviously supports resolution as File, and also as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see java.io.File
 */
public class FileSystemResource extends AbstractResource {

	private final File file;

	/**
	 * Create a new FileSystemResource.
	 * @param file a File handle
	 */
	public FileSystemResource(File file) {
		this.file = file;
	}

	/**
	 * Create a new FileSystemResource.
	 * @param path a file path
	 */
	public FileSystemResource(String path) {
		this.file = new File(path);
	}

	public boolean exists() {
		return this.file.exists();
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	public URL getURL() throws IOException {
		return new URL(URL_PROTOCOL_FILE + ":" + this.file.getAbsolutePath());
	}

	public File getFile() {
		return file;
	}

	public String getDescription() {
		return "file path [" + this.file.getAbsolutePath() + "]";
	}

}
