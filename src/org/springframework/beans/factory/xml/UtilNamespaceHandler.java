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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link NamespaceHandler} for the <code>util</code> namespace.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
		registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
		registerBeanDefinitionParser("list", new ListBeanDefinitionParser());
		registerBeanDefinitionParser("set", new SetBeanDefinitionParser());
		registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
	}


	private static class ConstantBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		private static final String STATIC_FIELD_ATTRIBUTE = "static-field";

		protected Class getBeanClass(Element element) {
			return FieldRetrievingFactoryBean.class;
		}

		protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute(STATIC_FIELD_ATTRIBUTE);
			}
			return id;
		}
	}


	private static class PropertyPathBeanDefinitionParser implements BeanDefinitionParser {

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			String id = element.getAttribute("id");
			String path = element.getAttribute("path");

			Assert.hasText(path, "Attribute 'path' must not be null or zero length");
			int dotIndex = path.indexOf(".");
			if (dotIndex == -1) {
				throw new IllegalArgumentException("Attribute 'path' must follow pattern 'beanName.propertyName'");
			}

			String beanName = path.substring(0, dotIndex);
			String propertyPath = path.substring(dotIndex + 1);

			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PropertyPathFactoryBean.class);
			builder.setSource(parserContext.extractSource(element));
			if (parserContext.isNested()) {
				// Inner bean definition should receive same singleton status as containing bean.
				builder.setSingleton(parserContext.getContainingBeanDefinition().isSingleton());
			}
			builder.addPropertyValue("targetBeanName", beanName);
			builder.addPropertyValue("propertyPath", propertyPath);

			AbstractBeanDefinition definition = builder.getBeanDefinition();
			id = (StringUtils.hasText(id) ? id :
					BeanDefinitionReaderUtils.generateBeanName(
							definition, parserContext.getRegistry(), parserContext.isNested()));
			parserContext.getRegistry().registerBeanDefinition(id, definition);

			return definition;
		}
	}


	private static class ListBeanDefinitionParser implements BeanDefinitionParser {

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			String id = element.getAttribute("id");
			String listClass = element.getAttribute("list-class");

			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ListFactoryBean.class);
			AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
			List parsedList = parserContext.getDelegate().parseListElement(element, beanDefinition);
			builder.setSource(parserContext.extractSource(element));
			builder.addPropertyValue("sourceList", parsedList);
			if (StringUtils.hasText(listClass)) {
				builder.addPropertyValue("targetListClass", listClass);
			}
			parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
			// cannot be used in a 'inner-bean' setting (use plain <list>)
			return null;
		}
	}


	private static class SetBeanDefinitionParser implements BeanDefinitionParser {

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			String id = element.getAttribute("id");
			String setClass = element.getAttribute("set-class");

			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SetFactoryBean.class);
			AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
			Set parsedSet = parserContext.getDelegate().parseSetElement(element, beanDefinition);
			builder.setSource(parserContext.extractSource(element));
			builder.addPropertyValue("sourceSet", parsedSet);
			if (StringUtils.hasText(setClass)) {
				builder.addPropertyValue("targetSetClass", setClass);
			}
			parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
			// cannot be used in a 'inner-bean' setting (use plain <set>)
			return null;
		}
	}


	private static class MapBeanDefinitionParser implements BeanDefinitionParser {

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			String id = element.getAttribute("id");
			String mapClass = element.getAttribute("map-class");

			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(MapFactoryBean.class);
			AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
			Map parsedMap = parserContext.getDelegate().parseMapElement(element, beanDefinition);
			builder.setSource(parserContext.extractSource(element));
			builder.addPropertyValue("sourceMap", parsedMap);
			if (StringUtils.hasText(mapClass)) {
				builder.addPropertyValue("targetMapClass", mapClass);
			}
			parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
			// cannot be used in a 'inner-bean' setting (use plain <map>)
			return null;
		}
	}


	private static class PropertiesBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return PropertiesFactoryBean.class;
		}
	}

}
