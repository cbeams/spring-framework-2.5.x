package org.springframework.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class offers a convenient validate method for invoking a validator.
 * Used by BindUtils' bindAndValidate method.
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @since 06.05.2003
 * @see Validator
 * @see Errors
 * @see org.springframework.web.bind.BindUtils#bindAndValidate
 * @version $Id: ValidationUtils.java,v 1.3 2003-12-05 02:27:13 dkopylenko Exp $
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
			logger.debug("Invoking validator [" + validator + "]");
			if (!validator.supports(object.getClass()))
				throw new IllegalArgumentException("Validator " + validator.getClass() + " does not support " + object.getClass());
			validator.validate(object, errors);
			if (errors.hasErrors())
				logger.debug("Validator found " + errors.getErrorCount() + " errors");
			else
				logger.debug("Validator found no errors");
		}
	}
	
	/**
	 * Reject field if the value is empty
	 * @param errors Errors instance containing bound fields
	 * @param field field name to check
	 * @param errorCode to reject with
	 * @param defaultMessage to reject with
	 */
	public static void rejectIfEmpty(Errors errors, String field, String errorCode, String defaultMessage) {
		Object fieldValue = errors.getFieldValue(field);
		if (fieldValue == null || fieldValue.toString().length() == 0) {
			errors.rejectValue(field, errorCode, defaultMessage);
		}
	}
}
