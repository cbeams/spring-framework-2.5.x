package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * Interceptor that checks and prepares request and response. Checks for supported
 * methods and a required session, and applies the specified number of cache seconds.
 * See superclass bean properties for configuration options.
 *
 * <p>All the settings supported by this interceptor can also be set on AbstractController.
 * This interceptor is mainly intended for applying checks and preparations to a set of
 * controllers mapped by a HandlerMapping.
 *
 * @author Juergen Hoeller
 * @since 27.11.2003
 * @see AbstractController
 */
public class WebContentInterceptor extends WebContentGenerator implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws ServletException {
		checkAndPrepare(request, response, handler instanceof LastModified);
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
	}

}
