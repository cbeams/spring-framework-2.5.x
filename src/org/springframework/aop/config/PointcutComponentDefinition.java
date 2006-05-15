/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractComponentDefinition;
import org.springframework.util.Assert;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class PointcutComponentDefinition extends AbstractComponentDefinition {

	private final String beanName;

	private final BeanDefinition pointcutDefinition;

	private final String description;

	private final String expression;

	public PointcutComponentDefinition(String beanName, BeanDefinition pointcutDefinition, String expression) {
		Assert.notNull(beanName, "'beanName' cannot be null.");
		Assert.notNull(pointcutDefinition, "'pointcutDefinition' cannot be null.");
		Assert.notNull(expression, "'expression' cannot be null.");
		this.beanName = beanName;
		this.pointcutDefinition = pointcutDefinition;
		this.expression = expression;
		this.description = "Pointcut <name='" + getName() + "', expression='" + this.expression + ">'";
	}

	public String getName() {
		return this.beanName;
	}

	public String getDescription() {
		return this.description;
	}

	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[]{this.pointcutDefinition};
	}

	public Object getSource() {
		return this.pointcutDefinition.getSource();
	}
}
