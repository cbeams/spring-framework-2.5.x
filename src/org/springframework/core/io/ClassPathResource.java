/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Resource implementation for class path resources.
 * Uses either the thread context class loader, a given ClassLoader
 * or a given Class for loading resources.
 *
 * <p>Supports resolution as File if the class path resource
 * resides in the file system, but not for resources in a JAR.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see java.lang.Thread#getContextClassLoader
 * @see java.lang.ClassLoader#getResourceAsStream
 * @see java.lang.Class#getResourceAsStream
 */
public class ClassPathResource extends AbstractResource {

	private final String path;

	private ClassLoader classLoader;

	private Class clazz;

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * <p>The thread context class loader will be used for
	 * loading the resource.
	 * @param path the absolute path within the classpath
	 * @see java.lang.ClassLoader#getResourceAsStream
	 * @see java.lang.Thread#getContextClassLoader
	 */
	public ClassPathResource(String path) {
		this(path, (ClassLoader) null);
	}

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * @param path the absolute path within the classpath
	 * @param classLoader the class loader to load the resource with
	 * @see java.lang.ClassLoader#getResourceAsStream
	 */
	public ClassPathResource(String path, ClassLoader classLoader) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		this.path = path;
		this.classLoader = classLoader;
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
			ClassLoader cl = this.classLoader;
			if (cl == null) {
				// no class loader specified -> use thread context class loader
				cl = Thread.currentThread().getContextClassLoader();
			}
			is = cl.getResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	public URL getURL() throws IOException {
		URL url = null;
		if (this.clazz != null) {
			url = this.clazz.getResource(this.path);
		}
		else {
			ClassLoader cl = this.classLoader;
			if (cl == null) {
				// no class loader specified -> use thread context class loader
				cl = Thread.currentThread().getContextClassLoader();
			}
			url = cl.getResource(this.path);
		}
		if (url == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	public File getFile() throws IOException {
		URL url = getURL();
		if (!URL_PROTOCOL_FILE.equals(url.getProtocol())) {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: URL=[" + url + "]");
		}
		return new File(URLDecoder.decode(url.getFile()));
	}

	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return new ClassPathResource(pathToUse, this.clazz);
	}

	public String getFilename() {
		return StringUtils.getFilename(this.path);
	}

	public String getDescription() {
		return "class path resource [" + this.path + "]";
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ClassPathResource) {
			ClassPathResource otherRes = (ClassPathResource) obj;
			return (this.path.equals(otherRes.path) && ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
		}
		return false;
	}

	public int hashCode() {
		return this.path.hashCode();
	}

}
