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

package org.springframework.web.servlet.config;

import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.tiles.TilesJstlView;
import org.springframework.web.servlet.view.tiles.TilesView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses <code>&lt;view-resolvers&gt;</code> elements and their
 * nested elements to create the view resolver configuration for
 * Spring MVC applications.
 *
 * @author Alef Arendsen
 * @see MvcNamespaceHandler
 * @since 2.0
 */
public class ViewResolversBeanDefinitionParser extends MvcBeanDefinitionParserSupport
				implements BeanDefinitionParser {

	//---------------------------------------------------------------------
	// Static section
	//---------------------------------------------------------------------

	private static final String INTERNAL_RESOURCE_VIEW_RESOLVER = "internal-resource-view-resolver";

	private static final String JSTL = "jstl";

	private static final String TILES = "tiles";

	private static final String TILES_JSTL = "tiles-jstl";

	private static final String BEAN_NAME_VIEW_RESOLVER = "bean-name-view-resolver";

	//---------------------------------------------------------------------
	// Instance section
	//---------------------------------------------------------------------

	public BeanDefinition parse(Element element, ParserContext parserContext) {

		Assert.notNull(element);
		Assert.notNull(parserContext);

		BeanDefinitionRegistry registry = parserContext.getRegistry();
		NodeList viewResolverChildren = element.getChildNodes();

		int viewResolverCount = 0;
		for (int i = 0; i < viewResolverChildren.getLength(); i++) {
			Node viewResolverNode = viewResolverChildren.item(i);

			if (viewResolverNode.getNodeType() == Node.ELEMENT_NODE) {
				Element viewResolverElement = (Element) viewResolverNode;

				RootBeanDefinition viewResolverDefinition = null;

				if (INTERNAL_RESOURCE_VIEW_RESOLVER.equals(viewResolverNode.getLocalName())) {
					viewResolverDefinition = parseInternalResourceViewResolver(viewResolverElement, registry);
				}
				else if (BEAN_NAME_VIEW_RESOLVER.equals(viewResolverNode.getLocalName())) {
					viewResolverDefinition = parseBeanNameViewResolver(viewResolverElement, registry);
				}

				if (Ordered.class.isAssignableFrom(viewResolverDefinition.getBeanClass())) {
					viewResolverDefinition.getPropertyValues().addPropertyValue("order", new Integer(viewResolverCount++));
				}
				else {
					// TODO possibly implement detection of non-ordered view resolvers not being last
				}

				registry.registerBeanDefinition(BeanDefinitionReaderUtils.generateBeanName(
								viewResolverDefinition, registry, false), viewResolverDefinition);
			}
		}

		return null;
	}

	private RootBeanDefinition parseBeanNameViewResolver(Element viewResolverElement, BeanDefinitionRegistry registry) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(BeanNameViewResolver.class);
		return beanDefinition;
	}

	private RootBeanDefinition parseInternalResourceViewResolver(Element el, BeanDefinitionRegistry registry) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(InternalResourceViewResolver.class);

		setPropertyIfAvailable(el, "prefix", "prefix", beanDefinition);
		setPropertyIfAvailable(el, "suffix", "suffix", beanDefinition);
		setPropertyIfAvailable(el, "enable-caching", "cache", beanDefinition);
		setPropertyIfAvailable(el, "content-type", "contentType", beanDefinition);
		setPropertyIfAvailable(el, "redirect-context-relative", "redirectContextRelative", beanDefinition);
		setPropertyIfAvailable(el, "redirect-http10-compatible", "redirectHttp10Compatible", beanDefinition);
		setPropertyIfAvailable(el, "request-context-attribute", "requestContextAttribute", beanDefinition);

		String type = el.getAttribute("type");
		if (JSTL.equals(type)) {
			beanDefinition.getPropertyValues().addPropertyValue("viewClass", JstlView.class);
		}
		else if (TILES.equals(type)) {
			beanDefinition.getPropertyValues().addPropertyValue("viewClass", TilesView.class);
		}
		else if (TILES_JSTL.equals(type)) {
			beanDefinition.getPropertyValues().addPropertyValue("viewClass", TilesJstlView.class);
		}
		else {
			beanDefinition.getPropertyValues().addPropertyValue("viewClass", InternalResourceView.class);
		}

		return beanDefinition;
	}

}
