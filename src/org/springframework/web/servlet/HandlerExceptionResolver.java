package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface to be implemented by objects than can resolve exceptions thrown
 * by handlers, in the typical case to error views. Implementors are typically
 * registered as beans in the application context.
 *
 * <p>Error views are analogous to the error page JSPs, but can be used with
 * any kind of exception including any checked exception, with potentially
 * fine-granular mappings for specific handlers.
 *
 * @author Juergen Hoeller
 * @since 22.11.2003
 */
public interface HandlerExceptionResolver {

	/**
	 * Try to resolve the given exception that got thrown during on handler execution,
	 * returning a ModelAndView that represents a specific error page if appropriate.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler
	 * @param ex the exception that got thrown during handler execution
	 * @return a matching ModelAndView to forward to, or null for default processing
	 */
	ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
	                              Object handler, Exception ex);

}
