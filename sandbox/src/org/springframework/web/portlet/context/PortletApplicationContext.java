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

package org.springframework.web.portlet.context;

import javax.portlet.PortletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.ThemeSource;

/** 
 * Interface to provide configuration for a portlet application. This is read-only while
 * the application is running, but may be reloaded if the implementation supports this.
 *
 * <p>This interface adds a getPortletContext method to the generic ApplicationContext
 * interface, and defines a well-known application attribute name that the root
 * context must be bound to in the bootstrap process.
 *
 * TODO: review this.
 * <p>Like generic application contexts, portlet application contexts are hierarchical.
 * There is a single root context per application, while each portlet in the application
 * (including a dispatcher portlet in the MVC framework) has its own child context.
 *
 * <p>In addition to standard application context lifecycle capabilities,
 * PortletApplicationContext implementations need to detect PortletContextAware
 * beans and invoke the setPortletContext method accordingly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 19, 2001
 * @version $Revision: 1.1 $
 * @see PortletContextAware#setPortletContext
 */
public interface PortletApplicationContext extends ApplicationContext, ThemeSource {

	/**
	 * Context attribute to bind root PortletApplicationContext to on successful startup.
	 * <p>Note: If the startup of the root context fails, this attribute can contain
	 * an exception or error as value. Use PortletApplicationContextUtils for convenient
	 * lookup of the root PortletApplicationContext.
	 * @see org.springframework.web.portlet.context.support.PortletApplicationContextUtils#getPortletApplicationContext
	 * @see org.springframework.web.portlet.context.support.PortletApplicationContextUtils#getRequiredPortletApplicationContext
	 */
	String ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = PortletApplicationContext.class + ".ROOT";

	/**
	 * Return the standard Portlet API PorletContext for this application.
	 */
	PortletContext getPortletContext();
	
}
