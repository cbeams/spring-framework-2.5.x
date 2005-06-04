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
package org.springframework.web.struts;

import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * A thin Struts action form adapter that delegates to Spring's more complete and advanced
 * data binder and bind exception object underneath the covers to bind to POJOs
 * and manage rejected values.
 * <p>
 * Also provides hierarchical message resolution, which is traditionally not
 * supported in struts-based apps.
 * <p>
 * Note this Action form is designed explicitly for use in <code>request scope</code>
 * 
 * @author Keith Donald
 * @author Ben Alex
 */
public class BindingActionForm extends ActionForm {
	protected final Log logger = LogFactory.getLog(getClass());

	/** Used to separate elements when detecting most specific error message */
	public static final String CODE_SEPARATOR = ".";

	private Errors errors;

	private Locale locale;

	private MessageResources messageResources;

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

	public void setRequest(HttpServletRequest request) {
		// Obtain the locale from the Struts well-known location
		this.locale = (Locale)request.getSession().getAttribute(Globals.LOCALE_KEY);

		// Obtain the MessageResources from the Struts well-known location
		this.messageResources = (MessageResources)request.getAttribute(Globals.MESSAGES_KEY);
	}

	protected Errors getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		if (getErrors() == null) {
			return false;
		}
		return getErrors().hasErrors();
	}

	public ActionErrors getActionErrors() {
		assertErrorsSet();
		ActionErrors actionErrors = new ActionErrors();
		if (!hasErrors()) {
			return actionErrors;
		}
		Errors errors = getErrors();
		Iterator it = errors.getAllErrors().iterator();
		while (it.hasNext()) {
			ObjectError objectError = (ObjectError)it.next();
			if (objectError instanceof FieldError) {
				FieldError fieldError = (FieldError)objectError;
				String effectiveMessageKey = findEffectiveMessageKey(objectError, objectError.getObjectName(),
						fieldError.getField());
				actionErrors.add(fieldError.getField(), new ActionMessage(effectiveMessageKey, resolveArgs(fieldError
						.getArguments())));
			}
			else {
				String effectiveMessageKey = findEffectiveMessageKey(objectError, objectError.getObjectName(), null);
				actionErrors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(effectiveMessageKey,
						resolveArgs(objectError.getArguments())));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Final ActionErrors: " + actionErrors);
		}
		return actionErrors;
	}

	private void assertErrorsSet() {
		Assert.notNull(this.errors,
				"The errors instance must be set on this BindingActionForm in order to access form properties, however it is null.");
	}

	private Object[] resolveArgs(Object[] arguments) {
		if (arguments == null || arguments.length == 0) {
			return arguments;
		}
		for (int i = 0; i < arguments.length; i++) {
			Object arg = arguments[i];
			if (arg instanceof MessageSourceResolvable) {
				MessageSourceResolvable resolvable = (MessageSourceResolvable)arg;
				String[] codes = resolvable.getCodes();
				boolean resolved = false;
				if (messageResources != null) {
					for (int j = 0; j < codes.length; j++) {
						String code = codes[j];
						if (messageResources.isPresent(this.locale, code)) {
							arguments[i] = messageResources.getMessage(this.locale, code, resolveArgs(resolvable
									.getArguments()));
							resolved = true;
							break;
						}
					}
				}
				if (!resolved) {
					arguments[i] = resolvable.getDefaultMessage();
				}
			}
		}
		return arguments;
	}

	private String findEffectiveMessageKey(ObjectError error, String objectName, String field) {
		if (messageResources != null) {
			String[] possibleMatches = error.getCodes();
			for (int i = 0; i < possibleMatches.length; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Testing error code '" + possibleMatches[i] + "'");
				}
				if (this.messageResources.isPresent(this.locale, possibleMatches[i])) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found error code '" + possibleMatches[i] + "' in resource bundle!");
					}
					return possibleMatches[i];
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Could not find a suitable message error code, returning default message");
		}
		return error.getDefaultMessage();
	}

	/**
	 * Does the property at the provided path have a type conversion error?
	 * @param propertyPath The property path
	 * @return true if yes, false otherwise.
	 */
	public boolean hasTypeConversionError(String propertyPath) {
		assertErrorsSet();
		if (getErrors() != null) {
			Errors errors = getErrors();
			if (errors.hasFieldErrors(propertyPath)) {
				FieldError error = errors.getFieldError(propertyPath);
				return error.isBindingFailure();
			}
		}
		return false;
	}

	/**
	 * Returns the rejected value for the specified bean property path
	 * @param propertyPath
	 * @return the rejected value
	 */
	public Object getRejectedValue(String propertyPath) {
		assertErrorsSet();
		if (getErrors() != null) {
			Errors errors = getErrors();
			if (errors.hasFieldErrors(propertyPath)) {
				FieldError error = errors.getFieldError(propertyPath);
				return error.getRejectedValue();
			}
		}
		return null;
	}

	/**
	 * Get the formatted value for the property at the provided path. The
	 * formatted value is a string value for display, converted via a registered
	 * property editor.
	 * @param propertyPath The property path
	 * @return The formatted property value
	 */
	public Object getFieldValue(String propertyPath) {
		assertErrorsSet();
		return getErrors().getFieldValue(propertyPath);
	}

	/**
	 * Return a suitable display value for the field, i.e. empty string instead
	 * of a null value, or null if not field-specific.
	 */
	protected String getDefaultFormattedValue(Object value) {
		return (value != null) ? value.toString() : "";
	}
}