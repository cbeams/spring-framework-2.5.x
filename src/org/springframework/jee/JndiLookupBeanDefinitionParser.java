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

package org.springframework.jee;

import org.springframework.jndi.JndiObjectFactoryBean;
import org.w3c.dom.Element;

/**
 * Simple {@link org.springframework.beans.factory.xml.BeanDefinitionParser} implementation that
 * translates '<code>jndi-lookup</code>' tag into {@link JndiObjectFactoryBean} definitions.
 *
 * @author Rob Harrop
 * @see JndiObjectFactoryBean
 * @since 2.0
 */
class JndiLookupBeanDefinitionParser extends AbstractJndiLocatedBeanDefinitionParser {

	protected Class getBeanClass(Element element) {
		return JndiObjectFactoryBean.class;
	}
}
