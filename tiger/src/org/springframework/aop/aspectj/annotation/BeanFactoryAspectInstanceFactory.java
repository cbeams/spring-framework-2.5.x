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

import org.springframework.aop.aspectj.annotation.AspectMetadata;
import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * AspectInstanceFactory backed by Spring IoC bean.
 *
 * <p>Note that this may instantiate multiple times if using a prototype,
 * which probably won't give the semantics you expect. 
 * Use a LazySingletonMetadataAwareAspectInstanceFactoryDecorator
 * to wrap this to ensure only one new aspect comes back.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.LazySingletonMetadataAwareAspectInstanceFactoryDecorator
 */
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory {

	private final BeanFactory beanFactory;

	private final String name;

	private final AspectMetadata am;

	private int instantiations;


	/**
	 * Create a BeanFactoryAspectInstance factory. AspectJ will be called to
	 * introspect to create AJType metadata using the type returned for the given bean name
	 * from the BeanFactory. 
	 * @param beanFactory BeanFactory to obtain instance(s) from
	 * @param name name of the bean
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
		this(beanFactory, name, beanFactory.getType(name));
	}
	
	/**
	 * Create a BeanFactoryAspectInstance factory, providing a type that AspectJ should
	 * introspect to create AJType metadata. Use if the BeanFactory may consider the type
	 * to be a subclass (as when using CGLIB), and the information should relate to a superclass.
	 * @param beanFactory BeanFactory to obtain instance(s) from
	 * @param name name of the bean
	 * @param type type that should be introspected by AspectJ. 
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name, Class type) {
		this.beanFactory = beanFactory;
		this.name = name;
		am = new AspectMetadata(type,name);
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
		return getClass().getSimpleName() + ": bean name='" + name + "'; " +
			"instantiations=" + instantiations;
	}

}
