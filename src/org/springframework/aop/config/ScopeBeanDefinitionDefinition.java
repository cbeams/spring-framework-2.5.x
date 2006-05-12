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

import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.aop.target.scope.ScopedProxyFactoryBean;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * {@link BeanDefinitionDecorator} responsible for parsing the <code>&lt;aop:scope/&gt;</code> tag.
 *
 * @author Rob Harrop
 * @since 2.0
 */
class ScopeBeanDefinitionDefinition implements BeanDefinitionDecorator {

	private static String REQUEST_SCOPE_MAP = "org.springframework.web.context.scope.RequestScopeMap";

	private static String SESSION_SCOPE_MAP = "org.springframework.web.context.scope.SessionScopeMap";

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		Element element = (Element) node;
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		// must use class proxying for any AOP advice now
		NamespaceHandlerUtils.forceAutoProxyCreatorToUseClassProxying(registry);

		String originalBeanName = definition.getBeanName();
		String targetBeanName = "__" + originalBeanName;

		RootBeanDefinition scopeFactoryDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		scopeFactoryDefinition.setPropertyValues(mpvs);

		// target bean name
		mpvs.addPropertyValue("targetBeanName", targetBeanName);

		// scope key
		mpvs.addPropertyValue("scopeKey", originalBeanName);

		// scope map
		String type = element.getAttribute("type");

		String scopeMapClassName;
		if("request".equals(type)) {
			scopeMapClassName = REQUEST_SCOPE_MAP;
		}
		else if("session".equals(type)) {
			scopeMapClassName = SESSION_SCOPE_MAP;
		}
		else {
			throw new IllegalStateException("Scope [" + type + "] is not recognised.");
		}

		Class scopeMapClass;
		try {
			scopeMapClass = ClassUtils.forName(scopeMapClassName);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Unable to locate ScopeMap class [" + scopeMapClassName + "].");
		}

		mpvs.addPropertyValue("scopeMap", new RootBeanDefinition(scopeMapClass));

		// register the scope factory
		registry.registerBeanDefinition(originalBeanName, scopeFactoryDefinition);

		// Switch the old definition to prototype.
		((AbstractBeanDefinition) definition.getBeanDefinition()).setSingleton(false);
		return new BeanDefinitionHolder(definition.getBeanDefinition(), targetBeanName);
	}
}
