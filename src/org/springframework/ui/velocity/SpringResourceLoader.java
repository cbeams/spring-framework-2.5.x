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

package org.springframework.ui.velocity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Velocity ResourceLoader adapter that loads via a Spring ResourceLoader.
 * Used by VelocityEngineFactory for any resource loader path that cannot
 * be resolved to a java.io.File.
 *
 * <p>Note that this loader does not allow for modification detection:
 * Use Velocity's default FileResourceLoader for java.io.File resources.
 *
 * <p>Expects "spring.resource.loader" and "spring.resource.loader.path"
 * application attributes in the Velocity runtime: the former of type
 * org.springframework.core.io.ResourceLoader, the latter a String.
 *
 * @author Juergen Hoeller
 * @since 14.03.2004
 * @see VelocityEngineFactory#setResourceLoaderPath
 * @see org.springframework.core.io.ResourceLoader
 * @see org.apache.velocity.runtime.resource.loader.FileResourceLoader
 */
public class SpringResourceLoader extends ResourceLoader {

	public static final String NAME = "spring";

	public static final String SPRING_RESOURCE_LOADER_CLASS = "spring.resource.loader.class";

	public static final String SPRING_RESOURCE_LOADER = "spring.resource.loader";

	public static final String SPRING_RESOURCE_LOADER_PATH = "spring.resource.loader.path";


	protected final Log logger = LogFactory.getLog(getClass());

	private org.springframework.core.io.ResourceLoader resourceLoader;

	private String resourceLoaderPath;


	public void init(ExtendedProperties configuration) {
		this.resourceLoader = (org.springframework.core.io.ResourceLoader)
				this.rsvc.getApplicationAttribute(SPRING_RESOURCE_LOADER);
		this.resourceLoaderPath = (String) this.rsvc.getApplicationAttribute(SPRING_RESOURCE_LOADER_PATH);
		if (this.resourceLoader == null) {
			throw new IllegalArgumentException("'resourceLoader' application attribute must be present " +
																				 "for SpringResourceLoader");
		}
		if (this.resourceLoaderPath == null) {
			throw new IllegalArgumentException("'resourceLoaderPath' application attribute must be present " +
																				 "for SpringResourceLoader");
		}
		if (!this.resourceLoaderPath.endsWith("/")) {
			this.resourceLoaderPath += "/";
		}
		logger.info("SpringResourceLoader for Velocity: using resource loader [" + this.resourceLoader +
								"] and resource loader path [" + this.resourceLoaderPath + "]");
	}

	public InputStream getResourceStream(String source) throws ResourceNotFoundException {
		if (logger.isDebugEnabled()) {
			logger.debug("Looking for Velocity resource with name [" + source + "]");
		}
		try {
			return this.resourceLoader.getResource(this.resourceLoaderPath + source).getInputStream();
		}
		catch (IOException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not find Velocity resource [" + this.resourceLoaderPath + source + "]", ex);
			}
			throw new ResourceNotFoundException(ex.getMessage());
		}
	}

	public boolean isSourceModified(Resource resource) {
		return false;
	}

	public long getLastModified(Resource resource) {
		return 0;
	}

}
