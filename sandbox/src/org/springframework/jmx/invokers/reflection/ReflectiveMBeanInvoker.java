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
package org.springframework.jmx.invokers.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.invokers.AbstractMBeanInvoker;
import org.springframework.jmx.invokers.MethodKey;

/**
 * An implementation of <tt>MBeanInvoker</tt> that uses reflection
 * to get/set attribute values and to invoke methods. Methods
 * that are accessed using reflection are cached for increased
 * speed on subsequent invocations of the same method.
 * @author Rob Harrop
 */
public class ReflectiveMBeanInvoker extends AbstractMBeanInvoker {

	/**
	 * Cache to store methods retreived using reflection for faster
	 * retreival on subsequent invocations.
	 * @see ReflectiveMBeanInvoker.MethodKey
	 */
	private static final Map methodCache = new HashMap();
	
	private static final Map beanWrapperCache = new HashMap();

	/**
	 * Retreive the value of the named attribute using reflection.
	 * @param attributeName The name of the attribute whose value you want to retreive
	 * @return The value of the named attribute
	 */
	public Object getAttribute(ObjectName objectName, String attributeName)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		BeanWrapper beanWrapper = getBeanWrapper(objectName);
		return beanWrapper.getPropertyValue(attributeName);
	}

	public void setAttribute(ObjectName objectName, Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		BeanWrapper beanWrapper = getBeanWrapper(objectName);
		beanWrapper.setPropertyValue(attribute.getName(), attribute
				.getValue());
	}

	/**
	 * Invoke a method using reflection.
	 */
	public Object invoke(ObjectName objectName, String method, Object[] args, String[] signature)
			throws MBeanException, ReflectionException {

		Object managedResource = getManagedResource(objectName);
		
		// check for invalid attribute invocation
		checkForInvalidAttributeInvoke(method);

		// get cache key
		MethodKey mk = new MethodKey(method, signature);

		// attempt to retreive from cache
		Method m = (Method) methodCache.get(mk);
	
		
		try {

			// if the method was not in cache then locate it
			if (m == null) {
				Class[] types = JmxUtils.typeNamesToTypes(signature);

				m = managedResource.getClass().getMethod(method, types);
				methodCache.put(mk, m);
			}
			
			return m.invoke(managedResource, args);

		} catch (NoSuchMethodException ex) {
			throw new ReflectionException(ex, "Unable to find method: "
					+ method + " with signature: " + signature);
		} catch (InvocationTargetException ex) {
			throw new ReflectionException(ex,
					"An error occured when invoking method: " + method
							+ " with signature: " + signature);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex, "Access to method: " + method
					+ " is denied");
		} catch (ClassNotFoundException ex) {
			throw new ReflectionException(ex, "Invalid argument type specified");
		}
	}


	protected void afterManagedResourceRegister(ObjectName objectName, Object managedResource) {
		BeanWrapper beanWrapper = new BeanWrapperImpl(managedResource);
	
		synchronized(beanWrapperCache) {
			beanWrapperCache.put(objectName, beanWrapper);
		}
	}
	
	private BeanWrapper getBeanWrapper(ObjectName objectName) {
		return (BeanWrapper)beanWrapperCache.get(objectName);
	}
}