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

import java.util.HashSet;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.samples.petportal.service.PetService;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.PortletRequestUtils;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * This controller provides a simple example of modifying portlet preferences.
 * In this case, it allows the user to change the default date format.
 * 
 * @author Mark Fisher
 */
public class DateFormatController extends AbstractController implements InitializingBean {
	
	private Set availableFormats;
	
	public void afterPropertiesSet() throws Exception {
		availableFormats = new HashSet();
		availableFormats.add(PetService.DEFAULT_DATE_FORMAT);
		availableFormats.add("MM-dd-yyyy");
		availableFormats.add("dd/MM/yyyy");
		availableFormats.add("dd-MM-yyyy");
	}
	
	/**
	 * In the action phase, the dateFormat preference is modified. To persist any
	 * modifications, the PortletPreferences must be stored.
	 */
	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		String dateFormat = PortletRequestUtils.getStringParameter(request, "dateFormat");
		if (dateFormat != null) {
			PortletPreferences preferences = request.getPreferences();
			preferences.setValue("dateFormat", dateFormat);
			preferences.store();
		}
	}
	
	/**
	 * In the render phase, the current format and available formats will be 
	 * exposed to the 'dateFormat' view via the model.
	 */
	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("dateFormat");
		mav.addObject("currentFormat", request.getPreferences().getValue("dateFormat", PetService.DEFAULT_DATE_FORMAT));
		mav.addObject("availableFormats", availableFormats);
		return mav;
	}

}
