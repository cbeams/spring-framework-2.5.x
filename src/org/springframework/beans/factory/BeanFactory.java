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

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by objects that hold a number of bean definitions,
 * each uniquely identified by a String name. An independent instance of any of
 * these objects can be obtained (the Prototype design pattern), or a single
 * shared instance can be obtained (a superior alternative to the Singleton
 * design pattern). Which type of instance will be returned depends on the bean
 * factory configuration - the API is the same. The Singleton approach is much
 * more useful and more common in practice.
 *
 * <p>The point of this approach is that the BeanFactory is a central registry
 * of application components, and centralizes the configuring of application
 * components (no more do individual objects need to read properties files,
 * for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and
 * Development" for a discussion of the benefits of this approach.
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
 * <p>Bean factories are supposed to support the standard bean lifecycle interfaces
 * as far as possible. The maximum set of initialization methods and their standard
 * order is:<br>
 * 1. BeanNameAware's setBeanName<br>
 * 2. BeanFactoryAware's setBeanFactory<br>
 * 3. ApplicationContextAware's setApplicationContext (only applicable if running
 * in an application context)<br>
 * 4. postProcessBeforeInitialization methods of BeanPostProcessors<br>
 * 5. InitializingBean's afterPropertiesSet<br>
 * 6. a custom init-method definition<br>
 * 7. postProcessAfterInitialization methods of BeanPostProcessors
 *
 * <p>On shutdown of a bean factory, the following lifecycle methods apply:<br>
 * 1. DisposableBean's destroy<br>
 * 2. a custom destroy-method definition
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @version $Revision: 1.7 $
 * @see BeanNameAware#setBeanName
 * @see BeanFactoryAware#setBeanFactory
 * @see InitializingBean#afterPropertiesSet
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 */
public interface BeanFactory {

	/**
	 * Return an instance (possibly shared or independent) of the given bean name.
	 * This method allows a bean factory to be used as a replacement for
	 * the Singleton or Prototype design pattern.
	 * <p>Note that callers should retain references to returned objects. There is
	 * no guarantee that this method will be implemented to be efficient. For example,
	 * it may be synchronized, or may need to run an RDBMS query.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name name of the bean to return
	 * @return the instance of the bean
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 * @throws BeansException if the bean could not be created
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the given bean name.
	 * Provides a measure of type safety by throwing an exception if the bean is not
	 * of the required type.
	 * <p>Note that callers should retain references to returned objects. There is
	 * no guarantee that this method will be implemented to be efficient. For example,
	 * it may be synchronized, or may need to run an RDBMS query.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name name of the bean to return
	 * @param requiredType type the bean may match. Can be an interface or superclass
	 * of the actual class. For example, if the value is Object.class, this method will
	 * succeed whatever the class of the returned instance.
	 * @return the instance of the bean
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 * @throws BeansException if the bean could not be created
	 */
	Object getBean(String name, Class requiredType) throws BeansException;

	/**
	 * Does this bean factory contain a bean with the given name?
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name name of the bean to query
	 * @return whether a bean with the given name is defined
	 */
	boolean containsBean(String name);

	/**
	 * Is this bean a singleton? That is, will getBean() always return the same object?
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name name of the bean to query
	 * @return is this bean a singleton
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Return the aliases for the given bean name, if defined.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the bean name to check for aliases
	 * @return the aliases, or an empty array if none
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
	String[] getAliases(String name) throws NoSuchBeanDefinitionException;

}
