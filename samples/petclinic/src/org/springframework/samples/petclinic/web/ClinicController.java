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

package org.springframework.samples.petclinic.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Ken Krebs
 */
public class ClinicController extends MultiActionController implements InitializingBean {

	private Clinic clinic;


	public void setClinic(Clinic clinic) {
		this.clinic = clinic;
	}

	public void afterPropertiesSet() {
		if (this.clinic == null) {
			throw new IllegalArgumentException("Must set clinic bean property on " + getClass());
		}
	}


	/**
	 * Custom handler for welcome
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView welcomeHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView();
	}

	/**
	 * Custom handler for vets display
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView vetsHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView().addObject("vets", this.clinic.getVets());
	}

	/**
	 * Custom handler for owner display
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView ownerHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		Owner owner = this.clinic.loadOwner(ServletRequestUtils.getIntParameter(request, "ownerId", 0));
		if (owner == null) {
			return new ModelAndView("findOwnersRedirect");
		}
		return new ModelAndView(owner);
	}
}
