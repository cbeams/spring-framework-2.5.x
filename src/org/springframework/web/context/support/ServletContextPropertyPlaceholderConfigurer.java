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

package org.springframework.web.context.support;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.web.context.ServletContextAware;

/**
 * Subclass of PropertyPlaceholderConfigurer that falls back to ServletContext
 * init parameters (that is, web.xml context-param entries) if a placeholder
 * could not be resolved against the provided properties.
 *
 * <p>Can be combined with "locations" and/or "properties" values for fallback
 * to web.xml context-params. Alternatively, can be defined without properties
 * locations and values, to resolve all placeholders as web.xml context-params
 * (or JVM system properties).
 *
 * <p>Optionally supports searching of ServletContext attributes: If turned on,
 * an otherwise unresolvable placeholder will matched against the corresponding
 * ServletContext attribute, using its stringified value if found. This can be
 * used to feed dynamic values into Spring's placeholder resolution.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see #setLocations
 * @see #setProperties
 * @see #setSystemPropertiesModeName
 * @see #setSearchAttributes
 */
public class ServletContextPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
		implements ServletContextAware {

	private ServletContext servletContext;

	private boolean searchAttributes;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Set whether to search ServletContext attributes if a placeholder
	 * could not be resolved in any other way. Default is false.
	 * <p>If turned on, the configurer will look for a ServletContext
	 * attribute with the same name as the placeholder, and use its
	 * stringified value if found.
	 * @see javax.servlet.ServletContext#getAttribute
	 */
	public void setSearchAttributes(boolean searchAttributes) {
		this.searchAttributes = searchAttributes;
	}

	protected String resolvePlaceholder(String placeholder, Properties props) {
		String value = super.resolvePlaceholder(placeholder, props);
		if (value == null && this.servletContext != null) {
			value = this.servletContext.getInitParameter(placeholder);
			if (value == null && this.searchAttributes) {
				Object attrValue = this.servletContext.getAttribute(placeholder);
				if (attrValue != null) {
					value = attrValue.toString();
				}
			}
		}
		return value;
	}

}
