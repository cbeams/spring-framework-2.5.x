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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.util.StringUtils;

/**
 * PropertyValues implementation created from ServetConfig parameters.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
class ServletConfigPropertyValues extends MutablePropertyValues {

	/**
	 * Create new ServletConfigPropertyValues.
	 * @param config ServletConfig we'll use to take PropertyValues from
	 * @throws ServletException should never be thrown from this method
	 */
	public ServletConfigPropertyValues(ServletConfig config) throws ServletException {
		this(config, null);
	}

	/**
	 * Create new ServletConfigPropertyValues.
	 * @param config ServletConfig we'll use to take PropertyValues from
	 * @param requiredProperties array of property names we need, where
	 * we can't accept default values
	 * @throws ServletException if any required properties are missing
	 */
	public ServletConfigPropertyValues(ServletConfig config, String[] requiredProperties) throws ServletException {
		List missingProps = (requiredProperties != null) ? Arrays.asList(requiredProperties) : null;

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
