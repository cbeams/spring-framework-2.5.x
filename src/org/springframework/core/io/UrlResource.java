/*
 * Copyright 2002-2005 the original author or authors.
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * Resource implementation for java.net.URL locators.
 * Obviously supports resolution as URL, and also as File
 * in case of the "file:" protocol.
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see java.net.URL
 */
public class UrlResource extends AbstractResource {

	private final URL url;

	/**
	 * Create a new UrlResource.
	 * @param url a URL
	 */
	public UrlResource(URL url) {
		Assert.notNull(url, "url is required");
		this.url = url;
	}

	/**
	 * Create a new UrlResource.
	 * @param path a URL path
	 */
	public UrlResource(String path) throws MalformedURLException {
		Assert.notNull(path, "path is required");
		this.url = new URL(path);
	}

	public InputStream getInputStream() throws IOException {
		return this.url.openStream();
	}

	public URL getURL() throws IOException {
		return this.url;
	}

	public File getFile() throws IOException {
		return ResourceUtils.getFile(this.url, getDescription());
	}

	public Resource createRelative(String relativePath) throws MalformedURLException {
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}
		return new UrlResource(new URL(this.url, relativePath));
	}

	public String getFilename() {
		return new File(this.url.getFile()).getName();
	}

	public String getDescription() {
		return "URL [" + this.url + "]";
	}

	public boolean equals(Object obj) {
		return (obj == this ||
		    (obj instanceof UrlResource && this.url.equals(((UrlResource) obj).url)));
	}

	public int hashCode() {
		return this.url.hashCode();
	}

}
