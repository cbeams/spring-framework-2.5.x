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

package org.springframework.context.support;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;

/**
 * ApplicationContext that allows concrete registration of beans and
 * messages in code, rather than from external configuration sources.
 * Mainly useful for testing.
 * @author Rod Johnson
 */
public class StaticApplicationContext extends AbstractApplicationContext {

	private DefaultListableBeanFactory beanFactory;

	/**
	 * Create new StaticApplicationContext.
	 */
	public StaticApplicationContext() throws BeansException {
		this(null);
	}

	/**
	 * Create new StaticApplicationContext with the given parent.
	 * @param parent the parent application context
	 */
	public StaticApplicationContext(ApplicationContext parent) throws BeansException {
		super(parent);

		// create bean factory with parent
		this.beanFactory = new DefaultListableBeanFactory(getInternalParentBeanFactory());

		// Register the message source bean
		registerSingleton(MESSAGE_SOURCE_BEAN_NAME, StaticMessageSource.class, null);
	}

	/**
	 * Return the underlying bean factory of this context.
	 */
	public DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return beanFactory;
	}

	/**
	 * Return underlying bean factory for super class.
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Do nothing: We rely on callers to update our public methods.
	 */
	protected void refreshBeanFactory() {
	}

	/**
	 * Register a singleton bean with the default bean factory.
	 */
	public void registerSingleton(String name, Class clazz, MutablePropertyValues pvs) throws BeansException {
		this.beanFactory.registerBeanDefinition(name, new RootBeanDefinition(clazz, pvs));
	}

	/**
	 * Register a prototype bean with the default bean factory.
	 */
	public void registerPrototype(String name, Class clazz, MutablePropertyValues pvs) throws BeansException {
		this.beanFactory.registerBeanDefinition(name, new RootBeanDefinition(clazz, pvs, false));
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
	 * @param locale locale message should be found within
	 * @param defaultMessage message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String defaultMessage) {
		StaticMessageSource messageSource = (StaticMessageSource) getBean(MESSAGE_SOURCE_BEAN_NAME);
		messageSource.addMessage(code, locale, defaultMessage);
	}

}
