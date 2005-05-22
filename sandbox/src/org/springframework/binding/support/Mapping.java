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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.convert.ConversionExecutor;
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

	private ConversionExecutor valueConversionExecutor;

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
	 * @param valueConversionExecutor the type converter
	 */
	public Mapping(String sourceAttributeName, ConversionExecutor valueConversionExecutor) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = sourceAttributeName;
		this.valueConversionExecutor = valueConversionExecutor;
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
	 * @param valueConversionExecutor the type converter
	 */
	public Mapping(String sourceAttributeName, String targetAttributeName, ConversionExecutor valueConversionExecutor) {
		setSourceAttributeName(sourceAttributeName);
		this.targetAttributeName = targetAttributeName;
		this.valueConversionExecutor = valueConversionExecutor;
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
	 */
	public void map(AttributeSource source, MutableAttributeSource target) {
		// get source value
		if (source.containsAttribute(sourceAttributeName)) {
			Object sourceValue = source.getAttribute(sourceAttributeName);
			Object targetValue = sourceValue;
			// convert source value to a expected target type if neccessary
			if (valueConversionExecutor != null) {
				targetValue = valueConversionExecutor.call(sourceValue);
			}
			// set target value
			if (logger.isDebugEnabled()) {
				logger.debug("Mapping source attribute '" + sourceAttributeName + "' with value " + sourceValue + " to" +
						"target attribute '" + targetAttributeName + "' with value " + targetValue);
			}
			target.setAttribute(targetAttributeName, targetValue);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No source attribute '" + sourceAttributeName + "' found in source '" + source + "' -- not mapping to perform");
			}
		}
	}

	public String toString() {
		return new ToStringBuilder(this).append(sourceAttributeName + "->" + targetAttributeName).
			append("valueConversionExecutor", valueConversionExecutor).toString();
	}
}