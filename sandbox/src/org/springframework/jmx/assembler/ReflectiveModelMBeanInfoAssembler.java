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

package org.springframework.jmx.assembler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;

/**
 * Simple extension to <code>AbstractReflectionBasedModelMBeanInfoAssembler</code> that always
 * votes yes for method and property inclusion - effectively exposing all public methods and
 * properties as operations and attributes.
 *
 * @author Rob Harrop
 */
public class ReflectiveModelMBeanInfoAssembler extends AbstractReflectionBasedModelMBeanInfoAssembler {

	/**
	 * Returns a simple description for the MBean based on the <code>Class</code> name.
	 *
	 * @param beanKey the key associated with the MBean in the <code>beans</code> <code>Map</code>
	 * of the <code>MBeanExporter</code>.
	 * @param beanClass the <code>Class</code> of the MBean.
	 * @return the description.
	 */
	protected String getDescription(String beanKey, Class beanClass) {
		return beanClass.getName() + " instance";
	}

	/**
	 * Always returns <code>true</code>.
	 */
	protected boolean includeReadAttribute(Method method) {
		return true;
	}

	/**
	 * Always returns <code>true</code>.
	 */
	protected boolean includeWriteAttribute(Method method) {
		return true;
	}

	/**
	 * Always returns <code>true</code>.
	 */
	protected boolean includeOperation(Method method) {
		return true;
	}

	/**
	 * Returns a description for the operation that is the name of corresponding <code>Method</code>.
	 *
	 * @param method the method corresponding to the operation.
	 * @return the description.
	 */
	protected String getOperationDescription(Method method) {
		return method.getName();
	}

	/**
	 * Returns a description for the attribute that is the display name of the
	 * corresponding <code>PropertyDescriptor</code>.
	 *
	 * @param propertyDescriptor the <code>PropertyDescriptor</code> for this attribute.
	 * @return the description.
	 */
	protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
		return propertyDescriptor.getDisplayName();
	}

	/**
	 * Performs no additional processing.
	 */
	protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass) {
		// no-op
	}

	/**
	 * Performs no additional processing.
	 */
	protected void populateAttributeDescriptor(Descriptor descriptor, Method getter, Method setter) {
		// no-op
	}

	/**
	 * Performs no additional processing.
	 */
	protected void populateOperationDescriptor(Descriptor descriptor, Method method) {
		// no-op
	}

}
