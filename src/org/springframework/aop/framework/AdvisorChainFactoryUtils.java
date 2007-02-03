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

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility methods for use by {@link AdvisorChainFactory} implementations.
 *
 * <p>The {@link #calculateInterceptorsAndDynamicInterceptionAdvice} method
 * is the definitive way of working out an advice chain for a Method,
 * given an {@link Advised} object.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @deprecated as of Spring 2.0.3, in favor of {@link DefaultAdvisorChainFactory}.
 * This utility class will be removed in Spring 2.1.
 */
public abstract class AdvisorChainFactoryUtils {

	/**
	 * Canonical instance of a simple AdvisorChainFactory implementation.
	 */
	public static final AdvisorChainFactory SIMPLE_ADVISOR_CHAIN_FACTORY = new DefaultAdvisorChainFactory();


	/**
	 * Return the static interceptors and dynamic interception advice that may apply
	 * to this method invocation.
	 * @param config the AOP configuration in the form of an Advised object
	 * @param proxy the proxy object
	 * @param method the proxied method
	 * @param targetClass the target class
	 * @return List of MethodInterceptors (may also include InterceptorAndDynamicMethodMatchers)
	 * @see AdvisorChainFactory
	 */
	public static List calculateInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Object proxy, Method method, Class targetClass) {

		return SIMPLE_ADVISOR_CHAIN_FACTORY.getInterceptorsAndDynamicInterceptionAdvice(
				config, proxy, method, targetClass);
	}

}
