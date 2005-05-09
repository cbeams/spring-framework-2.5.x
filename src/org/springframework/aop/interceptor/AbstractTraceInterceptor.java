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

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base <code>MethodInterceptor</code> implementation for tracing.
 *
 * <p>By default, log messages are written to the log for the interceptor class,
 * not the class which is being intercepted. Setting the <code>useDynamicLog</code>
 * bean property to <code>true</code> causes all log messages to be written to
 * the <code>Log</code> for the target class being intercepted.
 *
 * <p>Subclasses must implement the <code>invokeUnderTrace</code> method, which
 * is invoked by this class ONLY when a particularinvocation SHOULD be traced.
 * Subclasses should write to the <code>Log</code> instance provided.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setUseDynamicLogger
 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
 */
public abstract class AbstractTraceInterceptor implements MethodInterceptor, Serializable {

	/**
	 * The default <code>Log</code> instance used to write trace messages.
	 * This instance is mapped to the implementing <code>Class</code>.
	 */
	protected transient Log defaultLogger = LogFactory.getLog(getClass());


	/**
	 * Set whether to use a dynamic logger or a static logger.
	 * Default is a static logger for this trace interceptor.
	 * <p>Used to determine which <code>Log</code> instance should be used to write
	 * log messages for a particular method invocation: a dynamic one for the
	 * <code>Class</code> getting called, or a static one for the <code>Class</code>
	 * of the trace interceptor.
	 * @see #getLoggerForInvocation(org.aopalliance.intercept.MethodInvocation)
	 */
	public void setUseDynamicLogger(boolean useDynamicLog) {
		// Release default logger if it is not being used.
		this.defaultLogger = (useDynamicLog ? null : LogFactory.getLog(getClass()));
	}


	/**
	 * Determines whether or not logging is enabled for the particular <code>MethodInvocation</code>.
	 * If not, the method invocation proceeds as normal, otherwise the method invocation is passed
	 * to the <code>invokeUnderTrace</code> method for handling.
	 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Log logger = getLoggerForInvocation(invocation);
		if (logger.isTraceEnabled()) {
			return invokeUnderTrace(invocation, logger);
		}
		else {
			return invocation.proceed();
		}
	}

	/**
	 * Return the appropriate <code>Log</code> instance to use for the given
	 * <code>MethodInvocation</code>. If the <code>useDynamicLogger</code> flag
	 * is set, the <code>Log</code> instance will be for the target class of the
	 * <code>MethodInvocation</code>, otherwise the <code>Log</code> will be the
	 * default static logger.
	 * @param invocation the <code>MethodInvocation</code> being traced
	 * @return the <code>Log</code> instance to use
	 * @see #setUseDynamicLogger
	 */
	protected Log getLoggerForInvocation(MethodInvocation invocation) {
		return (this.defaultLogger != null ?
				this.defaultLogger : LogFactory.getLog(invocation.getThis().getClass()));
	}

	/**
	 * Subclasses must override this method to perform any tracing around the
	 * supplied <code>MethodInvocation</code>. Subclasses are responsible for
	 * ensuring that the <code>MethodInvocation</code> actually executes by
	 * calling <code>MethodInvocation.proceed()</code>.
	 * <p>The passed-in <code>Log</code> instance will have log level "trace"
	 * enabled. Subclasses do not have to check for this again.
	 * @param logger the <code>Log</code> to write trace messages to
	 * @return the result of the call to <code>MethodInvocation.proceed()</code>
	 * @throws Throwable if the call to <code>MethodInvocation.proceed()</code>
	 * encountered any errors
	 */
	protected abstract Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable;

}
