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
	protected void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// expose the model object as request attributes
		exposeModelAsRequestAttributes(model, request);

		// expose helpers as request attributes, if any
		exposeHelpers(request);

		// determine the path for the request dispatcher
		String dispatcherPath = prepareForRendering(request, response);

		// forward to the resource (typically a JSP)
		// Note: The JSP is supposed to determine the content type itself.
		RequestDispatcher rd = request.getRequestDispatcher(dispatcherPath);
		if (rd == null) {
			throw new ServletException(
					"Could not get RequestDispatcher for [" + getUrl() + "]: check that this file exists within your WAR");
		}

		// if already included, include again, else forward
		if (request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE) != null) {
			rd.include(request, response);
			if (logger.isDebugEnabled()) {
				logger.debug("Included resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
			}
		}
		else {
			rd.forward(request, response);
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarded to resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
			}
		}
	}

	/**
	 * Expose the model objects in the given map as request attributes.
	 * Names will be taken from the map.
	 * <p>Called by renderMergedOutputModel.
	 * This method is suitable for all resources reachable by RequestDispatcher.
	 * @param model Map of model objects to expose
	 * @param request current HTTP request
	 * @see #renderMergedOutputModel
	 */
	protected void exposeModelAsRequestAttributes(Map model, HttpServletRequest request) throws Exception {
		if (model != null) {
			Iterator it = model.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				if (!(entry.getKey() instanceof String)) {
					throw new ServletException(
							"Invalid key [" + entry.getKey() + "] in model Map - only Strings allowed as model keys");
				}
				String modelName = (String) entry.getKey();
				Object modelValue = entry.getValue();
				if (modelValue != null) {
					request.setAttribute(modelName, modelValue);
					if (logger.isDebugEnabled()) {
						logger.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() +
						    "] to request in InternalResourceView '" + getBeanName() + "'");
					}
				}
				else {
					request.removeAttribute(modelName);
					if (logger.isDebugEnabled()) {
						logger.debug("Removed model object '" + modelName +
								"' from request in InternalResourceView '" + getBeanName() + "'");
					}
				}
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to request.");
		}
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's contexts etc.
	 * <p>Called by renderMergedTemplateModel. The default implementation is empty.
	 * This method can be overridden to add custom helpers as request attributes.
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding attributes
	 * @see #renderMergedOutputModel
	 * @see JstlView#exposeHelpers
	 * @see org.springframework.web.servlet.view.tiles.TilesJstlView#exposeHelpers
	 */
	protected void exposeHelpers(HttpServletRequest request) throws Exception {
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
