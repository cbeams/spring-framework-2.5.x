package org.springframework.samples.countries.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.samples.countries.dao.IDaoCountry;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * An interceptor that tell the views about the possibility of a copy
 * of the countries to a database.
 * @author Jean-Pierre Pawlak
 */
public class CopyInterceptor extends HandlerInterceptorAdapter implements ApplicationContextAware {

	protected final Log logger = LogFactory.getLog(getClass());

	private Boolean copyAvailable;

	/**
	 * Set <code>copyAvailable</code> to True if the <code>countriesController</code>
	 * has a <code>secondDao</code> declared and if this one is of a DATABASE type.
	 * Otherwise <code>copyAvailable</code> is set to False.
	 */
	public void setApplicationContext(ApplicationContext ctx)
		throws ApplicationContextException {
		Object o = ctx.getBean("countriesController");
		if (null == o) {
			copyAvailable = Boolean.FALSE;
		}
		else {
			BeanWrapper bw = new BeanWrapperImpl(o);
			Object dao;
			try {
				dao = bw.getPropertyValue("secondDaoCountry");
				bw = new BeanWrapperImpl(dao);
				if (IDaoCountry.DATABASE.equals(bw.getPropertyValue("type"))) {
					copyAvailable = Boolean.TRUE;
				}
				else {
					copyAvailable = Boolean.FALSE;
				}
			}
			catch (BeansException e) {
				copyAvailable = Boolean.FALSE;
			}
		}
		if (copyAvailable.booleanValue()) {
			logger.info("The countriesController has a valid secondDao. Copy to the database is available.");
		}
		else {
			logger.info("The countriesController has no valid secondDao. Copy to the database is not available.");
		}
	}

	/**
	 * Makes the <code>copyAvailable</code> value available to views.
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
												 ModelAndView modelAndView) {
		modelAndView.addObject("copyAvailable",copyAvailable);
	}

}
