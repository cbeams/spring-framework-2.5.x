package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Resource implementations for URLs.
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see java.net.URL
 */
public class UrlResource extends AbstractResource {

	public static final String PROTOCOL_FILE = "file";

	private final URL url;

	/**
	 * Create a new UrlResource.
	 * @param url a URL
	 */
	public UrlResource(URL url) {
		this.url = url;
	}

	public InputStream getInputStream() throws IOException {
		return this.url.openStream();
	}

	public File getFile() throws IOException {
		if (PROTOCOL_FILE.equals(this.url.getProtocol())) {
			return new File(this.url.getFile());
		}
		else {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path - " +
																			"no 'file:' protocol");
		}
	}

	public String getDescription() {
		return "URL [" + this.url + "]";
	}

}
