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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Rob Harrop
 */
public abstract class AbstractTraceInterceptor implements MethodInterceptor {

	/**
	 * Indicates whether the <code>Log</code> instance used to write log messages should be determined dynamically
	 * based on the target of the method invocation, or whether the default <code>Log</code> should be used. Default
	 * is <code>false</code> meaning the default <code>Log</code> instance will be used.
	 */
	private boolean useDynamicLog = false;

	/**
	 * The default <code>Log</code> instance used to write trace messages. This instance is mapped to the implementing
	 * <code>Class</code>.
	 */
	protected Log defaultLogger = LogFactory.getLog(getClass());

	/**
	 * Sets the value of the <code>useDynamicLog</code> flag. Used to determine which <code>Log</code> instance should
	 * be used to write log messages for a particular method invocation.
	 *
	 * @see #getLoggerForMethodInvocation(org.aopalliance.intercept.MethodInvocation)
	 */
	public void setUseDynamicLog(boolean useDynamicLog) {
		this.useDynamicLog = useDynamicLog;

		// release default logger if it is not being used
		if (this.useDynamicLog) {
			defaultLogger = null;
		}
	}

	/**
	 * Returns the appropriate <code>Log</code> instance to use for the given <code>MethodInvocation</code>. If
	 * <code>useDynamicLog</code> is set to <code>true</code>, the <code>Log</code> instance will be for the target
	 * class of the <code>MethodInvocation</code>, otherwise the <code>Log</code> will be the <code>defaultLogger</code>.
	 *
	 * @see #setUseDynamicLog(boolean)
	 */
	protected Log getLoggerForMethodInvocation(MethodInvocation methodInvocation) {
		return (this.useDynamicLog) ? LogFactory.getLog(methodInvocation.getThis().getClass()) : this.defaultLogger;
	}

	/**
	 * Determines whether or not logging is enabled for the particular <code>MethodInvocation</code>. If not, the
	 * method invocation proceeds as normal, otherwise the method invocation is passed to the <code>invokeUnderTrace</code>
	 * method for handling.
	 */
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Log logger = getLoggerForMethodInvocation(methodInvocation);

		if (logger.isTraceEnabled()) {
			return invokeUnderTrace(methodInvocation, logger);
		}
		else {
			return methodInvocation.proceed();
		}
	}

	/**
	 * Subclasses should override this method to perform any tracing around the supplied <code>MethodInvocation</code>.
	 * Subclasses are responsible for ensuring that the <code>MethodInvocation</code> actually executes by calling
	 * <code>MethodInvocation.proceed()</code>.
	 *
	 * @param methodInvocation the <code>MethodInvocation</code> being traced.
	 * @param logger the <code>Log</code> to write trace messages to.
	 * @return the result of the call to <code>MethodInvocation.proceed()</code>.
	 * @throws Throwable if the call to <code>MethodInvocation.proceed()</code> encountered any errors.
	 */
	protected abstract Object invokeUnderTrace(MethodInvocation methodInvocation, Log logger) throws Throwable;
}
