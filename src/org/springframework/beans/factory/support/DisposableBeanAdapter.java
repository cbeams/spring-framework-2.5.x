/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.util.Assert;

/**
 * Adapter that implements the DisposableBean interface performing
 * various destruction steps on a given bean instance:
 * <ul>
 * <li>DestructionAwareBeanPostProcessors
 * <li>the bean implementing DisposableBean itself
 * <li>a custom destroy method specified on the bean definition
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see AbstractBeanFactory
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
 * @see AbstractBeanDefinition#getDestroyMethodName()
 */
class DisposableBeanAdapter implements DisposableBean, Runnable {

	private static final Log logger = LogFactory.getLog(DisposableBeanAdapter.class);

	private final Object bean;

	private final String beanName;

	private final RootBeanDefinition mergedBeanDefinition;

	private final List beanPostProcessors;


	/**
	 * Create a new DisposableBeanAdapter for the given bean.
	 * @param bean the bean instance (never <code>null</code>)
	 * @param beanName the name of the bean
	 * @param mergedBeanDefinition the merged bean definition, if any
	 * @param beanPostProcessors the List of BeanPostProcessors
	 * (potentially DestructionAwareBeanPostProcessor), if any
	 */
	public DisposableBeanAdapter(
			Object bean, String beanName, RootBeanDefinition mergedBeanDefinition, List beanPostProcessors) {

		Assert.notNull(bean, "Bean must not be null");
		this.bean = bean;
		this.beanName = beanName;
		this.mergedBeanDefinition = mergedBeanDefinition;
		this.beanPostProcessors = beanPostProcessors;
	}


	public void run() {
		destroy();
	}

	public void destroy() {
		if (this.beanPostProcessors != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Applying DestructionAwareBeanPostProcessors to bean with name '" + this.beanName + "'");
			}
			for (int i = this.beanPostProcessors.size() - 1; i >= 0; i--) {
				Object beanProcessor = this.beanPostProcessors.get(i);
				if (beanProcessor instanceof DestructionAwareBeanPostProcessor) {
					((DestructionAwareBeanPostProcessor) beanProcessor).postProcessBeforeDestruction(this.bean, this.beanName);
				}
			}
		}

		if (this.bean instanceof DisposableBean) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking destroy() on bean with name '" + this.beanName + "'");
			}
			try {
				((DisposableBean) this.bean).destroy();
			}
			catch (Throwable ex) {
				logger.error("Couldn't invoke destroy method of bean with name '" + this.beanName + "'", ex);
			}
		}

		if (this.mergedBeanDefinition != null) {
			invokeCustomDestroyMethod();
		}
	}

	/**
	 * Invoke the specified custom destroy method on the given bean.
	 * <p>This implementation invokes a no-arg method if found, else checking
	 * for a method with a single boolean argument (passing in "true",
	 * assuming a "force" parameter), else logging an error.
	 */
	private void invokeCustomDestroyMethod() {
		String destroyMethodName = this.mergedBeanDefinition.getDestroyMethodName();
		if (destroyMethodName != null) {
			try {
				Method destroyMethod = BeanUtils.findMethodWithMinimalParameters(this.bean.getClass(), destroyMethodName);
				if (destroyMethod == null) {
					if (this.mergedBeanDefinition.isEnforceDestroyMethod()) {
						logger.error("Couldn't find a destroy method named '" + destroyMethodName +
								"' on bean with name '" + this.beanName + "'");
					}
				}

				else {
					Class[] paramTypes = destroyMethod.getParameterTypes();
					if (paramTypes.length > 1) {
						logger.error("Method '" + destroyMethodName + "' of bean '" + this.beanName +
								"' has more than one parameter - not supported as destroy method");
					}
					else if (paramTypes.length == 1 && !paramTypes[0].equals(boolean.class)) {
						logger.error("Method '" + destroyMethodName + "' of bean '" + this.beanName +
								"' has a non-boolean parameter - not supported as destroy method");
					}

					else {
						Object[] args = new Object[paramTypes.length];
						if (paramTypes.length == 1) {
							args[0] = Boolean.TRUE;
						}
						if (!Modifier.isPublic(destroyMethod.getModifiers())) {
							destroyMethod.setAccessible(true);
						}

						if (logger.isDebugEnabled()) {
							logger.debug("Invoking custom destroy method on bean with name '" + this.beanName + "'");
						}
						try {
							destroyMethod.invoke(this.bean, args);
						}
						catch (InvocationTargetException ex) {
							logger.error("Couldn't invoke destroy method '" + destroyMethodName +
									"' of bean with name '" + this.beanName + "'", ex.getTargetException());
						}
						catch (Throwable ex) {
							logger.error("Couldn't invoke destroy method '" + destroyMethodName +
									"' of bean with name '" + this.beanName + "'", ex);
						}
					}
				}
			}
			catch (IllegalArgumentException ex) {
				// thrown from findMethodWithMinimalParameters
				logger.error("Couldn't find a unique destroy method on bean with name '" +
						this.beanName + ": " + ex.getMessage());
			}
		}
	}

}
