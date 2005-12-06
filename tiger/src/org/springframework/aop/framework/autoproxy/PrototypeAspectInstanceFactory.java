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

package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.aspectj.AspectMetadata;
import org.springframework.aop.aspectj.MetadataAwareAspectInstanceFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * AspectInstanceFactory backed by Spring IoC prototype.
 * Note that this may instantiate multiple times, which probably won't give
 * the semantics you expect. Use a LazySingletonMetadataAwareAspectInstanceFactoryDecorator
 * to wrap this to ensure only one new aspect comes back.
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.aspectj.LazySingletonMetadataAwareAspectInstanceFactoryDecorator
 */
public class PrototypeAspectInstanceFactory implements MetadataAwareAspectInstanceFactory {

	private final BeanFactory beanFactory;
	private final String name;
	private final AspectMetadata am;
	private int instantiations;
	
	public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
		this.beanFactory = beanFactory;
		if (beanFactory.isSingleton(name)) {
			throw new IllegalArgumentException("Cannot use PrototypeAspectInstanceFactory with bean named '" + name + "': not a prototype");
		}
		this.name = name;
		am = new AspectMetadata(beanFactory.getType(name));
	}
	
	public synchronized Object getAspectInstance() {
		++instantiations;
		return this.beanFactory.getBean(name);
	}
	
	public AspectMetadata getAspectMetadata() {
		return this.am;
	}
	
	public int getInstantiationCount() {
		return instantiations;
	}
	
	@Override
	public String toString() {
		return "PrototypeAspectInstanceFactory: bean name='" + name + "'; " +
			"instantiations=" + instantiations;
	}
}