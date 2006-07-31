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

package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

/**
 * Models an arbitrary location in a {@link Resource resource}.
 * 
 * <p>Typically used to track the location of problematic or erroneous
 * metadata in XML configuration files. For example, a
 * {@link #getSource() source} location might be 'The bean defined on
 * line 76 of beans.properties has an invalid Class'; another source might
 * be the actual DOM Element from a parsed XML {@link org.w3c.dom.Document}.
 * 
 * @author Rob Harrop
 * @since 2.0
 */
public class Location {

	private final Resource resource;

	private final Object source;


	/**
	 * Creates a new instance if the {@link Location} class.
	 * @param resource the resource with which this location is associated
	 */
	public Location(Resource resource) {
		this(resource, null);
	}

	/**
	 * Creates a new instance if the {@link Location} class.
	 * @param resource the resource with which this location is associated
	 * @param source the actual location within the associated resource
	 */
	public Location(Resource resource, Object source) {
		this.resource = resource;
		this.source = source;
	}


	/**
	 * Gets the resource with which this location is associated.
	 * @return resource with which this location is associated
	 */
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * Gets the actual location within the associated {@link #getResource() resource}.
	 * <p>See the {@link Location class level javadoc for this class} for examples
	 * of what the actual type of the returned object may be.
	 * @return the actual location within the associated {@link #getResource() resource}
	 */
	public Object getSource() {
		return this.source;
	}

}
