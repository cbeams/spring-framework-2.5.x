/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. However, please
 * acknowledge the source and include the above URL in each
 * class using or derived from this code. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.web.servlet;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.BeansException;

/**
 * Simple extension of javax.servlet.http.HttpServlet that treats its config
 * parameters as bean properties. A very handy superclass for any type of servlet.
  * Type conversion is automatic. It is also
 * possible for subclasses to specify required properties. This servlet leaves
 * request handling to subclasses, inheriting the default behaviour of HttpServlet.
 * <p/>This servlet superclass has no dependency on the application context.
 * However, it does use Java 1.4 logging emulation, which must have been
 * configured by another component.
 * @author Rod Johnson
 * @version $Revision: 1.2 $
 */
public class HttpServletBean extends HttpServlet {
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** 
	 * May be null. List of required properties (Strings) that must
	 * be supplied as config parameters to this servlet.
	 */
	private List requiredProperties = new LinkedList();
	
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
	 * @throws ServletException if bean properties are invalid (or required properties
	 * are missing), or if subclass initialization fails.
	 */
	public final void init() throws ServletException {
		logger.info("Initializing servlet '" + getServletName() + "' ");

		// Set bean properties
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

		// Let subclasses do whatever initialization they like
		initServletBean();
		logger.info("Servlet '" + getServletName() + "' configured successfully");
	}
	
	/**
	 * Subclasses may override this to perform custom initialization.
	 *  All bean properties of this servlet will have been set before this
	 * method is invoked. This default implementation does nothing.
	 * @throws ServletException if subclass initialization fails
	 */
	protected void initServletBean() throws ServletException {
	}
	
}
