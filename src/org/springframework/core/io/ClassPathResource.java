package org.springframework.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Resource implementation for classpath resources.
 * Uses either the Thread context class loader
 * or a given Class for loading resources.
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see java.lang.Thread#getContextClassLoader
 * @see java.lang.ClassLoader#getResourceAsStream
 * @see java.lang.Class#getResourceAsStream
 */
public class ClassPathResource extends AbstractResource {

	private final String path;

	private Class clazz;

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * @param path the absolute path within the classpath
	 * @see java.lang.ClassLoader#getResourceAsStream
	 */
	public ClassPathResource(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		this.path = path;
	}

	/**
	 * Create a new ClassPathResource for Class usage.
	 * The path can be relative to the given class,
	 * or absolute within the classpath via a leading slash.
	 * @param path relative or absolute path within the classpath
	 * @param clazz the class to load resources with
	 * @see java.lang.Class#getResourceAsStream
	 */
	public ClassPathResource(String path, Class clazz) {
		this.path = path;
		this.clazz = clazz;
	}

	public InputStream getInputStream() throws IOException {
		InputStream is = null;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else {
			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			is = ccl.getResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	public String getDescription() {
		return "classpath resource [" + this.path + "]";
	}

}
