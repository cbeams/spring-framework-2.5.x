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
import javax.management.ReflectionException;

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

	/**
	 * Cglib class wrapper for the managed resource class.
	 */
	private FastClass fastClass;
	
	/**
	 * Provides a map like interface to the bean properties.
	 * Used to access the JMX attributes of the Bean.
	 */
	private BeanMap beanMap;

	private static final Map methodCache = new HashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.jmx.MBeanInvoker#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attributeName)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		
		return beanMap.get(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.jmx.MBeanInvoker#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		beanMap.put(attribute.getName(), attribute.getValue());
	}

	public Object invoke(String method, Object[] args, String[] signature)
			throws MBeanException, ReflectionException {

		// check for invalid attribute access
		checkForInvalidAttributeInvoke(method);

		return invokeInternal(method, args, signature);
	}

	private Object invokeInternal(String method, Object[] args, String[] signature) throws ReflectionException {
		FastMethod fm = getFastMethod(method, signature);
		try {
			return fm.invoke(managedResource, args);
		} catch (InvocationTargetException ex) {
			throw new ReflectionException(ex,
					"An error occured when invoking method: " + method
							+ " with signature: " + signature);
		}
	}
	
	private FastMethod getFastMethod(String method, String[] signature)
			throws ReflectionException {
		try {
			// create cache key
			MethodKey mk = new MethodKey(method, signature);
			FastMethod fm = (FastMethod) methodCache.get(mk);

			if (fm != null)
				return fm;

			fm = fastClass.getMethod(method, typeNamesToTypes(signature));
			methodCache.put(mk, fm);
			
			return fm;
		} catch (ClassNotFoundException ex) {
			throw new ReflectionException(ex, "Invalid argument type specified");
		}
	}

	protected void afterManagedResourceSet() {
		fastClass = FastClass.create(managedResource.getClass());
		beanMap = BeanMap.create(managedResource);
	}
}