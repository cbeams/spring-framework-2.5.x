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
package org.springframework.jmx.invokers;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanException;

import org.springframework.util.ClassUtils;
import org.springframework.jmx.MBeanInvoker;

/**
 * Abstract MBeanInvoker providing basic services for all MBeanInvoker
 * implementations.
 * 
 * @author Rob Harrop
 */
public abstract class AbstractMBeanInvoker implements MBeanInvoker {

	/**
	 * Store a reference to the resource managed by this invoker
	 */
	protected Object managedResource = null;

	/**
	 * Set the resource to be managed.
	 */
	public void setManagedResource(Object managedResource) {
		this.managedResource = managedResource;
		afterManagedResourceSet();
	}

	/**
	 * Retreive the managed resource.
	 */
	public Object getManagedResource() {
		return this.managedResource;
	}

	/**
	 * Converts an array of type names into an array of <tt>Class</tt>
	 * instances.
	 * 
	 * @param typeNames
	 *            The names of the types to obtain
	 * @return An array of <tt>Class</tt>
	 */
	protected Class[] typeNamesToTypes(String[] typeNames)
			throws ClassNotFoundException {
		Class[] types = null;

		if ((typeNames != null) && (typeNames.length > 0)) {
			types = new Class[typeNames.length];

			for (int x = 0; x < typeNames.length; x++) {
				types[x] = ClassUtils.forName(typeNames[x]);
			}
		}

		return types;
	}

	/**
	 * Checks to see if an attribute is being accessed as an operation rather
	 * than as an attribute. This behavior is forbidden by the JMX 1.2
	 * specification.
	 * 
	 * @param methodName
	 */
	protected void checkForInvalidAttributeInvoke(String methodName)
			throws MBeanException {
		//TODO: This is probably not a good idea - would be useful to be able
		// to turn this off.
		if ((methodName.startsWith("get")) || (methodName.startsWith("set"))) {
			throw new MBeanException(
					null,
					"Cannot access an attribute using invoke. Please use the appropriate get/setAttribute method");
		}
	}

	/**
	 * Called after the managed resource is stored. Allows subclasses to
	 * prebuild any operation or attribute caches as required.
	 *  
	 */
	protected void afterManagedResourceSet() {
		;
	}

	/**
	 * Retreive a set of attributes matching the names provided.
	 * @param attributeNames The names of the attributes whose values you wish to retreive
	 * @return An <tt>AttributeList</tt> instance containing the retreived attributes and their values.
	 */
	public AttributeList getAttributes(String[] attributeNames) {
		AttributeList attributes = new AttributeList();
	
		for (int x = 0; x < attributeNames.length; x++) {
			try {
				attributes.add(new Attribute(attributeNames[x],
						getAttribute(attributeNames[x])));
			} catch (JMException ex) {
				// TODO: do we skip or fail here?
			}
		}
		return attributes;
	}

	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList al = new AttributeList();
	
		for (int x = 0; x < attributes.size(); x++) {
			Attribute a = (Attribute) attributes.get(x);
			try {
				setAttribute(a);
				al.add(a);
			} catch (JMException ex) {
				// ignore any errors
			}
		}
	
		return al;
	}

}