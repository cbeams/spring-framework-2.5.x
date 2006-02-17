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

package org.springframework.web.servlet.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Bean definition parser <code>&lt;url:urlmappings&gt;</code> tag,
 * resulting in a SimpleUrlHandlerMapping.
 * 
 * XML should adhere to the spring-web.xsd schema. 
 * 
 * @author Alef Arendsen
 * @since 2.0
 */
public class UrlHandlerMappingBeanDefinitionParser implements
		BeanDefinitionParser {
	
	//---------------------------------------------------------------------
	// Static section
	//---------------------------------------------------------------------
	
	private static final String URLMAPPING = "urlmapping";
	
	private static final String ALWAYS_USE_FULL_PATH = "alwaysUseFullPath";
	private static final String URL_DECODE = "urlDecode";
	private static final String LAZY_INIT_HANDLERS = "lazyInitHandlers";
	private static final String ORDER = "order";
	private static final String DEFAULT_HANDLER = "defaultHandler";

	private static final String INTERCEPTOR = "interceptor";

	//---------------------------------------------------------------------
	// Instance section
	//---------------------------------------------------------------------

	/**
	 * Parses the url:handlermappings element resulting in a SimpleUrlHandlerMapping
	 * bean definition. Except for all the normal behavior, adds behavior
	 * for HandlerInterceptors matched on a specific path.
	 */
	public void parse(Element element, BeanDefinitionRegistry registry) {
		Assert.notNull(element);
		Assert.notNull(registry);
		
		Properties mappings = new Properties();
		ManagedList interceptors = new ManagedList();
		
		NodeList childNodes = element.getChildNodes();
		
		// parse url mappings
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childElement = childNodes.item(i);
			// just to be sure that it's really a urlmapping
			if (URLMAPPING.equals(childElement.getLocalName()) && childElement.getNodeType() == Node.ELEMENT_NODE) {
				// parse handler mapping mapping paths to urls
				Element el = (Element)childElement;
				String path = el.getAttribute("path");
				String ref = el.getAttribute("ref");
				
				mappings.put(path, ref);
			} else if (INTERCEPTOR.equals(childElement.getLocalName()) && childElement.getNodeType() == Node.ELEMENT_NODE) {
				// parse interceptor
				Element el = (Element)childElement;
				String ref = el.getAttribute("ref");
				interceptors.add(new RuntimeBeanReference(ref));
			}
		}	
		
		// create root bean definition
		RootBeanDefinition definition = new RootBeanDefinition();
		definition.setBeanClass(SimpleUrlHandlerMapping.class);
		definition.getPropertyValues().addPropertyValue("mappings", mappings);
		
		definition.getPropertyValues().addPropertyValue("interceptors", interceptors);
		
		// parse attributes
		setPropertyIfAvailable(element, ALWAYS_USE_FULL_PATH, definition);
		setPropertyIfAvailable(element, URL_DECODE, definition);
		setPropertyIfAvailable(element, LAZY_INIT_HANDLERS, definition);
		setPropertyIfAvailable(element, ORDER, definition);
		
		String defaultHandler = element.getAttribute(DEFAULT_HANDLER);
		if (StringUtils.hasText(defaultHandler)) {
			definition.getPropertyValues().addPropertyValue("defaultHandler", defaultHandler);
		}
		
		registry.registerBeanDefinition(
				BeanDefinitionReaderUtils.generateBeanName(definition, registry, false), definition);
	}
	
	private void setPropertyIfAvailable(Element el, String property, RootBeanDefinition definition) {
		String propertyValue = el.getAttribute(property);
		if (StringUtils.hasText(propertyValue)) {
			definition.getPropertyValues().addPropertyValue(property, propertyValue);
		}
	}

}
