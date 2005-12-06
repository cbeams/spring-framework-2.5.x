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

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rob Harrop
 */
public abstract class NamespaceHandlerUtils {

	public static final String AUTO_PROXY_CREATOR_BEAN_NAME = ".defaultAutoProxyCreator";

	public static void registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		// TODO: factory into reusable method
		String[] beanDefinitionNames = registry.getBeanDefinitionNames();
		for (int i = 0; i < beanDefinitionNames.length; i++) {
			String beanDefinitionName = beanDefinitionNames[i];
			AbstractBeanDefinition def = (AbstractBeanDefinition) registry.getBeanDefinition(beanDefinitionName);

			if (DefaultAdvisorAutoProxyCreator.class.equals(def.getBeanClass())) {
				// already registered
				return;
			}
		}
		RootBeanDefinition definition = new RootBeanDefinition(DefaultAdvisorAutoProxyCreator.class);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, definition);
	}

}
