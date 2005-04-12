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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Constants;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Rob Harrop
 */
public class CustomizableTraceInterceptor implements MethodInterceptor {

	public static final String METHOD_NAME_PLACEHOLDER = "${methodName}";

	public static final String TARGET_CLASS_NAME_PLACEHOLDER = "${targetClassName}";

	public static final String TARGET_CLASS_SHORT_NAME_PLACEHOLDER = "${targetClassShortName}";

	public static final String RETURN_VALUE_PLACEHOLDER = "${returnValue}";

	public static final String ARGUMENT_TYPES_PLACEHOLDER = "${argumentTypes}";

	public static final String ARGUMENTS_PLACEHOLDER = "${arguments}";

	public static final String EXCEPTION_PLACEHOLDER = "${exception}";

	/**
	 * Default <code>Log</code> used for writing trace messages.
	 */
	protected static final Log defaultLogger = LogFactory.getLog(CustomizableTraceInterceptor.class);

	private static final String DEFAULT_ENTER_MESSAGE = "Entering method [" + METHOD_NAME_PLACEHOLDER + "] in class [" + TARGET_CLASS_NAME_PLACEHOLDER + "].";

	private static final String DEFAULT_EXIT_MESSAGE = "Exiting method [" + METHOD_NAME_PLACEHOLDER + "] in class [" + TARGET_CLASS_NAME_PLACEHOLDER + "].";

	private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception thrown in method [" + METHOD_NAME_PLACEHOLDER + "] in class [" + TARGET_CLASS_NAME_PLACEHOLDER + "].";

	private static final Pattern PATTERN = Pattern.compile("\\$\\{\\p{Alpha}+\\}");

	private static final Constants CONSTANTS = new Constants(CustomizableTraceInterceptor.class);

	private String enterMessage = DEFAULT_ENTER_MESSAGE;

	private String exitMessage = DEFAULT_EXIT_MESSAGE;

	private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;


	/**
	 * Flag indicating whether the default <code>Log</code> should be used (false) or whether each invocation should be
	 * logged against a <code>Log</code> for the <code>Class</code> of the invocation target.
	 */
	private boolean useDynamicLog = false;

	public void setEnterMessage(String enterMessage) {
		Assert.hasText(enterMessage, "enterMessage cannot be null or zero length.");
		checkForInvalidPlaceholders(enterMessage);
		assertDoesNotContainsText(enterMessage, RETURN_VALUE_PLACEHOLDER, "enterMessage cannot contain placeholder " + RETURN_VALUE_PLACEHOLDER);
		assertDoesNotContainsText(enterMessage, EXCEPTION_PLACEHOLDER, "enterMessage cannot contain placeholder " + EXCEPTION_PLACEHOLDER);

		this.enterMessage = enterMessage;
	}

	public void setExitMessage(String exitMessage) {
		Assert.hasText(exitMessage, "exitMessage cannot be null or zero length.");
		checkForInvalidPlaceholders(exitMessage);
		assertDoesNotContainsText(exitMessage, EXCEPTION_PLACEHOLDER, "exitMessage cannot contain placeholder " + EXCEPTION_PLACEHOLDER);

		this.exitMessage = exitMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		Assert.hasText(exceptionMessage, "exceptionMessage cannot be null or zero length.");
		checkForInvalidPlaceholders(exceptionMessage);
		assertDoesNotContainsText(exceptionMessage, RETURN_VALUE_PLACEHOLDER, "exceptionMessage cannot contain placeholder " + RETURN_VALUE_PLACEHOLDER);

		this.exceptionMessage = exceptionMessage;
	}

	public void setUseDynamicLog(boolean useDynamicLog) {
		this.useDynamicLog = useDynamicLog;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Log logger = getLoggerForInvocation(methodInvocation);

		if (logger.isDebugEnabled()) {
			return invokeUnderTrace(logger, methodInvocation);
		}
		else {
			return methodInvocation.proceed();
		}
	}

