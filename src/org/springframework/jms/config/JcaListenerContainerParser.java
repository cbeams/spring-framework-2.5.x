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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.listener.endpoint.JmsActivationSpecConfig;
import org.springframework.jms.listener.endpoint.JmsMessageEndpointManager;
import org.springframework.util.StringUtils;

/**
 * Parser for the JMS <code>&lt;jca-listener-container&gt;</code> element.
 *
 * @author Juergen Hoeller
 * @since 2.1
 */
public class JcaListenerContainerParser implements BeanDefinitionParser {

	private static final String RESOURCE_ADAPTER_ATTRIBUTE = "resource-adapter";

	private static final String DEFAULT_RESOURCE_ADAPTER_BEAN_NAME = "resourceAdapter";

	private static final String ACTIVATION_SPEC_FACTORY_ATTRIBUTE = "activation-spec-factory";

	private static final String DESTINATION_TYPE_ATTRIBUTE = "destination-type";

	private static final String DESTINATION_TYPE_QUEUE = "queue";

	private static final String DESTINATION_TYPE_TOPIC = "topic";

	private static final String DESTINATION_TYPE_DURABLE_TOPIC = "durableTopic";

	private static final String CLIENT_ID_ATTRIBUTE = "client-id";

	private static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";

	private static final String CONCURRENCY_ATTRIBUTE = "concurrency";

	private static final String ID_ATTRIBUTE = "id";

	private static final String DESTINATION_ATTRIBUTE = "destination";

	private static final String SUBSCRIPTION_ATTRIBUTE = "subscription";

	private static final String SELECTOR_ATTRIBUTE = "selector";

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

	private void parseListener(Element listenerEle, Element containerEle, ParserContext parserContext) {
		RootBeanDefinition containerDef = parseContainer(containerEle, parserContext);
		RootBeanDefinition configDef = (RootBeanDefinition)
				containerDef.getPropertyValues().getPropertyValue("activationSpecConfig").getValue();
		RootBeanDefinition listenerDef = new RootBeanDefinition(MessageListenerAdapter.class);

		String destinationName = listenerEle.getAttribute(DESTINATION_ATTRIBUTE);
		if (!StringUtils.hasText(destinationName)) {
			parserContext.getReaderContext().error(
					"Listener 'destination' attribute contains empty value.", listenerEle);
		}
		configDef.getPropertyValues().addPropertyValue("destinationName", destinationName);

		if (listenerEle.hasAttribute(SUBSCRIPTION_ATTRIBUTE)) {
			String subscriptionName = listenerEle.getAttribute(SUBSCRIPTION_ATTRIBUTE);
			if (!StringUtils.hasText(subscriptionName)) {
				parserContext.getReaderContext().error(
						"Listener 'subscription' attribute contains empty value.", listenerEle);
			}
			configDef.getPropertyValues().addPropertyValue("durableSubscriptionName", subscriptionName);
		}

		if (listenerEle.hasAttribute(SELECTOR_ATTRIBUTE)) {
			String selector = listenerEle.getAttribute(SELECTOR_ATTRIBUTE);
			if (!StringUtils.hasText(selector)) {
				parserContext.getReaderContext().error(
						"Listener 'selector' attribute contains empty value.", listenerEle);
			}
			configDef.getPropertyValues().addPropertyValue("messageSelector", selector);
		}

		String handlerBean = listenerEle.getAttribute(HANDLER_BEAN_ATTRIBUTE);
		if (!StringUtils.hasText(handlerBean)) {
			parserContext.getReaderContext().error(
					"Listener 'ref' attribute contains empty value.", listenerEle);
		}
		listenerDef.getPropertyValues().addPropertyValue("delegate", new RuntimeBeanReference(handlerBean));

		String handlerMethod = null;
		if (listenerEle.hasAttribute(HANDLER_METHOD_ATTRIBUTE)) {
			handlerMethod = listenerEle.getAttribute(HANDLER_METHOD_ATTRIBUTE);
			if (!StringUtils.hasText(handlerMethod)) {
				parserContext.getReaderContext().error(
						"Listener 'method' attribute contains empty value.", listenerEle);
			}
		}
		listenerDef.getPropertyValues().addPropertyValue("defaultListenerMethod", handlerMethod);

		String listenerBeanName = parserContext.getReaderContext().registerWithGeneratedName(listenerDef);
		containerDef.getPropertyValues().addPropertyValue("messageListener", new RuntimeBeanReference(listenerBeanName));

		String containerBeanName = listenerEle.getAttribute(ID_ATTRIBUTE);
		if (StringUtils.hasText(containerBeanName)) {
			parserContext.getRegistry().registerBeanDefinition(containerBeanName, containerDef);
		}
		else {
			parserContext.getReaderContext().registerWithGeneratedName(containerDef);
		}
	}

