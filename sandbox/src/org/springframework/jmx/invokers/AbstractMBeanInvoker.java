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

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanException;
import javax.management.ObjectName;

import org.springframework.jmx.MBeanInvoker;
import org.springframework.jmx.ManagedResourceAlreadyRegisteredException;

/**
 * Abstract MBeanInvoker providing basic services for all MBeanInvoker
 * implementations.
 * 
 * @author Rob Harrop
 */
public abstract class AbstractMBeanInvoker implements MBeanInvoker {

	/**
	 * Store the managed resources. Keyed by ObjectName
	 */
	private Map managedResources = new HashMap();

	/**
	 * Register a managed resource
	 */
	public void registerManagedResource(ObjectName objectName,
			Object managedResource) {

		if (managedResources.containsKey(objectName)) {
			throw new ManagedResourceAlreadyRegisteredException(
					"The managed resource with ObjectName " + objectName
							+ " has already been registered with this invoker");
		}

		synchronized(managedResources) {
			managedResources.put(objectName, managedResource);
		}
		
		afterManagedResourceRegister(objectName, managedResource);
	}

	/**
	 * Retreive a managed resource.
	 */
	public Object getManagedResource(ObjectName objectName) {
		return managedResources.get(objectName);
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
		// TODO: this check isn't very accurate - make it more so
		if ((methodName.startsWith("get") || (methodName.startsWith("set")))) {
			throw new MBeanException(
					null,
					"Cannot access an attribute using invoke. Please use the appropriate get/setAttribute method");
		}
	}

	/**
	 * Called after a managed resource is registered. Allows sub classes
	 * to preparse a managed resource and cache any data needed.
	 *  
	 */
	protected void afterManagedResourceRegister(ObjectName objectName, Object managedResource) {
		;
	}
}