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
package org.springframework.web.portlet.context.support;

import javax.portlet.PortletContext;

import org.springframework.web.portlet.context.PortletApplicationContext;


/**
 * Convenience methods to retrieve the root PortletApplicationContext for a given
 * PortletContext. This is e.g. useful for accessing a Spring context from
 * within custom web views or Struts actions.
 * TODO: is this still true?
 * 
 * @author Juergen Hoeller
 * @version $Revision: 1.1 $
 * @see org.springframework.web.portlet.context.ContextLoader
 */
public abstract class PortletApplicationContextUtils {

	
	/**
	 * Find the root PortletApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * TODO: where to load the PortletApplicationContext???
	 * @param pc PortletContext to find the portlet application context for
	 * @return the root PortletApplicationContext for this portlet app, or null if none
	 * @see org.springframework.web.portal.context.PortletApplicationContext#ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static PortletApplicationContext getPortletApplicationContext(PortletContext pc) {
		Object attr = pc.getAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
		if (attr == null) {
			return null;
		}
		if (attr instanceof RuntimeException) {
			throw (RuntimeException) attr;
		}
		if (attr instanceof Error) {
			throw (Error) attr;
		}
		if (!(attr instanceof PortletApplicationContext)) {
			throw new IllegalStateException("Root context attribute is not of type PortletApplicationContext: " + attr);
		}
		return (PortletApplicationContext) attr;
	}

	/**
	 * Find the root PortletApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * TODO: where to load PortletApplicationContext
	 * @param pc PortletContext to find the portlet application context for
	 * @return the root PortletApplicationContext for this portlet app
	 * @throws IllegalStateException if the root PortletApplicationContext could not be found
	 * @see org.springframework.web.portlet.context.PortletApplicationContext#PORTLET_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static PortletApplicationContext getRequiredWebApplicationContext(PortletContext sc)
	    throws IllegalStateException {
		PortletApplicationContext pac = getPortletApplicationContext(sc);
		if (pac == null) {
		    // TODO where are we loading PortletApplicationContext?
			throw new IllegalStateException("No PortletApplicationContext found: no ContextLoaderListener registered?");
		}
		return pac;
	}


}
