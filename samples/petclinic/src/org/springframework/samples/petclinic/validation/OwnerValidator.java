package org.springframework.samples.petclinic.validation;

import org.springframework.samples.petclinic.Owner;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * JavaBean <code>Validator</code> for <code>Owner</code> Forms.
 *
 * @author Ken Krebs
 */
public class OwnerValidator implements Validator {

	public boolean supports(Class clazz) {
		return Owner.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		Owner owner = (Owner) obj;
		String firstName = owner.getFirstName();
		if (firstName == null || "".equals(firstName)) {
			errors.rejectValue("firstName", "required", null, "required");
		}
		String lastName = owner.getLastName();
		if (lastName == null || "".equals(lastName)) {
			errors.rejectValue("lastName", "required", null, "required");
		}
		String address = owner.getAddress();
		if (address == null || "".equals(address)) {
			errors.rejectValue("address", "required", null, "required");
		}
		String city = owner.getCity();
		if (city == null || "".equals(city)) {
			errors.rejectValue("city", "required", null, "required");
		}
		String telephone = owner.getTelephone();
		if (telephone == null || "".equals(telephone)) {
			errors.rejectValue("telephone", "required", null, "required");
			return;
		}
		for (int i = 0; i < telephone.length(); ++i) {
			if ((Character.isDigit(telephone.charAt(i))) == false) {
				errors.rejectValue("telephone", "nonNumeric", null, "non-numeric");
				break;
			}
		}
	}

}
