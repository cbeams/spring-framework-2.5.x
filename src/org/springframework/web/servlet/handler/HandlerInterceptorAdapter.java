package org.springframework.web.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Abstract adapter class for the HandlerInterceptor interface,
 * for simplified implementation of pre-only/post-only interceptors.
 * @author Juergen Hoeller
 * @since 05.12.2003
 */
public abstract class HandlerInterceptorAdapter implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
													 Object handler) throws Exception {
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response,
												 Object handler, ModelAndView modelAndView) throws Exception {
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
															Object handler, Exception ex) throws Exception {
	}

}
