/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextException;

/**
 * Wrapper for a JSP or other resource within the WAR.
 * Sets request attributes and forwards the request to the specified
 * specified resource using a RequestDispatcher.
 *
 * @author Rod Johnson
 * @version $Id: InternalResourceView.java,v 1.2 2003-11-05 19:19:15 jhoeller Exp $
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
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Return the resource URL that this view forwards to.
	 * @return the URL of the resource this view forwards to
	 */
	protected String getUrl() {
		return url;
	}

	/**
	 * Overridden lifecycle method to check that URL property is set.
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		if (this.url == null) 
			throw new ApplicationContextException("Must set url property in class " + getClass().getName());
	}

	/**
	 * Render the internal resource given the specified model.
	 * This includes setting the model as request attributes.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		if (this.url == null)
			throw new ServletException("InternalResourceView is not configured: URL cannot be null");
			
		exposeModelsAsRequestAttributes(model, request);

		// Let the target resource set the content type
				
		// Simply forward to the JSP
		RequestDispatcher rd = request.getRequestDispatcher(this.url);
		if (rd == null)
			throw new ServletException("Can't get RequestDispatcher for '" + this.url + "': check that this file exists within your WAR");
		rd.forward(request, response);
		logger.debug("Forwarded OK to resource within current application with url '" + this.url + "' in InternalResource view with name '" + getName() + "'");
	}

	/**
	 * Expose the models in the given map as request attributes.
	 * Names will be taken from the map.
	 * This method is suitable for all resources reachable by RequestDispatcher.
	 * @param model Map of models to expose
	 * @param request HttpServletRequest to preprocess.
	 */
	protected void exposeModelsAsRequestAttributes(Map model, HttpServletRequest request) throws ServletException {
		if (model != null) {
			Iterator itr = model.keySet().iterator();
			while (itr.hasNext()) {
				Object key = itr.next();
				if (!(key instanceof String)) {
					throw new ServletException("Invalid key [" + key + "] in model Map - only Strings allowed as model keys");
				}
				String modelName = (String) key;
				Object modelValue = model.get(modelName);
				if (logger.isDebugEnabled()) {
					String msg = "Added model with name '" + modelName + "' to request in InternalResourceView with name '" + getName() + "' ";
					msg += (modelValue != null) ? "and class " + modelValue.getClass() : "(null)";
					logger.debug(msg);
				}
				request.setAttribute(modelName, modelValue);
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to request.");
		}
	}
	
}
