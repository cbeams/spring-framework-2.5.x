package org.springframework.web.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that can expose an internal resource, including a 
 * default URL if the specified resource is not found. An alternative,
 * for example, to trying and catching exceptions when using JSP include.
 *
 * <p>A typical usage would map all URLs with a .res extension onto an
 * instanceof this servlet, and use the JSP include action to include
 * with the resource parameter (specificed in a jsp:param sub-action)
 * indicating the actual path in the WAR.
 *
 * <p>The defaultUrl bean property must be set to the
 * internal path of the default (placeholder) URL.
 *
 * @author Rod Johnson
 */
public class ResourceServlet extends HttpServletBean {
	
	/**
	 * Name of the parameter that must contain the actual resource path.
	 */
	public static final String RESOURCE_PARAM = "resource";
	
	/**
	 * URL within the current web application from which to include
	 * content if the requested path isn't found.
	 */
	private String defaultUrl;

	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}

	public String getDefaultUrl() {
		return defaultUrl;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String resource = request.getParameter(RESOURCE_PARAM);
		if (resource == null) {
			throw new ServletException("Path is required");
		}
		try {
			RequestDispatcher rd = request.getRequestDispatcher(resource);
			rd.include(request, response);
			logger.debug("Included content of resource [" + resource + "]");
		}
		catch (Exception ex) {
			RequestDispatcher rd = request.getRequestDispatcher(this.defaultUrl);
			rd.include(request, response);
			logger.warn("Failed to include content of resource [" + resource + "]");
		}
	}

}
