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
import org.springframework.util.enums.LabeledEnum;

/**
 * Converter that converts textual representations of <coded>CodedEnum</code>
 * instances to a specific instance of <code>LabeledEnum</code>
 * @author Keith Donald
 */
public class TextToLabeledEnumConverter extends AbstractFormattingConverter {

	private Class[] labeledEnumClasses;

	public TextToLabeledEnumConverter(FormatterLocator formatterLocator) {
		super(formatterLocator);
	}

	public TextToLabeledEnumConverter(Class labeledEnumClasses, FormatterLocator formatterLocator) {
		this(new Class[] { labeledEnumClasses }, formatterLocator);
	}

	public TextToLabeledEnumConverter(Class[] labeledEnumClasses, FormatterLocator formatterLocator) {
		super(formatterLocator);
		this.labeledEnumClasses = labeledEnumClasses;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		if (labeledEnumClasses == null) {
			return new Class[] { LabeledEnum.class };
		} else {
			return labeledEnumClasses;
		}
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		return getFormatterLocator().getLabeledEnumFormatter(targetClass).parseValue((String)source);
	}
}