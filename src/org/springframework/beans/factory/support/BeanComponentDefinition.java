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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.Assert;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanComponentDefinition implements ComponentDefinition {

	private final BeanDefinitionHolder holder;

	public BeanComponentDefinition(BeanDefinitionHolder holder) {
		Assert.notNull(holder, "'holder' cannot be null.");
		this.holder = holder;
	}

	public String getName() {
		return this.holder.getBeanName();
	}

	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[]{this.holder.getBeanDefinition()};
	}

	public Object getSource() {
		return this.holder.getBeanDefinition().getSource();
	}
}
