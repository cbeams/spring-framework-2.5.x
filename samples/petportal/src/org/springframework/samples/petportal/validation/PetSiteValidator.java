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

import org.springframework.samples.petportal.domain.PetSite;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A validator for {@link PetSite PetSite} objects.
 * 
 * @author Mark Fisher
 */
public class PetSiteValidator implements Validator {

	public boolean supports(Class clazz) {
		return PetSite.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors) {
		PetSite petSite = (PetSite)obj;
		validateName(petSite, errors);
		validateUrl(petSite, errors);
	}
	
	public void validateName(PetSite petSite, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "name", "NAME_REQUIRED", "Name is required.");
	}

	public void validateUrl(PetSite petSite, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "url", "URL_REQUIRED", "URL is required.");
	}
	
}
