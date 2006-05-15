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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.samples.petportal.domain.PetSite;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

/**
 * This Controller demonstrates a redirect to a website that is external 
 * to the portlet. The 'petsites-portlet' HandlerMapping will map to this 
 * view whenever in VIEW mode. See 'WEB-INF/context/petsites-portlet.xml' 
 * for details.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public class PetSitesRedirectController extends SimpleFormController implements InitializingBean {

	private Properties petSites;
	
	public void afterPropertiesSet() throws Exception {
		this.setRedirectAction(true);
	}
	
	public void setPetSites(Properties petSites) {
		this.petSites = petSites;
	}
	
	protected Map referenceData(PortletRequest request) throws Exception {
		Map data = new HashMap();
		data.put("petSites", petSites);
		return data;
	}

	public void onSubmitAction(ActionRequest request,
			                   ActionResponse response,
			                   Object command,
			                   BindException errors) throws Exception {
		PetSite redirect = (PetSite) command;
		response.sendRedirect(redirect.getUrl().toString());
	}

}
