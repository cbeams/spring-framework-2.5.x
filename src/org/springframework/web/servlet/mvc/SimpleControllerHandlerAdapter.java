package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adapter to use the Controller workflow interface with the generic DispatcherServlet.
 * Supports controllers that implement the LastModified interface.
 *
 * <p>This is an SPI class, not used directly by application code.
 *
 * @author Rod Johnson
 * @version $Id: SimpleControllerHandlerAdapter.java,v 1.6 2003-12-09 08:48:40 jhoeller Exp $
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see Controller
 * @see LastModified
 */
public class SimpleControllerHandlerAdapter implements HandlerAdapter {
	
	public boolean supports(Object handler) {
		return (handler instanceof Controller);
	}
	
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return ((Controller) handler).handleRequest(request, response);
	}
	
	public long getLastModified(HttpServletRequest request, Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;
	}

}
