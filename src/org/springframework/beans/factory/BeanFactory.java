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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * The root interface for accessing a Spring bean container.
 * This is the basic client view of a bean container; further interfaces
 * such as <code>ListableBeanFactory</code> and <code>ConfigurableBeanFactory</code>
 * are available for specific purposes.
 *
 * <p>This interface is implemented by objects that hold a number of bean definitions,
 * each uniquely identified by a String name. Depending on the bean definition,
 * the factory will return either an independent instance of a contained object
 * (the Prototype design pattern), or a single shared instance (a superior
 * alternative to the Singleton design pattern, in which the instance is a
 * singleton in the scope of the factory). Which type of instance will be returned
 * depends on the bean factory configuration: the API is the same. The Singleton
 * approach is more useful and more common in practice.
 *
 * <p>The point of this approach is that the BeanFactory is a central registry
 * of application components, and centralizes configuration of application
 * components (no more do individual objects need to read properties files,
 * for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and
 * Development" for a discussion of the benefits of this approach.
 *
 * <p>Note that it is generally better to rely on Dependency Injection
 * ("push" configuration) to configure application objects through setters
 * or constructors, rather than use any form of "pull" configuration like a
 * BeanFactory lookup. Spring's Dependency Injection functionality is
 * implemented using BeanFactory and its subinterfaces.
 *
 * <p>Normally a BeanFactory will load bean definitions stored in a configuration
 * source (such as an XML document), and use the org.springframework.beans package
 * to configure the beans. However, an implementation could simply return Java
 * objects it creates as necessary directly in Java code. There are no constraints
 * on how the definitions could be stored: LDAP, RDBMS, XML, properties file etc.
 * Implementations are encouraged to support references amongst beans, to either
 * Singletons or Prototypes.
 *
 * <p>In contrast to the methods in ListableBeanFactory, all of the methods in this
 * interface will also check parent factories if this is a HierarchicalBeanFactory.
 * If a bean is not found in this factory instance, the immediate parent is asked.
 * Beans in this factory instance are supposed to override beans of the same name
 * in any parent factory.
 *
 * <p>Bean factory implementations should support the standard bean lifecycle interfaces
 * as far as possible. The maximum set of initialization methods and their standard
 * order is:<br>
 * 1. BeanNameAware's <code>setBeanName</code><br>
 * 2. BeanFactoryAware's <code>setBeanFactory</code><br>
 * 3. ResourceLoaderAware's <code>setResourceLoader</code>
 * (only applicable when running in an application context)<br>
 * 4. ApplicationEventPublisherAware's <code>setApplicationEventPublisher</code>
 * (only applicable when running in an application context)<br>
 * 5. MessageSourceAware's <code>setMessageSource</code>
 * (only applicable when running in an application context)<br>
 * 6. ApplicationContextAware's <code>setApplicationContext</code>
 * (only applicable when running in an application context)<br>
 * 7. ServletContextAware's <code>setServletContext</code>
 * (only applicable when running in a web application context)<br>
 * 8. <code>postProcessBeforeInitialization</code> methods of BeanPostProcessors<br>
 * 9. InitializingBean's <code>afterPropertiesSet</code><br>
 * 10. a custom init-method definition<br>
 * 11. <code>postProcessAfterInitialization</code> methods of BeanPostProcessors
 *
 * <p>On shutdown of a bean factory, the following lifecycle methods apply:<br>
 * 1. DisposableBean's <code>destroy</code><br>
 * 2. a custom destroy-method definition
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see ListableBeanFactory
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 * @see BeanNameAware#setBeanName
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
public interface BeanFactory {

	/**
	 * Used to dereference a FactoryBean and distinguish it from beans
	 * <i>created</i> by the FactoryBean. For example, if the bean named
	 * <code>myEjb</code> is a FactoryBean, getting <code>&myEjb</code> will
	 * return the factory, not the instance returned by the factory.
	 */
	String FACTORY_BEAN_PREFIX = "&";


	/**
	 * Return an instance, which may be shared or independent, of the given bean name.
	 * This method allows a Spring BeanFactory to be used as a replacement for the
	 * Singleton or Prototype design pattern.
	 * <p>Callers may retain references to returned objects in the case of Singleton beans.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to return
	 * @return the instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no bean definition
	 * with the specified name
	 * @throws BeansException if the bean could not be obtained
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the given bean name.
	 * <p>Behaves the same as getBean(String), but provides a measure of type safety by
	 * throwing a Spring BeansException if the bean is not of the required type.
	 * This means that ClassCastException can't be thrown on casting the result correctly,
	 * as can happen with <code>getBean(String)</code>.
	 * @param name the name of the bean to return
	 * @param requiredType type the bean must match. Can be an interface or superclass
	 * of the actual class, or <code>null</code> for any match. For example, if the value
	 * is <code>Object.class</code>, this method will succeed whatever the class of the
	 * returned instance.
	 * @return an instance of the bean (never <code>null</code>)
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 * @throws BeansException if the bean could not be created
	 */
	Object getBean(String name, Class requiredType) throws BeansException;

	/**
	 * Does this bean factory contain a bean definition with the given name?
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return whether a bean with the given name is defined
	 */
	boolean containsBean(String name);

	/**
	 * Is this bean a singleton? That is, will <code>getBean</code> always return the same object?
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return is this bean a singleton
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @see #getBean
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Determine the type of the bean with the given name.
	 * More specifically, checks the type of object that <code>getBean</code> would return.
	 * For a FactoryBean, returns the type of object that the FactoryBean creates.
	 * @param name the name of the bean to query
	 * @return the type of the bean, or <code>null</code> if not determinable
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 1.1.2
	 * @see #getBean
	 * @see FactoryBean#getObjectType
	 */
	Class getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Return the aliases for the given bean name, if defined.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the bean name to check for aliases
	 * @return the aliases, or an empty array if none
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
	String[] getAliases(String name) throws NoSuchBeanDefinitionException;

}
