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

package org.springframework.web.servlet.config;

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * Bean definition parser <code>&lt;url:urlmappings&gt;</code> tag,
 * resulting in a SimpleUrlHandlerMapping.
 *
 * @author Alef Arendsen
 */
public class UrlHandlerMappingBeanDefinitionParser implements BeanDefinitionParser {
	
	private static final String URLMAPPING = "url-mapping";
	private static final String ALWAYS_USE_FULL_PATH = "always-use-full-path";
	private static final String ALWAYS_USE_FULL_PATH_PROPERTY = "alwaysUseFullPath";
	private static final String URL_DECODE = "url-decode";
	private static final String URL_DECODE_PROPERTY = "urlDecode";
	private static final String LAZY_INIT_HANDLERS = "lazy-init-handlers";
	private static final String LAZY_INIT_HANLDERS_PROPERTY = "lazyInitHandlers";
	private static final String DEFAULT_HANDLER = "default-handler";

	private static final String HANDLER = "handler";	
	private static final String PATH = "path";
	private static final String CONTROLLER_REF = "controller-ref";

	private static final String INTERCEPTOR = "interceptor";
	private static final String INTERCEPTOR_REF = "interceptor-ref";


	/**
	 * Parses the url:handlermappings element resulting in a SimpleUrlHandlerMapping
	 * bean definition. Except for all the normal behavior, adds behavior
	 * for HandlerInterceptors matched on a specific path.
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		NodeList handlerMappingChildren = element.getChildNodes();
		
		int handlerCount = 0;
		for (int i = 0; i < handlerMappingChildren.getLength(); i++) {
			Node handlerMapping = handlerMappingChildren.item(i);
		
			if (URLMAPPING.equals(handlerMapping.getLocalName()) && handlerMapping.getNodeType() == Node.ELEMENT_NODE) {
				Element ele = (Element) handlerMapping;
				RootBeanDefinition handlerMappingDefinition = parseHandlerMappingDefinition(ele);
				handlerMappingDefinition.setSource(parserContext.extractSource(element));
				handlerMappingDefinition.getPropertyValues().addPropertyValue("order", new Integer(handlerCount++));
				parserContext.getReaderContext().registerWithGeneratedName(handlerMappingDefinition);
			}
		}

		return null;
	}
	
	private RootBeanDefinition parseHandlerMappingDefinition(Element element) {
		Properties mappings = new Properties();
		ManagedList interceptors = new ManagedList();
		
		NodeList childNodes = element.getChildNodes();
		
		// parse url mappings
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childElement = childNodes.item(i);
			// just to be sure that it's really a urlmapping
			if (HANDLER.equals(childElement.getLocalName()) && childElement.getNodeType() == Node.ELEMENT_NODE) {
				// parse handler mapping mapping paths to urls
				Element el = (Element)childElement;
				String path = el.getAttribute(PATH);
				String ref = el.getAttribute(CONTROLLER_REF);
				mappings.put(path, ref);
			}
			else if (INTERCEPTOR.equals(childElement.getLocalName()) && childElement.getNodeType() == Node.ELEMENT_NODE) {
				// parse interceptor
				Element el = (Element)childElement;
				String ref = el.getAttribute(INTERCEPTOR_REF);
				interceptors.add(new RuntimeBeanReference(ref));
			}
		}	
		
		// create root bean definition
		RootBeanDefinition definition = new RootBeanDefinition();	
		definition.setBeanClass(SimpleUrlHandlerMapping.class);
		definition.getPropertyValues().addPropertyValue("mappings", mappings);
		
		definition.getPropertyValues().addPropertyValue("interceptors", interceptors);
		
		// parse attributes
		setPropertyIfAvailable(element, ALWAYS_USE_FULL_PATH, ALWAYS_USE_FULL_PATH_PROPERTY, definition);
		setPropertyIfAvailable(element, URL_DECODE, URL_DECODE_PROPERTY, definition);
		setPropertyIfAvailable(element, LAZY_INIT_HANDLERS, LAZY_INIT_HANLDERS_PROPERTY, definition);
		
		String defaultHandler = element.getAttribute(DEFAULT_HANDLER);
		if (StringUtils.hasText(defaultHandler)) {
			definition.getPropertyValues().addPropertyValue("defaultHandler", defaultHandler);
		}

		return definition;
	}

	private void setPropertyIfAvailable(Element el, String attribute, String property, RootBeanDefinition definition) {
		String propertyValue = el.getAttribute(attribute);
		if (StringUtils.hasText(propertyValue)) {
			definition.getPropertyValues().addPropertyValue(property, propertyValue);
		}
	}

}
