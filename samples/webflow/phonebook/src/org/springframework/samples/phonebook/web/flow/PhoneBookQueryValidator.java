package org.springframework.samples.phonebook.web.flow;

import org.springframework.samples.phonebook.domain.PhoneBookQuery;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PhoneBookQueryValidator implements Validator {

	public boolean supports(Class clazz) {
		return clazz.equals(PhoneBookQuery.class);
	}

	public void validate(Object obj, Errors errors) {
		PhoneBookQuery query = (PhoneBookQuery)obj;

		if ((query.getFirstName() == null || query.getFirstName().length() == 0)
				&& (query.getLastName() == null || query.getLastName().length() == 0)) {
			errors.reject("noCriteria", "Please provide some query criteria!");
		}
	}
}