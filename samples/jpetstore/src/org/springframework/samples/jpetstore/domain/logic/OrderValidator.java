package org.springframework.samples.jpetstore.domain.logic;

import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Juergen Hoeller
 * @since 01.12.2003
 */
public class OrderValidator extends PetStoreAbstractValidator implements Validator {

	public boolean supports(Class clazz) {
		return Order.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		validateCreditCard((Order) obj, errors);
		validateBillingAddress((Order) obj, errors);
		validateShippingAddress((Order) obj, errors);
	}

	public void validateCreditCard(Order order, Errors errors) {
		rejectIfEmpty(errors, "creditCard", "CCN_REQUIRED", "FAKE (!) credit card number required.");
		rejectIfEmpty(errors, "expiryDate", "EXPIRY_DATE_REQUIRED", "Expiry date is required.");
		rejectIfEmpty(errors, "cardType", "CARD_TYPE_REQUIRED", "Card type is required.");
	}

	public void validateBillingAddress(Order order, Errors errors) {
		rejectIfEmpty(errors, "billToFirstName", "FIRST_NAME_REQUIRED", "Billing Info: first name is required.");
		rejectIfEmpty(errors, "billToLastName", "LAST_NAME_REQUIRED", "Billing Info: last name is required.");
		rejectIfEmpty(errors, "billAddress1", "ADDRESS_REQUIRED", "Billing Info: address is required.");
		rejectIfEmpty(errors, "billCity", "CITY_REQUIRED", "Billing Info: city is required.");
		rejectIfEmpty(errors, "billState", "STATE_REQUIRED", "Billing Info: state is required.");
		rejectIfEmpty(errors, "billZip", "ZIP_REQUIRED", "Billing Info: zip/postal code is required.");
		rejectIfEmpty(errors, "billCountry", "COUNTRY_REQUIRED", "Billing Info: country is required.");
	}

	public void validateShippingAddress(Order order, Errors errors) {
		rejectIfEmpty(errors, "shipToFirstName", "FIRST_NAME_REQUIRED", "Shipping Info: first name is required.");
		rejectIfEmpty(errors, "shipToLastName", "LAST_NAME_REQUIRED", "Shipping Info: last name is required.");
		rejectIfEmpty(errors, "shipAddress1", "ADDRESS_REQUIRED", "Shipping Info: address is required.");
		rejectIfEmpty(errors, "shipCity", "CITY_REQUIRED", "Shipping Info: city is required.");
		rejectIfEmpty(errors, "shipState", "STATE_REQUIRED", "Shipping Info: state is required.");
		rejectIfEmpty(errors, "shipZip", "ZIP_REQUIRED", "Shipping Info: zip/postal code is required.");
		rejectIfEmpty(errors, "shipCountry", "COUNTRY_REQUIRED", "Shipping Info: country is required.");
	}
}
