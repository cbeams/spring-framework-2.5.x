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

import org.springframework.web.context.WebApplicationContext;


/**
 * Convenience methods to retrieve the root WebApplicationContext for a given
 * PortletContext. This is e.g. useful for accessing a Spring context from
 * within custom web views or Struts actions.
 * 
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @see org.springframework.web.servlet.context.ContextLoader
 */
public abstract class PortletWebApplicationContextUtils {

	
	/**
	 * Find the root WebApplicationContext for this portlet app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * 
	 * @param pc PortletContext to find the web application context for
	 * @return the root WebApplicationContext for this portlet app, or null if none
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getWebApplicationContext(PortletContext pc) {
		Object attr = pc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (attr == null) {
			return null;
		}
		if (attr instanceof RuntimeException) {
			throw (RuntimeException) attr;
		}
		if (attr instanceof Error) {
			throw (Error) attr;
		}
		if (!(attr instanceof WebApplicationContext)) {
			throw new IllegalStateException("Root context attribute is not of type WebApplicationContext: " + attr);
		}
		return (WebApplicationContext) attr;
	}

	/**
	 * Find the root WebApplicationContext for this portlet app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * 
	 * @param pc PortletContext to find the root web application context for
	 * @return the root WebApplicationContext for this portlet app
	 * @throws IllegalStateException if the root WebApplicationContext could not be found
	 * @see org.springframework.web.context.WebApplicationContext#WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getRequiredWebApplicationContext(PortletContext sc)
	    throws IllegalStateException {
		WebApplicationContext wac = getWebApplicationContext(sc);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
		}
		return wac;
	}


}
