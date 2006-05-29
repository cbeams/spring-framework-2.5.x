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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * @author Rob Harrop
 * @author Adrian Colyer
 * @since 2.0
 */
class TxAdviceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String ATTRIBUTES = "attributes";

	public static final String TIMEOUT = "timeout";

	public static final String READ_ONLY = "read-only";

	public static final String NAME_MAP = "nameMap";

	public static final String PROPAGATION = "propagation";

	public static final String ISOLATION = "isolation";

	public static final String ROLLBACK_FOR = "rollback-for";

	public static final String NO_ROLLBACK_FOR = "no-rollback-for";


	protected Class getBeanClass(Element element) {
		return TransactionInterceptor.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		// Set the transaction manager property.
		builder.addPropertyReference(TxNamespaceUtils.TRANSACTION_MANAGER_PROPERTY,
				element.getAttribute(TxNamespaceUtils.TRANSACTION_MANAGER_ATTRIBUTE));

		List txAttributes = DomUtils.getChildElementsByTagName(element, ATTRIBUTES);
		if (txAttributes.size() > 1) {
			throw new IllegalStateException("Element 'attributes' is allowed at most once inside element 'advice'");
		}
		else if (txAttributes.size() == 1) {
			// Using attributes source.
			parseAttributes((Element) txAttributes.get(0), builder);
		}
		else {
			// Assume annotations source.
			Class sourceClass = TxNamespaceUtils.getAnnotationTransactionAttributeSourceClass();
			builder.addPropertyValue(TxNamespaceUtils.TRANSACTION_ATTRIBUTE_SOURCE, new RootBeanDefinition(sourceClass));
		}
	}

	private void parseAttributes(Element attributesElement, BeanDefinitionBuilder builder) {
		List methods = DomUtils.getChildElementsByTagName(attributesElement, "method");
		Map transactionAttributeMap = new HashMap(methods.size());

		for (int i = 0; i < methods.size(); i++) {
			Element methodElement = (Element) methods.get(i);

			String name = methodElement.getAttribute("name");

			RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();
			attribute.setPropagationBehaviorName(
					RuleBasedTransactionAttribute.PREFIX_PROPAGATION + methodElement.getAttribute(PROPAGATION));
			attribute.setIsolationLevelName(
					RuleBasedTransactionAttribute.PREFIX_ISOLATION + methodElement.getAttribute(ISOLATION));
			attribute.setTimeout(Integer.parseInt(methodElement.getAttribute(TIMEOUT)));
			attribute.setReadOnly(Boolean.valueOf(methodElement.getAttribute(READ_ONLY)).booleanValue());

			List rollbackRules = new LinkedList();
			if (methodElement.hasAttribute(ROLLBACK_FOR)) {
				String rollbackForValue = methodElement.getAttribute(ROLLBACK_FOR);
				addRollbackRuleAttributesTo(rollbackRules,rollbackForValue);
			}
			if (methodElement.hasAttribute(NO_ROLLBACK_FOR)) {
				String noRollbackForValue = methodElement.getAttribute(NO_ROLLBACK_FOR);
				addNoRollbackRuleAttributesTo(rollbackRules,noRollbackForValue);
			}
			attribute.setRollbackRules(rollbackRules);

			transactionAttributeMap.put(name, attribute);
		}

		RootBeanDefinition attributeSourceDefinition = new RootBeanDefinition(NameMatchTransactionAttributeSource.class);
		attributeSourceDefinition.setPropertyValues(new MutablePropertyValues());
		attributeSourceDefinition.getPropertyValues().addPropertyValue(NAME_MAP, transactionAttributeMap);

		builder.addPropertyValue(TxNamespaceUtils.TRANSACTION_ATTRIBUTE_SOURCE, attributeSourceDefinition);
	}

	private void addRollbackRuleAttributesTo(List rollbackRules, String rollbackForValue) {
		String[] exceptionTypeNames = StringUtils.commaDelimitedListToStringArray(rollbackForValue);
		for (int i = 0; i < exceptionTypeNames.length; i++) {
			rollbackRules.add(new RollbackRuleAttribute(StringUtils.trimWhitespace(exceptionTypeNames[i])));
		}
	}

	private void addNoRollbackRuleAttributesTo(List rollbackRules, String noRollbackForValue) {
		String[] exceptionTypeNames = StringUtils.commaDelimitedListToStringArray(noRollbackForValue);
		for (int i = 0; i < exceptionTypeNames.length; i++) {
			rollbackRules.add(new NoRollbackRuleAttribute(StringUtils.trimWhitespace(exceptionTypeNames[i])));
		}
	}

}
