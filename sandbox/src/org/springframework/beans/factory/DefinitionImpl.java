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

package org.springframework.beans.factory;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;

/**
 * @author Rod Johnson
 */
public class DefinitionImpl implements Definition {

	private String name;

	private AbstractBeanDefinition definition;

	// TODO child

	public DefinitionImpl(String name, Class clazz, int autowireMode) {
		Assert.hasText(name, "Name must not be empty");
		this.name = name;
		definition = new RootBeanDefinition(clazz, autowireMode);
	}

	public DefinitionImpl(String name, String factoryBean, String factoryMethod, int autowireMode) {
		Assert.hasText(name, "Name must not be empty");
		this.name = name;
		definition = new RootBeanDefinition(null, autowireMode);
		factoryBean(factoryBean, factoryMethod);
	}

	/**
	 * @see org.springframework.beans.factory.Definition#getBeanDefinition()
	 */
	public BeanDefinition getBeanDefinition() {
		return definition;
	}

	/**
	 * @see org.springframework.beans.factory.Definition#getBeanDefinitionName()
	 */
	public String getBeanDefinitionName() {
		return name;
	}

	public Definition destroyMethodName(String methodName) {
		definition.setDestroyMethodName(methodName);
		return this;
	}

	public Definition factoryMethod(String factoryMethod) {
		definition.setFactoryMethodName(factoryMethod);
		return this;
	}

	public Definition factoryBean(String factoryBean, String factoryMethod) {
		definition.setFactoryBeanName(factoryBean);
		definition.setFactoryMethodName(factoryMethod);
		return this;
	}

	/**
	 * @see org.springframework.beans.factory.Definition#singleton(boolean)
	 */
	public Definition singleton(boolean singleton) {
		definition.setSingleton(singleton);
		return this;
	}

	// TODO overload: can't be used for refs!?
	public Definition prop(String name, Object value) {
		definition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
		return this;
	}

	public Definition carg(String name, Object value) {
		throw new UnsupportedOperationException();
		// definition.getConstructorArgumentValues().addArgumentValues()
	}

	public Definition ref(String name, String bean) {
		definition.getPropertyValues().addPropertyValue(new PropertyValue(name, new RuntimeBeanReference(bean)));
		return this;
	}

	public Definition carg(Object val) {
		definition.getConstructorArgumentValues().addGenericArgumentValue(val);
		return this;
	}

	/**
	 * @see org.springframework.beans.factory.Definition#noAutowire()
	 */
	public Definition noAutowire() {
		definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);
		return this;
	}

	public Definition autowireByName() {
		definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
		return this;
	}

	public Definition autowireByType() {
		definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		return this;
	}

	public Definition autowireConstructor() {
		definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		return this;
	}

}
