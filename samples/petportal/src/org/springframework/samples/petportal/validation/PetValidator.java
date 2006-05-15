/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.samples.petportal.validation;

import org.springframework.samples.petportal.domain.Pet;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A validator for {@link Pet Pet} objects.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetValidator implements Validator {

	public boolean supports(Class clazz) {
		return Pet.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		Pet pet = (Pet)obj;
		validateSpecies(pet, errors);
		validateBreed(pet, errors);
		validateName(pet, errors);
		validateBirthdate(pet, errors);
	}
	
	public void validateSpecies(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "species", "SPECIES_REQUIRED", "Species is required.");
	}

	public void validateBreed(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "breed", "BREED_REQUIRED", "Breed is required.");
	}
	
	public void validateName(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "name", "NAME_REQUIRED", "Name is required.");
	}
	
	public void validateBirthdate(Pet pet, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "birthdate", "required.java.util.Date", "Birthdate is required.");
	}
}
