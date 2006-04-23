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

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * <code>NamespaceHandler</code> for the <code>aop</code> namespace.
 *
 * <p>Provides a {@link org.springframework.beans.factory.xml.BeanDefinitionParser} for the
 * <code>&lt;aop:config&gt;</code> tag. A <code>config</code> tag can include nested
 * <code>pointcut</code>, <code>advisor</code> and <code>aspect</code> tags.
 *
 * <p>The <code>pointcut</code> tag allows for creation of named
 * {@link AspectJExpressionPointcut} beans using a simple syntax:
 * <pre>
 * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;
 * </pre>
 *
 * <p>Using the <code>advisor</code> tag you can configure an {@link org.springframework.aop.Advisor}
 * and have it applied to all relevant beans in you {@link org.springframework.beans.factory.BeanFactory}
 * automatically. The <code>advisor</code> tag supports both in-line and referenced
 * {@link org.springframework.aop.Pointcut Pointcuts}:
 *
 * <pre>
 * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;
 *              pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;
 *              advice-ref=&quot;getAgeCounter&quot;/&gt;
 *
 * &lt;aop:advisor id=&quot;getNameAdvisor&quot;
 *              pointcut-ref=&quot;getNameCalls&quot;
 *              advice-ref=&quot;getNameCounter&quot;/&gt;
 * </pre>
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Rod Johnson
 * @since 2.0
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * Register the {@link BeanDefinitionParser BeanDefinitionParsers} for the
	 * '<code>config</code>', '<code>spring-configured</code>' and '<code>aspectj-autoproxy</code>' tag.
	 */
	public void init() {
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());

		registerBeanDefinitionDecorator("scope", new ScopeBeanDefinitionDefinition());
	}


	private static class AspectJAutoProxyBeanDefinitionParser implements BeanDefinitionParser {

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			NamespaceHandlerUtils.registerAtAspectJAutoProxyCreatorIfNecessary(parserContext.getRegistry());
			if (element.hasChildNodes()) {
				addIncludePatterns(element, parserContext.getRegistry());
			}
			return null;
		}
		
		private void addIncludePatterns(Element element, BeanDefinitionRegistry registry) {
			BeanDefinition beanDef = registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
			List includePatterns = new LinkedList();
			NodeList childNodes = element.getChildNodes();
			for(int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node instanceof Element) {
					Element include = (Element) node;
					String patternText = include.getAttribute("name");
					includePatterns.add(patternText);
				}
			}
			beanDef.getPropertyValues().addPropertyValue("includePatterns", includePatterns);
		}
	}

}
