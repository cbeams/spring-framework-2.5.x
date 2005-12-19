/*
 * Copyright 2002-2005 the original author or authors.
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
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ClassUtils;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class NamespaceHandlerUtils {

	// TODO: Can short-circuit these methods when previously registered via this class.
	// TODO: How eagerly should this try to look for user-registered auto-proxy creators?

	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
			"org.springframework.aop.config.internalAutoProxyCreator";

	public static final String CONFIGURABLE_TARGET_SOURCE_CREATOR_NAME =
			"org.springframework.aop.framework.target.internalTargetSourceCreator";

	public static final String ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME =
			"org.springframework.aop.aspectj.autoproxy.AspectJAutoProxyCreator";


	public static void registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			// already have the apc
			return;
		}
		RootBeanDefinition definition = new RootBeanDefinition(InvocationContextExposingAdvisorAutoProxyCreator.class);
		definition.setPropertyValues(new MutablePropertyValues());
		definition.getPropertyValues().addPropertyValue("proxyTargetClass", Boolean.TRUE);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, definition);
	}

	public static void registerAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		Class baseApcClass = InvocationContextExposingAdvisorAutoProxyCreator.class;
		Class ajApcClass = getAspectJAutoProxyCreatorClassIfPossible();

		if (ajApcClass == null) {
			throw new IllegalStateException(
					"Unable to register AspectJ AutoProxyCreator. Cannot find class [" +
					ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME + "]. Are you running on Java 5.0+?");
		}


		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			AbstractBeanDefinition definition =
					(AbstractBeanDefinition) registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (baseApcClass.equals(definition.getBeanClass())) {
				// switch APC type
				definition.setBeanClass(ajApcClass);
			}
			return;
		}

		RootBeanDefinition definition = new RootBeanDefinition(ajApcClass);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, definition);
	}

	private static Class getAspectJAutoProxyCreatorClassIfPossible() {
		try {
			return ClassUtils.forName(ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME);
		}
		catch (ClassNotFoundException ex) {
			return null;
		}
	}

}
