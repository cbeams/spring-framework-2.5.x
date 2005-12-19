/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.mock.web.portlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;

import org.springframework.util.Assert;

/**
 * Mock implementation of the PortletConfig interface.
 * 
 * @author John A. Lewis
 * @since 2.0
 */
public class MockPortletConfig implements PortletConfig {

	private final PortletContext portletContext;

	private final String name;
	
	private final HashMap resourceBundles = new HashMap();
	
	private final Hashtable initParameters = new Hashtable();


	/**
	 * Create new MockPortletConfig with empty String as name.
	 * @param portletContext the PortletContext that the portlet runs in
	 */
	public MockPortletConfig(PortletContext portletContext) {
		this(portletContext, "");
	}

	/**
	 * Create new MockPortletConfig.
	 * @param portletContext the PortletContext that the portlet runs in
	 * @param name the name of the portlet
	 */
	public MockPortletConfig(PortletContext portletContext, String name) {
		this.portletContext = portletContext;
		this.name = name;
	}

	
	//---------------------------------------------------------------------
	// PortletConfig methods
	//---------------------------------------------------------------------
	
	public String getPortletName() {
		return name;
	}
	
	public PortletContext getPortletContext() {
		return portletContext;
	}
	
    public ResourceBundle getResourceBundle(Locale locale) {
		Assert.notNull(locale, "locale may not be null");
        return (ResourceBundle)this.resourceBundles.get(locale);
    }
    
	public String getInitParameter(String name) {
		Assert.notNull(name, "name may not be null");
		return (String)this.initParameters.get(name);
	}

	public Enumeration getInitParameterNames() {
		return this.initParameters.keys();
	}

	
	//---------------------------------------------------------------------
	// MockPortletConfig methods
	//---------------------------------------------------------------------
	
	public void setResourceBundle(Locale locale, ResourceBundle resourceBundle) {
		Assert.notNull(locale, "locale may not be null");
	    this.resourceBundles.put(locale, resourceBundle);
	}

	public void addInitParameter(String name, String value) {
		Assert.notNull(name, "name may not be null");
	    this.initParameters.put(name, value);
	}

}
