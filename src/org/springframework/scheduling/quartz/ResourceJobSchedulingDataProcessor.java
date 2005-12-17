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

package org.springframework.scheduling.quartz;

import java.io.IOException;
import java.io.InputStream;

import org.quartz.xml.JobSchedulingDataProcessor;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.SchedulingException;

/**
 * Subclass of Quartz' JobSchedulingDataProcessor that considers
 * given filenames as Spring resource locations.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.core.io.ResourceLoader
 */
public class ResourceJobSchedulingDataProcessor extends JobSchedulingDataProcessor
    implements ResourceLoaderAware {

	private ResourceLoader resourceLoader = new DefaultResourceLoader();


	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}


	protected InputStream getInputStream(String fileName) {
		try {
			return this.resourceLoader.getResource(fileName).getInputStream();
		}
		catch (IOException ex) {
			throw new SchedulingException("Could not load job scheduling data XML file", ex);
		}
	}

}
