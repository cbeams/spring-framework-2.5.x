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

/**
 * Interface to be implemented by objects used within a BeanFactory
 * that are themselves factories. If a bean implements this interface,
 * it is used as a factory, not directly as a bean.
 *
 * <p><b>NB: A bean that implements this interface cannot be used
 * as a normal bean.</b>
 *
 * <p>FactoryBeans can support singletons and prototypes.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since March 08, 2003
 * @see org.springframework.beans.factory.BeanFactory
 */
public interface FactoryBean {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory. As with a BeanFactory, this allows
	 * support for both the Singleton and Prototype design pattern.
	 * @return an instance of the bean (should never be null)
	 * @throws Exception in case of creation errors
	 */
	Object getObject() throws Exception;

	/**
	 * Return the type of object that this FactoryBean creates, or null
	 * if not known in advance. This allows to check for specific types of
	 * beans without instantiating objects, e.g. on autowiring.
	 * <p>For a singleton, this can simply return getObject().getClass(),
	 * or even null, as autowiring will always check the actual objects
	 * for singletons. For prototypes, returning a meaningful type here
	 * is highly advisable, as autowiring will simply ignore them else.
	 * @return the type of object that this FactoryBean creates, or null
	 * @see ListableBeanFactory#getBeansOfType
	 */
	Class getObjectType();

	/**
	 * Is the bean managed by this factory a singleton or a prototype?
	 * That is, will getObject() always return the same object?
	 * <p>The singleton status of the FactoryBean itself will
	 * generally be provided by the owning BeanFactory.
	 * @return if this bean is a singleton
	 */
	boolean isSingleton();

}
