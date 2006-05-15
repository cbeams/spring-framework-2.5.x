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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.samples.petportal.domain.InvalidFileTypeException;
import org.springframework.samples.petportal.domain.Pet;
import org.springframework.samples.petportal.domain.PetDescription;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.bind.PortletRequestUtils;
import org.springframework.web.portlet.multipart.MultipartActionRequest;
import org.springframework.web.portlet.mvc.SimpleFormController;

/**
 * This Controller demonstrates multipart file uploads. In this case, 
 * an uploaded text file will be used as the description for a Pet.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetDescriptionUploadController extends SimpleFormController {
	
	private PetService petService;
	
	public void setPetService(PetService petService) {
		this.petService = petService;
	}
	
	protected Map referenceData(PortletRequest request) throws Exception {
		Map data = new HashMap();
		data.put("pets", (Collection) petService.getAllPets());
		return data;
	}
	
	/**
	 * register the PropertyEditor for converting from a MultipartFile to an array of bytes.
	 */
	protected void initBinder(PortletRequest request, PortletRequestDataBinder binder) throws Exception {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

	/**
	 * If there are no Pets, display the 'noPetsForUpload' view. 
	 * Otherwise show the upload form.
	 */
	protected ModelAndView showForm(RenderRequest request, RenderResponse response, BindException errors, Map model) throws Exception {
		ModelAndView mav = super.showForm(request, response, errors, model); 
		Collection pets = (Collection) mav.getModel().get("pets");
		if (pets.size() == 0) {
			mav = new ModelAndView("noPetsForUpload");
		}
		return mav;
	}

	/**
	 * On submit, set the description property for the selected Pet as a String.
	 * @throws PortletException when the file is not the correct content type.
	 */
	public void onSubmitAction(ActionRequest request, 
			                   ActionResponse response,
			                   Object command,
			                   BindException errors) throws Exception {
		if (request instanceof MultipartActionRequest) {
			MultipartActionRequest multipartRequest = (MultipartActionRequest) request;
			MultipartFile multipartFile = multipartRequest.getFile("file");
			if (multipartFile != null) {
				logger.info("isEmpty: " + multipartFile.isEmpty());
				logger.info("contentType: " + multipartFile.getContentType());
				logger.info("name: " + multipartFile.getName());
				logger.info("size: " + multipartFile.getSize());
				logger.info("orginalFilename: " + multipartFile.getOriginalFilename());
				if (!"text/plain".equals(multipartFile.getContentType())) {
					throw new InvalidFileTypeException("File is of type '" +
							multipartFile.getContentType() +
							"', not 'text/plain'");
				}
				logger.info("content: " + multipartFile.getBytes().toString());
			}
			else {
				logger.info("MultipartFile returned NULL");
			}
		}
		PetDescription upload = (PetDescription) command;
		if (upload != null) {
			byte[] file = upload.getFile();
			if (file != null) {
				String description = new String(file);
				int petKey = PortletRequestUtils.getRequiredIntParameter(request, "selectedPet");
				Pet pet = petService.getPet(petKey);
				pet.setDescription(description);
				petService.savePet(pet);
			}
		}
	}

	protected ModelAndView onSubmitRender(RenderRequest request, 
			                              RenderResponse response,
			                              Object command,
			                              BindException errors) throws Exception {
		return this.showNewForm(request, response);
	}

}
