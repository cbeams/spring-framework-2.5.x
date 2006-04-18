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

/**
 * Interface to be implemented by objects used within a BeanFactory
 * that are themselves factories. If a bean implements this interface,
 * it is used as a factory, not directly as a bean.
 *
 * <p><b>NB: A bean that implements this interface cannot be used
 * as a normal bean.</b> A FactoryBean is defined in a bean style,
 * but the object exposed for bean references is always the object
 * that it creates.
 *
 * <p>FactoryBeans can support singletons and prototypes, and can
 * either create objects lazily on demand or eagerly on startup.
 *
 * <p>This interface is heavily used within the framework, for
 * example for the AOP ProxyFactoryBean or JndiObjectFactoryBean.
 * It can be used for application components, but this is not common
 * outside of infrastructure code.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 08.03.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public interface FactoryBean {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory. As with a BeanFactory, this allows
	 * support for both the Singleton and Prototype design pattern.
	 * <p>If this method returns <code>null</code>, the factory will consider
	 * the FactoryBean as not fully initialized and throw a corresponding
	 * FactoryBeanNotInitializedException.
	 * @return an instance of the bean (should not be <code>null</code>;
	 * a <code>null</code> value will be considered as an indication of
	 * incomplete initialization)
	 * @throws Exception in case of creation errors
	 * @see FactoryBeanNotInitializedException
	 */
	Object getObject() throws Exception;

	/**
	 * Return the type of object that this FactoryBean creates, or <code>null</code>
	 * if not known in advance. This allows to check for specific types
	 * of beans without instantiating objects, for example on autowiring.
	 * <p>For a singleton, this should try to avoid singleton creation
	 * as far as possible; it should rather estimate the type in advance.
	 * For prototypes, returning a meaningful type here is advisable too.
	 * <p>This method can be called <i>before</i> this FactoryBean has
	 * been fully initialized. It must not rely on state created during
	 * initialization; of course, it can still use such state if available.
	 * <p><b>NOTE:</b> Autowiring will simply ignore FactoryBeans that return
	 * <code>null</code> here. Therefore it is highly recommended to implement
	 * this method properly, using the current state of the FactoryBean.
	 * @return the type of object that this FactoryBean creates,
	 * or <code>null</code> if not known at the time of the call
	 * @see ListableBeanFactory#getBeansOfType
	 */
	Class getObjectType();

	/**
	 * Is the bean managed by this factory a singleton or a prototype?
	 * That is, will <code>getObject()</code> always return the same object
	 * (a reference that can be cached)?
	 * <p><b>NOTE:</b> If a FactoryBean indicates to hold a singleton object,
	 * the object returned from <code>getObject()</code> might get cached
	 * by the owning BeanFactory. Hence, do not return <code>true</code>
	 * unless the FactoryBean always exposes the same reference.
	 * <p>The singleton status of the FactoryBean itself will generally
	 * be provided by the owning BeanFactory; usually, it has to be
	 * defined as singleton there.
	 * @return if this bean is a singleton
	 * @see #getObject()
	 */
	boolean isSingleton();

}
