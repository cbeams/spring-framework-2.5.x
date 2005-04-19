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
public class CustomizableTraceInterceptor extends AbstractTraceInterceptor {

	/**
	 * The <code>${methodName}</code> placeholder. Replaced with the name of the method being invoked.
	 */
	public static final String METHOD_NAME_PLACEHOLDER = "${methodName}";

	/**
	 * The <code>${targetClassName}</code> placeholder. Replaced with the fully-qualifed name of the <code>Class</code>
	 * of the method invocation target.
	 */
	public static final String TARGET_CLASS_NAME_PLACEHOLDER = "${targetClassName}";

	/**
	 * The <code>${targetClassShortName}</code> placeholder. Replaced with the short name of the <code>Class</code> of the
	 * method invocation target.
	 */
	public static final String TARGET_CLASS_SHORT_NAME_PLACEHOLDER = "${targetClassShortName}";

	/**
	 * The <code>${returnValue}</code> placeholder. Replaced with the <code>String</code> representation of the value
	 * returned by the method invocation.
	 */
	public static final String RETURN_VALUE_PLACEHOLDER = "${returnValue}";

	/**
	 * The <code>${argumentTypes}</code> placeholder. Replaced with a comma seperated list of the argument types for the
	 * method invocation. Argument types are written as short class names.
	 */
	public static final String ARGUMENT_TYPES_PLACEHOLDER = "${argumentTypes}";

	/**
	 * The <code>${arguments}</code> placeholder. Replaced with a comma separated list of the argument values for the
	 * method invocation. Relies on the <code>toString()</code> method of each argument type.
	 */
	public static final String ARGUMENTS_PLACEHOLDER = "${arguments}";

	/**
	 * The <code>${exception}</code> placeholder. Replaced with the
	 */
	public static final String EXCEPTION_PLACEHOLDER = "${exception}";

	/**
	 * The default message used for writing method entry messages.
	 */
	private static final String DEFAULT_ENTER_MESSAGE = "Entering method [" + METHOD_NAME_PLACEHOLDER + "] in class [" + TARGET_CLASS_NAME_PLACEHOLDER + "].";

	/**
	 * The default message used for writing method exit messages.
	 */
	private static final String DEFAULT_EXIT_MESSAGE = "Exiting method [" + METHOD_NAME_PLACEHOLDER + "] in class [" + TARGET_CLASS_NAME_PLACEHOLDER + "].";

	/**
	 * The default method used for writing exception messages.
	 */
	private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception thrown in method [" + METHOD_NAME_PLACEHOLDER + "] in class [" + TARGET_CLASS_NAME_PLACEHOLDER + "].";

	/**
	 * The <code>Pattern</code> used to match placeholders.
	 */
	private static final Pattern PATTERN = Pattern.compile("\\$\\{\\p{Alpha}+\\}");

	/**
	 * The <code>Set</code> of allowed placeholders.
	 */
	private static final Set ALLOWED_PLACEHOLDERS = new Constants(CustomizableTraceInterceptor.class).getValues("");

	/**
	 * The message for method entry.
	 */
	private String enterMessage = DEFAULT_ENTER_MESSAGE;

	/**
	 * The message for method exit.
	 */
	private String exitMessage = DEFAULT_EXIT_MESSAGE;

	/**
	 * The message for exceptions during method execution.
	 */
	private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;

	/**
	 * Sets the template used for method entry log messages. This template can contain any of the following placeholders:
	 * <ul>
	 * <li><code>${targetClassName}</code></li>
	 * <li><code>${targetClassShortName}</code></li>
	 * <li><code>${argumentTypes}</code></li>
	 * <li><code>${arguments}</code></li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException if the message template is empty or it contains any invalid placeholders.
	 */
	public void setEnterMessage(String enterMessage) throws IllegalArgumentException {
		Assert.hasText(enterMessage, "enterMessage cannot be null or zero length.");
		checkForInvalidPlaceholders(enterMessage);
		assertDoesNotContainsText(enterMessage, RETURN_VALUE_PLACEHOLDER, "enterMessage cannot contain placeholder " + RETURN_VALUE_PLACEHOLDER);
		assertDoesNotContainsText(enterMessage, EXCEPTION_PLACEHOLDER, "enterMessage cannot contain placeholder " + EXCEPTION_PLACEHOLDER);

		this.enterMessage = enterMessage;
	}

