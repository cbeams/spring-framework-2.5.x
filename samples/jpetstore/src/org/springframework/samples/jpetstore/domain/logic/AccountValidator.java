package org.springframework.samples.jpetstore.domain.logic;

import org.springframework.samples.jpetstore.domain.Account;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Juergen Hoeller
 * @since 01.12.2003
 */
public class AccountValidator extends PetStoreAbstractValidator implements Validator {

	public boolean supports(Class clazz) {
		return Account.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		rejectIfEmpty(errors, "firstName", "FIRST_NAME_REQUIRED", "First name is required.");
		rejectIfEmpty(errors, "lastName", "LAST_NAME_REQUIRED", "Last name is required.");
		rejectIfEmpty(errors, "email", "EMAIL_REQUIRED", "Email address is required.");
		rejectIfEmpty(errors, "phone", "PHONE_REQUIRED", "Phone number is required.");
		rejectIfEmpty(errors, "address1", "ADDRESS_REQUIRED", "Address (1) is required.");
		rejectIfEmpty(errors, "city", "CITY_REQUIRED", "City is required.");
		rejectIfEmpty(errors, "state", "STATE_REQUIRED", "State is required.");
		rejectIfEmpty(errors, "zip", "ZIP_REQUIRED", "ZIP is required.");
		rejectIfEmpty(errors, "country", "COUNTRY_REQUIRED", "Country is required.");
	}
}
