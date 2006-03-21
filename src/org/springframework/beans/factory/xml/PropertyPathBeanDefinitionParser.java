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
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
 * @since 2.0
 */
class PropertyPathBeanDefinitionParser implements BeanDefinitionParser {


	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String id = element.getAttribute("id");
		String path = element.getAttribute("path");

		Assert.hasText(path, "Attribute 'path' must not be null or zero length.");
		int dotIndex = path.indexOf(".");
		if (dotIndex == -1) {
			throw new IllegalArgumentException("Attribute 'path' must follow pattern 'beanName.propertyName'.");
		}

		String beanName = path.substring(0, dotIndex);
		String propertyPath = path.substring(dotIndex + 1);

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PropertyPathFactoryBean.class);
		builder.addPropertyValue("targetBeanName", beanName);
		builder.addPropertyValue("propertyPath", propertyPath);

		BeanDefinition definition = builder.getBeanDefinition();
		id = (StringUtils.hasText(id) ? id :
						BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) definition, parserContext.getRegistry(), false));
		parserContext.getRegistry().registerBeanDefinition(id, builder.getBeanDefinition());

		return definition;
	}
}
