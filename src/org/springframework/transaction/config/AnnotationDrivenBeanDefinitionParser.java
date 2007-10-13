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

package org.springframework.transaction.config;

import org.w3c.dom.Element;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser}
 * implementation that allows users to easily configure all the infrastructure
 * beans required to enable annotation-driven transaction demarcation.
 *
 * <p>By default, all proxies are created as JDK proxies. This may cause some
 * problems if you are injecting objects as concrete classes rather than
 * interfaces. To overcome this restriction you can set the
 * '<code>proxy-target-class</code>' attribute to '<code>true</code>', which
 * will result in class-based proxies being created.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 */
class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	public static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

	/**
	 * The bean name of the internally managed transaction advisor (mode="proxy").
	 */
	public static final String TRANSACTION_ADVISOR_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAdvisor";

	/**
	 * The bean name of the internally managed transaction aspect (mode="aspectj").
	 */
	public static final String TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAspect";

	private static final String TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.AnnotationTransactionAspect";


	/**
	 * Parses the '<code>&lt;tx:annotation-driven/&gt;</code>' tag. Will
	 * {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary register an AutoProxyCreator}
	 * with the container as necessary.
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String mode = element.getAttribute("mode");
		if ("aspectj".equals(mode)) {
			// mode="aspectj"
			registerTransactionAspect(element, parserContext);
		}
		else {
			// mode="proxy"
			AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
		}
		return null;
	}

	private void registerTransactionAspect(Element element, ParserContext parserContext) {
		if (!parserContext.getRegistry().containsBeanDefinition(TRANSACTION_ASPECT_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			def.setBeanClassName(TRANSACTION_ASPECT_CLASS_NAME);
			def.setFactoryMethodName("aspectOf");
			registerTransactionManager(element, def);
			parserContext.registerBeanComponent(new BeanComponentDefinition(def, TRANSACTION_ASPECT_BEAN_NAME));
		}
	}

	private static void registerTransactionManager(Element element, BeanDefinition def) {
		String transactionManagerName = (element.hasAttribute(TxNamespaceUtils.TRANSACTION_MANAGER_ATTRIBUTE) ?
				element.getAttribute(TxNamespaceUtils.TRANSACTION_MANAGER_ATTRIBUTE) : DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
		def.getPropertyValues().addPropertyValue(
				TxNamespaceUtils.TRANSACTION_MANAGER_PROPERTY, new RuntimeBeanReference(transactionManagerName));
	}


	/**
	 * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
	 */
	private static class AopAutoProxyConfigurer {

		public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);

			if (!parserContext.getRegistry().containsBeanDefinition(TRANSACTION_ADVISOR_BEAN_NAME)) {
				// Create the TransactionInterceptor definition.
				RootBeanDefinition interceptorDef = new RootBeanDefinition(TransactionInterceptor.class);
				interceptorDef.setSource(parserContext.extractSource(element));
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				registerTransactionManager(element, interceptorDef);

				Class sourceClass = TxNamespaceUtils.getAnnotationTransactionAttributeSourceClass();
				interceptorDef.getPropertyValues().addPropertyValue(
						TxNamespaceUtils.TRANSACTION_ATTRIBUTE_SOURCE, new RootBeanDefinition(sourceClass));

				// Create the TransactionAttributeSourceAdvisor definition.
				RootBeanDefinition advisorDef = new RootBeanDefinition(TransactionAttributeSourceAdvisor.class);
				advisorDef.setSource(parserContext.extractSource(element));
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().addPropertyValue("transactionInterceptor", interceptorDef);
				if (element.hasAttribute("order")) {
					advisorDef.getPropertyValues().addPropertyValue("order", element.getAttribute("order"));
				}

				parserContext.registerBeanComponent(new BeanComponentDefinition(advisorDef, TRANSACTION_ADVISOR_BEAN_NAME));
			}
		}
	}

}
