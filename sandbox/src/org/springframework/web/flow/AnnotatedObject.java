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
package org.springframework.web.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;

/**
 * Superclass of all objects in the web flow system that support annotation
 * using simple properties.
 * 
 * @author Erwin Vervaet
 */
public abstract class AnnotatedObject implements MutableAttributeSource {

	/**
	 * Additional properties further describing this object. 
	 */
	private Map properties = new HashMap();

	/**
	 * Returns the additional properties describing this object.
	 */
	public Map getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Set the additional properties describing this object.
	 */
	public void setProperties(Map properties) {
		if (properties != null) {
			this.properties = new HashMap(properties);
		}
	}

	/**
	 * Returns the value of given property, or null if not found.
	 */
	public Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	
	/**
	 * Set the value of named property.
	 * @param propertyName the name of the property
	 * @param value the value to set
     * @return previous value associated with specified name
	 */
	public Object setProperty(String propertyName, Object value) {
		return this.properties.put(propertyName, value);
	}

	// implementing MutableAttributeSource
	
	public boolean containsAttribute(String attributeName) {
		return new MapAttributeSource(this.properties).containsAttribute(attributeName);
	}
	
	public Object getAttribute(String attributeName) {
		return new MapAttributeSource(this.properties).getAttribute(attributeName);
	}
	
	public Object setAttribute(String attributeName, Object attributeValue) {
		return setProperty(attributeName, attributeValue);
	}
}