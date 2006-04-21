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

package org.springframework.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;

/**
 * This utility class offers convenient methods for invoking a Validator
 * and for rejecting empty fields. Empty field checks in Validator
 * implementations can become one-liners.
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @since 06.05.2003
 * @see Validator
 * @see Errors
 */
public abstract class ValidationUtils {

	private static Log logger = LogFactory.getLog(ValidationUtils.class);


	/**
	 * Invoke the given validator for the given object and Errors instance.
	 * @param validator validator to be invoked, or <code>null</code> if no validation
	 * @param obj object to bind the parameters to
	 * @param errors Errors instance that should store the errors
	 */
	public static void invokeValidator(Validator validator, Object obj, Errors errors) {
		if (validator != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking validator [" + validator + "]");
			}
			if (obj != null && !validator.supports(obj.getClass())) {
				throw new IllegalArgumentException("Validator " + validator.getClass() +
						" does not support " + obj.getClass());
			}
			validator.validate(obj, errors);
			if (logger.isDebugEnabled()) {
				if (errors.hasErrors()) {
					logger.debug("Validator found " + errors.getErrorCount() + " errors");
				}
				else {
					logger.debug("Validator found no errors");
				}
			}
		}
	}
	
	/**
	 * Reject the given field with the given error code and message
	 * if the value is empty.
	 * <p>The object to validate does not have to be passed in,
	 * as the Errors instance allows to check field values
	 * (it will usually hold an internal reference to the target object).
	 * @param errors Errors instance to register errors on
	 * @param field the field name to check
	 * @param errorCode error code, interpretable as message key
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode) {
		rejectIfEmpty(errors, field, errorCode, null, null);
	}

	/**
	 * Reject the given field with the given error code and message
	 * if the value is empty.
	 * <p>The object to validate does not have to be passed in,
	 * as the Errors instance allows to check field values
	 * (it will usually hold an internal reference to the target object).
	 * @param errors Errors instance to register errors on
	 * @param field the field name to check
	 * @param errorCode error code, interpretable as message key
	 * @param defaultMessage fallback default message
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode, String defaultMessage) {
		rejectIfEmpty(errors, field, errorCode, null, defaultMessage);
	}

	/**
	 * Reject the given field with the given error code, error arguments
	 * and message if the value is empty.
	 * <p>The object to validate does not have to be passed in,
	 * as the Errors instance allows to check field values
	 * (it will usually hold an internal reference to the target object).
	 * @param errors Errors instance to register errors on
	 * @param field the field name to check
	 * @param errorCode error code, interpretable as message key
	 * @param errorArgs error arguments, for argument binding via MessageFormat
	 * (can be <code>null</code>)
	 * @param defaultMessage fallback default message
	 */
	public static void rejectIfEmpty(
			Errors errors, String field, String errorCode, Object[] errorArgs, String defaultMessage) {

		Object value = errors.getFieldValue(field);
		if (value == null || !StringUtils.hasLength(value.toString())) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}

	/**
	 * Reject the given field with the given error code and message
	 * if the value is empty or just contains whitespace.
	 * <p>The object to validate does not have to be passed in,
	 * as the Errors instance allows to check field values
	 * (it will usually hold an internal reference to the target object).
	 * @param errors Errors instance to register errors on
	 * @param field the field name to check
	 * @param errorCode error code, interpretable as message key
	 */
	public static void rejectIfEmptyOrWhitespace(Errors errors, String field, String errorCode) {
		rejectIfEmptyOrWhitespace(errors, field, errorCode, null, null);
	}

	/**
	 * Reject the given field with the given error code and message
	 * if the value is empty or just contains whitespace.
	 * <p>The object to validate does not have to be passed in,
	 * as the Errors instance allows to check field values
	 * (it will usually hold an internal reference to the target object).
	 * @param errors Errors instance to register errors on
	 * @param field the field name to check
	 * @param errorCode error code, interpretable as message key
	 * @param defaultMessage fallback default message
	 */
	public static void rejectIfEmptyOrWhitespace(
			Errors errors, String field, String errorCode, String defaultMessage) {

		rejectIfEmptyOrWhitespace(errors, field, errorCode, null, defaultMessage);
	}

	/**
	 * Reject the given field with the given error code, error arguments
	 * and message if the value is empty or just contains whitespace.
	 * <p>The object to validate does not have to be passed in,
	 * as the Errors instance allows to check field values
	 * (it will usually hold an internal reference to the target object).
	 * @param errors Errors instance to register errors on
	 * @param field the field name to check
	 * @param errorCode error code, interpretable as message key
	 * @param errorArgs error arguments, for argument binding via MessageFormat
	 * (can be <code>null</code>)
	 * @param defaultMessage fallback default message
	 */
	public static void rejectIfEmptyOrWhitespace(
			Errors errors, String field, String errorCode, Object[] errorArgs, String defaultMessage) {

		Object value = errors.getFieldValue(field);
		if (value == null ||!StringUtils.hasText(value.toString())) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}

}
