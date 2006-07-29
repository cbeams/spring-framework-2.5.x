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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Utility class for handling registration of auto-proxy creators used internally
 * by the '<code>aop</code>' namespace tags.
 *
 * <p>Only a single auto-proxy creator can be registered and multiple tags may wish
 * to register different concrete implementations. As such this class wraps a simple
 * escalation protocol, allowing clases to request a particular auto-proxy creator
 * and know that class, <code>or a subclass thereof</code>, will eventually be resident
 * in the application context.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AopNamespaceUtils {

	/**
	 * The bean name of the internally managed auto-proxy creator.
	 */
	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
					"org.springframework.aop.config.internalAutoProxyCreator";

	/**
	 * The class name of the '<code>AnnotationAwareAspectJAutoProxyCreator</code>' class.
	 * Only available with AspectJ and Java 5.
	 */
	public static final String ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME =
					"org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator";


	/**
	 * Stores the auto proxy creator classes in escalation order.
	 */
	private static final List APC_PRIORITY_LIST = new ArrayList();

	/**
	 * Setup the escalation list.
	 */
	static {
		APC_PRIORITY_LIST.add(InvocationContextExposingAdvisorAutoProxyCreator.class.getName());
		APC_PRIORITY_LIST.add(AspectJInvocationContextExposingAdvisorAutoProxyCreator.class.getName());
		APC_PRIORITY_LIST.add(ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME);
	}


	/**
	 * Register the basic auto proxy creator if required. This provides supports for using
	 * basic Spring Advisors with classic Pointcuts.
	 */
	public static void registerAutoProxyCreatorIfNecessary(ParserContext parserContext) {
		registryOrEscalateApcAsRequired(InvocationContextExposingAdvisorAutoProxyCreator.class, parserContext);
	}

	public static void registerAspectJAutoProxyCreatorIfNecessary(ParserContext parserContext) {
		registryOrEscalateApcAsRequired(AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, parserContext);
	}

	public static void registerAtAspectJAutoProxyCreatorIfNecessary(ParserContext parserContext) {
		Class cls = getAspectJAutoProxyCreatorClassIfPossible();
		if (cls == null) {
			throw new IllegalStateException("Unable to register AspectJ AutoProxyCreator. Cannot find class [" +
							ASPECTJ_AUTO_PROXY_CREATOR_CLASS_NAME + "]. Are you running on Java 5.0+?");
		}
		registryOrEscalateApcAsRequired(cls, parserContext);
	}

	private static void registryOrEscalateApcAsRequired(Class cls, ParserContext parserContext) {
		Assert.notNull(cls, "Class cannot be null");
		Assert.notNull(parserContext, "ParserContext cannot be null");

		BeanDefinitionRegistry registry = parserContext.getRegistry();

		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			AbstractBeanDefinition abd =
					(AbstractBeanDefinition) registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
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
			RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
			// Notify of bean registration.
			BeanComponentDefinition componentDefinition =
					new BeanComponentDefinition(beanDefinition, AUTO_PROXY_CREATOR_BEAN_NAME);
			parserContext.getReaderContext().fireComponentRegistered(componentDefinition);
		}
	}


	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
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

	private static final int findPriorityForClass(String className) {
		Assert.notNull(className, "Class name must not be null");
		for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
			String str = (String) APC_PRIORITY_LIST.get(i);
			if (className.equals(str)) {
				return i;
			}
		}
		throw new IllegalArgumentException(
				"Class name [" + className + "] is not a known auto-proxy creator class");
	}

}
