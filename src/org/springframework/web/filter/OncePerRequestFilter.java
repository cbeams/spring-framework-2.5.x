package org.springframework.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter base class that guarantees to be just executed once per request,
 * on any servlet container. It provides a doFilterInternal method with
 * HttpServletRequest and HttpServletResponse arguments.
 * @author Juergen Hoeller
 * @since 06.12.2003
 */
public abstract class OncePerRequestFilter extends GenericFilterBean {

	private static final String ALREADY_FILTERED = OncePerRequestFilter.class.getName() + ".FILTERED";

	/**
	 * This doFilter implementation stores a request attribute for "already filtered",
	 * proceeding without filtering again if the attribute is already there.
	 */
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (request.getAttribute(ALREADY_FILTERED) != null || shouldNotFilter(httpRequest)) {
			filterChain.doFilter(request, response);
			return;
		}
		else {
			request.setAttribute(ALREADY_FILTERED, Boolean.TRUE);
			doFilterInternal(httpRequest, httpResponse, filterChain);
		}
	}

	/**
	 * Can return true to avoid filtering of the given request.
	 * The default implementation always returns false.
	 * Can be overridden in subclasses for custom filtering control.
	 * @param request current HTTP request
	 * @return whether the given request should <i>not</i> be filtered
	 * @throws ServletException in case of errors
	 */
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return false;
	}

	/**
	 * Same contract as for doFilter, but guaranteed to be just invoked once per request.
	 * Provides HttpServletRequest and HttpServletResponse arguments instead of the default
	 * ServletRequest and ServletResponse ones.
	 */
	protected abstract void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
																					 FilterChain filterChain) throws ServletException, IOException;

}
