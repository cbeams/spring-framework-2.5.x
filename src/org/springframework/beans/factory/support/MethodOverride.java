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
 * Object representing the override of a method on a managed
 * object by the IoC container.
 *
 * <p>Note that the override mechanism is <i>not</i> intended as a
 * generic means of inserting crosscutting code: use AOP for that.
 *
 * @author Rod Johnson
 */
public abstract class MethodOverride {
	
	private final String methodName;

	/**
	 * Create a new override for the given method.
	 * @param methodName the name of the method to be overridden
	 */
	protected MethodOverride(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Return the name of the method to be overridden.
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Subclasses must override this to indicate whether they match
	 * the given method. This allows for argument list checking
	 * as well as method name checking.
	 * @param method the method to check
	 * @param overrides owning MethodOverrides object.
	 * This allows us to check whether the method is overloaded.
	 * @return whether this override matches the given method
	 */
	public abstract boolean matches(Method method, MethodOverrides overrides);

}
