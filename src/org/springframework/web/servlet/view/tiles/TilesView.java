package org.springframework.web.servlet.view.tiles;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.Controller;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.TilesUtilImpl;

import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.view.InternalResourceView;

/**
 * TilesView retrieves a Tiles definition.
 * The "url" property is interpreted as name of a Tiles definition.
 *
 * <p>TilesJstlView with JSTL support is a separate class,
 * mainly to avoid JSTL dependencies in this class.
 *
 * <p>A component controller specified in the Tiles definition will receive
 * a reference to the current Spring ApplicationContext if it implements
 * ApplicationContextAware. The ComponentControllerSupport class provides
 * a convenient base class for such Spring-aware component controllers.
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @see #setUrl
 * @see TilesConfigurer
 * @see TilesJstlView
 * @see ComponentControllerSupport
 * @see org.springframework.context.ApplicationContextAware
 */
public class TilesView extends InternalResourceView {

	/**
	 * The actual rendering of the Tiles definition.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		try {
			// get definitions factory
			DefinitionsFactory factory = (DefinitionsFactory) getApplicationContext().sharedObject(TilesUtilImpl.DEFINITIONS_FACTORY);
			if (factory == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				                   "Tiles definitions factory not found: TilesConfigurer not defined?");
			}

			// get component definition
			ComponentDefinition definition = factory.getDefinition(getUrl(), request, getServletContext());
			if (definition == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
				                   "Tile with name '" + getName() + "' not found");
				return;
			}

			exposeModelsAsRequestAttributes(model, request);

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
		catch (DefinitionsFactoryException ex) {
			throw new ServletException(ex.getMessage(), ex);
		}
		catch (InstantiationException ex) {
			throw new ServletException(ex.getMessage(), ex);
		}
	}

}
