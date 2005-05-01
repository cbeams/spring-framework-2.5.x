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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ConstantException;
import org.springframework.core.Constants;

/**
 * Base <code>MethodInterceptor</code> for logging interceptors.
 * <p/>
 * Allows for the level at which log messages are written to be configured via the <code>logLevel</code> property.
 * <p/>
 * By default, log messages are written to the log for the interceptor class not the class which is being intercepted.
 * Setting the <code>useDynamicLog</code> property to <code>true</code> causes all log messages to be written to
 * the <code>Log</code> for the class being intercepted.
 * <p/>
 * Subclasses must implement the <code>invokeUnderTrace</code> method which is invoked by this class ONLY when a particular
 * invocation SHOULD be traced. Subclasses should write to the <code>Log</code> instance provided and should delegate to
 * one of the <code>log</code> methods supplied by this class to write the actual log message.
 *
 * @author Rob Harrop
 * @see #setLogLevel(String)
 * @see #setUseDynamicLog(boolean)
 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
 * @see #log(org.apache.commons.logging.Log, Object)
 * @see #log(org.apache.commons.logging.Log, Object, Throwable)
 */
public abstract class AbstractTraceInterceptor implements MethodInterceptor {
	public static final int LEVEL_FATAL = 0;

	public static final int LEVEL_ERROR = 1;

	public static final int LEVEL_WARN = 2;

	public static final int LEVEL_INFO = 3;

	public static final int LEVEL_DEBUG = 4;

	public static final int LEVEL_TRACE = 5;

	private static final String CONSTANT_PREFIX = "LEVEL_";

	private static final Constants constants = new Constants(AbstractTraceInterceptor.class);

	/**
	 * The default <code>Log</code> instance used to write trace messages. This instance is mapped to the implementing
	 * <code>Class</code>.
	 */
	protected Log defaultLogger = LogFactory.getLog(getClass());

	/**
	 * Indicates whether the <code>Log</code> instance used to write log messages should be determined dynamically
	 * based on the target of the method invocation, or whether the default <code>Log</code> should be used. Default
	 * is <code>false</code> meaning the default <code>Log</code> instance will be used.
	 */
	private boolean useDynamicLog = false;

	/**
	 * The current log level. Default value is <code>TRACE</code>.
	 */
	private int logLevel = constants.asNumber(CONSTANT_PREFIX + "TRACE").intValue();


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
	 * Sets the level at which log messages should be written. Valid values are:
	 * <ul>
	 * <li>ERROR</li>
	 * <li>WARN</li>
	 * <li>INFO</li>
	 * <li>DEBUG</li>
	 * <li>TRACE</li>
	 * <li>WARN</li>
	 * </ul>
	 * The default value is <code>INFO</code>.
	 */
	public void setLogLevel(String level) {
		Number levelValue = null;
		try {
			levelValue = constants.asNumber(CONSTANT_PREFIX + level.toUpperCase());
		}
		catch (ConstantException ex) {
			throw new IllegalArgumentException("Log level [" + level + "] is unrecognized.");
		}

		this.logLevel = levelValue.intValue();
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

		if (isEnabled(logger)) {
			return invokeUnderTrace(methodInvocation, logger);
		}
		else {
			return methodInvocation.proceed();
		}
	}

	/**
	 * Determines whether the supplied <code>Log</code> is currently enabled to write log messages at the level specified
	 * by the <code>logLevel</code> property.
	 *
	 * @return <code>true</code> if the configured log level is enabled for the supplied <code>Log</code> else <code>false</code>.
	 * @see #setLogLevel(String)
	 */
	protected boolean isEnabled(Log logger) {
		switch (this.logLevel) {
			case 0:
				return logger.isErrorEnabled();
			case 1:
				return logger.isWarnEnabled();
			case 2:
				return logger.isInfoEnabled();
			case 3:
				return logger.isDebugEnabled();
			case 4:
				return logger.isTraceEnabled();
			case 5:
				return logger.isFatalEnabled();
			default:
				throw new IllegalArgumentException("Unknown log level [" + this.logLevel + "].");
		}
	}

	/**
	 * Writes the supplied message to the supplied <code>Log</code> at the configured log level.
	 * <p/>
	 * Should be used by subclasses in place of <code>Log.xxx()</code> to ensure that log messages are written at
	 * the correct level.
	 *
	 * @see #setLogLevel(String)
	 * @see #log(org.apache.commons.logging.Log, Object, Throwable)
	 */
	protected void log(Log logger, Object message) {
		log(logger, message, null);
	}

	/**
	 * Writes the supplied message and optional <code>Throwable</code> to the supplied <code>Log</code> at
	 * the configured log level.
	 * <p/>
	 * Should be used by subclasses in place of <code>Log.xxx()</code> to ensure that log messages are written at
	 * the correct level.
	 *
	 * @param logger the <code>Log</code> to write the message to.
	 * @param message the message to write
	 * @param t any <code>Throwable</code> that should be logged alongside the message. Can be null.
	 */
	protected void log(Log logger, Object message, Throwable t) {
		switch (this.logLevel) {
			case 0:
				logger.error(message, t);
				break;
			case 1:
				logger.warn(message, t);
				break;
			case 2:
				logger.info(message, t);
				break;
			case 3:
				logger.debug(message, t);
				break;
			case 4:
				logger.trace(message, t);
				break;
			case 5:
				logger.fatal(message, t);
				break;
			default:
				throw new IllegalArgumentException("Unknown log level [" + this.logLevel + "].");
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
