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

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;

import org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator;
import org.springframework.aop.framework.autoproxy.target.PoolingAttribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.metadata.Attributes;

/**
 * PoolingTargetSourceCreator driven by metadata.
 * @author Rod Johnson
 */
public class AttributesPoolingTargetSourceCreator extends AbstractPoolingTargetSourceCreator {

	private final Attributes attributes;

	public AttributesPoolingTargetSourceCreator(Attributes attributes) {
		this.attributes = attributes;
	}

	protected PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory beanFactory) {
		Class beanClass = bean.getClass();
		// See if there's a pooling attribute
		Collection atts = attributes.getAttributes(beanClass, PoolingAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute
			return null;
		}
		else if (atts.size() > 1) {
			throw new BeanDefinitionStoreException("Cannot have more than one pooling attribute on " + beanClass);
		}
		else {
			return (PoolingAttribute) atts.iterator().next();
		}
	}

}