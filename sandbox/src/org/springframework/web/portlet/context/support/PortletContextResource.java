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

package org.springframework.web.portlet.context.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.portlet.PortletContext;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Resource implementation for PortletContext resources,
 * interpreting relative paths within the portlet application root.
 *
 * <p>Always supports stream access, but only allows java.io.File
 * access when the portlet application archive is expanded.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see javax.portlet.PortletContext#getResourceAsStream
 * @see javax.portlet.PortletContext#getRealPath
 */
public class PortletContextResource extends AbstractResource {

	private final PortletContext portletContext;

	private final String path;

	/**
	 * Create a new PortletContextResource.
	 * @param portletContext the PortletContext to load from
	 * @param path the path of the resource
	 */
	public PortletContextResource(PortletContext portletContext, String path) {
		this.portletContext = portletContext;
		this.path = path;
	}

	/**
	 * This implementation delegates to PortletContext.getResourceAsStream,
	 * but throws a FileNotFoundException if not found.
	 * @see javax.portlet.PortletContext#getResourceAsStream
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = this.portletContext.getResourceAsStream(this.path);
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	public URL getURL() throws IOException {
		URL url = this.portletContext.getResource(this.path);
		if (url == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	/**
	 * This implementation delegates to PortletContext.getRealPath,
	 * but throws a FileNotFoundException if not found or not resolvable.
	 * @see javax.portlet.PortletContext#getRealPath
	 */
	public File getFile() throws IOException {
		String realPath = this.portletContext.getRealPath(this.path);
		if (realPath == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to absolute file path - portlet application archive not expanded?");
		}
		return new File(realPath);
	}

	public Resource createRelative(String relativePath) throws IOException {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return new PortletContextResource(this.portletContext, pathToUse);
	}

	public String getFilename() {
		return StringUtils.getFilename(this.path);
	}

	public String getDescription() {
		return "resource [" + this.path + "] of PortletContext";
	}

}