	private Object invokeUnderTrace(Log logger, MethodInvocation methodInvocation) throws Throwable {
		try {
			logger.debug(replaceTokens(this.enterMessage, methodInvocation, null, null));
			Object returnValue = methodInvocation.proceed();
			logger.debug(replaceTokens(this.exitMessage, methodInvocation, returnValue, null));
			return returnValue;
		}
		catch (Throwable t) {
			logger.debug(replaceTokens(this.exceptionMessage, methodInvocation, null, t));
			throw t;
		}
	}


	/**
	 * Both returnValue and throwable CAN be null.
	 *
	 * @param message
	 * @param methodInvocation
	 * @param returnValue
	 * @param throwable
	 * @return
	 */
	protected String replaceTokens(String message, MethodInvocation methodInvocation, Object returnValue, Throwable throwable) {
		Matcher matcher = PATTERN.matcher(message);

		StringBuffer output = new StringBuffer();
		while (matcher.find()) {
			String match = matcher.group();
			if (METHOD_NAME_PLACEHOLDER.equals(match)) {
				matcher.appendReplacement(output, methodInvocation.getMethod().getName());
			}
			else if (TARGET_CLASS_NAME_PLACEHOLDER.equals(match)) {
				matcher.appendReplacement(output, methodInvocation.getThis().getClass().getName());
			}
			else if (TARGET_CLASS_SHORT_NAME_PLACEHOLDER.equals(match)) {
				matcher.appendReplacement(output, ClassUtils.getShortName(methodInvocation.getThis().getClass()));
			}
			else if (ARGUMENTS_PLACEHOLDER.equals(match)) {
				matcher.appendReplacement(output, StringUtils.arrayToCommaDelimitedString(methodInvocation.getArguments()));
			}
			else if (ARGUMENT_TYPES_PLACEHOLDER.equals(match)) {
				appendArgumentTypes(methodInvocation, matcher, output);
			}
			else if (RETURN_VALUE_PLACEHOLDER.equals(match)) {
				appendReturnTypes(methodInvocation, matcher, output, returnValue);
			}
			else {
				// consider throwing earlier
				throw new IllegalArgumentException("Unknown placeholder [" + match + "]");
			}
		}
		matcher.appendTail(output);

		return output.toString();
	}

	private void appendReturnTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, Object returnValue) {
		if (methodInvocation.getMethod().getReturnType() == void.class) {
			matcher.appendReplacement(output, "void");
		}
		else if (returnValue == null) {
			matcher.appendReplacement(output, "null");
		}
		else {
			matcher.appendReplacement(output, returnValue.toString());
		}
	}

	private void appendArgumentTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output) {
		Class[] argumentTypes = methodInvocation.getMethod().getParameterTypes();
		String[] argumentTypeShortNames = new String[argumentTypes.length];
		for (int i = 0; i < argumentTypeShortNames.length; i++) {
			argumentTypeShortNames[i] = ClassUtils.getShortName(argumentTypes[i]);
		}
		matcher.appendReplacement(output, StringUtils.arrayToCommaDelimitedString(argumentTypeShortNames));
	}

	/**
	 * Returns the appropriate <code>Log</code> instance to use for the given <code>MethodInvocation</code>.
	 *
	 * @param methodInvocation
	 * @return
	 */
	private Log getLoggerForInvocation(MethodInvocation methodInvocation) {
		return (this.useDynamicLog) ? LogFactory.getLog(methodInvocation.getThis().getClass()) : defaultLogger;
	}

	private void assertDoesNotContainsText(String target, String desiredText, String message) {
		if (target.indexOf(desiredText) > -1) {
			throw new IllegalArgumentException(message);
		}
	}

	private void checkForInvalidPlaceholders(String message) throws IllegalArgumentException {
		Set allowedPlaceholders = CONSTANTS.getValues("");

		Matcher matcher = PATTERN.matcher(message);

		while (matcher.find()) {
			String match = matcher.group();
			if (!allowedPlaceholders.contains(match)) {
				throw new IllegalArgumentException("Placeholder " + match + " is not valid.");
			}
		}
	}

}
