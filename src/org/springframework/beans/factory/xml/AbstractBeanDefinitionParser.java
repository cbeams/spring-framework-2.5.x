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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Abstract {@link BeanDefinitionParser} implementation providing a number
 * of convenience methods and a
 * {@link AbstractBeanDefinitionParser#parseInternal(org.w3c.dom.Element, ParserContext) template method}
 * that subclasses must override to provide the actual parsing logic.
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @since 2.0
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * Constant for the id attribute.
	 */
	public static final String ID_ATTRIBUTE = "id";


	public final BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinition definition = parseInternal(element, parserContext);
		String id = resolveId(definition, parserContext, element);
		if (StringUtils.hasText(id)) {
			BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id);
			registerBeanDefinition(holder, parserContext.getRegistry(), parserContext.isNested());
			if (shouldFireEvents()) {
				BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
				postProcessComponentDefinition(componentDefinition);
				parserContext.getReaderContext().fireComponentRegistered(componentDefinition);
			}
		}
		else if (!parserContext.isNested()) {
			throw new IllegalArgumentException("Attribute '" + ID_ATTRIBUTE + "' is required for element '"
							+ element.getLocalName() + "' when used as a top-level tag.");
		}
		return definition;
	}


	/**
	 * Register the supplied {@link BeanDefinitionHolder bean} with the supplied
	 * {@link BeanDefinitionRegistry registry}.
	 * <p>Subclasses can override this method to control whether or not the supplied
	 * {@link BeanDefinitionHolder bean} is actually even registered, or to
	 * register even more beans.
	 * <p>The default implementation registers the supplied {@link BeanDefinitionHolder bean} with the supplied
	 * {@link BeanDefinitionRegistry registry} only if the <code>isNested</code>
	 * parameter is <code>false</code>, because one typically does not want
	 * inner beans to be registered as top level beans.
	 * @param bean the bean to be registered
	 * @param registry the registry that the bean is to be registered with 
	 * @param isNested <code>true</code> if the supplied {@link BeanDefinitionHolder bean} was created from a nested element  
	 * @see BeanDefinitionReaderUtils#registerBeanDefinition(org.springframework.beans.factory.config.BeanDefinitionHolder, org.springframework.beans.factory.support.BeanDefinitionRegistry) 
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder bean, BeanDefinitionRegistry registry, boolean isNested) {
		if (!isNested) {
			BeanDefinitionReaderUtils.registerBeanDefinition(bean, registry);
		}
	}

	/**
	 * Central template method to actually parse the supplied {@link Element}
	 * into one or more {@link BeanDefinition BeanDefinitions}.
	 * @param element			 the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * @param parserContext the object encapsulating the current state of the parse;
	 *                      provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return the primary {@link BeanDefinition} resulting from the parsing of the supplied {@link Element}
	 * @see #parse(org.w3c.dom.Element, ParserContext)
	 * @see #postProcessComponentDefinition(org.springframework.beans.factory.support.BeanComponentDefinition)
	 */
	protected abstract BeanDefinition parseInternal(Element element, ParserContext parserContext);

	/**
	 * Hook method called after the primary parsing of a
	 * {@link BeanComponentDefinition} but before the
	 * {@link BeanComponentDefinition} has been registered with a
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
	 * <p>Derived classes can override this emthod to supply any custom logic that
	 * is to be executed after all the parsing is finished.
	 * <p>The default implementation is a no-op.
	 * @param componentDefinition the {@link BeanComponentDefinition} that is to be processed
	 */
	protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
	}

	/**
	 * Convenience method to extract the id of a bean definition from the supplied
	 * {@link Element element}.
	 * @param element the element from which an id is to be extracted
	 * @return the extracted id; must return <code>null</code> if no id could be extracted
	 * @see #ID_ATTRIBUTE
	 */
	protected String extractId(Element element) {
		return element.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Controls whether this instance is to
	 * {@link org.springframework.beans.factory.support.ReaderContext#fireComponentRegistered(org.springframework.beans.factory.support.ComponentDefinition) fire an event}
	 * when a bean definition has been totally parsed?
	 * <p>Implementations must return <code>true</code> if they want an event
	 * will be fired when a bean definition has been totally parsed; returning
	 * <code>false</code> means that an event will not be fired.
	 * <p>This implementation returns <code>true</code> by default; that is, an event
	 * will be fired when a bean definition has been totally parsed.
	 * @return <code>true</code> if this instance is to
	 *         {@link org.springframework.beans.factory.support.ReaderContext#fireComponentRegistered(org.springframework.beans.factory.support.ComponentDefinition) fire an event}
	 *         when a bean definition has been totally parsed
	 */
	protected boolean shouldFireEvents() {
		return true;
	}

	/**
	 * Should an ID be autogenerated instead of read for the passed in {@link Element}? Subclasses
	 * can override this to enable ID autogeneration, which is disabled by default.
	 */
	protected boolean autogenerateId() {
		return false;
	}
	

	/**
	 * Resolves the ID for the supplied {@link BeanDefinition}. When using {@link #autogenerateId autogeneration}
	 * a name is generated automatically, otherwise the ID is extracted by delegating to {@link #extractId}.
	 */
	private String resolveId(BeanDefinition definition, ParserContext parserContext, Element element) {
		if (autogenerateId()) {
			return BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) definition, parserContext.getRegistry(), parserContext.isNested());
		}
		else {
			return extractId(element);
		}
	}

}
