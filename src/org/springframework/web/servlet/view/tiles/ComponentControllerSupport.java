package org.springframework.web.servlet.view.tiles;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.Controller;

import org.springframework.web.context.support.WebApplicationObjectSupport;

/**
 * Convenience class for Spring-aware Tiles component controllers.
 * Provides a reference to the current Spring application context,
 * e.g. for bean lookup or resource loading.
 * @author Juergen Hoeller
 * @since 22.08.2003
 */
public abstract class ComponentControllerSupport extends WebApplicationObjectSupport implements Controller {

	/**
	 * This implementation delegates to the simplified doPerform,
	 * as the ServletContext is provided by getServletContext anyway.
	 * @see #doPerform
	 * @see #getServletContext
	 */
	public final void perform(ComponentContext componentContext, HttpServletRequest request,
	                          HttpServletResponse response, ServletContext servletContext)
	    throws ServletException, IOException {
		doPerform(componentContext, request, response);
	}

	/**
	 * Perform the actual preparation for the component.
	 * The ServletContext can be retrieved via getServletContext, if necessary.
	 * @param componentContext current Tiles component context
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws ServletException in case of execution errors
	 * @throws IOException in case of I/O errors
	 * @see org.apache.struts.tiles.Controller#perform
	 * @see #getServletContext
	 */
	protected abstract void doPerform(ComponentContext componentContext, HttpServletRequest request,
	                                  HttpServletResponse response) throws ServletException, IOException;

}
