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

package org.springframework.web.filter;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * Simple base implementation of javax.servlet.Filter that treats its config
 * parameters as bean properties. A very handy superclass for any type of filter.
 * Type conversion is automatic. It is also possible for subclasses to specify
 * required properties.
 *
 * <p>This filter leaves actual filtering to subclasses.
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see #initFilterBean
 */
public abstract class GenericFilterBean implements Filter {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Set of required properties (Strings) that must be supplied as
	 * config parameters to this filter.
	 */
	private final Set requiredProperties = new HashSet();

	/* The FilterConfig of this filter */
	private FilterConfig filterConfig;


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
	 * Alternative way of initializing this filter.
	 * Used by Servlet Filter version that shipped with WebLogic 6.1.
	 * @param filterConfig the configuration for this filter
	 * @throws FatalBeanException wrapping a ServletException
	 * thrown by the init method
	 * @see #init
	 */
	public final void setFilterConfig(FilterConfig filterConfig) {
		try {
			init(filterConfig);
		}
		catch (ServletException ex) {
			throw new FatalBeanException("Couldn't initialize filter bean", ex);
		}
	}

	/**
	 * Map config parameters onto bean properties of this filter, and
	 * invoke subclass initialization.
	 * @param filterConfig the configuration for this filter
	 * @throws ServletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 * @see #initFilterBean
	 */
	public final void init(FilterConfig filterConfig) throws ServletException {
		logger.info("Initializing filter '" + filterConfig.getFilterName() + "'");
		this.filterConfig = filterConfig;

		// set bean properties
		try {
			PropertyValues pvs = new FilterConfigPropertyValues(filterConfig, this.requiredProperties);
			BeanWrapper bw = new BeanWrapperImpl(this);
			ResourceLoader resourceLoader = new ServletContextResourceLoader(filterConfig.getServletContext());
			bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
			initBeanWrapper(bw);
			bw.setPropertyValues(pvs);
		}
		catch (BeansException ex) {
			String msg = "Failed to set bean properties on filter '" + filterConfig.getFilterName() + "': " + ex.getMessage();
			logger.error(msg, ex);
			throw new ServletException(msg, ex);
		}

		// let subclasses do whatever initialization they like
		initFilterBean();
		logger.info("Filter '" + filterConfig.getFilterName() + "' configured successfully");
	}

	/**
	 * Initialize the BeanWrapper for this GenericFilterBean,
	 * possibly with custom editors.
	 * @param bw the BeanWrapper to initialize
	 * @throws BeansException if thrown by BeanWrapper methods
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}

	/**
	 * Make the FilterConfig of this filter available.
	 * Analogous to GenericServlet's getServletConfig.
	 * <p>Public to resemble the getFilterConfig method of the
	 * Servlet Filter version that shipped with WebLogic 6.1.
	 * @see javax.servlet.GenericServlet#getServletConfig
	 */
	public final FilterConfig getFilterConfig() {
		return this.filterConfig;
	}

	/**
	 * Make the name of this filter available to subclasses.
	 * Analogous to GenericServlet's getServletName.
	 * @see javax.servlet.GenericServlet#getServletName
	 */
	protected final String getFilterName() {
		return this.filterConfig.getFilterName();
	}

	/**
	 * Make the ServletContext of this filter available to subclasses.
	 * Analogous to GenericServlet's getServletContext.
	 * @see javax.servlet.GenericServlet#getServletContext
	 */
	protected final ServletContext getServletContext() {
		return this.filterConfig.getServletContext();
	}

	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this filter will have been set before this
	 * method is invoked. This default implementation does nothing.
	 * @throws ServletException if subclass initialization fails
	 */
	protected void initFilterBean() throws ServletException {
	}

	/**
	 * Subclasses may override this to perform custom filter shutdown.
	 * This default implementation does nothing.
	 */
	public void destroy() {
	}


	/**
	 * PropertyValues implementation created from FilterConfig init parameters.
	 */
	private static class FilterConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new FilterConfigPropertyValues.
		 * @param config FilterConfig we'll use to take PropertyValues from
		 * @param requiredProperties set of property names we need, where
		 * we can't accept default values
		 * @throws ServletException if any required properties are missing
		 */
		private FilterConfigPropertyValues(FilterConfig config, Set requiredProperties) throws ServletException {
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
				throw new ServletException("Initialization from FilterConfig for filter '" + config.getFilterName() +
																	 "' failed; the following required properties were missing: " +
																	 StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
