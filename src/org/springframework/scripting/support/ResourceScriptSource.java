/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.scripting.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * ScriptSource implementation based on Spring's Resource abstraction.
 * Loads the script text from the underlying Resource's InputStream
 * and tracks the file timestamp of the Resource (if possible).
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.core.io.Resource#getInputStream()
 * @see org.springframework.core.io.Resource#getFile()
 * @see org.springframework.core.io.ResourceLoader
 */
public class ResourceScriptSource implements ScriptSource {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private final Resource resource;

	private long lastModified = -1;


	/**
	 * Create a new ResourceScriptSource for the given resource.
	 * @param resource the Resource to load the script from
	 */
	public ResourceScriptSource(Resource resource) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
	}

	/**
	 * Return the Resource to load the script from.
	 */
	public final Resource getResource() {
		return resource;
	}


	public String getScriptAsString() throws IOException {
		this.lastModified = retrieveLastModifiedTime();
		InputStream is = this.resource.getInputStream();
		return FileCopyUtils.copyToString(new InputStreamReader(is));
	}

	public boolean isModified() {
		if (this.lastModified < 0) {
			return true;
		}
		return (retrieveLastModifiedTime() > this.lastModified);
	}


	/**
	 * Retrieve the current last-modified timestamp of the
	 * underlying timestamp.
	 * @return the current timestamp, or 0 if not determinable
	 */
	protected long retrieveLastModifiedTime() {
		try {
			File file = getResource().getFile();
			return file.lastModified();
		}
		catch (IOException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						getResource() + " could not be resolved in the file system - current timestamp not available", ex);
			}
			return 0;
		}
	}


	public String toString() {
		return this.resource.toString();
	}

}
