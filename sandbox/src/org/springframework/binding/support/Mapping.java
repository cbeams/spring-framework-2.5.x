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
 * A single mapping definition.
 * @author Keith Donald
 */
public class Mapping implements Serializable {
	protected static final Log logger = LogFactory.getLog(Mapping.class);

	private String sourceAttributeName;

	private String targetAttributeName;

	private TypeConverter valueTypeConverter;

	public Mapping(String sourceAttributeName) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = sourceAttributeName;
	}

	public Mapping(String sourceAttributeName, TypeConverter valueTypeConverter) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = sourceAttributeName;
		this.valueTypeConverter = valueTypeConverter;
	}

	public Mapping(String sourceAttributeName, String targetAttributeName) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = targetAttributeName;
	}

	public Mapping(String sourceAttributeName, String targetAttributeName, TypeConverter valueTypeConverter) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = targetAttributeName;
		this.valueTypeConverter = valueTypeConverter;
	}

	private void setSourceAttributeName(String sourceAttributeName) {
		Assert.notNull(sourceAttributeName, "The source attribute name is required");
		this.sourceAttributeName = sourceAttributeName;
	}

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

	private BeanWrapper createBeanWrapper(Object attribute) {
		return new BeanWrapperImpl(attribute);
	}
}