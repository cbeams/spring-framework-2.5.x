package org.springframework.samples.petclinic.validation;

import org.springframework.samples.petclinic.Visit;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * JavaBean <code>Validator</code> for <code>Visit</code> Forms.
 *
 * @author Ken Krebs
 */
public class VisitValidator implements Validator {

	public boolean supports(Class clazz) {
		return Visit.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		Visit visit = (Visit) obj;
		String description = visit.getDescription();
		if (description == null || "".equals(description)) {
			errors.rejectValue("description", "required", null, "required");
		}
	}

}
