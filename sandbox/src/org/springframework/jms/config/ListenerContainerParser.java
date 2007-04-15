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

package org.springframework.jms.config;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for the <code>&lt;listener-container&gt;</code> element.
 * 
 * @author Mark Fisher
 */
public class ListenerContainerParser implements BeanDefinitionParser {

	private static final String ID = "id";

	private static final String DEFAULT_CONNECTION_FACTORY_BEAN_NAME = "connectionFactory";

	private static final String CONNECTION_FACTORY_ATTRIBUTE = "connection-factory";

	private static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";

	private static final String DESTINATION_ATTRIBUTE = "destination";

	private static final String HANDLER_BEAN_ATTRIBUTE = "ref";

	private static final String HANDLER_METHOD_ATTRIBUTE = "method";

	private static final String LISTENER_ELEMENT = "listener";

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				String localName = child.getLocalName();
				if (LISTENER_ELEMENT.equals(localName)) {
					parseListener((Element) child, element, parserContext);
				}
			}
		}
		return null;
	}

	private void parseListener(Element listenerElement, Element containerElement, ParserContext parserContext) {
		RootBeanDefinition listenerBeanDefinition = new RootBeanDefinition(MessageListenerAdapter.class);
		MutablePropertyValues listenerPropertyValues = new MutablePropertyValues();
		listenerPropertyValues.addPropertyValue("delegate", new RuntimeBeanReference(listenerElement
				.getAttribute(HANDLER_BEAN_ATTRIBUTE)));
		listenerPropertyValues.addPropertyValue("defaultListenerMethod", listenerElement
				.getAttribute(HANDLER_METHOD_ATTRIBUTE));
		listenerBeanDefinition.getPropertyValues().addPropertyValues(listenerPropertyValues);

		String listenerBeanName = listenerElement.getAttribute(ID);
		if (StringUtils.hasText(listenerBeanName)) {
			parserContext.getRegistry().registerBeanDefinition(listenerBeanName, listenerBeanDefinition);
		}
		else {
			listenerBeanName = parserContext.getReaderContext().registerWithGeneratedName(listenerBeanDefinition);
		}

		String destinationBeanName = listenerElement.getAttribute(DESTINATION_ATTRIBUTE);
		parseContainer(containerElement, parserContext, destinationBeanName, listenerBeanName);
	}

	private void parseContainer(Element element, ParserContext parserContext, String destinationBeanName,
			String listenerBeanName) {
		String connectionFactoryBeanName = resolveConnectionFactoryBeanName(element);

		RootBeanDefinition containerBeanDefinition = new RootBeanDefinition(DefaultMessageListenerContainer.class);
		MutablePropertyValues containerPropertyValues = new MutablePropertyValues();
		containerPropertyValues.addPropertyValue("destination", new RuntimeBeanReference(destinationBeanName));
		containerPropertyValues.addPropertyValue("messageListener", new RuntimeBeanReference(listenerBeanName));
		containerPropertyValues.addPropertyValue("connectionFactory", new RuntimeBeanReference(
				connectionFactoryBeanName));

		String transactionManagerBeanName = element.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE);
		if (StringUtils.hasText(transactionManagerBeanName)) {
			containerPropertyValues.addPropertyValue("transactionManager", new RuntimeBeanReference(
					transactionManagerBeanName));
		}
		containerBeanDefinition.getPropertyValues().addPropertyValues(containerPropertyValues);
		parserContext.getReaderContext().registerWithGeneratedName(containerBeanDefinition);
	}

	private String resolveConnectionFactoryBeanName(Element element) {
		String connectionFactoryBeanName = element.getAttribute(CONNECTION_FACTORY_ATTRIBUTE);
		if (!StringUtils.hasText(connectionFactoryBeanName)) {
			connectionFactoryBeanName = DEFAULT_CONNECTION_FACTORY_BEAN_NAME;
		}
		return connectionFactoryBeanName;
	}

}
