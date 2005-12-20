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

package org.springframework.beans.factory.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Enumeration determining autowiring status: 
 * that is, whether a bean should have its dependencies automatically injected
 * by the Spring container using setter injection. This is a core concept in Spring DI.
 * Note that constructor autowiring is not available as bean creation methods will themselves
 * invoke bean constructors.
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 */
public enum Autowire {
	
	UNSPECIFIED(-1),
	NO(AbstractBeanDefinition.AUTOWIRE_NO),
	BY_TYPE(AbstractBeanDefinition.AUTOWIRE_BY_TYPE),
	BY_NAME(AbstractBeanDefinition.AUTOWIRE_BY_NAME),
	AUTODETECT(AbstractBeanDefinition.AUTOWIRE_AUTODETECT);

	private final int value;

	Autowire(int value) { this.value = value; }
	
	public int value() { return value; }
	
	public boolean isAutowire() {
		return !(value == UNSPECIFIED.value || value == NO.value);
	}

}
