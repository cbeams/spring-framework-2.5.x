/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An implementation of the {@link ObjectNamingStrategy} interface
 * that reads the <code>ObjectName</code> from the source-level metadata.
 * Falls back to the bean key (bean name) if no <code>ObjectName</code>
 * can be found in source-level metadata.
 *
 * <p>Uses the {@link JmxAttributeSource} strategy interface, so that
 * metadata can be read using any supported implementation. Out of the box,
 * two strategies are included:
 * <ul>
 * <li><code>AttributesJmxAttributeSource</code>, for Commons Attributes
 * <li><code>AnnotationJmxAttributeSource</code>, for JDK 1.5+ annotations
 * </ul>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see ObjectNamingStrategy
 */
public class MetadataNamingStrategy implements ObjectNamingStrategy, InitializingBean {

	/**
	 * The <code>JmxAttributeSource</code> implementation to use for reading metadata.
	 */
	private JmxAttributeSource attributeSource;


	/**
	 * Create a new <code>MetadataNamingStrategy<code> which needs to be
	 * configured through the {@link #setAttributeSource} method.
	 */
	public MetadataNamingStrategy() {
	}

	/**
	 * Create a new <code>MetadataNamingStrategy<code> for the given
	 * <code>JmxAttributeSource</code>.
	 * @param attributeSource the JmxAttributeSource to use
	 */
	public MetadataNamingStrategy(JmxAttributeSource attributeSource) {
		Assert.notNull(attributeSource, "JmxAttributeSource must not be null");
		this.attributeSource = attributeSource;
	}


	/**
	 * Set the implementation of the <code>JmxAttributeSource</code> interface to use
	 * when reading the source level metadata.
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		this.attributeSource = attributeSource;
	}

	public void afterPropertiesSet() {
		if (this.attributeSource == null) {
			throw new IllegalArgumentException("Property 'attributeSource' is required");
		}
	}


	/**
	 * Reads the <code>ObjectName</code> from the source level metadata associated
	 * with the managed resource's <code>Class</code>.
	 */
	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException {
		if (AopUtils.isJdkDynamicProxy(managedBean)) {
			throw new IllegalArgumentException("MetadataNamingStrategy does not support JDK dynamic proxies - " +
					"export the target beans directly or use CGLIB proxies instead");
		}

		Class managedClass = JmxUtils.getClassToExpose(managedBean);
		ManagedResource mr = this.attributeSource.getManagedResource(managedClass);

		// Check that an object name has been specified.
		String objectName = null;
		if (mr != null && StringUtils.hasText(mr.getObjectName())) {
			objectName = mr.getObjectName();
		}
		else {
			objectName = beanKey;
		}

		// Now try to parse the name.
		return ObjectNameManager.getInstance(objectName);
	}

}
