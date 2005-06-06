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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.expression.ExpressionEvaluator;
import org.springframework.binding.expression.ExpressionEvaluatorFactory;
import org.springframework.binding.expression.ExpressionEvaluatorSetter;
import org.springframework.core.ToStringCreator;

/**
 * A single mapping definition, encapulating the information neccessary to map a
 * single attribute from a source attributes map to a target map.
 * @author Keith Donald
 */
public class Mapping implements Serializable {
	protected static final Log logger = LogFactory.getLog(Mapping.class);

	private ExpressionEvaluator sourceAttribute;

	private ExpressionEvaluatorSetter targetAttribute;

	private ConversionExecutor valueConverter;

	public Mapping(String sourceTargetAttributeExpressionString) {
		this(ExpressionEvaluatorFactory.evaluatorFor(sourceTargetAttributeExpressionString),
			 ExpressionEvaluatorFactory.setterFor(sourceTargetAttributeExpressionString));
	}

	public Mapping(String sourceTargetAttributeExpressionString, ConversionExecutor valueConverter) {
		this(ExpressionEvaluatorFactory.evaluatorFor(sourceTargetAttributeExpressionString),
			 ExpressionEvaluatorFactory.setterFor(sourceTargetAttributeExpressionString), valueConverter);
	}

	public Mapping(String sourceAttributeExpressionString, String targetAttributeExpressionString) {
		this(ExpressionEvaluatorFactory.evaluatorFor(sourceAttributeExpressionString),
			 ExpressionEvaluatorFactory.setterFor(targetAttributeExpressionString));
	}

	public Mapping(String sourceAttributeExpressionString, String targetAttributeExpressionString,
			ConversionExecutor valueConverter) {
		this(ExpressionEvaluatorFactory.evaluatorFor(sourceAttributeExpressionString),
			 ExpressionEvaluatorFactory.setterFor(targetAttributeExpressionString), valueConverter);
	}

	public Mapping(ExpressionEvaluatorSetter sourceTargetAttribute) {
		this.sourceAttribute = sourceTargetAttribute;
		this.targetAttribute = sourceTargetAttribute;
	}

	public Mapping(ExpressionEvaluator sourceAttribute, ExpressionEvaluatorSetter targetAttribute) {
		this.sourceAttribute = sourceAttribute;
		this.targetAttribute = targetAttribute;
	}

	public Mapping(ExpressionEvaluator sourceAttribute, ExpressionEvaluatorSetter targetAttribute,
			ConversionExecutor valueConverter) {
		this.sourceAttribute = sourceAttribute;
		this.targetAttribute = targetAttribute;
		this.valueConverter = valueConverter;
	}

	/**
	 * Map the <code>sourceAttribute</code> in to the
	 * <code>targetAttribute</code> target map, performing type conversion
	 * if necessary.
	 * @param source The source
	 * @param target The target
	 */
	public void map(Object source, Object target, Map mappingContext) {
		// get source value
		Object sourceValue = sourceAttribute.evaluate(source, mappingContext);
		Object targetValue = sourceValue;
		if (valueConverter != null) {
			targetValue = valueConverter.call(sourceValue);
		}
		// set target value
		if (logger.isDebugEnabled()) {
			logger.debug("Mapping source attribute '" + sourceAttribute + "' with value " + sourceValue + " to" +
					"target attribute '" + targetAttribute + "' with value " + targetValue);
		}
		targetAttribute.set(target, targetValue, mappingContext);
	}

	public String toString() {
		return new ToStringCreator(this).append(sourceAttribute + "->" + targetAttribute).
			append("valueConversionExecutor", valueConverter).toString();
	}
}