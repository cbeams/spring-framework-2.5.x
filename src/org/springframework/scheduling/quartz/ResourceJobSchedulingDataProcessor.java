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

package org.springframework.scheduling.quartz;

import java.io.IOException;
import java.io.InputStream;

import org.quartz.xml.JobSchedulingDataProcessor;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Subclass of Quartz' JobSchedulingDataProcessor that considers
 * given filenames as Spring resource locations.
 * @author Juergen Hoeller
 * @since 07.06.2004
 * @see org.springframework.core.io.ResourceLoader
 */
public class ResourceJobSchedulingDataProcessor extends JobSchedulingDataProcessor
    implements ResourceLoaderAware {

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	protected InputStream getInputStream(String fileName) {
		try {
			return this.resourceLoader.getResource(fileName).getInputStream();
		}
		catch (IOException ex) {
			throw new JobSchedulingDataInitializationException(ex);
		}
	}


	/**
	 * Exception to be thrown if a resource cannot be loaded.
	 */
	public static class JobSchedulingDataInitializationException extends NestedRuntimeException {

		private JobSchedulingDataInitializationException(IOException ex) {
			super("Could not load job scheduling data XML file", ex);
		}
	}

}
