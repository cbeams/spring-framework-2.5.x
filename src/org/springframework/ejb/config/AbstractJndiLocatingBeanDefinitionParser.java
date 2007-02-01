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

package org.springframework.ejb.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.util.xml.DomUtils;

/**
 * Abstract base class for BeanDefinitionParsers which build
 * JNDI-locating beans, supporting an optional "jndiEnvironment"
 * bean property, populated from an "environment" XML sub-element.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
abstract class AbstractJndiLocatingBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	public static final String ENVIRONMENT = "environment";

	public static final String JNDI_ENVIRONMENT = "jndiEnvironment";

	protected void postProcess(BeanDefinitionBuilder definitionBuilder, Element element) {
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (ENVIRONMENT.equals(node.getLocalName())) {
				definitionBuilder.addPropertyValue(JNDI_ENVIRONMENT, DomUtils.getTextValue((Element) node));
			}
		}
	}

}
