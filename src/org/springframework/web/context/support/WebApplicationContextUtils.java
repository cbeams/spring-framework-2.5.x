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

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;

/**
 * Convenience methods to retrieve the root WebApplicationContext for a given
 * ServletContext. This is e.g. useful for accessing a Spring context from
 * within custom web views or Struts actions.
 *
 * <p>Note that there are more convenient ways of accessing the root context for
 * many web frameworks, either part of Spring or available as external library.
 * This helper class is just the most generic way to access the root context.
 *
 * @author Juergen Hoeller
 * @see org.springframework.web.context.ContextLoader
 * @see org.springframework.web.servlet.FrameworkServlet
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.struts.ActionSupport
 * @see org.springframework.web.struts.DelegatingActionProxy
 * @see org.springframework.web.jsf.FacesContextUtils
 * @see org.springframework.web.jsf.DelegatingVariableResolver
 */
public abstract class WebApplicationContextUtils {
	
	/**
	 * Find the root WebApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * <p>Will rethrow an exception that happened on root context startup,
	 * to differentiate between a failed context startup and no context at all.
	 * @param sc ServletContext to find the web application context for
	 * @return the root WebApplicationContext for this web app, or null if none
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getWebApplicationContext(ServletContext sc) {
		Object attr = sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
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
	 * Find the root WebApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * <p>Will rethrow an exception that happened on root context startup,
	 * to differentiate between a failed context startup and no context at all.
	 * @param sc ServletContext to find the web application context for
	 * @return the root WebApplicationContext for this web app
	 * @throws IllegalStateException if the root WebApplicationContext could not be found
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getRequiredWebApplicationContext(ServletContext sc)
	    throws IllegalStateException {
		WebApplicationContext wac = getWebApplicationContext(sc);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
		}
		return wac;
	}

}
