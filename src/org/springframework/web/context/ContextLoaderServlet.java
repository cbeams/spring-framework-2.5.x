package org.springframework.web.context;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Bootstrap servlet to start up Spring's root WebApplicationContext.
 * Simply delegates to ContextLoader.
 *
 * <p>This servlet should have a lower load-on-startup value in web.xml
 * than any servlets that access the root application context.
 *
 * <p><i>Note that this class has been deprecated for containers implementing
 * Servlet API 2.4 or higher in favour of ContextLoaderListener.</i><br>
 * According to Servlet 2.4, listeners must be initialized before load-on-startup
 * servlets. Many Servlet 2.3 containers already enforce this behaviour. If you
 * use such a container, this servlet can be replaced with ContextLoaderListener.
 * Else or if working with a Servlet 2.2 container, stick with this servlet.
 *
 * <p>Servlet 2.3 containers known to work with listeners are:
 * <ul>
 * <li>Apache Tomcat 4.x
 * <li>Jetty 4.x (JBossWeb 3.x)
 * <li>Resin 2.1.8+
 * <li>Orion 2.0.2+
 * <li>Oracle OC4J 9.0.3+
 * <li>IBM WebSphere 5.x
 * </ul>
 * For working with any of them, ContextLoaderListener is recommended.
 *
 * <p>Servlet 2.3 containers known <i>not</i> to work with listeners are:
 * <ul>
 * <li>BEA WebLogic up to 8.1
 * </ul>
 * If you happen to work with such a server, this servlet has to be used.
 *
 * @author Juergen Hoeller
 * @author Darren Davison
 * @deprecated beyond Servlet 2.3 - use ContextLoaderListener in a
 * Servlet 2.4 compliant container, or a 2.3 container that initializes
 * listeners before load-on-startup servlets.
 * @see ContextLoader
 * @see ContextLoaderListener
 */
public class ContextLoaderServlet extends HttpServlet {

	/**
	 * Initialize the root web application context.
	 */
	public void init() throws ServletException {
		ContextLoader.initContext(getServletContext());
	}

	/**
	 * Close the root web application context.
	 */
	public void destroy() {
		ContextLoader.closeContext(getServletContext());
	}

	/**
	 * This should never even be called since no mapping to this servlet should
	 * ever be created in web.xml. That's why a correctly invoked Servlet 2.3
	 * listener is much more appropriate for initialization work ;-)
	 */
	public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
		getServletContext().log("Attempt to call service method on ContextLoaderServlet as " + request.getRequestURI() + " was ignored");
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

	public String getServletInfo() {
		return "ContextLoaderServlet for Servlet API 2.2/2.3 (deprecated in favour of ContextLoaderListener for Servlet API 2.4)";
	}

}
