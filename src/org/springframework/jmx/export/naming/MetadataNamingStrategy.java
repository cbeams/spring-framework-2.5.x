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

package org.springframework.jmx.export.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.util.StringUtils;
import org.springframework.aop.support.AopUtils;

/**
 * An implementation of the <code>ObjectNamingStrategy</code> interface
 * that reads the <code>ObjectName</code> from the source-level metadata.
 *
 * @author Rob Harrop
 * @since 1.2
 * @see ObjectNamingStrategy
 */
public class MetadataNamingStrategy implements ObjectNamingStrategy {

	/**
	 * The <code>JmxAttributeSource</code> implementation to use for reading metadata.
	 */
	private JmxAttributeSource attributeSource;


	/**
	 * Set the implementation of the <code>JmxAttributeSource</code> interface to use
	 * when reading the source level metadata.
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		this.attributeSource = attributeSource;
	}


	/**
	 * Reads the <code>ObjectName</code> from the source level metadata associated
	 * with the managed resource's <code>Class</code>.
	 */
	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException {
		if (AopUtils.isJdkDynamicProxy(managedBean)) {
			throw new IllegalArgumentException(
							"MetadataNamingStrategy does not support JDK dynamic proxies - " +
											"export the target beans directly or use CGLIB proxies instead");
		}
		Class managedClass = JmxUtils.getClassToExpose(managedBean);
		ManagedResource mr = this.attributeSource.getManagedResource(managedClass);

		// Check that the managed resource attribute has been specified.
		if (mr == null) {
			throw new MalformedObjectNameException("Your bean class [" + managedBean.getClass().getName() +
					"] must be marked with a valid ManagedResource attribute when using MetadataNamingStrategy");
		}

		// Check that an object name has been specified.
		String objectName = mr.getObjectName();

		if (!StringUtils.hasText(objectName)) {
			throw new MalformedObjectNameException(
					"You must specify an ObjectName for class [" + managedBean.getClass().getName() + "]");
		}

		// Now try to parse the name.
		return ObjectNameManager.getInstance(objectName);
	}

}
