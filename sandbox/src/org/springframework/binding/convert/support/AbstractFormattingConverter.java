/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.convert.support;

import org.springframework.binding.format.FormatterLocator;

/**
 * A converter that delegates to a formatter to perform the conversion.
 * Formatters are typically not thread safe, so we use a FormatterLocator that
 * is expected to provide us with synchronized instances as neccessary.
 * @author Keith Donald
 */
public abstract class AbstractFormattingConverter extends AbstractConverter {
	private FormatterLocator formatterLocator;

	protected AbstractFormattingConverter(FormatterLocator formatterLocator) {
		setFormatterLocator(formatterLocator);
	}

	protected FormatterLocator getFormatterLocator() {
		return this.formatterLocator;
	}

	public void setFormatterLocator(FormatterLocator formatterSource) {
		this.formatterLocator = formatterSource;
	}
}