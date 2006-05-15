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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.Controller;

/**
 * This is a simple Controller which delegates to the 
 * {@link PetService PetService} and then populates the model with all 
 * returned Pets. This could have extended AbstractController in which 
 * case only the render phase would have required handling. However, 
 * this demonstrates the ability to simply implement the Controller 
 * interface.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetController implements Controller, InitializingBean {

	private PetService petService;
	
	public void setPetService(PetService petService) {
		this.petService = petService;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.petService == null) {
			throw new IllegalArgumentException("A PetService is required");
		}
	}

	public ModelAndView handleRenderRequest(RenderRequest request, RenderResponse response) throws Exception {
		return new ModelAndView("pets", "pets", petService.getAllPets());
	}

	public void handleActionRequest(ActionRequest request, ActionResponse response) throws Exception {
	}
	
}
