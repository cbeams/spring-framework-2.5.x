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
package org.springframework.jmx.invokers.cglib;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.invokers.AbstractMBeanInvoker;
import org.springframework.jmx.invokers.MethodKey;

import net.sf.cglib.beans.BeanMap;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * @author Rob Harrop
 *  
 */
public class CglibMBeanInvoker extends AbstractMBeanInvoker {

	private static final Map fastClassCache  = new HashMap();
	private static final Map methodCache = new HashMap();
	private static final Map beanMapCache = new HashMap();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.jmx.MBeanInvoker#getAttribute(java.lang.String)
	 */
	public Object getAttribute(ObjectName objectName, String attributeName)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		BeanMap beanMap = getBeanMap(objectName);
		return beanMap.get(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.jmx.MBeanInvoker#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(ObjectName objectName, Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		BeanMap beanMap = getBeanMap(objectName);
		beanMap.put(attribute.getName(), attribute.getValue());
	}

	public Object invoke(ObjectName objectName, String method, Object[] args, String[] signature)
			throws MBeanException, ReflectionException {

		// check for invalid attribute access
		checkForInvalidAttributeInvoke(method);

		return invokeInternal(objectName, method, args, signature);
	}

	private Object invokeInternal(ObjectName objectName, String method, Object[] args, String[] signature) throws ReflectionException {
		Object managedResource = getManagedResource(objectName);
		
		FastMethod fm = getFastMethod(managedResource, method, signature);
		try {
			return fm.invoke(managedResource, args);
		} catch (InvocationTargetException ex) {
			throw new ReflectionException(ex,
					"An error occured when invoking method: " + method
							+ " with signature: " + signature);
		}
	}
	
	private FastMethod getFastMethod(Object managedResource, String method, String[] signature)
			throws ReflectionException {
		try {
			// create cache key
			MethodKey mk = new MethodKey(method, signature);
			FastMethod fm = (FastMethod) methodCache.get(mk);

			if (fm != null)
				return fm;

			FastClass fastClass = getFastClass(managedResource);
			fm = fastClass.getMethod(method, JmxUtils.typeNamesToTypes(signature));
			methodCache.put(mk, fm);
			
			return fm;
		} catch (ClassNotFoundException ex) {
			throw new ReflectionException(ex, "Invalid argument type specified");
		}
	}

	protected void afterManagedResourceRegister(ObjectName objectName, Object managedResource) {
		FastClass fastClass = FastClass.create(managedResource.getClass());
		
		synchronized(fastClassCache) {
			fastClassCache.put(managedResource.getClass(), fastClass);
		}
		
		BeanMap beanMap = BeanMap.create(managedResource);
		
		synchronized(beanMapCache) {
			beanMapCache.put(objectName, beanMap);
		}
	}
	
	private FastClass getFastClass(Object managedResource) {
		return (FastClass)fastClassCache.get(managedResource.getClass());
	}
	
	private BeanMap getBeanMap(ObjectName objectName) {
		return (BeanMap)beanMapCache.get(objectName);
	}
}