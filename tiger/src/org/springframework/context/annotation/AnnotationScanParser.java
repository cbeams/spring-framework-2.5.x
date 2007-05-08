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
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.typefilter.AnnotationTypeFilter;
import org.springframework.core.typefilter.AssignableTypeFilter;
import org.springframework.core.typefilter.FilterType;
import org.springframework.core.typefilter.RegexPatternTypeFilter;
import org.springframework.core.typefilter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for the &lt;context:annotation-scan/&gt; element.
 * 
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @since 2.1
 */
public class AnnotationScanParser extends AnnotationConfigParser {

	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	
	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";
	
	private static final String EXCLUDE_FILTER_ELEMENT = "exclude-filter";
	
	private static final String INCLUDE_FILTER_ELEMENT = "include-filter";
	
	private static final String FILTER_TYPE_ATTRIBUTE = "type";

	private static final String FILTER_EXPRESSION_ATTRIBUTE = "expression";
	
	
	private BeanNamingStrategy beanNamingStrategy = new DefaultBeanNamingStrategy();
	
	
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// create the component provider
		boolean useDefaultFilters = Boolean.valueOf(element.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE));
		String basePackage = element.getAttribute(BASE_PACKAGE_ATTRIBUTE);
		CandidateComponentProvider candidateComponentProvider = 
				new ClassPathScanningCandidateComponentProvider(useDefaultFilters);
		if (basePackage != null) {
			candidateComponentProvider.setBasePackage(basePackage);
		}
		
		// parse exclude and include filter elements
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = node.getLocalName();
				if (EXCLUDE_FILTER_ELEMENT.equals(localName)) {
					TypeFilter typeFilter = createTypeFilter((Element) node);
					candidateComponentProvider.addExcludeFilter(typeFilter);
				}
				else if (INCLUDE_FILTER_ELEMENT.equals(localName)) {
					TypeFilter typeFilter = createTypeFilter((Element) node);
					candidateComponentProvider.addIncludeFilter(typeFilter);	
				}
			}
		}
		
		// find candidate components and retrieve their metadata
		Set<Class> candidates = candidateComponentProvider.findCandidateComponents();		
		ComponentMetadataResolver metadataResolver = new ComponentMetadataResolver();
		
		// register base bean definitions
		for (Class beanClass : candidates) {
			ComponentMetadata metadata = metadataResolver.resolveMetadata(beanClass);
			BeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
			String beanName = metadata.getProvidedBeanName();
			if (!StringUtils.hasLength(beanName)) {
				beanName = beanNamingStrategy.generateName(beanDefinition);
			}
			parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
		}

		registerPostProcessors(parserContext);
		return null;
	}

	@SuppressWarnings("unchecked")
	private TypeFilter createTypeFilter(Element element) {
		String filterTypeName = element.getAttribute(FILTER_TYPE_ATTRIBUTE);
		String expression = element.getAttribute(FILTER_EXPRESSION_ATTRIBUTE);
		FilterType type = FilterType.valueOf(filterTypeName);
		try {
			switch (type) {
				case ANNOTATION :
					return new AnnotationTypeFilter((Class<Annotation>) ClassUtils.getDefaultClassLoader().loadClass(expression), false);
				case INHERITABLE_ANNOTATION :
					return new AnnotationTypeFilter((Class<Annotation>) ClassUtils.getDefaultClassLoader().loadClass(expression), true);
				case ASSIGNABLE_TYPE :
					return new AssignableTypeFilter(ClassUtils.forName(expression));
				case REGEX_PATTERN :
					return new RegexPatternTypeFilter(Pattern.compile(expression));
				/* TODO: add this after reintroducing the AspectJPatternTypeFilter
				case ASPECTJ_PATTERN :
					return new AspectJPatternTypeFilter(expression);
				*/
				default : 
					throw new IllegalStateException("unsupported type-filter: " + filterTypeName);
			}
		}
		catch (ClassNotFoundException e) {
			throw new FatalBeanException("no class for type-filter: " + type, e);
		}
	}

}
