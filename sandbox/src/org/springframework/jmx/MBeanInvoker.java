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
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * Represents a generic invocation model for 
 * JMX managed resources. An implementation of this interface
 * provides all the invocation abilities required for the ModelMBean.
 * @author Rob Harrop
 */
public interface MBeanInvoker {

	/**
	 * Called by <tt>JmxMBeanAdapter</tt>. Provides the invoker with
	 * the resource on which invocations are to be made.
	 * @param managedResource
	 */
    public void setManagedResource(Object managedResource);

    /**
     * Return the resource upon which invocations are targeted.
     * @return The managed resource.
     */
    public Object getManagedResource();

    public Object getAttribute(String attributeName)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException;

    public AttributeList getAttributes(String[] attributeNames);

    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException;

    public AttributeList setAttributes(AttributeList attributes);

    public Object invoke(String method, Object[] args, String[] signature)
            throws MBeanException, ReflectionException;
}