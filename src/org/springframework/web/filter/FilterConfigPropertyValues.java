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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.util.StringUtils;

/**
 * PropertyValues implementation created from ServetConfig parameters.
 * This class is immutable once initialized.
 * @author Juergen Hoeller
 */
class FilterConfigPropertyValues implements PropertyValues {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * PropertyValues delegate. We use delegation rather than simply subclass
	 * MutablePropertyValues as we don't want to expose MutablePropertyValues's
	 * update methods. This class is immutable once initialized.
	 */
	private MutablePropertyValues mutablePropertyValues;

	/**
	 * Create a new FilterConfigPropertyValues object.
	 * @param config ServletConfig we'll use to take PropertyValues from
	 * @throws ServletException should never be thrown from this method
	 */
	public FilterConfigPropertyValues(FilterConfig config) throws ServletException {
		this(config, null);
	}

	/**
	 * Creates new FilterConfigPropertyValues object.
	 * @param config ServletConfig we'll use to take PropertyValues from
	 * @param requiredProperties array of property names we need, where
	 * we can't accept default values
	 * @throws ServletException if any required properties are missing
	 */
	public FilterConfigPropertyValues(FilterConfig config, List requiredProperties) throws ServletException {
		// ensure we have a deep copy
		List missingProps = (requiredProperties == null) ? new ArrayList(0) : new ArrayList(requiredProperties);

		this.mutablePropertyValues = new MutablePropertyValues();
		Enumeration enum = config.getInitParameterNames();
		while (enum.hasMoreElements()) {
			String property = (String) enum.nextElement();
			Object value = config.getInitParameter(property);
			this.mutablePropertyValues.addPropertyValue(new PropertyValue(property, value));
			missingProps.remove(property);
		}

		// fail if we are still missing properties
		if (missingProps.size() > 0) {
			throw new ServletException("Initialization from ServletConfig for servlet '" + config.getFilterName() +
																 "' failed: the following required properties were missing -- (" +
			                           StringUtils.collectionToDelimitedString(missingProps, ", ") + ")");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Found PropertyValues in ServletConfig: " + this.mutablePropertyValues);
		}
	}

	public PropertyValue[] getPropertyValues() {
		return this.mutablePropertyValues.getPropertyValues();
	}

	public boolean contains(String propertyName) {
		return this.mutablePropertyValues.contains(propertyName);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		return this.mutablePropertyValues.getPropertyValue(propertyName);
	}

	public PropertyValues changesSince(PropertyValues old) {
		return this.mutablePropertyValues.changesSince(old);
	}

}
