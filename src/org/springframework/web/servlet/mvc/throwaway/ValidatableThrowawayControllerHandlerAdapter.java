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
public class ValidatableThrowawayControllerHandlerAdapter implements HandlerAdapter {

	public boolean supports(Object handler) {
		return (handler instanceof ValidatableThrowawayController);
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		ValidatableThrowawayController throwaway = (ValidatableThrowawayController) handler;
		ServletRequestDataBinder binder = new ServletRequestDataBinder(throwaway, throwaway.getName());
		throwaway.initBinder(binder);
		binder.bind(request);
		return throwaway.execute(binder.getErrors());
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return 0;
	}

}
