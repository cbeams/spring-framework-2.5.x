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

package org.springframework.web.servlet.view.tiles;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.Controller;

import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Convenience class for Spring-aware Tiles component controllers.
 * Provides a reference to the current Spring application context,
 * e.g. for bean lookup or resource loading.
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @since 22.08.2003
 */
public abstract class ComponentControllerSupport extends WebApplicationObjectSupport implements Controller {

	/**
	 * This implementation delegates to doPerform, lazy-initializing the application context
	 * reference if necessary, and converting non-Servlet/IO Exceptions to ServletException.
	 * @see #doPerform
	 */
	public final void perform(ComponentContext componentContext, HttpServletRequest request,
	                          HttpServletResponse response, ServletContext servletContext)
	    throws ServletException, IOException {
		// TODO the following is inserted because ComponentControllerSupport does NOT work when
		// inserting tiles directly with the tiles:insert tag in JSPs, since the <tiles:insert> tag
		// - org.apache.struts.taglib.tiles.InsertTag (line 869) - manually creates and executes
		// controllers. For now, we'll check for the application context and set it if necessary!
		synchronized (this) {
			if (getWebApplicationContext() == null) {
				setApplicationContext(RequestContextUtils.getWebApplicationContext(request));
			}
		}
		try {
			doPerform(componentContext, request, response);
		}
		catch (ServletException ex) {
			throw ex;
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new ServletException(ex.getMessage(), ex);
		}
	}

	/**
	 * Perform the preparation for the component, allowing for any Exception to be thrown.
	 * The ServletContext can be retrieved via getServletContext, if necessary.
	 * The Spring WebApplicationContext can be accessed via getWebApplicationContext.
	 * @param componentContext current Tiles component context
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of errors
	 * @see org.apache.struts.tiles.Controller#perform
	 * @see #getServletContext
	 * @see #getWebApplicationContext
	 */
	protected abstract void doPerform(ComponentContext componentContext, HttpServletRequest request,
	                                  HttpServletResponse response) throws Exception;

}
