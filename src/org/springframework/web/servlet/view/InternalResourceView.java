/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * @version $Id: InternalResourceView.java,v 1.9 2004-02-07 00:18:27 jhoeller Exp $
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

		exposeModelAsRequestAttributes(model, request);

		// let the target resource set the content type
		// simply forward to the JSP
		RequestDispatcher rd = request.getRequestDispatcher(getUrl());
		if (rd == null) {
			throw new ServletException("Can't get RequestDispatcher for [" + getUrl() +
			                           "']: check that this file exists within your WAR");
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
			Iterator itr = model.keySet().iterator();
			while (itr.hasNext()) {
				Object key = itr.next();
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

}
