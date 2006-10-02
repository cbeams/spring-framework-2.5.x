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
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.parsing.AbstractComponentDefinition;
import org.springframework.util.Assert;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectComponentDefinition extends AbstractComponentDefinition {

	private final String aspectName;

	private final BeanDefinition[] beanDefinitions;

	private final BeanReference[] beanReferences;

	private final Object source;


	public AspectComponentDefinition(
			String aspectName, BeanDefinition[] beanDefinitions, BeanReference[] beanReferences, Object source) {

		Assert.notNull(aspectName, "Aspect name must not be null");
		this.aspectName = aspectName;
		this.beanDefinitions = (beanDefinitions != null ? beanDefinitions : new BeanDefinition[0]);
		this.beanReferences = (beanReferences != null ? beanReferences : new BeanReference[0]);
		this.source = source;
	}


	public String getName() {
		return this.aspectName;
	}

	public BeanDefinition[] getBeanDefinitions() {
		return this.beanDefinitions;
	}

	public BeanReference[] getBeanReferences() {
		return this.beanReferences;
	}

	public Object getSource() {
		return this.source;
	}

}
