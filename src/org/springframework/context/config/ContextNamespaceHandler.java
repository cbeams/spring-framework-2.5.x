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

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.core.JdkVersion;
import org.springframework.util.ClassUtils;

/**
 * Handler for the context namespace.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 */
public class ContextNamespaceHandler extends NamespaceHandlerSupport {

	private static final String PROPERTY_PLACEHOLDER_ELEMENT = "property-placeholder";

	private static final String ANNOTATION_CONFIG_ELEMENT = "annotation-config";
	
	private static final String COMPONENT_SCAN_ELEMENT = "component-scan";


	public void init() {
		registerBeanDefinitionParser(PROPERTY_PLACEHOLDER_ELEMENT, new PropertyPlaceholderBeanDefinitionParser());
		if (JdkVersion.isAtLeastJava15()) {
			registerBeanDefinitionParser(ANNOTATION_CONFIG_ELEMENT, createAnnotationConfigParser());
			registerBeanDefinitionParser(COMPONENT_SCAN_ELEMENT, createComponentScanParser());
		}
	}


	protected BeanDefinitionParser createAnnotationConfigParser() {
		return doCreateParser("org.springframework.context.annotation.AnnotationConfigBeanDefinitionParser");
	}

	protected BeanDefinitionParser createComponentScanParser() {
		return doCreateParser("org.springframework.context.annotation.ComponentScanBeanDefinitionParser");
	}

	protected static BeanDefinitionParser doCreateParser(String className) {
		try {
			return (BeanDefinitionParser) ClassUtils.forName(className).newInstance();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to create JDK 1.5 dependent parser: " + className, ex);
		}
	}

}
