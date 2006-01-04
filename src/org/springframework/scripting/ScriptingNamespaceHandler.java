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

package org.springframework.scripting;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
 * @since 2.0M2
 */
public class ScriptingNamespaceHandler extends NamespaceHandlerSupport {

	public ScriptingNamespaceHandler() {
		registerBeanDefinitionParser("groovy", new GroovyBeanDefinitionParser());
	}

	private static class GroovyBeanDefinitionParser implements BeanDefinitionParser {

		public void parse(Element element, BeanDefinitionRegistry registry) {
			throw new UnsupportedOperationException();
		}
	}
}
