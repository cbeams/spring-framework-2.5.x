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

package org.springframework.aop.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * {@link BeanDefinitionDecorator} responsible for parsing the <code>&lt;aop:scope/&gt;</code> tag.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
class ScopedProxyBeanDefinitionDecorator implements BeanDefinitionDecorator {

	/** The '<code>proxy-target-class</code>' attribute */
	private static final String PROXY_TARGET_CLASS = "proxy-target-class";

	private static final String TARGET_NAME_PREFIX = "scopedTarget.";


	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		String originalBeanName = definition.getBeanName();
		BeanDefinition targetDefinition = definition.getBeanDefinition();
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		String targetBeanName = TARGET_NAME_PREFIX + originalBeanName;
		RootBeanDefinition scopeFactoryDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
		scopeFactoryDefinition.getPropertyValues().addPropertyValue("targetBeanName", targetBeanName);

		boolean proxyTargetClass = (!(node instanceof Element) ||
				Boolean.valueOf(((Element) node).getAttribute(PROXY_TARGET_CLASS)).booleanValue());
		if (proxyTargetClass) {
			targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
			// ScopedFactoryBean's "proxyTargetClass" default is TRUE, so we don't need to set it explicitly here.
		}
		else {
			scopeFactoryDefinition.getPropertyValues().addPropertyValue("proxyTargetClass", Boolean.FALSE);
		}

		// Register the scope factory.
		registry.registerBeanDefinition(originalBeanName, scopeFactoryDefinition);

		return new BeanDefinitionHolder(targetDefinition, targetBeanName);
	}

}
