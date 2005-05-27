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

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;

import org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator;
import org.springframework.aop.framework.autoproxy.target.PoolingAttribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.metadata.Attributes;

/**
 * PoolingTargetSourceCreator driven by metadata.
 *
 * @author Rod Johnson
 */
public class AttributesPoolingTargetSourceCreator extends AbstractPoolingTargetSourceCreator {

	/**
	 * Underlying Attributes implementation that we're using.
	 */
	private Attributes attributes;

	/**
	 * Create a new AttributesPoolingTargetSourceCreator.
	 * @see #setAttributes
	 */
	public AttributesPoolingTargetSourceCreator() {
	}

	/**
	 * Create a new AttributesPrototypeTargetSourceCreator.
	 * @param attributes the Attributes implementation to use
	 */
	public AttributesPoolingTargetSourceCreator(Attributes attributes) {
		if (attributes == null) {
			throw new IllegalArgumentException("Attributes is required");
		}
		this.attributes = attributes;
	}

	/**
	 * Set the Attributes implementation to use.
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public void afterPropertiesSet() {
		if (this.attributes == null) {
			throw new IllegalArgumentException("'attributes' is required");
		}
	}

	protected PoolingAttribute getPoolingAttribute(Class beanClass, String beanName) {
		// See if there's a pooling attribute.
		Collection atts = this.attributes.getAttributes(beanClass, PoolingAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute found.
			return null;
		}
		else if (atts.size() > 1) {
			throw new BeanDefinitionStoreException(
					"Cannot have more than one pooling attribute on [" + beanClass.getName() + "]");
		}
		else {
			return (PoolingAttribute) atts.iterator().next();
		}
	}

}
