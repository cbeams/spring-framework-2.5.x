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

import org.springframework.util.enums.LabeledEnumResolver;
import org.springframework.util.enums.support.StaticLabeledEnumResolver;

/**
 * Base class for a converter that converts textual representations of
 * <coded>CodedEnum</code> instances to a specific instance of <code>
 * LabeledEnum</code>
 * @author Keith Donal
 */
public abstract class TextToLabeledEnumConverter extends AbstractFormattingConverter {

	private LabeledEnumResolver enumResolver = StaticLabeledEnumResolver.instance();

	public void setEnumResolver(LabeledEnumResolver enumResolver) {
		this.enumResolver = enumResolver;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public final Class[] getTargetClasses() {
		return getLabeledEnumClasses();
	}

	/**
	 * Subclasses should override to return the labeled enum classes that should
	 * be converted for your application.
	 * @return The classes, all implementations of <code>LabeledEnum</code>
	 */
	protected abstract Class[] getLabeledEnumClasses();

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		return getFormatterLocator().getLabeledEnumFormatter(targetClass).parseValue((String)source);
	}
}