	/**
	 * Sets the template used for method exit log messages. This template can contain any of the following placeholders:
	 * <ul>
	 * <li><code>${targetClassName}</code></li>
	 * <li><code>${targetClassShortName}</code></li>
	 * <li><code>${argumentTypes}</code></li>
	 * <li><code>${arguments}</code></li>
	 * <li><code>${returnValue}</code></li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException if the message template is empty or it contains any invalid placeholders.
	 */
	public void setExitMessage(String exitMessage) {
		Assert.hasText(exitMessage, "exitMessage cannot be null or zero length.");
		checkForInvalidPlaceholders(exitMessage);
		assertDoesNotContainsText(exitMessage, EXCEPTION_PLACEHOLDER, "exitMessage cannot contain placeholder " + EXCEPTION_PLACEHOLDER);

		this.exitMessage = exitMessage;
	}

	/**
	 * Sets the template used for method exception log messages. This template can contain any of the following placeholders:
	 * <ul>
	 * <li><code>${targetClassName}</code></li>
	 * <li><code>${targetClassShortName}</code></li>
	 * <li><code>${argumentTypes}</code></li>
	 * <li><code>${arguments}</code></li>
	 * <li><code>${exception}</code></li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException if the message template is empty or it contains any invalid placeholders.
	 */
	public void setExceptionMessage(String exceptionMessage) {
		Assert.hasText(exceptionMessage, "exceptionMessage cannot be null or zero length.");
		checkForInvalidPlaceholders(exceptionMessage);
		assertDoesNotContainsText(exceptionMessage, RETURN_VALUE_PLACEHOLDER, "exceptionMessage cannot contain placeholder " + RETURN_VALUE_PLACEHOLDER);

		this.exceptionMessage = exceptionMessage;
	}

	protected Object invokeUnderTrace(MethodInvocation methodInvocation, Log logger) throws Throwable {
		try {
			logger.trace(replaceTokens(this.enterMessage, methodInvocation, null, null));
			Object returnValue = methodInvocation.proceed();
			logger.trace(replaceTokens(this.exitMessage, methodInvocation, returnValue, null));
			return returnValue;
		}
		catch (Throwable t) {
			logger.trace(replaceTokens(this.exceptionMessage, methodInvocation, null, t));
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
				appendReturnValue(methodInvocation, matcher, output, returnValue);
			}
			else if (EXCEPTION_PLACEHOLDER.equals(match)) {
				matcher.appendReplacement(output, throwable.toString());
			}
			else {
				// should not happen since placeholders are checked earlier.
				throw new IllegalArgumentException("Unknown placeholder [" + match + "]");
			}
		}
		matcher.appendTail(output);

		return output.toString();
	}

	/**
	 * Adds the <code>String</code> representation of the method return value to the supplied <code>StringBuffer</code>.
	 *
	 * @param methodInvocation the <code>MethodInvocation</code> that returned the value.
	 * @param matcher the <code>Matcher</code> containing the matched placeholder.
	 * @param output the <code>StringBuffer</code> to write output to.
	 * @param returnValue the value returned by the method invocation.
	 */
	private void appendReturnValue(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, Object returnValue) {
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

	private void assertDoesNotContainsText(String target, String desiredText, String message) {
		if (target.indexOf(desiredText) > -1) {
			throw new IllegalArgumentException(message);
		}
	}

	private void checkForInvalidPlaceholders(String message) throws IllegalArgumentException {
		Matcher matcher = PATTERN.matcher(message);

		while (matcher.find()) {
			String match = matcher.group();
			if (!ALLOWED_PLACEHOLDERS.contains(match)) {
				throw new IllegalArgumentException("Placeholder " + match + " is not valid.");
			}
		}
	}

}
