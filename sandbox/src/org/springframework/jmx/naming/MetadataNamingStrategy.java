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

package org.springframework.jmx.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.metadata.CommonsJmxAttributeSource;
import org.springframework.jmx.metadata.JmxAttributeSource;
import org.springframework.jmx.metadata.ManagedResource;
import org.springframework.jmx.util.ObjectNameManager;
import org.springframework.util.StringUtils;

/**
 * An implementation of the <code>ObjectNamingStrategy</code> interface
 * that reads the the <code>ObjectName</code> from the source level
 * metadata.
 * @author Rob Harrop
 * @see ObjectNamingStrategy
 */
public class MetadataNamingStrategy implements ObjectNamingStrategy {

	/**
	 * The <code>JmxAttributeSource</code> implementation to use for reading
	 * metdata. Uses <code>CommonsJmxAttributeSource</code> by
	 * default.
	 */
	private JmxAttributeSource attributeSource = new CommonsJmxAttributeSource();

	/**
	 * Set the implementation of the <code>JmxAttributeSource</code> interface to use
	 * when reading the source level metadata.
	 * @param attributeSource An implementation of the <code>JmxAttributeSource</code> interface.
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		this.attributeSource = attributeSource;
	}

	/**
	 * Reads the <code>ObjectName</code> from the source level metadata associated
	 * with the managed resource's <code>Class</code>.
	 */
	public ObjectName getObjectName(Object managedResource, String key) throws MalformedObjectNameException {
		ManagedResource mr = this.attributeSource.getManagedResource(managedResource.getClass());

		// check that the managed resource attribute has been specified
		if (mr == null) {
			throw new MalformedObjectNameException("Your bean class [" + managedResource.getClass().getName() +
					"] must be marked with a valid ManagedResource attribute when using MetadataNamingStrategy");
		}

		// check that an object name has been specified
		String objectName = mr.getObjectName();

		if (!StringUtils.hasText(objectName)) {
			throw new MalformedObjectNameException("You must specify an ObjectName for Class: " +
					managedResource.getClass().getName());
		}

		// now try to parse the name
		return ObjectNameManager.getInstance(objectName);
	}

}
