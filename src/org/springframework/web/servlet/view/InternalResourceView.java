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

import org.springframework.web.util.WebUtils;

/**
 * Wrapper for a JSP or other resource within the same web application.
 *
 * <p>Exposes model objects as request attributes and forwards the request
 * to the specified resource URL using a RequestDispatcher.
 *
 * @author Rod Johnson
 * @version $Id: InternalResourceView.java,v 1.6 2003-12-15 03:53:40 colins Exp $
 */
public class InternalResourceView extends AbstractView {

	/** URL of the JSP or other resource within the WAR */
	private String url;

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
	 * Set the resource URL that this view forwards to.
	 * @param url the URL of the resource this view forwards to
	 */
	public final void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Return the resource URL that this view forwards to.
	 * @return the URL of the resource this view forwards to
	 */
	protected final String getUrl() {
		return url;
	}

	/**
	 * Overridden lifecycle method to check that 'url' property is set.
	 */
	protected void initApplicationContext() throws IllegalArgumentException {
		if (this.url == null) {
			throw new IllegalArgumentException("Must set 'url' property in class [" + getClass().getName() + "]");
		}
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
		RequestDispatcher rd = request.getRequestDispatcher(this.url);
		if (rd == null) {
			throw new ServletException("Can't get RequestDispatcher for [" + this.url +
			                           "']: check that this file exists within your WAR");
		}

		// if already included, include again, else forward
		if (request.getAttribute(WebUtils.INCLUDE_URI_REQUEST_ATTRIBUTE) != null) {
			rd.include(request, response);
			logger.debug("Included resource [" + this.url + "] in InternalResourceView '" + getName() + "'");
		}
		else {
			rd.forward(request, response);
			logger.debug("Forwarded to resource [" + this.url + "] in InternalResourceView '" + getName() + "'");
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
						    "] to request in InternalResourceView '" + getName() + "' ");
					}
				}
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to request.");
		}
	}
	
}
