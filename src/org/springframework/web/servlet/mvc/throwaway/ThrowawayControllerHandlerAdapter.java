package org.springframework.web.servlet.mvc.throwaway;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @since 08.12.2003
 */
public class ThrowawayControllerHandlerAdapter implements HandlerAdapter {

	public static final String THROWAWAY_CONTROLLER_NAME = "throwawayController";

	public boolean supports(Object handler) {
		return (handler instanceof ThrowawayController);
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		ThrowawayController throwaway = (ThrowawayController) handler;
		ServletRequestDataBinder binder = new ServletRequestDataBinder(throwaway, THROWAWAY_CONTROLLER_NAME);
		binder.bind(request);
		binder.closeNoCatch();
		return throwaway.execute();
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

}
