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

package org.springframework.jmx.support;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Wraps the creation of <code>ObjectName</code> instances.
 *
 * <p><code>ObjectName</code> instances will be cached if your
 * JMX implementation supports caching for <code>ObjectName</code>s.
 *
 * @author Rob Harrop
 * @since 1.2
 */
public class ObjectNameManager {

	/**
	 * Flag that indicates whether the <code>ObjectName.getInstance()</code> methods are available.
	 */
	private static boolean getInstanceAvailable;

	/**
	 * Detects whether or not the <code>getInstance</code> method on ObjectName is available
	 * and sets the <code>getInstanceAvailable</code> flag appropriately.
	 */

	static {
		try {
			ObjectName.class.getMethod("getInstance", new Class[] {String.class});
			getInstanceAvailable = true;
		}
		catch (NoSuchMethodException ex) {
			getInstanceAvailable = false;
		}
	}

	/**
	 * Retrieve the <code>ObjectName</code> instance corresponding to the supplied name.
	 * @param objectName the <code>ObjectName</code> in <code>String</code> format.
	 * @return the <code>ObjectName</code> instance.
	 * @throws MalformedObjectNameException in case of an invalid object name specification
	 * @see ObjectName#ObjectName(String)
	 * @see ObjectName#getInstance(String)
	 */
	public static ObjectName getInstance(String objectName) throws MalformedObjectNameException {
		if (getInstanceAvailable) {
			return ObjectName.getInstance(objectName);
		}
		else {
			return new ObjectName(objectName);
		}
	}

	/**
	 * Retrieve an <code>ObjectName</code> instance for the specified domain and a
	 * single property with the supplied key and value.
	 * @param domainName the domain name for the <code>ObjectName</code>
	 * @param key the key for the single property in the <code>ObjectName</code>
	 * @param value the value for the single property in the <code>ObjectName</code>
	 * @return the <code>ObjectName</code> instance
	 * @throws MalformedObjectNameException in case of an invalid object name specification
	 * @see ObjectName#ObjectName(String, String, String)
	 * @see ObjectName#getInstance(String, String, String)
	 */
	public static ObjectName getInstance(String domainName, String key, String value)
			throws MalformedObjectNameException {

		if (getInstanceAvailable) {
			return ObjectName.getInstance(domainName, key, value);
		}
		else {
			return new ObjectName(domainName, key, value);
		}
	}

	/**
	 * Retrieve an <code>ObjectName</code> instance with the specified domain name
	 * and the supplied key/name properties.
	 * @param domainName the domain name for the <code>ObjectName</code>
	 * @param properties the properties for the <code>ObjectName</code>
	 * @return the <code>ObjectName</code> instance
	 * @throws MalformedObjectNameException in case of an invalid object name specification
	 * @see ObjectName#ObjectName(String, java.util.Hashtable)
	 * @see ObjectName#getInstance(String, java.util.Hashtable)
	 */
	public static ObjectName getInstance(String domainName, Hashtable properties)
			throws MalformedObjectNameException {

		if (getInstanceAvailable) {
			return ObjectName.getInstance(domainName, properties);
		}
		else {
			return new ObjectName(domainName, properties);
		}
	}

}
