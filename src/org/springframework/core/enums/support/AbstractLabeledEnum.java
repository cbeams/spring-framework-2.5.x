/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.core.enums.support;

import java.io.Serializable;

import org.springframework.core.enums.LabeledEnum;
import org.springframework.core.enums.LabeledEnumResolver;
import org.springframework.util.Assert;

/**
 * Abstract base superclass for LabeledEnum implementations.
 * @author Keith Donald
 */
public abstract class AbstractLabeledEnum implements LabeledEnum, Serializable {

	private String label;

	protected AbstractLabeledEnum() {
	}

	protected AbstractLabeledEnum(String label) {
		setLabel(label);
	}

	public abstract Comparable getCode();

	protected void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public Class getType() {
		return getClass();
	}

	public String getKey() {
		return getType() + "." + getCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof AbstractLabeledEnum)) {
			return false;
		}
		AbstractLabeledEnum e = (AbstractLabeledEnum)o;
		return this.getCode().equals(e.getCode()) && this.getType().equals(e.getType());
	}

	public int compareTo(Object o) {
		AbstractLabeledEnum e = (AbstractLabeledEnum)o;
		Assert.isTrue(getType().equals(e.getType()), "You may only compare enumerations of the same type.");
		return getCode().compareTo(e.getCode());
	}

	public int hashCode() {
		return getType().hashCode() + getCode().hashCode();
	}

	protected LabeledEnumResolver getEnumResolver() {
		return getStaticEnumResolver();
	}

	protected final StaticLabeledEnumResolver getStaticEnumResolver() {
		return StaticLabeledEnumResolver.instance();
	}

	/*
	 * Handle the deserialization of the class to ensure that multiple
	 * copies are not wastefully created if this enum is static.</p>
	 * @return the resolved object
	 */
	protected Object readResolve() {
		try {
			return getEnumResolver().getLabeledEnum(getClass(), getCode());
		}
		catch (Exception e) {
			return this;
		}
	}

	public String toString() {
		String enumStr = (label != null ? (getLabel() + " (" + getCode() + ")") : String.valueOf(getCode()));
		return "[" + getType() + "." + enumStr + "]";
	}
}