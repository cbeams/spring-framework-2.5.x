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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public interface ComponentDefinition {

	String getName();

	/**
	 * Returns a friendly description of the described component. Implementations are encouraged to
	 * return the same value for {@link #toString()}.
	 */
	String getDescription();

	/**
	 * Returns the {@link BeanDefinition BeanDefinitions} that were registed with the {@link BeanDefinitionRegistry}
	 * to form this <code>ComponentDefinition</code>. It should be noted that a <code>ComponentDefinition</code> may
	 * well be related with other {@link BeanDefinition BeanDefinitions} via {@link RuntimeBeanReference references},
	 * however these are <strong>not</strong> included as they may be not available immediately. Important
	 * {@link RuntimeBeanReference RuntimeBeanReferences} are available from {@link #getBeanReferences()}. Implementations
	 * are encouraged to highlight any important references as part of the {@link #getDescription() description}. Tools
	 */
	BeanDefinition[] getBeanDefinitions();

	RuntimeBeanReference[] getBeanReferences();

	Object getSource();
}
