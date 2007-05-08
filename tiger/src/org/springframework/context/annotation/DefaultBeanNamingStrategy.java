/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.context.annotation;

import java.beans.Introspector;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A simple implementation for naming beans based on the short name of the class
 * with the first-letter lowercased. 
 * 
 * For example: com.xyz.FooServiceImpl -&gt; fooServiceImpl
 * 
 * @author Mark Fisher
 * @since 2.1
 */
public class DefaultBeanNamingStrategy implements BeanNamingStrategy {

	public String generateName(BeanDefinition beanDefinition) {
		Assert.notNull(beanDefinition, "beanDefinition cannot be null");
		String shortName = ClassUtils.getShortName(beanDefinition.getBeanClassName());
		return Introspector.decapitalize(shortName);
	}

}
