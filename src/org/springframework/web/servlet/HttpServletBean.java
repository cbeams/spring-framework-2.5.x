/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

/**
 * Simple extension of javax.servlet.http.HttpServlet that treats its config
 * parameters as bean properties. A very handy superclass for any type of servlet.
 * Type conversion is automatic. It is also possible for subclasses to specify
 * required properties.
 *
 * <p>This servlet leaves request handling to subclasses, inheriting the
 * default behaviour of HttpServlet.
 *
 * <p>This servlet superclass has no dependency on the application context.
 *
 * @author Rod Johnson
 * @see #initServletBean
 */
public abstract class HttpServletBean extends HttpServlet {
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** 
	 * May be null. List of required properties (Strings) that must
	 * be supplied as config parameters to this servlet.
	 */
	private List requiredProperties = new ArrayList();

	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter.
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		requiredProperties.add(property);
	}
	
	/**
	 * Map config parameters onto bean properties of this servlet, and
	 * invoke subclass initialization.
	 * @throws ServletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 */
	public final void init() throws ServletException {
		logger.info("Initializing servlet '" + getServletName() + "'");

		// set bean properties
		try {
			PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
			BeanWrapper bw = new BeanWrapperImpl(this);
			bw.setPropertyValues(pvs);
		}
		catch (BeansException ex) {
			String msg = "Failed to set bean properties on servlet '" + getServletName() + "': " + ex.getMessage();
			logger.error(msg, ex);
			throw new ServletException(msg, ex);
		}

		// let subclasses do whatever initialization they like
		initServletBean();
		logger.info("Servlet '" + getServletName() + "' configured successfully");
	}
	
	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this servlet will have been set before this
	 * method is invoked. This default implementation does nothing.
	 * @throws ServletException if subclass initialization fails
	 */
	protected void initServletBean() throws ServletException {
	}
	
}
