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
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
												 "Tile with name '" + getBeanName() + "' not found");
			return;
		}

		exposeModelAsRequestAttributes(model, request);

		// get current tile context
		ComponentContext tileContext = ComponentContext.getContext(request);
		if (tileContext == null) {
			tileContext = new ComponentContext(definition.getAttributes());
			ComponentContext.setContext(tileContext, request);
		}
		else {
			tileContext.addMissing(definition.getAttributes());
		}

		// execute controller associated to definition, if any
		Controller controller = definition.getOrCreateController();
		if (controller instanceof ApplicationContextAware) {
			((ApplicationContextAware) controller).setApplicationContext(getApplicationContext());
		}
		if (controller != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing controller [" + controller + "]");
			}
			controller.perform(tileContext, request, response, getServletContext());
		}

		// process the definition
		RequestDispatcher rd = request.getRequestDispatcher(definition.getPath());
		if (rd == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		rd.include(request, response);
	}

}
