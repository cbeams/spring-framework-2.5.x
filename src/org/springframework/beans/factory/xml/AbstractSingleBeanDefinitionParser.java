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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.Assert;

/**
 * Base class for those {@link BeanDefinitionParser} implementations that
 * need to parse and define just a <i>single</i> {@link BeanDefinition}.
 *
 * <p>Extend this parser class when you want to create a single bean definition
 * from an arbitrarily complex XML element. You may wish to consider extending
 * the {@link AbstractSimpleBeanDefinitionParser} when you want to create a
 * single bean definition from a relatively simple custom XML element.
 *
 * <p>The resulting <code>BeanDefinition</code> will be automatically registered
 * with the {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
 * Your job simply is to
 * {@link #doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder) parse}
 * the custom XML {@link Element} into a single <code>BeanDefinition</code>.
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @since 2.0
 */
public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * Creates a {@link BeanDefinitionBuilder} instance for the
	 * {@link #getBeanClass bean Class} and passes it to the
	 * {@link #doParse} strategy method.
	 * @param element the element that is to be parsed into a single {@link BeanDefinition}
	 * @param parserContext the object encapsulating the current state of the parse
	 * @return the {@link BeanDefinition} resulting from the parsing of the supplied {@link Element}
	 * @throws IllegalStateException if the bean {@link Class} returned from
	 * {@link #getBeanClass(org.w3c.dom.Element)} is <code>null</code>
	 * @see #doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
	 */
	protected final BeanDefinition parseInternal(Element element, ParserContext parserContext) {
		Class beanClass = getBeanClass(element);
		Assert.state(beanClass != null, "Class returned from getBeanClass(Element) must not be null");
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
		if (parserContext.isNested()) {
			// Inner bean definition must receive same singleton status as containing bean.
			builder.setSingleton(parserContext.getContainingBeanDefinition().isSingleton());
		}
		doParse(element, builder);
		return builder.getBeanDefinition();
	}

	/**
	 * Determine the bean class corresponding to the supplied {@link Element}.
	 * @param element the <code>Element</code> that is being parsed
	 * @return the {@link Class} of the bean that is being defined via parsing the supplied <code>Element</code>
	 * (must <b>not</b> be <code>null</code>)
	 * @see #parseInternal(org.w3c.dom.Element, ParserContext)   
	 */
	protected abstract Class getBeanClass(Element element);

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * <p>The default implementation does nothing.
	 * @param element the XML element being parsed
	 * @param builder used to define the <code>BeanDefinition</code>
	 */
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
	}

}
