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

package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Base class for monitoring interceptors, such as performance monitors.
 * Provides <code>prefix</code> and <code>suffix</code> properties
 * that help to classify/group performance monitoring results.
 *
 * <p>Subclasses should call the <code>createInvocationTraceName(MethodInvocation)</code>
 * method to create a name for the given trace that includes information about the
 * method invocation under trace along with the prefix and suffix added as appropriate.
 * 
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2.7
 * @see #setPrefix
 * @see #setSuffix
 * @see #createInvocationTraceName
 */
public abstract class AbstractMonitoringInterceptor extends AbstractTraceInterceptor {

	private String prefix = "";

	private String suffix = "";


	/**
	 * Set the text that will get appended to the trace data.
	 */
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * Return the text that will get appended to the trace data.
	 */
	protected String getPrefix() {
		return prefix;
	}

	/**
	 * Set the text that will get prepended to the trace data.
	 */
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	/**
	 * Return the text that will get prepended to the trace data.
	 */
	protected String getSuffix() {
		return suffix;
	}


	/**
	 * Create a <code>String</code> name for the given <code>MethodInvocation</code>
	 * that can be used for trace/logging purposes. This name is made up of the
	 * configured prefix, followed by the fully-qualified name of the method being
	 * invoked, followed by the configured suffix.
	 * @see #setPrefix
	 * @see #setSuffix
	 */
	protected String createInvocationTraceName(MethodInvocation invocation) {
		StringBuffer sb = new StringBuffer(getPrefix());
		sb.append(invocation.getMethod().getDeclaringClass().getName());
		sb.append('.').append(invocation.getMethod().getName());
		sb.append(getSuffix());
		return sb.toString();
	}

}
