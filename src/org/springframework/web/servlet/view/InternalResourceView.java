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

package org.springframework.web.servlet.view;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.UrlPathHelper;

/**
 * Wrapper for a JSP or other resource within the same web application.
 * Exposes model objects as request attributes and forwards the request to
 * the specified resource URL using a RequestDispatcher. Will fall back to
 * an include if already in an included request.
 *
 * <p>A URL for this view is supposed to specify a resource within the web
 * application, i.e. suitable for RequestDispatcher's forward/include methods.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: InternalResourceView.java,v 1.12 2004-03-24 11:38:48 jhoeller Exp $
 * @see javax.servlet.RequestDispatcher#forward
 * @see javax.servlet.RequestDispatcher#include
 */
public class InternalResourceView extends AbstractUrlBasedView {

	/**
	 * Constructor for use as a bean.
	 */
	public InternalResourceView() {
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * @param url the URL to forward to
	 */
	public InternalResourceView(String url) {
		setUrl(url);
	}

	/**
	 * Render the internal resource given the specified model.
	 * This includes setting the model as request attributes.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request,
	                                       HttpServletResponse response) throws Exception {

		// expose the model object as request attributes
		exposeModelAsRequestAttributes(model, request);

		// determine the path for the request dispatcher
		String dispatcherPath = prepareForRendering(request, response);

		// forward to the resource (typically a JSP)
		// Note: The JSP is supposed to determine the content type itself.
		RequestDispatcher rd = request.getRequestDispatcher(dispatcherPath);
		if (rd == null) {
			throw new ServletException("Could not get RequestDispatcher for [" + getUrl() +
			                           "]: check that this file exists within your WAR");
		}

		// if already included, include again, else forward
		if (request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE) != null) {
			rd.include(request, response);
			logger.debug("Included resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
		}
		else {
			rd.forward(request, response);
			logger.debug("Forwarded to resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
		}
	}

	/**
	 * Expose the model objects in the given map as request attributes.
	 * Names will be taken from the map.
	 * This method is suitable for all resources reachable by RequestDispatcher.
	 * @param model Map of model objects to expose
	 * @param request current HTTP request
	 */
	protected void exposeModelAsRequestAttributes(Map model, HttpServletRequest request) throws ServletException {
		if (model != null) {
			Iterator it = model.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				if (!(key instanceof String)) {
					throw new ServletException("Invalid key [" + key + "] in model Map - only Strings allowed as model keys");
				}
				String modelName = (String) key;
				Object modelValue = model.get(modelName);
				if (modelValue != null) {
					request.setAttribute(modelName, modelValue);
					if (logger.isDebugEnabled()) {
						logger.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() +
						    "] to request in InternalResourceView '" + getBeanName() + "' ");
					}
				}
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to request.");
		}
	}

	/**
	 * Prepare for rendering, and determine the request dispatcher path
	 * to forward to respectively to include.
	 * <p>This implementation simply returns the configured URL.
	 * Subclasses can override this to determine a resource to render,
	 * typically interpreting the URL in a different manner.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return the request dispatcher path to use
	 * @throws Exception if preparations failed
	 * @see #getUrl
	 * @see org.springframework.web.servlet.view.tiles.TilesView#prepareForRendering
	 */
	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		return getUrl();
	}

}
