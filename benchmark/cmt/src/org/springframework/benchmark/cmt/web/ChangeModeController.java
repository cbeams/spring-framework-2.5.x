/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * 
 * @author Rod Johnson
 */
public class ChangeModeController implements Controller {
	
	private Config config;
	
	public ChangeModeController(Config config) {
		this.config = config;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String mode = request.getParameter("mode");
		if (mode == null) {
			// Just display it
			response.getOutputStream().println("Mode is '" + config.getMode() + "'");
		}
		else {
			// Change it
			String oldMode = config.getMode();
			config.setMode(mode);
			response.getOutputStream().println("Changed mode from '" + oldMode + "' to '" + mode + "'");
		}
		response.getOutputStream().flush();
		return null;
	}

}
