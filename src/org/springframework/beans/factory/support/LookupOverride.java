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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * Represents an override of a method that looks up an object in the same IoC context.
 *
 * <p>Methods eligible for lookup override must not have arguments.
 *
 * @author Rod Johnson
 */
public class LookupOverride extends MethodOverride {
	
	private final String beanName;

	/**
	 * Construct a new LookupOverride.
	 * @param methodName name of the method to override. This
	 * method must have no arguments.
	 * @param beanName name of the bean in the current BeanFactory
	 * or ApplicationContext that the overriden method should return
	 */
	public LookupOverride(String methodName, String beanName) {
		super(methodName);
		this.beanName = beanName;
	}
	
	/**
	 * Return the name of the bean that should be returned
	 * by this method.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Doesn't allow for overloading, so matching method name is fine.
	 */
	public boolean matches(Method method, MethodOverrides overrides) {
		return method.getName().equals(getMethodName());
	}

	public String toString() {
		return "LookupOverride for method '" + getMethodName() + "'; will return bean '" + beanName + "'";
	}

}
