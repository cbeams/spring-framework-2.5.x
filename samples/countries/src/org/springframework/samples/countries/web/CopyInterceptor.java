package org.springframework.samples.countries.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.samples.countries.dao.IDaoCountry;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * An interceptor that tell the views about the possibility of a copy
 * of the countries to a database.
 * @author Jean-Pierre Pawlak
 */
public class CopyInterceptor extends HandlerInterceptorAdapter {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean copyAvailable = false;

	/**
	 * Set <code>copyAvailable</code> to True if the <code>countriesController</code>
	 * has a <code>secondDao</code> declared and if this one is of a DATABASE type.
	 * Otherwise <code>copyAvailable</code> is set to False.
	 */
	public void setCountriesController(CountriesController countriesController) {
		IDaoCountry dao = countriesController.getSecondDaoCountry();
		if (dao != null) {
			this.copyAvailable = IDaoCountry.DATABASE.equals(dao.getType());
		}
		if (this.copyAvailable) {
			logger.info("The countriesController has a valid secondDao. Copy to the database is available.");
		}
		else {
			logger.info("The countriesController has no valid secondDao. Copy to the database is not available.");
		}
	}

	/**
	 * Make the <code>copyAvailable</code> value available to views.
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
												 ModelAndView modelAndView) {
		modelAndView.addObject("copyAvailable", new Boolean(this.copyAvailable));
	}

}
