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

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.samples.petportal.service.PetService;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * This Controller delegates to the {@link PetService PetService} in
 * order to populate the model with a Pet object for the selected key.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetViewController extends AbstractController {
	
	private PetService petService;
	
	public void setPetService(PetService petService) {
		this.petService = petService;
	}
	
	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		Integer id = new Integer(request.getParameter("pet"));
        return new ModelAndView("petView", "pet", petService.getPet(id));
	}

}
