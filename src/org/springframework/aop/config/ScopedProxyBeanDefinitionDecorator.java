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

import org.w3c.dom.Node;

import org.springframework.aop.scope.ScopedProxyFactoryBean;
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

	private static final String TARGET_NAME_PREFIX = "scopedTarget.";


	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		// Must use class proxying for any AOP advice now.
		AopNamespaceUtils.forceAutoProxyCreatorToUseClassProxying(registry);

		String originalBeanName = definition.getBeanName();
		String targetBeanName = TARGET_NAME_PREFIX + originalBeanName;

		RootBeanDefinition scopeFactoryDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
		scopeFactoryDefinition.getPropertyValues().addPropertyValue("targetBeanName", targetBeanName);

		// Register the scope factory.
		registry.registerBeanDefinition(originalBeanName, scopeFactoryDefinition);

		return new BeanDefinitionHolder(definition.getBeanDefinition(), targetBeanName);
	}

}
