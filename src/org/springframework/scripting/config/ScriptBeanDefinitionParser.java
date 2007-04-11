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

package org.springframework.scripting.config;

import java.util.List;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * BeanDefinitionParser implementation for the '<code>&lt;lang:groovy/&gt;</code>',
 * '<code>&lt;lang:jruby/&gt;</code>' and '<code>&lt;lang:bsh/&gt;</code>' tags.
 * Allows for objects written using dynamic languages to be easily exposed with
 * the {@link org.springframework.beans.factory.BeanFactory}.
 *
 * <p>The script for each object can be specified either as a reference to the Resource
 * containing it (using the '<code>script-source</code>' attribute) or inline in the XML configuration
 * itself (using the '<code>inline-script</code>' attribute.
 *
 * <p>By default, dynamic objects created with these tags are <strong>not</strong> refreshable.
 * To enable refreshing, specify the refresh check delay for each object (in milliseconds) using the
 * '<code>refresh-check-delay</code>' attribute.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
class ScriptBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * The unique name under which the internally managed {@link ScriptFactoryPostProcessor} is
	 * registered in the {@link BeanDefinitionRegistry}.
	 */
	private static final String SCRIPT_FACTORY_POST_PROCESSOR_BEAN_NAME = ".scriptFactoryPostProcessor";

	private static final String SCRIPT_SOURCE_ATTRIBUTE = "script-source";

	private static final String INLINE_SCRIPT_ELEMENT = "inline-script";

	private static final String SCOPE_ATTRIBUTE = "scope";

	private static final String INIT_METHOD_ATTRIBUTE = "init-method";

	private static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

	private static final String SCRIPT_INTERFACES_ATTRIBUTE = "script-interfaces";

	private static final String REFRESH_CHECK_DELAY_ATTRIBUTE = "refresh-check-delay";
	
	private static final String CUSTOMIZER_REF_ATTRIBUTE = "customizer-ref";


	/**
	 * The {@link org.springframework.scripting.ScriptFactory} class that this
	 * parser instance will create bean definitions for.
	 */
	private final String scriptFactoryClassName;


	/**
	 * Create a new instance of this parser, creating bean definitions for the
	 * supplied {@link org.springframework.scripting.ScriptFactory} class.
	 * @param scriptFactoryClassName the ScriptFactory class to operate on
	 */
	public ScriptBeanDefinitionParser(String scriptFactoryClassName) {
		this.scriptFactoryClassName = scriptFactoryClassName;
	}


	/**
	 * Parses the dynamic object element and returns the resulting bean definition.
	 * Registers a {@link ScriptFactoryPostProcessor} if needed.
	 */
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		// Resolve the script source.
		String value = resolveScriptSource(element, parserContext.getReaderContext());
		if (value == null) {
			return null;
		}

		// Set up infrastructure.
		registerScriptFactoryPostProcessorIfNecessary(parserContext.getRegistry());

		// Create script factory bean definition.
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClassName(this.scriptFactoryClassName);
		beanDefinition.setSource(parserContext.extractSource(element));

		// Determine bean scope.
		String scope = element.getAttribute(SCOPE_ATTRIBUTE);
		if (StringUtils.hasLength(scope)) {
			beanDefinition.setScope(scope);
		}

		// Determine init method and destroy method.
		String initMethod = element.getAttribute(INIT_METHOD_ATTRIBUTE);
		if (StringUtils.hasLength(initMethod)) {
			beanDefinition.setInitMethodName(initMethod);
		}
		String destroyMethod = element.getAttribute(DESTROY_METHOD_ATTRIBUTE);
		if (StringUtils.hasLength(destroyMethod)) {
			beanDefinition.setDestroyMethodName(destroyMethod);
		}

		// Attach any refresh metadata.
		String refreshCheckDelay = element.getAttribute(REFRESH_CHECK_DELAY_ATTRIBUTE);
		if (StringUtils.hasText(refreshCheckDelay)) {
			beanDefinition.setAttribute(
					ScriptFactoryPostProcessor.REFRESH_CHECK_DELAY_ATTRIBUTE, new Long(refreshCheckDelay));
		}

		// Add constructor arguments.
		ConstructorArgumentValues cav = beanDefinition.getConstructorArgumentValues();
		int constructorArgNum = 0;
		cav.addIndexedArgumentValue(constructorArgNum++, value);
		if (element.hasAttribute(SCRIPT_INTERFACES_ATTRIBUTE)) {
			cav.addIndexedArgumentValue(constructorArgNum++, element.getAttribute(SCRIPT_INTERFACES_ATTRIBUTE));
		}
		
		// This is used for Groovy. It's a bean reference to a customizer bean.
		if (element.hasAttribute(CUSTOMIZER_REF_ATTRIBUTE)) {
			String customizerBeanName = element.getAttribute(CUSTOMIZER_REF_ATTRIBUTE);
			cav.addIndexedArgumentValue(constructorArgNum++, new RuntimeBeanReference(customizerBeanName));
		}

		// Add any property definitions that need adding.
		parserContext.getDelegate().parsePropertyElements(element, beanDefinition);

		return beanDefinition;
	}

	/**
	 * Resolves the script source from either the '<code>script-source</code>' attribute or
	 * the '<code>inline-script</code>' element. Logs and {@link XmlReaderContext#error} and
	 * returns <code>null</code> if neither or both of these values are specified.
	 */
	private String resolveScriptSource(Element element, XmlReaderContext readerContext) {
		boolean hasScriptSource = element.hasAttribute(SCRIPT_SOURCE_ATTRIBUTE);
		List elements = DomUtils.getChildElementsByTagName(element, INLINE_SCRIPT_ELEMENT);
		if (hasScriptSource && !elements.isEmpty()) {
			readerContext.error("Only one of 'script-source' and 'inline-script' should be specified.", element);
			return null;
		}
		else if (hasScriptSource) {
			return element.getAttribute(SCRIPT_SOURCE_ATTRIBUTE);
		}
		else if (!elements.isEmpty()) {
			Element inlineElement = (Element) elements.get(0);
			return "inline:" + DomUtils.getTextValue(inlineElement);
		}
		else {
			readerContext.error("Must specify either 'script-source' or 'inline-script'.", element);
			return null;
		}
	}

	/**
	 * Registers a {@link ScriptFactoryPostProcessor} bean definition in the supplied
	 * {@link BeanDefinitionRegistry} if the {@link ScriptFactoryPostProcessor} hasn't
	 * already been registered.
	 */
	private static void registerScriptFactoryPostProcessorIfNecessary(BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(SCRIPT_FACTORY_POST_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(ScriptFactoryPostProcessor.class);
			registry.registerBeanDefinition(SCRIPT_FACTORY_POST_PROCESSOR_BEAN_NAME, beanDefinition);
		}
	}

}
