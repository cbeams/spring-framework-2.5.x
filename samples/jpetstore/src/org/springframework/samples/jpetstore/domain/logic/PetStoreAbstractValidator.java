package org.springframework.samples.jpetstore.domain.logic;

import org.springframework.validation.Errors;

/**
 * Provides common logic to subclasses
 * @author Dmitriy Kopylenko
 * @version $Id: PetStoreAbstractValidator.java,v 1.1 2003-12-04 20:54:58 dkopylenko Exp $
 */
public abstract class PetStoreAbstractValidator {

	protected void rejectIfEmpty(Errors errors, String field, String errorCode, String defaultMessage) {
		Object fieldValue = errors.getFieldValue(field);
		if (fieldValue == null || fieldValue.toString().length() == 0) {
			errors.rejectValue(field, errorCode, defaultMessage);
		}
	}
}
