package org.springframework.samples.countries.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * @author Jean-Pierre Pawlak
 */
public class ErrorsController extends MultiActionController {
	// handlers
    
	/**
	 * Custom handler for http404
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleHttp404(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView("errorHttp404View");
	}

}
