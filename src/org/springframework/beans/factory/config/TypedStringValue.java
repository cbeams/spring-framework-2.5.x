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

package org.springframework.beans.factory.config;

import org.springframework.util.Assert;

/**
 * Holder for a typed String value. Can be added to bean definitions
 * to explicitly specify a target type for a String value, for example
 * for collection elements.
 *
 * <p>This holder will just store the String value and the target type.
 * The actual conversion will be performed by the bean factory.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see BeanDefinition#getPropertyValues
 * @see org.springframework.beans.MutablePropertyValues#addPropertyValue
 */
public class TypedStringValue {

	private String value;

	private Class targetType;

	/**
	 * Create a new TypedStrignValue for the given String
	 * value and target type.
	 * @param value the String value
	 * @param targetType the type to conver to
	 */
	public TypedStringValue(String value, Class targetType) {
		setValue(value);
		setTargetType(targetType);
	}

	/**
	 * Set the String value.
	 * Only necessary for manipulating a registered value,
	 * for example in BeanFactoryPostProcessors.
	 * @see PropertyPlaceholderConfigurer
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Return the String value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the type to convert to.
	 * Only necessary for manipulating a registered value,
	 * for example in BeanFactoryPostProcessors.
	 * @see PropertyPlaceholderConfigurer
	 */
	public void setTargetType(Class targetType) {
		Assert.notNull(targetType, "targetType is required");
		this.targetType = targetType;
	}

	/**
	 * Return the type to convert to.
	 */
	public Class getTargetType() {
		return targetType;
	}

}
