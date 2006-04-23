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

package org.springframework.aop.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.aspectj.autoproxy.AspectJInvocationContextExposingAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.InvocationContextExposingAdvisorAutoProxyCreator;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class NamespaceHandlerUtils {

	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
					"org.springframework.aop.config.internalAutoProxyCreator";

	public static final String ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME =
					"org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator";

	private static final List APC_PRIORITY_LIST = new ArrayList();

	static {
		APC_PRIORITY_LIST.add(InvocationContextExposingAdvisorAutoProxyCreator.class.getName());
		APC_PRIORITY_LIST.add(AspectJInvocationContextExposingAdvisorAutoProxyCreator.class.getName());
		APC_PRIORITY_LIST.add(ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME);
	}

	public static void registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		registryOrEscalateApcAsRequired(InvocationContextExposingAdvisorAutoProxyCreator.class, registry);
	}

	public static void registerAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		registryOrEscalateApcAsRequired(AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, registry);
	}

	public static void registerAtAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		Class cls = getAspectJAutoProxyCreatorClassIfPossible();
		if (cls == null) {
			throw new IllegalStateException("Unable to register AspectJ AutoProxyCreator. Cannot find class [" +
					ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME + "]. Are you running on Java 5.0+?");
		}
		registryOrEscalateApcAsRequired(cls, registry);
	}

	private static void registryOrEscalateApcAsRequired(Class cls, BeanDefinitionRegistry registry) {
		Assert.notNull(cls, "'cls' cannot be null.");
		Assert.notNull(registry, "'registry' cannot be null.");

		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			AbstractBeanDefinition abd = (AbstractBeanDefinition) registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);

			if (cls.equals(abd.getBeanClass())) {
				return;
			}

			int currentPriority = findPriorityForClass(abd.getBeanClass().getName());
			int requiredPriority = findPriorityForClass(cls.getName());

			if (currentPriority < requiredPriority) {
				abd.setBeanClass(cls);
			}
		}
		else {
			registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, new RootBeanDefinition(cls));
		}
	}


	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);

			if (definition.getPropertyValues() == null) {
				definition.setPropertyValues(new MutablePropertyValues());
			}

			definition.getPropertyValues().addPropertyValue("proxyTargetClass", Boolean.TRUE);
		}
	}

	private static Class getAspectJAutoProxyCreatorClassIfPossible() {
		try {
			return ClassUtils.forName(ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME);
		}
		catch (ClassNotFoundException ex) {
			return null;
		}
	}

	private static Class getAtAspectJAutoProxyCreatorClassIfPossible() {
		try {
			return ClassUtils.forName(ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME);
		}
		catch (ClassNotFoundException ex) {
			return null;
		}
	}

	private static final int findPriorityForClass(String className) {
		Assert.notNull(className, "'className' cannot be null.");
		for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
			String s = (String) APC_PRIORITY_LIST.get(i);
			if (className.equals(s)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Class name '" + className + "' is not a known APC class.");
	}

}
