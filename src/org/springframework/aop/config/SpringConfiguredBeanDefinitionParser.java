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
import org.springframework.beans.factory.support.BeanDefinitionRegistryBuilder;
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

	private static final String ASPECT_OF = "aspectOf";

	private static final String BEAN_CONFIGURER = "org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect";


	private boolean registered;


	public BeanDefinition parse(Element element, ParserContext parserContext) {
		if (!this.registered) {
			BeanDefinitionRegistryBuilder registryBuilder = new BeanDefinitionRegistryBuilder(parserContext.getRegistry());
			registryBuilder.register(BeanDefinitionBuilder.rootBeanDefinition(getBeanConfigurerClass(), ASPECT_OF));
			this.registered = true;
		}

		return null;
	}

	/**
	 * Returns the <code>Class</code> instance for the {@link #BEAN_CONFIGURER bean configurer}.
	 * @throws IllegalStateException if the bean configurer <code>Class</code> cannot be found
	 */
	private static Class getBeanConfigurerClass() throws IllegalStateException {
		try {
			return ClassUtils.forName(BEAN_CONFIGURER);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException(
					"Unable to locate class [" + BEAN_CONFIGURER + "]: cannot use @Configurable");
		}
	}

}
