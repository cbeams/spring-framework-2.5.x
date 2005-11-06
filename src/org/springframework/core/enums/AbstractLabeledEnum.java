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

package org.springframework.core.enums;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Abstract base superclass for LabeledEnum implementations.
 *
 * @author Keith Donald
 * @since 1.2.2
 */
public abstract class AbstractLabeledEnum implements LabeledEnum {

	private final String label;


	/**
	 * Create a new AbstractLabeledEnum instance with the given label.
	 */
	protected AbstractLabeledEnum(String label) {
		this.label = label;
	}

	public Class getType() {
		return getClass();
	}

	/**
	 * This abstract method declaration shadows the method in the LabeledEnum interface.
	 * This is necessary to properly work on Sun's JDK 1.3 classic VM in all cases.
	 */
	public abstract Comparable getCode();

	public String getLabel() {
		return label;
	}


	public int compareTo(Object obj) {
		Assert.isTrue(obj instanceof AbstractLabeledEnum, "You may only compare LabeledEnums");
		LabeledEnum other = (LabeledEnum) obj;
		Assert.isTrue(this.getType().equals(other.getType()),
				"You may only compare LabeledEnums of the same type");
		return this.getCode().compareTo(other.getCode());
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LabeledEnum)) {
			return false;
		}
		LabeledEnum other = (LabeledEnum) obj;
		return this.getCode().equals(other.getCode()) && this.getType().equals(other.getType());
	}

	public int hashCode() {
		return getType().hashCode() * 29 * getCode().hashCode();
	}

	public String toString() {
		String enumStr = (getLabel() != null ? getLabel() + " (" + getCode() + ")" : String.valueOf(getCode()));
		return "[" + getType() + "." + enumStr + "]";
	}

}
