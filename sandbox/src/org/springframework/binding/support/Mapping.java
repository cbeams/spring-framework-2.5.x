/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.binding.support;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeSetter;
import org.springframework.binding.TypeConverter;
import org.springframework.util.Assert;

/**
 * A single mapping definition, encapulating the information neccessary to map a
 * single attribute from a source attributes map to a target map.
 * @author Keith Donald
 */
public class Mapping implements Serializable {
	protected static final Log logger = LogFactory.getLog(Mapping.class);

	private String sourceAttributeName;

	private String targetAttributeName;

	private TypeConverter valueTypeConverter;

	/**
	 * Creates a mapping definition that will map the specified attribute name
	 * from a source attribute map to a target map with the same name and data
	 * type.
	 * @param sourceAttributeName The source attribute name
	 */
	public Mapping(String sourceAttributeName) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = sourceAttributeName;
	}

	/**
	 * Creates a mapping definition that will map the specified attribute name
	 * from a source attribute map to a target map with the same name. The type
	 * converter will be applied to the target value during the conversion.
	 * @param sourceAttributeName The source attribute name
	 * @param valueTypeConverter the type converter
	 */
	public Mapping(String sourceAttributeName, TypeConverter valueTypeConverter) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = sourceAttributeName;
		this.valueTypeConverter = valueTypeConverter;
	}

	/**
	 * Creates a mapping definition that will map the specified attribute name
	 * from a source attribute map to a target map with the specified target
	 * name.
	 * @param sourceAttributeName The source attribute name
	 * @param targetAttributeName The target attribute name
	 */
	public Mapping(String sourceAttributeName, String targetAttributeName) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = targetAttributeName;
	}

	/**
	 * Creates a mapping definition that will map the specified attribute name
	 * from a source attribute map to a target map with the specified target
	 * name. The type converter will be applied to the target value during the
	 * conversion.
	 * @param sourceAttributeName The source attribute name
	 * @param targetAttributeName The target attribute name
	 * @param valueTypeConverter the type converter
	 */
	public Mapping(String sourceAttributeName, String targetAttributeName, TypeConverter valueTypeConverter) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = targetAttributeName;
		this.valueTypeConverter = valueTypeConverter;
	}

	private void setSourceAttributeName(String sourceAttributeName) {
		Assert.notNull(sourceAttributeName, "The source attribute name is required");
		this.sourceAttributeName = sourceAttributeName;
	}

	/**
	 * Map the <code>sourceAttributeName</code> in the source map to the
	 * <code>targetAttributeName</code> target map, performing type conversion
	 * if neccessary.
	 * @param source The source map accessor
	 * @param target The target map accessor
	 * @param mapMissingAttributesToNull map attributes that aren't present to a
	 *        null value?
	 */
	public void map(AttributeAccessor source, AttributeSetter target, boolean mapMissingAttributesToNull) {
		Object value;
		BeanWrapper beanAccessor = null;
		int propertyDelimiterIndex = sourceAttributeName.indexOf('.');
		if (propertyDelimiterIndex != -1) {
			// sourceAttributeName is in the form "beanName.propertyPath"
			String beanName = sourceAttributeName.substring(0, propertyDelimiterIndex);
			String propertyPath = sourceAttributeName.substring(propertyDelimiterIndex + 1);
			beanAccessor = createBeanWrapper(source.getAttribute(beanName));
			value = beanAccessor.getPropertyValue(propertyPath);
		}
		else {
			value = source.getAttribute(sourceAttributeName);
		}
		// convert value to a expected target type if neccessary
		if (valueTypeConverter != null) {
			value = valueTypeConverter.convert(value);
		}
		// set target value
		propertyDelimiterIndex = targetAttributeName.indexOf('.');
		if (propertyDelimiterIndex != -1) {
			// targetName is of the form "beanName.propertyPath"
			String beanName = targetAttributeName.substring(0, propertyDelimiterIndex);
			String propertyPath = targetAttributeName.substring(propertyDelimiterIndex + 1);
			beanAccessor.setWrappedInstance(target.getAttribute(beanName));
			if (logger.isDebugEnabled()) {
				logger.debug("Mapping bean property attribute from path '" + sourceAttributeName + "' to path '"
						+ targetAttributeName + "' with value '" + value + "'");
			}
			beanAccessor.setPropertyValue(propertyPath, value);
		}
		else {
			if (value == null && !source.containsAttribute(sourceAttributeName)) {
				if (mapMissingAttributesToNull) {
					if (logger.isDebugEnabled()) {
						logger.debug("No value exists for attribute '" + sourceAttributeName
								+ "' in the from model - thus, I will map a null value");
					}
					target.setAttribute(targetAttributeName, null);
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("No value exists for attribute '" + sourceAttributeName
								+ "' in the from model - thus, I will NOT map a value");
					}
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Mapping attribute from name '" + sourceAttributeName + "' to name '"
							+ targetAttributeName + "' with value '" + value + "'");
				}
				target.setAttribute(targetAttributeName, value);
			}
		}
	}

	protected BeanWrapper createBeanWrapper(Object attribute) {
		return new BeanWrapperImpl(attribute);
	}
}