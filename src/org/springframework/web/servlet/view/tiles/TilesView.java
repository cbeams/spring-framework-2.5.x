package org.springframework.web.servlet.view.tiles;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.Controller;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.TilesUtilImpl;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.JstlView;

/**
 * TilesView retrieves a Tiles definition.
 * The "url" property is interpreted as name of a Tiles definition.
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @see #setUrl
 * @see TilesConfigurer
 */
public class TilesView extends JstlView {

	/**
	 * The actual method rendering the Tiles definition.
	 * @param model the model that has to be included
	 * @param request the request
	 * @param response the response
	 * @throws ServletException
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		exposeModelsAsRequestAttributes(model, request);
		response.setContentType("text/html");

		try {
			// get the required objects
			ServletContext sc = ((WebApplicationContext) getApplicationContext()).getServletContext();
			DefinitionsFactory factory = (DefinitionsFactory) sc.getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY);
			if (factory == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				                   "Tiles definitions factory not found: TilesConfigurer not defined?");
			}

			// get current tile context if any
			ComponentContext tileContext = ComponentContext.getContext(request);

			ComponentDefinition definition = factory.getDefinition(getUrl(), request, sc);
			if (definition == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
				                   "Tile with name '" + getName() + "' not found");
				return;
			}

			String uri = definition.getPath();
			Controller controller = definition.getOrCreateController();
			if (tileContext == null) {
				tileContext = new ComponentContext(definition.getAttributes());
				ComponentContext.setContext(tileContext, request);
			}
			else {
				tileContext.addMissing(definition.getAttributes());
			}

			// process the definition
			// execute controller associated to definition, if any
			if (controller != null) {
				controller.perform(tileContext, request, response, sc);
			}

			RequestDispatcher rd = request.getRequestDispatcher(uri);
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
