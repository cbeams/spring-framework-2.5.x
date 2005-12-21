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

package org.springframework.transaction.config;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.ClassUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.aop.config.NamespaceHandlerUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class TxNamespaceHandler extends NamespaceHandlerSupport {

	private static final String TRANSACTION_MANAGER = "transactionManager";

	private static final String TRANSACTION_ATTRIBUTE_SOURCE = "transactionAttributeSource";

	private static final String ANNOTATION_SOURCE_CLASS_NAME = "org.springframework.transaction.annotation.AnnotationTransactionAttributeSource";

	public TxNamespaceHandler() {
		registerBeanDefinitionParser("advice", new TxAdviceBeanDefinitionParser());
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
	}

	private static Class getAnnotationSourceClass() {
		try {
			return ClassUtils.forName(ANNOTATION_SOURCE_CLASS_NAME);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Unable to locate AnnotationTransactionAttributeSource. Are you running on Java 1.5?");
		}
	}

	private static class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

		private static final String TRANSACTION_ATTRIBUTE_SOURCE_ADVISOR_NAME = ".transactionAttributeSourceAdvisor";

		public static final String TRANSACTION_INTERCEPTOR = "transactionInterceptor";

		public void parse(Element element, BeanDefinitionRegistry registry) {
			// register the APC if needed
			NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);

			String transactionManagerName = element.getAttribute(TRANSACTION_MANAGER);

			// create the TransactionInterceptor definition
			RootBeanDefinition interceptorDefinition = new RootBeanDefinition(TransactionInterceptor.class);
			interceptorDefinition.setPropertyValues(new MutablePropertyValues());
			interceptorDefinition.getPropertyValues().addPropertyValue(TRANSACTION_MANAGER, new RuntimeBeanReference(transactionManagerName));
			interceptorDefinition.getPropertyValues().addPropertyValue(TRANSACTION_ATTRIBUTE_SOURCE, new RootBeanDefinition(getAnnotationSourceClass()));

			// create the TransactionAttributeSourceAdvisor definition
			RootBeanDefinition advisorDefinition = new RootBeanDefinition(TransactionAttributeSourceAdvisor.class);
			advisorDefinition.setPropertyValues(new MutablePropertyValues());
			advisorDefinition.getPropertyValues().addPropertyValue(TRANSACTION_INTERCEPTOR, interceptorDefinition);

			registry.registerBeanDefinition(TRANSACTION_ATTRIBUTE_SOURCE_ADVISOR_NAME, advisorDefinition);
		}
	}

	private static class TxAdviceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		private static final String ATTRIBUTES = "attributes";

		public static final String TIMEOUT = "timeout";

		public static final String READ_ONLY = "read-only";

		public static final String NAME_MAP = "nameMap";

		public static final String PROPAGATION = "propagation";

		public static final String ISOLATION = "isolation";

		protected BeanDefinition doParse(Element element) {
			RootBeanDefinition definition = new RootBeanDefinition(TransactionInterceptor.class);

			MutablePropertyValues mpvs = new MutablePropertyValues();
			definition.setPropertyValues(mpvs);

			// set the transaction manager property
			mpvs.addPropertyValue(TRANSACTION_MANAGER, new RuntimeBeanReference(element.getAttribute(TRANSACTION_MANAGER)));

			List txAttributes = DomUtils.getChildElementsByTagName(element, ATTRIBUTES, true);

			if (txAttributes.size() > 1) {
				throw new IllegalStateException("Element 'attributes' is allowed at most once inside element 'advice'");
			}
			else if (txAttributes.size() == 1) {
				// using Attributes source
				parseAttributes((Element) txAttributes.get(0), mpvs);
			}
			else {
				// assume annotations source
				// TODO: fix this to use direct class reference when building all on 1.5
				Class beanClass = getAnnotationSourceClass();

				mpvs.addPropertyValue(TRANSACTION_ATTRIBUTE_SOURCE, new RootBeanDefinition(beanClass));
			}
			return definition;
		}

		private void parseAttributes(Element attributesElement, MutablePropertyValues mpvs) {
			List methods = DomUtils.getChildElementsByTagName(attributesElement, "method", true);

			Map transactionAttributeMap = new HashMap(methods.size());

			for (int i = 0; i < methods.size(); i++) {
				Element methodElement = (Element) methods.get(i);

				String name = methodElement.getAttribute("name");

				RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();
				attribute.setPropagationBehaviorName(TransactionDefinition.PROPAGATION_CONSTANT_PREFIX + methodElement.getAttribute(PROPAGATION));
				attribute.setIsolationLevelName(TransactionDefinition.ISOLATION_CONSTANT_PREFIX + methodElement.getAttribute(ISOLATION));
				attribute.setTimeout(Integer.parseInt(methodElement.getAttribute(TIMEOUT)));
				attribute.setReadOnly(Boolean.valueOf(methodElement.getAttribute(READ_ONLY)).booleanValue());

				transactionAttributeMap.put(name, attribute);
			}

			RootBeanDefinition attributeSourceDefinition = new RootBeanDefinition(NameMatchTransactionAttributeSource.class);
			attributeSourceDefinition.setPropertyValues(new MutablePropertyValues());
			attributeSourceDefinition.getPropertyValues().addPropertyValue(NAME_MAP, transactionAttributeMap);

			mpvs.addPropertyValue(TRANSACTION_ATTRIBUTE_SOURCE, attributeSourceDefinition);
		}
	}
}
