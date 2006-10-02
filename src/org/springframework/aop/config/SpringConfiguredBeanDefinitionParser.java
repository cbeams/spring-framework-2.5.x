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

package org.springframework.aop.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;

/**
 * {@link BeanDefinitionParser} responsible for parsing the <code>&lt;aop:spring-configured/&gt;</code> tag.
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @since 2.0
 */
class SpringConfiguredBeanDefinitionParser implements BeanDefinitionParser {

	private static final String BEAN_CONFIGURER_CLASS_NAME =
			"org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect";


	private boolean registered;


	public BeanDefinition parse(Element element, ParserContext parserContext) {
		if (!this.registered) {
			BeanDefinitionBuilder builder =
					BeanDefinitionBuilder.rootBeanDefinition(getBeanConfigurerClass(), "aspectOf");
			builder.setSource(parserContext.extractSource(element));
			BeanDefinitionReaderUtils.registerWithGeneratedName(
					builder.getBeanDefinition(), parserContext.getRegistry());
			this.registered = true;
		}

		return null;
	}

	/**
	 * Load the <code>Class</code> instance for the bean configurer.
	 * @throws IllegalStateException if the bean configurer <code>Class</code> cannot be found
	 */
	private static Class getBeanConfigurerClass() throws IllegalStateException {
		try {
			return ClassUtils.forName(BEAN_CONFIGURER_CLASS_NAME);
		}
		catch (Throwable ex) {
			throw new IllegalStateException(
					"Unable to load aspect class [" + BEAN_CONFIGURER_CLASS_NAME +
					"]: cannot use @Configurable. Root cause: " + ex);
		}
	}

}