	private RootBeanDefinition parseContainer(Element ele, ParserContext parserContext) {
		String resourceAdapterBeanName = DEFAULT_RESOURCE_ADAPTER_BEAN_NAME;
		if (ele.hasAttribute(RESOURCE_ADAPTER_ATTRIBUTE)) {
			resourceAdapterBeanName = ele.getAttribute(RESOURCE_ADAPTER_ATTRIBUTE);
			if (!StringUtils.hasText(resourceAdapterBeanName)) {
				parserContext.getReaderContext().error(
						"Listener container 'resource-adapter' attribute contains empty value.", ele);
			}
		}

		RootBeanDefinition containerDef = new RootBeanDefinition(JmsMessageEndpointManager.class);
		containerDef.getPropertyValues().addPropertyValue("resourceAdapter",
				new RuntimeBeanReference(resourceAdapterBeanName));

		String activationSpecFactoryBeanName = ele.getAttribute(ACTIVATION_SPEC_FACTORY_ATTRIBUTE);
		if (StringUtils.hasText(activationSpecFactoryBeanName)) {
			containerDef.getPropertyValues().addPropertyValue("activationSpecFactory",
					new RuntimeBeanReference(activationSpecFactoryBeanName));
		}

		RootBeanDefinition configDef = new RootBeanDefinition(JmsActivationSpecConfig.class);

		String destinationType = ele.getAttribute(DESTINATION_TYPE_ATTRIBUTE);
		if (StringUtils.hasText(destinationType)) {
			boolean pubSubDomain = false;
			boolean subscriptionDurable = false;
			if (DESTINATION_TYPE_DURABLE_TOPIC.equals(destinationType)) {
				pubSubDomain = true;
				subscriptionDurable = true;
			}
			else if (DESTINATION_TYPE_TOPIC.equals(destinationType)) {
				pubSubDomain = true;
			}
			else if (!DESTINATION_TYPE_QUEUE.equals(destinationType)) {
				parserContext.getReaderContext().error("Invalid listener container 'destination-type': " +
						"only \"queue\", \"topic\" and \"durableTopic\" supported.", ele);
			}
			configDef.getPropertyValues().addPropertyValue("pubSubDomain", Boolean.valueOf(pubSubDomain));
			configDef.getPropertyValues().addPropertyValue("subscriptionDurable", Boolean.valueOf(subscriptionDurable));
		}

		if (ele.hasAttribute(CLIENT_ID_ATTRIBUTE)) {
			String clientId = ele.getAttribute(CLIENT_ID_ATTRIBUTE);
			if (!StringUtils.hasText(clientId)) {
				parserContext.getReaderContext().error(
						"Listener 'client-id' attribute contains empty value.", ele);
			}
			configDef.getPropertyValues().addPropertyValue("clientId", clientId);
		}

		String concurrency = ele.getAttribute(CONCURRENCY_ATTRIBUTE);
		if (StringUtils.hasText(concurrency)) {
			configDef.getPropertyValues().addPropertyValue("maxConcurrency", new Integer(concurrency));
		}

		containerDef.getPropertyValues().addPropertyValue("activationSpecConfig", configDef);

		String transactionManagerBeanName = ele.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE);
		if (StringUtils.hasText(transactionManagerBeanName)) {
			containerDef.getPropertyValues().addPropertyValue("transactionManager",
					new RuntimeBeanReference(transactionManagerBeanName));
		}

		return containerDef;
	}

}
