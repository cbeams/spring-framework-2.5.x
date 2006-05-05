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

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespacehandler that handles the <code>web</code> namespace, defined
 * in <code>spring-web.xsd</code>. For now only supports the configuration
 * of handler mappings through a schema.
 * 
 * @author Alef Arendsen
 * @see UrlHandlerMappingBeanDefinitionParser
 * @since 2.0
 */
public class MvcNamespaceHandler extends NamespaceHandlerSupport {
	
	public void init() {
		registerBeanDefinitionParser("url-mappings", new UrlHandlerMappingBeanDefinitionParser());
		registerBeanDefinitionParser("view-resolvers", new ViewResolversBeanDefinitionParser());		
	}

}
