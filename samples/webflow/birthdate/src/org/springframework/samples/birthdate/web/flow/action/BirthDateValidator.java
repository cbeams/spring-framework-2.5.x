package org.springframework.samples.birthdate.web.flow.action;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class BirthDateValidator implements Validator {

	public boolean supports(Class clazz) {
		return clazz.equals(BirthDate.class);
	}

	public void validate(Object obj, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "noName", "Please specify your name.");
		ValidationUtils.rejectIfEmpty(errors, "date", "noDate", "Please speicfy your birth date.");
	}

}
