package org.springframework.web.servlet.mvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.LastModified;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adapter to use the Controller workflow interface with the generic
 * DispatcherServlet. This is an SPI class, not used directly by
 * application code.
 * @author Rod Johnson
 * @see org.springframework.web.servlet.DispatcherServlet
 * @version $Id: SimpleControllerHandlerAdapter.java,v 1.2 2003-08-28 15:23:32 jhoeller Exp $
 */
public class SimpleControllerHandlerAdapter extends WebApplicationObjectSupport implements HandlerAdapter {
	
	public boolean supports(Object handler) {
		return handler != null && Controller.class.isAssignableFrom(handler.getClass());
	}
	
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws ServletException, IOException {
		Controller controller = (Controller) handler;
		return controller.handleRequest(request, response);
	}
	
	public long getLastModified(HttpServletRequest request, Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;
	}

}
