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

package org.springframework.aop.config;

import org.springframework.aop.framework.autoproxy.InvocationContextExposingAdvisorAutoProxyCreator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ClassUtils;

/**
 * @author Rob Harrop
 */
public abstract class NamespaceHandlerUtils {

	// TODO: can short-circuit these methods when previously registered via this class
	// TODO: How eagerly should this try to look for user-registered auto proxy creators?
	public static final String AUTO_PROXY_CREATOR_BEAN_NAME = "org.springframework.aop.config.internalAutoProxyCreator";

	public static final String ASPECTJ_AUTO_PROXY_CREATOR = "org.springframework.aop.framework.autoproxy.AspectJAutoProxyCreator";

	public static void registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		Class apcClass = InvocationContextExposingAdvisorAutoProxyCreator.class;
		Class ajApcClass = getAspectJAutoProxyCreatorClassIfPossible();

		String[] beanDefinitionNames = registry.getBeanDefinitionNames();


		for (int i = 0; i < beanDefinitionNames.length; i++) {
			String beanDefinitionName = beanDefinitionNames[i];
			AbstractBeanDefinition def = (AbstractBeanDefinition) registry.getBeanDefinition(beanDefinitionName);

			if (apcClass.equals(def.getBeanClass()) || (ajApcClass != null && ajApcClass.equals(def.getBeanClass()))) {
				// already registered
				return;
			}
		}
		RootBeanDefinition definition = new RootBeanDefinition(apcClass);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, definition);
	}

	public static void  registerAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		Class baseApcClass = InvocationContextExposingAdvisorAutoProxyCreator.class;
		Class ajApcClass = getAspectJAutoProxyCreatorClassIfPossible();

		if(ajApcClass == null) {
			throw new IllegalStateException("Unable to register AspectJ AutoProxyCreator. Cannot find class ["
					+ ASPECTJ_AUTO_PROXY_CREATOR + "]. Are you running on Java 5.0+?");
		}


		String[] beanDefinitionNames = registry.getBeanDefinitionNames();

		for (int i = 0; i < beanDefinitionNames.length; i++) {
			String beanDefinitionName = beanDefinitionNames[i];
			AbstractBeanDefinition def = (AbstractBeanDefinition) registry.getBeanDefinition(beanDefinitionName);

			if (baseApcClass.equals(def.getBeanClass())) {
				// switch the definition to an AJ APC
				def.setBeanClass(ajApcClass);
				return;
			} else if(ajApcClass.equals(def.getBeanClass())) {
				// already registered
				return;
			}
		}

		RootBeanDefinition definition = new RootBeanDefinition(ajApcClass);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, definition);
	}

	private static Class getAspectJAutoProxyCreatorClassIfPossible() {
		try {
			return ClassUtils.forName(ASPECTJ_AUTO_PROXY_CREATOR);
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}
}
