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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Subinterface of BeanPostProcessor that adds a before-instantiation callback,
 * and a callback after instantiation but before explicit properties are set or
 * autowiring occurs.
 *
 * <p>Typically used to suppress default instantiation for specific target beans,
 * for example to create proxies with special TargetSources (pooling targets,
 * lazily initializing targets, etc), or to implement additional injection strategies
 * such as field injection.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.2
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator
 * @see org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor <i>before the target bean gets instantiated</i>.
	 * The returned bean object may be a proxy to use instead of the target bean,
	 * effectively suppressing default instantiation of the target bean.
	 * <p>If a non-null object is returned by this method, the bean creation process
	 * will be short-circuited. The returned bean object will not be processed any
	 * further; in particular, no further BeanPostProcessor callbacks will be applied
	 * to it. This mechanism is mainly intended for exposing a proxy instead of an
	 * actual target bean.
	 * <p>This callback will only be applied to bean definitions with a bean class.
	 * In particular, it will not be applied to beans with a "factory-method".
	 * @param beanClass the class of the bean to be instantiated
	 * @param beanName the name of the bean
	 * @return the bean object to expose instead of a default instance of the target bean
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#hasBeanClass
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getFactoryMethodName
	 */
	Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException;
	
	/**
	 * Perform operations after the bean has been instantiated, via a constructor or factory method,
	 * but before Spring property population (from explicit properties or autowiring) occurs.
	 * @param bean bean instance created, but whose properties have not yet been set
	 * @param beanName the name of the bean
	 * @return true if properties should be set on the bean; false if property population
	 * should be skipped. Normal implementations should return true. Returning false will
	 * also prevent any subsequent InstantiationAwareBeanPostProcessor instances
	 * being invoked on this bean instance.
	 * @throws BeansException in the case of errors
	 */
	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

}
