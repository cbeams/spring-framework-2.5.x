package org.springframework.validation;

/**
 * Strategy interface for building message codes from validation error codes.
 * Used by BindException/DataBinder to build the codes list for ObjectErrors
 * and FieldErrors.
 *
 * <p>The resulting message codes correspond to the codes of a
 * MessageSourceResolvable (as implemented by ObjectError and FieldError).
 *
 * @author Juergen Hoeller
 * @since 27.03.2004
 * @see ObjectError
 * @see FieldError
 * @see org.springframework.context.MessageSourceResolvable#getCodes
 */
public interface MessageCodesResolver {

	/**
	 * Build message codes for the given error code and object name.
	 * Used for building the codes list of an ObjectError.
	 * @param errorCode the error code used for rejecting the object
	 * @param objectName the name of the object
	 * @return the message codes to use
	 */
	String[] resolveMessageCodes(String errorCode, String objectName);

	/**
	 * Build message codes for the given error code and field specification.
	 * Used for building the codes list of an FieldError.
	 * @param errorCode the error code used for rejecting the value
	 * @param objectName the name of the object
	 * @param field the field name
	 * @param fieldType the field type (may be null if not determinable)
	 * @return the message codes to use
	 */
	String[] resolveMessageCodes(String errorCode, String objectName, String field, Class fieldType);

}
