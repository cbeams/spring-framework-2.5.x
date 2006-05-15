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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * This controller delegates to the {@link PetService PetService}
 * to delete a Pet in its action phase.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetDeleteController extends AbstractController implements InitializingBean {

	private PetService petService;
	
	public void setPetService(PetService petService) {
		this.petService = petService;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.petService == null) {
			throw new IllegalArgumentException("A PetService is required");
		}
	}

	public void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		Integer id = new Integer(request.getParameter("pet"));
		petService.deletePet(id);
		response.setRenderParameter("action", "pets");
	}

}
