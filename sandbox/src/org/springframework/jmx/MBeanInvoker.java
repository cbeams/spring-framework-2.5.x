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
package org.springframework.jmx;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Represents a generic invocation model for 
 * JMX managed resources. An implementation of this interface
 * provides all the invocation abilities required for the ModelMBean.
 * @author Rob Harrop
 */
public interface MBeanInvoker {

	/**
	 * Called by the framework to register a resource as being managed by
	 * this invoker.
	 * @param objectName The <tt>ObjectName</tt> instance associated with the resource.
	 * @param managedResource The managed resource
	 */
	public void registerManagedResource(ObjectName objectName, Object managedResource);
	
    /**
     * Retreive the value of the specified attribute
     * @param attributeName The name of the attribute to retreive
     * @return The attribute value
     * @throws AttributeNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     */
    public Object getAttribute(ObjectName objectName, String attributeName)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException;


    public void setAttribute(ObjectName objectName, Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException;

    /**
     * Invoke a method on a managed resource
     * @param method The <tt>Method</tt> to invoke.
     * @param args The arguments to pass to the method
     * @param signature The method signature
     * @return The result of the method invocation.
     * @throws MBeanException
     * @throws ReflectionException
     */
    public Object invoke(ObjectName objectName, String method, Object[] args, String[] signature)
            throws MBeanException, ReflectionException;
}