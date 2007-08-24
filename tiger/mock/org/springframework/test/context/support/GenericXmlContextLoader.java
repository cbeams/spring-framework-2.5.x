/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context.support;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

/**
 * <p>
 * Concrete implementation of {@link AbstractGenericContextLoader} which reads
 * bean definitions from XML resources.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 */
public class GenericXmlContextLoader extends AbstractGenericContextLoader {

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Creates a new {@link XmlBeanDefinitionReader}.
	 * </p>
	 *
	 * @see org.springframework.test.context.support.AbstractGenericContextLoader#createBeanDefinitionReader(org.springframework.context.support.GenericApplicationContext)
	 * @see XmlBeanDefinitionReader
	 * @return a new XmlBeanDefinitionReader.
	 */
	@Override
	protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {

		return new XmlBeanDefinitionReader(context);
	}

	// ------------------------------------------------------------------------|

}
