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

package org.springframework.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This utility class offers convenient methods for invoking a validator and
 * for rejecting an empty field. Used by BindUtils' bindAndValidate method.
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @since 06.05.2003
 * @see Validator
 * @see Errors
 * @see org.springframework.web.bind.BindUtils#bindAndValidate
 * @version $Id: ValidationUtils.java,v 1.6 2004-04-19 08:59:15 jhoeller Exp $
 */
public abstract class ValidationUtils {

	private static Log logger = LogFactory.getLog(ValidationUtils.class);

	/**
	 * Invoke the given validator for the given object and Errors instance.
	 * @param validator validator to be invoked, or null if no validation
	 * @param object object to bind the parameters to
	 * @param errors Errors instance that should store the errors
	 */
	public static void invokeValidator(Validator validator, Object object, Errors errors) {
		if (validator != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking validator [" + validator + "]");
			}
			if (!validator.supports(object.getClass())) {
				throw new IllegalArgumentException("Validator " + validator.getClass() +
																					 " does not support " + object.getClass());
			}
			validator.validate(object, errors);
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
	 * @param errors Errors instance containing bound fields
	 * @param field field name to check
	 * @param errorCode to reject with
	 * @param defaultMessage to reject with
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode, String defaultMessage) {
		Object value = errors.getFieldValue(field);
		if (value == null || value.toString().length() == 0) {
			errors.rejectValue(field, errorCode, defaultMessage);
		}
	}

}
