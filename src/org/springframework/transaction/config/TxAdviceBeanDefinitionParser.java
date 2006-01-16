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

package org.springframework.transaction.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rob Harrop
 * @since 2.0
 */
class TxAdviceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String ATTRIBUTES = "attributes";

	public static final String TIMEOUT = "timeout";

	public static final String READ_ONLY = "read-only";

	public static final String NAME_MAP = "nameMap";

	public static final String PROPAGATION = "propagation";

	public static final String ISOLATION = "isolation";

	protected Class getBeanClass(Element element) {
		return TransactionInterceptor.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder builder) {

		// set the transaction manager property
		builder.addPropertyReference(TxNamespaceHandler.TRANSACTION_MANAGER, element.getAttribute(TxNamespaceHandler.TRANSACTION_MANAGER));

		List txAttributes = DomUtils.getChildElementsByTagName(element, ATTRIBUTES, true);

		if (txAttributes.size() > 1) {
			throw new IllegalStateException("Element 'attributes' is allowed at most once inside element 'advice'");
		}
		else if (txAttributes.size() == 1) {
			// using Attributes source
			parseAttributes((Element) txAttributes.get(0), builder);
		}
		else {
			// assume annotations source
			// TODO: fix this to use direct class reference when building all on 1.5
			builder.addPropertyValue(TxNamespaceHandler.TRANSACTION_ATTRIBUTE_SOURCE,
					new RootBeanDefinition(TxNamespaceHandler.getAnnotationSourceClass()));
		}
	}

	private void parseAttributes(Element attributesElement, BeanDefinitionBuilder builder) {
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

		builder.addPropertyValue(TxNamespaceHandler.TRANSACTION_ATTRIBUTE_SOURCE, attributeSourceDefinition);
	}
}
