package org.springframework.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Class to manage thread-bound HttpServletRequest.
 * @since 1.3
 * @author Rod Johnson
 */
public abstract class RequestHolder  {
	
	/**
	 * Holder for the current request.
	 */
	public static ThreadLocal currentRequest = new ThreadLocal();
	
	// TODO return old? use stack?
	public static void bind(HttpServletRequest request) {
		RequestHolder.currentRequest.set(request);
	}
	
	/**
	 * Clear any request bound to the thread.
	 */
	public static void clear() {
		RequestHolder.currentRequest.set(null);
	}
	
	/**
	 * Return the request currently bound to the thread.
	 * @return the request currently bound to the thread
	 * @throws IllegalStateException if no request is bound to the 
	 * current thread
	 */
	public static HttpServletRequest currentRequest() throws IllegalStateException {
		HttpServletRequest request = (HttpServletRequest) currentRequest.get();
		if (request == null) {
			throw new IllegalStateException("No thread-bound request: Try using RequestBindingFilter");		
		}
		return request;
	}
		
	/**
	 * Convenient method to return the session associated with the current request,
	 * creating one if none exists.
	 * @return the Session to which the current request belongs.
	 */
	public static HttpSession currentSession() {		
		return currentRequest().getSession(true);
	}

}
