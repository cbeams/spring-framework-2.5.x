/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Base class for performance monitoring interceptors. Provide <code>prefix</code> and <code>suffix</code> properties
 * that help to classify/group performance monitoring results.
 * <p/>
 * Sub-classes should call the <code>createInvocationTraceName(MethodInvocation)</code> method to create a name for the given
 * trace that includes information about the method invocation under trace along with the prefix and suffix added as
 * appropriate.
 * 
 * @author Rob Harrop
 * @see #setPrefix(String)
 * @see #setSuffix(String)
 * @see #createInvocationTraceName(org.aopalliance.intercept.MethodInvocation)
 */
public abstract class AbstractPerformanceMonitorInterceptor extends AbstractTraceInterceptor {

	private String prefix;

	private String suffix;

	protected String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the <code>String</code> value that gets appended to the trace data used by performance monitoring interceptors.
	 * @see #createInvocationTraceName(org.aopalliance.intercept.MethodInvocation)
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	protected String getSuffix() {
		return suffix;
	}

	/**
	 * Sets the <code>String</code> value that gets prepended to the trace data used by performance monitoring interceptors.
	 * @see #createInvocationTraceName(org.aopalliance.intercept.MethodInvocation)
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Creates a <code>String</code> name for the given <code>MethodInvocation</code> that can be used for trace/logging
	 * purposes. This name is made up of the configured prefix, followed by the fully-qualified name of the method being invoked,
	 * followed by the configured suffix.
	 * @see #setPrefix(String)
	 * @see #setSuffix(String)
	 */
	protected String createInvocationTraceName(MethodInvocation invocation) {
		String invocationData = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
		return getPrefix() + invocationData + getSuffix();
	}
}
