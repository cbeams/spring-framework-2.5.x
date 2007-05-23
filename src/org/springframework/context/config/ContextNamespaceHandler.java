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

package org.springframework.context.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.JdkVersion;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * for the '<code>context</code>' namespace.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 */
public class ContextNamespaceHandler extends NamespaceHandlerSupport {

	private static final String PROPERTY_PLACEHOLDER_ELEMENT = "property-placeholder";

	private static final String LOAD_TIME_WEAVER_ELEMENT = "load-time-weaver";

	private static final String ANNOTATION_CONFIG_ELEMENT = "annotation-config";
	
	private static final String COMPONENT_SCAN_ELEMENT = "component-scan";


	public void init() {
		registerBeanDefinitionParser(PROPERTY_PLACEHOLDER_ELEMENT, new PropertyPlaceholderBeanDefinitionParser());
		registerJava5DependentParser(LOAD_TIME_WEAVER_ELEMENT,
				"org.springframework.context.weaving.LoadTimeWeaverBeanDefinitionParser");
		registerJava5DependentParser(ANNOTATION_CONFIG_ELEMENT,
				"org.springframework.context.annotation.AnnotationConfigBeanDefinitionParser");
		registerJava5DependentParser(COMPONENT_SCAN_ELEMENT,
				"org.springframework.context.annotation.ComponentScanBeanDefinitionParser");
	}

	private void registerJava5DependentParser(final String elementName, final String parserClassName) {
		BeanDefinitionParser parser = null;
		if (JdkVersion.isAtLeastJava15()) {
			try {
				parser = (BeanDefinitionParser) ClassUtils.forName(parserClassName).newInstance();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Unable to create JDK 1.5 dependent parser: " + parserClassName, ex);
			}
		}
		else {
			parser = new BeanDefinitionParser() {
				public BeanDefinition parse(Element element, ParserContext parserContext) {
					throw new IllegalStateException("Context namespace element '" + elementName +
							"' and its parser class [" + parserClassName + "] are only available on JDK 1.5 and higher");
				}
			};
		}
		registerBeanDefinitionParser(elementName, parser);
	}

}
