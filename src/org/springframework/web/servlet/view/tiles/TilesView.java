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

import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.Controller;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.TilesUtilImpl;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.view.InternalResourceView;

/**
 * View implementation that retrieves a Tiles definition.
 * The "url" property is interpreted as name of a Tiles definition.
 *
 * <p>TilesJstlView with JSTL support is a separate class,
 * mainly to avoid JSTL dependencies in this class.
 *
 * <p>Depends on a Tiles DefinitionsFactory which must be available
 * in the ServletContext. This factory is typically set up via a
 * TilesConfigurer bean definition in the application context.
 *
 * <p>A component controller specified in the Tiles definition will receive
 * a reference to the current Spring ApplicationContext if it implements
 * ApplicationContextAware. The ComponentControllerSupport class provides
 * a convenient base class for such Spring-aware component controllers.
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @see #setUrl
 * @see TilesJstlView
 * @see TilesConfigurer
 * @see ComponentControllerSupport
 * @see org.springframework.context.ApplicationContextAware
 */
public class TilesView extends InternalResourceView {

	/**
	 * Name of the attribute that will override the path of the layout page
	 * to render. A Tiles component controller can set such an attribute
	 * to dynamically switch the look and feel of a Tiles page.
	 * @see #setPath
	 */
	public static final String PATH_ATTRIBUTE = TilesView.class.getName() + ".PATH";

	/**
	 * Set the path of the layout page to render.
	 * @param request current HTTP request
	 * @param path the path of the layout page
	 * @see #PATH_ATTRIBUTE
	 */
	public static void setPath(HttpServletRequest request, String path) {
		request.setAttribute(PATH_ATTRIBUTE, path);
	}


	private DefinitionsFactory definitionsFactory;

	protected void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();
		// get definitions factory
		this.definitionsFactory = (DefinitionsFactory)
		    getWebApplicationContext().getServletContext().getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY);
		if (this.definitionsFactory == null) {
			throw new ApplicationContextException("Tiles definitions factory not found: TilesConfigurer not defined?");
		}
	}

	/**
	 * The actual rendering of the Tiles definition.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request,
	                                       HttpServletResponse response) throws Exception {

		if (!response.isCommitted()) {
			response.setContentType(getContentType());
		}

		// get component definition
		ComponentDefinition definition = this.definitionsFactory.getDefinition(getUrl(), request,
		                                                                       getServletContext());
		if (definition == null) {
			throw new ServletException("Tile with name '" + getBeanName() + "' not found");
		}

		// expose model
		exposeModelAsRequestAttributes(model, request);

		// get current component context
		ComponentContext context = getComponentContext(request, definition);

		// execute component controller associated with definition, if any
		Controller controller = getController(request, definition);
		if (controller != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing Tiles controller [" + controller + "]");
			}
			controller.perform(context, request, response, getServletContext());
		}

		// determine the path of the definition
		String path = getPath(request, definition);
		if (path == null) {
			throw new ServletException("Could not determine a path for Tiles definition '" + definition.getName() + "'");
		}

		// process the definition
		RequestDispatcher rd = request.getRequestDispatcher(path);
		if (rd == null) {
			throw new ServletException("Could not get RequestDispatcher for [" + getUrl() +
			                           "]: check that this file exists within your WAR");
		}
		rd.include(request, response);
	}

	/**
	 * Determine the Tiles component context for the given Tiles definition.
	 * @param request current HTTP request
	 * @param definition the Tiles definition to render
	 * @return the component context
	 */
	protected ComponentContext getComponentContext(HttpServletRequest request, ComponentDefinition definition) {
		ComponentContext context = ComponentContext.getContext(request);
		if (context == null) {
			context = new ComponentContext(definition.getAttributes());
			ComponentContext.setContext(context, request);
		}
		else {
			context.addMissing(definition.getAttributes());
		}
		return context;
	}

	/**
	 * Determine and initialize the Tiles component controller for the
	 * given Tiles definition, if any.
	 * @param request current HTTP request
	 * @param definition the Tiles definition to render
	 * @return the component controller to execute, or null if none
	 */
	protected Controller getController(HttpServletRequest request, ComponentDefinition definition)
			throws InstantiationException {
		Controller controller = definition.getOrCreateController();
		if (controller instanceof ApplicationContextAware) {
			((ApplicationContextAware) controller).setApplicationContext(getApplicationContext());
		}
		return controller;
	}

	/**
	 * Determine the path for the given Tiles definition,
	 * i.e. the layout page to render.
	 * @param request current HTTP request
	 * @param definition the Tiles definition to render
	 * @return the path of the layout page to render
	 */
	protected String getPath(HttpServletRequest request, ComponentDefinition definition) {
		Object pathAttr = request.getAttribute(PATH_ATTRIBUTE);
		return (pathAttr != null ? pathAttr.toString() : definition.getPath());
	}

}
