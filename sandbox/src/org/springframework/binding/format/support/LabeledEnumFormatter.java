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
package org.springframework.binding.format.support;

import org.springframework.core.enums.LabeledEnum;
import org.springframework.core.enums.LabeledEnumResolver;
import org.springframework.core.enums.support.StaticLabeledEnumResolver;
import org.springframework.util.Assert;

/**
 * Converts from string to a <cod>LabeledEnum</code> instance and back.
 * @author Keith Donald
 */
public class LabeledEnumFormatter extends AbstractFormatter {

	private LabeledEnumResolver resolver = StaticLabeledEnumResolver.instance();

	public LabeledEnumFormatter(Class labeledEnumClass) {
		super(labeledEnumClass);
	}

	public LabeledEnumFormatter(Class labeledEnumClass, boolean allowEmpty) {
		super(labeledEnumClass, allowEmpty);
	}

	public LabeledEnumFormatter(Class labeledEnumClass, boolean allowEmpty, LabeledEnumResolver resolver) {
		super(labeledEnumClass, allowEmpty);
		setResolver(resolver);
	}

	public void setResolver(LabeledEnumResolver resolver) {
		Assert.notNull(resolver, "The labeled enum resolver is required");
		this.resolver = resolver;
	}

	protected String doFormatValue(Object value) {
		LabeledEnum labeledEnum = (LabeledEnum)value;
		return (String)labeledEnum.getLabel();
	}

	protected Object doParseValue(String label) throws IllegalArgumentException {
		LabeledEnum labeledEnum = this.resolver.getLabeledEnum(getValueClass(), label);
		if (!isAllowEmpty()) {
			Assert.notNull(labeledEnum, "The label '" + label + "' did not map to a valid enum instance for type "
					+ getValueClass());
			Assert.isInstanceOf(getValueClass(), labeledEnum);
		}
		return labeledEnum;
	}
}