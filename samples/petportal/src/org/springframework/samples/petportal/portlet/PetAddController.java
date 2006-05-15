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
package org.springframework.samples.petportal.portlet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.samples.petportal.domain.Pet;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.samples.petportal.validation.PetValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.mvc.AbstractWizardFormController;

/**
 * This Controller is a wizard for adding a Pet through a series of steps.
 * It demonstrates validation using the {@link PetValidator PetValidator}.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetAddController extends AbstractWizardFormController {

	private PetService petService;
	
	public void setPetService(PetService petService) {
		this.petService = petService;
	}

	/**
	 * For the page where the 'birthdate' is to be entered, the dateFormat is
	 * provided so that it may be displayed to the user. The format is 
	 * retrieved from the PortletPreferences.
	 */
	protected Map referenceData(PortletRequest request, int page) throws Exception {
		Map data = null;
		if (page == 3) {
			data = new HashMap();
			String dateFormat = request.getPreferences().getValue("dateFormat", PetService.DEFAULT_DATE_FORMAT);
			data.put("dateFormat", dateFormat);
		}
		return data;
	}

	/**
	 * Registers a PropertyEditor with the data binder for handling Dates
	 * using the format as currently specified in the PortletPreferences.
	 */
	protected void initBinder(PortletRequest request, PortletRequestDataBinder binder) throws Exception {
		String formatString = request.getPreferences().getValue("dateFormat", PetService.DEFAULT_DATE_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
		binder.registerCustomEditor(Date.class, null, new CustomDateEditor(dateFormat, true));
		binder.setAllowedFields(new String[] {"species", "breed", "name", "birthdate"});
	}

	protected void processFinish(ActionRequest request,
			                     ActionResponse response,
			                     Object command,
			                     BindException errors)
	  throws Exception {
		petService.addPet((Pet)command);
		response.setRenderParameter("action", "listPets");
	}
	
	protected void processCancel(ActionRequest request,
			                     ActionResponse response,
			                     Object command,
			                     BindException errors)
	  throws Exception {
		response.setRenderParameter("action", "listPets");
	}

	protected void validatePage(Object command, Errors errors, int page, boolean finish) {
		if(finish) {
			this.getValidator().validate(command, errors);
		}
		Pet pet = (Pet) command;
		PetValidator petValidator = (PetValidator) getValidator();
		switch (page) {
			case 0: petValidator.validateSpecies(pet, errors); break;
			case 1: petValidator.validateBreed(pet, errors); break;
			case 2: petValidator.validateName(pet, errors); break;
			case 3: petValidator.validateBirthdate(pet, errors); break;
		}
	}

	protected ModelAndView renderInvalidSubmit(RenderRequest request, RenderResponse response) throws Exception {
		return null;
	}

	protected void handleInvalidSubmit(ActionRequest request, ActionResponse response) throws Exception {
		response.setRenderParameter("action", "listPets");
	}
	
}
