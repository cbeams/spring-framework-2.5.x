/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.servlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * Simple extension of javax.servlet.http.HttpServlet that treats its config
 * parameters as bean properties. A very handy superclass for any type of servlet.
 * Type conversion is automatic. It is also possible for subclasses to specify
 * required properties.
 *
 * <p>This servlet leaves request handling to subclasses, inheriting the
 * default behaviour of HttpServlet.
 *
 * <p>This servlet superclass has no dependency on a Spring application context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #addRequiredProperty
 * @see #initServletBean
 */
public abstract class HttpServletBean extends HttpServlet {
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** 
	 * Set of required properties (Strings) that must be supplied as
	 * config parameters to this servlet.
	 */
	private final Set requiredProperties = new HashSet();

	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter.
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
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
			ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
			bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
			initBeanWrapper(bw);
			bw.setPropertyValues(pvs);
		}
		catch (BeansException ex) {
			logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
			throw ex;
		}

		// let subclasses do whatever initialization they like
		initServletBean();
		logger.info("Servlet '" + getServletName() + "' configured successfully");
	}
	
	/**
	 * Initialize the BeanWrapper for this HttpServletBean,
	 * possibly with custom editors.
	 * @param bw the BeanWrapper to initialize
	 * @throws BeansException if thrown by BeanWrapper methods
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}

	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this servlet will have been set before this
	 * method is invoked. This default implementation does nothing.
	 * @throws ServletException if subclass initialization fails
	 */
	protected void initServletBean() throws ServletException {
	}


	/**
	 * PropertyValues implementation created from ServletConfig init parameters.
	 */
	private static class ServletConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new ServletConfigPropertyValues.
		 * @param config ServletConfig we'll use to take PropertyValues from
		 * @param requiredProperties set of property names we need, where
		 * we can't accept default values
		 * @throws ServletException if any required properties are missing
		 */
		private ServletConfigPropertyValues(ServletConfig config, Set requiredProperties) throws ServletException {
			Set missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
					new HashSet(requiredProperties) : null;

			Enumeration enum = config.getInitParameterNames();
			while (enum.hasMoreElements()) {
				String property = (String) enum.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// fail if we are still missing properties
			if (missingProps != null && missingProps.size() > 0) {
				throw new ServletException("Initialization from ServletConfig for servlet '" + config.getServletName() +
																	 "' failed; the following required properties were missing: " +
																	 StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
