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

package org.springframework.web.servlet.support;

import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.util.HtmlUtils;

/**
 * Simple adapter to expose the bind status of a field or object.
 * Set as a variable both by the JSP bind tag and Velocity/FreeMarker macros.
 *
 * <p>Obviously, object status representations (i.e. errors at the object level
 * rather than the field level) do not have an expression and a value but only
 * error codes and messages. For simplicity's sake and to be able to use the same
 * tags and macros, the same status class is used for both scenarios.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Darren Davison
 * @see RequestContext#getBindStatus
 * @see org.springframework.web.servlet.tags.BindTag
 */
public class BindStatus {

	protected final Log logger = LogFactory.getLog(getClass());

	private final RequestContext requestContext;

	private final String path;

	private final boolean htmlEscape;

	private final String expression;

	private Object value;

	private final String[] errorCodes;

	private final String[] errorMessages;

	private final Errors errors;

	private PropertyEditor editor;


	/**
	 * Create a new BindStatus instance, representing a field or object status.
	 * @param requestContext the current RequestContext
	 * @param path the bean and property path for which values and errors
	 * will be resolved (e.g. "customer.address.street")
	 * @param htmlEscape whether to HTML-escape error messages and string values
	 * @throws IllegalStateException if no corresponding Errors object found
	 */
	public BindStatus(RequestContext requestContext, String path, boolean htmlEscape)
			throws IllegalStateException {

		this.requestContext = requestContext;
		this.path = path;
		this.htmlEscape = htmlEscape;

		// determine name of the object and property
		String beanName = null;
		int dotPos = path.indexOf('.');
		if (dotPos == -1) {
			// property not set, only the object itself
			beanName = path;
			this.expression = null;
		}
		else {
			beanName = path.substring(0, dotPos);
			this.expression = path.substring(dotPos + 1);
		}

		this.errors = requestContext.getErrors(beanName, false);

		if (this.errors != null) {
			// Usual case: An Errors instance is available as request attribute.
			// Can determine error codes and messages for the given expression.
			// Can use a custom PropertyEditor, as registered by a form controller.

			List objectErrors = null;

			if (this.expression != null) {
				if ("*".equals(this.expression)) {
					objectErrors = this.errors.getAllErrors();
				}
				else if (this.expression.endsWith("*")) {
					objectErrors = this.errors.getFieldErrors(this.expression);
				}
				else {
					objectErrors = this.errors.getFieldErrors(this.expression);
					this.value = this.errors.getFieldValue(this.expression);
					if (this.errors instanceof BindException) {
						this.editor = ((BindException) this.errors).getCustomEditor(this.expression);
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Cannot not expose custom property editor because Errors instance [" +
									this.errors + "] is not of type BindException");
						}
					}
					if (htmlEscape && this.value instanceof String) {
						this.value = HtmlUtils.htmlEscape((String) this.value);
					}
				}
			}
			else {
				objectErrors = this.errors.getGlobalErrors();
			}

			this.errorCodes = getErrorCodes(objectErrors);
			this.errorMessages = getErrorMessages(objectErrors);
		}

		else {
			// No Errors instance available as request attribute:
			// Probably forwarded directly to a form view.
			// Let's do the best we can: extract a plain value if appropriate.

			Object target = requestContext.getRequest().getAttribute(beanName);
			if (target == null) {
				throw new IllegalStateException("Neither Errors instance nor plain target object for bean name " +
						beanName + " available as request attribute");
			}

			if (this.expression != null && !"*".equals(this.expression) && !this.expression.endsWith("*")) {
				BeanWrapperImpl bw = new BeanWrapperImpl(target);
				this.value = bw.getPropertyValue(this.expression);
			}

			this.errorCodes = new String[0];
			this.errorMessages = new String[0];
		}
	}

	/**
	 * Extract the error codes from the given ObjectError list.
	 */
	private String[] getErrorCodes(List objectErrors) {
		String[] codes = new String[objectErrors.size()];
		for (int i = 0; i < objectErrors.size(); i++) {
			ObjectError error = (ObjectError) objectErrors.get(i);
			codes[i] = error.getCode();
		}
		return codes;
	}

	/**
	 * Extract the error messages from the given ObjectError list.
	 */
	private String[] getErrorMessages(List objectErrors) throws NoSuchMessageException {
		String[] messages = new String[objectErrors.size()];
		for (int i = 0; i < objectErrors.size(); i++) {
			ObjectError error = (ObjectError) objectErrors.get(i);
			messages[i] = this.requestContext.getMessage(error, this.htmlEscape);
		}
		return messages;
	}


	/**
	 * Return the bean and property path for which values and errors
	 * will be resolved (e.g. "customer.address.street").
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return a bind expression that can be used in HTML forms as input name
	 * for the respective field, or null if not field-specific.
	 * <p>Returns a bind path appropriate for resubmission, e.g. "address.street".
	 * Note that the complete bind path as required by the bind tag is
	 * "customer.address.street", if bound to a "customer" bean.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Return the current value of the field, i.e. either the property value
	 * or a rejected update, or null if not field-specific.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Return a suitable display value for the field, i.e. empty string
	 * instead of a null value, or null if not field-specific.
	 */
	public String getDisplayValue() {
		return (this.value != null) ? this.value.toString() : "";
	}

	/**
	 * Return if this status represents a field or object error.
	 */
	public boolean isError() {
		return (this.errorCodes != null && this.errorCodes.length > 0);
	}

	/**
	 * Return the error codes for the field or object, if any.
	 * Returns an empty array instead of null if none.
	 */
	public String[] getErrorCodes() {
		return errorCodes;
	}

	/**
	 * Return the first error codes for the field or object, if any.
	 */
	public String getErrorCode() {
		return (this.errorCodes.length > 0 ? this.errorCodes[0] : "");
	}

	/**
	 * Return the resolved error messages for the field or object,
	 * if any. Returns an empty array instead of null if none.
	 */
	public String[] getErrorMessages() {
		return errorMessages;
	}

	/**
	 * Return the first error message for the field or object, if any.
	 */
	public String getErrorMessage() {
		return (this.errorMessages.length > 0 ? this.errorMessages[0] : "");
	}

	/**
	 * Return an error message string, concatenating all messages
	 * separated by the given delimiter.
	 * @param delimiter separator string, e.g. ", " or "<br>"
	 * @return the error message string
	 */
	public String getErrorMessagesAsString(String delimiter) {
		return StringUtils.arrayToDelimitedString(this.errorMessages, delimiter);
	}

	/**
	 * Return the Errors instance that this bind status is currently bound to.
	 * @return the current Errors instance, or null if none
	 */
	public Errors getErrors() {
		return errors;
	}

	/**
	 * Return the PropertyEditor for the property that this bind status
	 * is currently bound to.
	 * @return the current PropertyEditor, or null if none
	 */
	public PropertyEditor getEditor() {
		return editor;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer("BindStatus: ");
		sb.append("expression=[").append(this.expression).append("]; ");
		sb.append("value=[").append(this.value).append("]");
		if (isError()) {
			sb.append("; errorCodes='" + Arrays.asList(this.errorCodes) + "'; ");
			sb.append("errorMessages='" + Arrays.asList(this.errorMessages));
		}
		return sb.toString();
	}

}
