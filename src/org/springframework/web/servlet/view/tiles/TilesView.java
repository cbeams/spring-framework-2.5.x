package org.springframework.web.servlet.view.tiles;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.JstlView;
import org.apache.struts.tiles.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 * <p>The TilesView, doing the actual work of retrieving the Tiles definition</p>
 * <p>The TilesView extends the JstlView, because Tiles basically is a
 * templatingmechanism for JSPs
 * @author Alef Arendsen
 * @see org.springframework.web.servlet.view.tiles.TilesConfigurer
 */
public class TilesView extends JstlView {

	/** Logger */
	private final static Log log = LogFactory.getLog(TilesView.class);

    /**
     * Method from the JstlView
     * @param model the model
     * @param request the request
     * @throws ServletException
     */
    protected void exposeModelsAsRequestAttributes(
            Map model, HttpServletRequest request) throws ServletException {
        super.exposeModelsAsRequestAttributes(model, request);
    }

    /**
     * The actual method rendering the Tiles definition
     * @param model the model that has to be included
     * @param request the request
     * @param response the response
     * @throws ServletException
     */
    protected void renderMergedOutputModel(
            Map model, HttpServletRequest request, HttpServletResponse response)
    throws ServletException {

        // ok, first expose the model
        exposeModelsAsRequestAttributes(model, request);

        // if not already done, set the mimetype
        if (!response.isCommitted()) {
            response.setContentType("text/html");
        }
        try {
            // get the required objects
            WebApplicationContext wac =
                    (WebApplicationContext)getApplicationContext();
            ServletContext sctx = wac.getServletContext();
            DefinitionsFactory factory = (DefinitionsFactory)
                    sctx.getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY);
            if (factory == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "definitionsfactory not found");
            }

            // Do we do a forward (original behavior) or an include ?
            boolean doInclude = false;
            // Controller associated to a definition, if any
            Controller controller = null;
            ComponentContext tileContext = null;

            // Get current tile context if any, if context exist, do an include
            tileContext = ComponentContext.getContext( request );
            doInclude = tileContext != null;
            ComponentDefinition definition;

            definition = factory.getDefinition(getName(), request, sctx);
            if (definition == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Tile with name '" + getName() + "' not found");
                return;
            }

            String uri = definition.getPath();
            controller = definition.getOrCreateController();
            if(tileContext == null) {
                tileContext = new ComponentContext(definition.getAttributes());
                ComponentContext.setContext(tileContext, request);
            } else {
                tileContext.addMissing(definition.getAttributes());
            }

            // Process the definition
            // Execute controller associated to definition, if any.
            if (controller != null) {
                controller.perform( tileContext, request, response, sctx);
            }

            if (doInclude) {
                doInclude(uri, request, response);
            } else {
                doForward(uri, request, response);   // original behavior
            }
        } catch (DefinitionsFactoryException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (ServletException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    private void doForward(String uri, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        doInclude(uri, request, response);
    }

    /**
     * Include method
     * @param uri
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    protected void doInclude(
        String uri,
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException {

        WebApplicationContext wac =
                (WebApplicationContext)getApplicationContext();
        ServletContext sctx = wac.getServletContext();

        RequestDispatcher rd = sctx.getRequestDispatcher(uri);
        if (rd == null) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        try {
            rd.include(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);

        }
    }

    /**
     * Debug method not really used in production
     * @param request
     */
    protected void debugRequest(HttpServletRequest request) {
        Enumeration enum = request.getAttributeNames();
        while (enum.hasMoreElements()) {
            String s = (String) enum.nextElement();
            log.debug("Req atttribute " + s + " with value " + request.getAttribute(s));
        }
    }
}
