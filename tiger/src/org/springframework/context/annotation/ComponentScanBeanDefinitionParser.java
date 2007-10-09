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

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

/**
 * Parser for the &lt;context:component-scan/&gt; element.
 * 
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.5
 */
public class ComponentScanBeanDefinitionParser implements BeanDefinitionParser {

	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";

	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resource-pattern";

	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";

	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";
	
	private static final String NAME_GENERATOR_ATTRIBUTE = "name-generator";
	
	private static final String SCOPE_RESOLVER_ATTRIBUTE = "scope-resolver";
	
	private static final String SCOPED_PROXY_ATTRIBUTE = "scoped-proxy";

	private static final String EXCLUDE_FILTER_ELEMENT = "exclude-filter";

	private static final String INCLUDE_FILTER_ELEMENT = "include-filter";

	private static final String FILTER_TYPE_ATTRIBUTE = "type";

	private static final String FILTER_EXPRESSION_ATTRIBUTE = "expression";


	public BeanDefinition parse(Element element, ParserContext parserContext) {
		ResourceLoader resourceLoader = parserContext.getReaderContext().getResourceLoader();
		Object source = parserContext.extractSource(element);

		boolean useDefaultFilters = true;
		if (element.hasAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)) {
			useDefaultFilters = Boolean.valueOf(element.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE));
		}
		boolean annotationConfig = true;
		if (element.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) {
			annotationConfig = Boolean.valueOf(element.getAttribute(ANNOTATION_CONFIG_ATTRIBUTE));
		}

		// Delegate bean definition registration to scanner class.
		ClassPathBeanDefinitionScanner scanner =
				new ClassPathBeanDefinitionScanner(parserContext.getRegistry(), useDefaultFilters);
		scanner.setResourceLoader(resourceLoader);
		scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
		scanner.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());

		String basePackage = element.getAttribute(BASE_PACKAGE_ATTRIBUTE);
		String[] basePackages = StringUtils.commaDelimitedListToStringArray(basePackage);

		if (element.hasAttribute(RESOURCE_PATTERN_ATTRIBUTE)) {
			scanner.setResourcePattern(element.getAttribute(RESOURCE_PATTERN_ATTRIBUTE));
		}

		// Parse exclude and include filter elements.
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = node.getLocalName();
				if (INCLUDE_FILTER_ELEMENT.equals(localName)) {
					TypeFilter typeFilter = createTypeFilter((Element) node, resourceLoader.getClassLoader());
					scanner.addIncludeFilter(typeFilter);
				}
				else if (EXCLUDE_FILTER_ELEMENT.equals(localName)) {
					TypeFilter typeFilter = createTypeFilter((Element) node, resourceLoader.getClassLoader());
					scanner.addExcludeFilter(typeFilter);
				}
			}
		}

		// Register BeanNameGenerator if class name provided.
		if (element.hasAttribute(NAME_GENERATOR_ATTRIBUTE)) {
			BeanNameGenerator beanNameGenerator = (BeanNameGenerator) instantiateUserDefinedStrategy(
					element.getAttribute(NAME_GENERATOR_ATTRIBUTE), BeanNameGenerator.class, resourceLoader.getClassLoader());
			scanner.setBeanNameGenerator(beanNameGenerator);
		}

		// Register ScopeMetadataResolver if class name provided.
		if (element.hasAttribute(SCOPE_RESOLVER_ATTRIBUTE)) {
			if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
				parserContext.getReaderContext().error(
						"Cannot define both 'scope-resolver' and 'scoped-proxy' on <component-scan> tag.", element);
			}
			ScopeMetadataResolver scopeMetadataResolver = (ScopeMetadataResolver) instantiateUserDefinedStrategy(
					element.getAttribute(SCOPE_RESOLVER_ATTRIBUTE), ScopeMetadataResolver.class, resourceLoader.getClassLoader());
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		// Set scoped-proxy mode on scanner if provided.
		if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
			String mode = element.getAttribute(SCOPED_PROXY_ATTRIBUTE);
			if ("targetClass".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);
			}
			else if ("interfaces".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.INTERFACES);
			}
			else if ("no".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.NO);
			}
			else {
				throw new IllegalArgumentException("scoped-proxy only supports 'no', 'interfaces' and 'targetClass'");
			}
		}

		// Actually scan for bean definitions and register them.
		Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan(basePackages);

		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);
		for (Iterator it = beanDefinitions.iterator(); it.hasNext();) {
			BeanDefinitionHolder beanDefHolder = (BeanDefinitionHolder) it.next();
			AbstractBeanDefinition beanDef = (AbstractBeanDefinition) beanDefHolder.getBeanDefinition();
			beanDef.setSource(parserContext.extractSource(beanDef.getSource()));
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
		}
		parserContext.getReaderContext().fireComponentRegistered(compositeDef);

		// Register annotation config processors, if necessary.
		if (annotationConfig) {
			Set<BeanDefinitionHolder> processorDefinitions =
					AnnotationConfigUtils.registerAnnotationConfigProcessors(parserContext.getRegistry(), source);
			for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
				parserContext.registerComponent(new BeanComponentDefinition(processorDefinition));
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private Object instantiateUserDefinedStrategy(String className, Class strategyType, ClassLoader classLoader) {
		Object result = null;
		try {
			Class clazz = Class.forName(className, true, classLoader);
			result = clazz.newInstance();
		}
		catch (ClassNotFoundException ex) {
			throw new BeanCreationException("Class [" + className + "] for strategy [" +
					strategyType.getName() + "] not found", ex);
		}
		catch (Exception ex) {
			throw new BeanCreationException("Unable to instantiate class [" + className + "] for strategy [" +
					strategyType.getName() + "]. A zero-argument constructor is required", ex);
		}
		
		if (!strategyType.isAssignableFrom(result.getClass())) {
			throw new BeanCreationException("Provided class name must be an implementation of " + strategyType);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private TypeFilter createTypeFilter(Element element, ClassLoader classLoader) {
		String filterType = element.getAttribute(FILTER_TYPE_ATTRIBUTE);
		String expression = element.getAttribute(FILTER_EXPRESSION_ATTRIBUTE);
		try {
			if ("annotation".equals(filterType)) {
				return new AnnotationTypeFilter((Class<Annotation>) classLoader.loadClass(expression));
			}
			else if ("assignable".equals(filterType)) {
				return new AssignableTypeFilter(classLoader.loadClass(expression));
			}
			else if ("regex".equals(filterType)) {
				return new RegexPatternTypeFilter(Pattern.compile(expression));
			}
			else if ("aspectj".equals(filterType)) {
				return new AspectJTypeFilter(expression, classLoader);
			}
			else {
				throw new IllegalArgumentException("Unsupported filter type: " + filterType);
			}
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Type filter class not found: " + expression, ex);
		}
	}

}
