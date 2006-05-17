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

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public final class ParserContext {

	private final XmlReaderContext readerContext;

	private final BeanDefinitionParserDelegate delegate;

	private final boolean nested;


	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate, boolean nested) {
		this.readerContext = readerContext;
		this.delegate = delegate;
		this.nested = nested;
	}


	public XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	public BeanDefinitionRegistry getRegistry() {
		return getReaderContext().getReader().getBeanFactory();
	}

	public BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}

	/**
	 * Indicates whether or not the custom tag being parsed is nested under a
	 * <code>&lt;property&gt;</code> or other such container tag.
	 * @return <code>false</code> when parsing a tag nested under <code>&lt;beans&gt;</code>
	 * or <code>&lt;bean&gt;</code> tag otherwise returns <code>true</code>.
	 */
	public boolean isNested() {
		return this.nested;
	}

}
