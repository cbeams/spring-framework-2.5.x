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
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.flow.AttributesAccessor;

/**
 * @author Keith Donald
 * @author Ben Alex
 */
public class BindingActionForm extends ActionForm {
	protected final Log logger = LogFactory.getLog(getClass());

	private Errors errors;

	private Locale locale;

	private MessageResources messageResources;

	private HttpServletRequest httpServletRequest;

	private AttributesAccessor model;

	/** Used to separate elements when detecting most specific error message */
	public static final String CODE_SEPARATOR = ".";

	public BindingActionForm() {
		super();
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
		updateFromHttpServletRequest();
	}

	public void setModel(AttributesAccessor model) {
		this.model = model;
	}

	public ActionErrors bind(HttpServletRequest request, ServletRequestDataBinder binder) {
		binder.bind(request);
		setErrors(binder.getErrors());
		setHttpServletRequest(request);
		assertErrorsSet();
		return getActionErrors();
	}

	private void assertErrorsSet() {
		Assert.notNull(this.errors,
				"The errors instance must be set on this action form in order to access form properties");
	}

	protected Errors getErrors() {
		return errors;
	}

	private void updateFromHttpServletRequest() {
		// Obtain the locale from the Struts well-known location
		this.locale = (Locale)httpServletRequest.getSession().getAttribute(Globals.LOCALE_KEY);

		// Obtain the MessageResources from the Struts well-known location
		this.messageResources = (MessageResources)httpServletRequest.getAttribute(Globals.MESSAGES_KEY);

		Assert.notNull(this.locale, "The locale could not be retrieved");
		Assert.notNull(this.messageResources, "The message resources could not be retrieved");
	}

	public boolean hasErrors() {
		if (getErrors() == null) {
			return false;
		}
		return getErrors().hasErrors();
	}

	public ActionErrors getActionErrors() {
		assertErrorsSet();
		Errors errors = getErrors();
		if (!errors.hasErrors()) {
			return new ActionErrors();
		}
		ActionErrors actionErrors = new ActionErrors();
		Iterator it = errors.getAllErrors().iterator();
		while (it.hasNext()) {
			ObjectError objectError = (ObjectError)it.next();
			if (objectError instanceof FieldError) {
				FieldError fieldError = (FieldError)objectError;
				String effectiveMessageKey = findEffectiveMessageKey(objectError.getCode(),
						objectError.getObjectName(), fieldError.getField());
				actionErrors.add(fieldError.getField(), new ActionMessage(effectiveMessageKey, resolveArgs(fieldError
						.getArguments())));
			}
			else {
				String effectiveMessageKey = findEffectiveMessageKey(objectError.getCode(),
						objectError.getObjectName(), null);
				actionErrors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(effectiveMessageKey,
						resolveArgs(objectError.getArguments())));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Final ActionErrors: " + actionErrors);
		}
		return actionErrors;
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
				for (int j = 0; j < codes.length; j++) {
					String code = codes[j];
					if (messageResources.isPresent(this.locale, code)) {
						arguments[i] = messageResources.getMessage(this.locale, code, resolveArgs(resolvable
								.getArguments()));
						break;
					}
				}
				arguments[i] = resolvable.getDefaultMessage();
			}
		}
		return arguments;
	}

	private String findEffectiveMessageKey(String errorCode, String objectName, String field) {
		Assert.notNull(this.locale, "The locale must be set to enable the most specific error message to be resolved");
		Assert.notNull(this.messageResources,
				"The message resources must be set to enable the most specific error message to be resolved");

		MessageCodesResolver mcr = new DefaultMessageCodesResolver();
		String[] possibleMatches = mcr.resolveMessageCodes(errorCode, objectName);

		if (field != null) {
			possibleMatches = mcr.resolveMessageCodes(errorCode, objectName, field, null);
		}

		for (int i = 0; i < possibleMatches.length; i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("Testing..: " + possibleMatches[i]);
			}
			if (this.messageResources.isPresent(this.locale, possibleMatches[i])) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found....: " + possibleMatches[i]);
				}
				return possibleMatches[i];
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Could not find a suitable message key");
		}
		return objectName + "." + errorCode;
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
				Assert
						.notNull(error, "The field error property should not be null for property '" + propertyPath
								+ "'");
				return error.isBindingFailure();
			}
		}
		return false;
	}

	/**
	 * @param propertyPath
	 * @return
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