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

import org.springframework.aop.framework.autoproxy.target.AbstractPrototypeBasedTargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeBasedTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.metadata.Attributes;

/**
 * PrototypeTargetSourceCreator driven by metadata. Creates a prototype
 * only if there's a PrototypeAttribute associated with the class.
 * @author Rod Johnson
 * @see org.springframework.aop.target.PrototypeTargetSource
 */
public class AttributesPrototypeTargetSourceCreator extends AbstractPrototypeBasedTargetSourceCreator {

	private final Attributes attributes;

	public AttributesPrototypeTargetSourceCreator(Attributes attributes) {
		this.attributes = attributes;
	}

	protected AbstractPrototypeBasedTargetSource createPrototypeTargetSource(Object bean, String beanName,
																																					 BeanFactory bf) {
		Class beanClass = bean.getClass();
		// See if there's a pooling attribute
		Collection atts = attributes.getAttributes(beanClass, PrototypeAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute: don't create a custom TargetSource
			return null;
		}
		else {
			return new PrototypeTargetSource();
		}
	}

}
