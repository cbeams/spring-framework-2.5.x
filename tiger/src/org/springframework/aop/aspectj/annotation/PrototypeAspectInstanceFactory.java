/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory;

/**
 * AspectInstanceFactory backed by Spring IoC prototype, and enforcing
 * prototype semantics.
 *
 * <p>Note that this may instantiate multiple times, which probably won't give
 * the semantics you expect. Use a LazySingletonMetadataAwareAspectInstanceFactoryDecorator
 * to wrap this to ensure only one new aspect comes back.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.LazySingletonMetadataAwareAspectInstanceFactoryDecorator
 */
public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory {
	
	public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
		super(beanFactory, name);
		if (beanFactory.isSingleton(name)) {
			throw new IllegalArgumentException(
					"Cannot use PrototypeAspectInstanceFactory with bean named '" + name + "': not a prototype");
		}
	}

}
