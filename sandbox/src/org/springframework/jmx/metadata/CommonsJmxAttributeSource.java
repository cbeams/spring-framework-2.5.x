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

package org.springframework.jmx.metadata;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.beans.BeanUtils;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * Implementation of the <code>JmxAttributeSource</code> interface that
 * reads metadata created using the Commons Attributes metatdaa mechanism.
 * @author Rob Harrop
 */
public class CommonsJmxAttributeSource implements JmxAttributeSource {

	private Attributes attributes = new CommonsAttributes();

	/**
	 * If the specified class has a <code>ManagedResource</code> attribute, then it is returned.
	 * Otherwise returns null. An <code>InvalidMetadataException</code> is thrown if more than one
	 * ManagedResource attribute exists.
	 * @param cls The <code>Class</code> to read the attribute data from.
	 * @return The attribute if found, otherwise <code>null<code>.
	 */
	public ManagedResource getManagedResource(Class cls) {
		Collection attrs = attributes.getAttributes(cls, ManagedResource.class);

		if (attrs.isEmpty()) {
			return null;
		}
		else if (attrs.size() == 1) {
			return (ManagedResource) attrs.iterator().next();
		}
		else {
			throw new InvalidMetadataException("A Class can have only one ManagedResource attribute");
		}
	}


	/**
	 * If the specified method has a <code>ManagedAttribute</code> attribute, then it is returned.
	 * Otherwise returns null. An <code>InvalidMetadataException</code> is thrown if more than one
	 * <code>ManagedAttribute</code> attribute exists, or if the supplied method does not represent a JavaBean
	 * property.
	 * @param method The <code>Method</code> to read the attribute data from.
	 * @return The attribute if found, otherwise <code>null</code>.
	 */
	public ManagedAttribute getManagedAttribute(Method method) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd == null) {
			throw new InvalidMetadataException(
					"The ManagedAttribute attribute is only valid for JavaBean properties. Use ManagedOperation for methods.");
		}
		Collection attrs = attributes.getAttributes(method, ManagedAttribute.class);
		if (attrs.isEmpty()) {
			return null;
		}
		else if (attrs.size() == 1) {
			return (ManagedAttribute) attrs.iterator().next();
		}
		else {
			throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
		}
	}

	/**
	 * If the specified method has a <code>ManagedOperation</code> attribute, then it is returned.
	 * Otherwise return null. An <code>InvalidMetadataException</code> is thrown if more than one
	 * ManagedOperation attribute exists, or if the supplied method represents a JavaBean property.
	 * @param method The <code>Method</code> to read attribute data from.
	 * @return The attribute if found, otherwise <code>null</code>.
	 * @see org.springframework.metadata.Attributes
	 * @see org.springframework.jmx.metadata.ManagedOperation
	 */
	public ManagedOperation getManagedOperation(Method method) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			throw new InvalidMetadataException(
					"The ManagedOperation attribute is not valid for JavaBean properties. Use ManagedAttribute instead.");
		}
		Collection attrs = attributes.getAttributes(method, ManagedOperation.class);
		if (attrs.isEmpty()) {
			return null;
		}
		else if (attrs.size() == 1) {
			return (ManagedOperation) attrs.iterator().next();
		}
		else {
			throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
		}
	}

}
