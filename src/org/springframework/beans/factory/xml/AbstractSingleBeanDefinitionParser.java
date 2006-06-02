/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * Creates a {@link BeanDefinitionBuilder} instance for the {@link #getBeanClass bean Class} and passes
	 * it to the {@link #doParse} strategy method.
	 */
	protected final BeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanClass(element));
		doParse(element, definitionBuilder);
		return definitionBuilder.getBeanDefinition();
	}

	/**
	 * Gets the bean class corresponding to the supplied {@link Element}.
	 */
	protected abstract Class getBeanClass(Element element);

	/**
	 * Parse the supplied {@link Element} and populate the supplied {@link BeanDefinitionBuilder} as
	 * required.
	 */
	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
	}
}
