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
 *
 * <p>How to identify that a request is already filtered is determined
 * by the getAlreadyFilteredAttributeName method. The default implementation
 * is based on the class name of the concrete filter, but this can be made
 * more specific by overriding that method in subclasses.
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see #doFilterInternal
 * @see #getAlreadyFilteredAttributeName
 */
public abstract class OncePerRequestFilter extends GenericFilterBean {

	/**
	 * Suffix that gets appended to the actual class name for the
	 * "already filtered" request attribute.
	 * @see #getAlreadyFilteredAttributeName
	 */
	public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

	/**
	 * This doFilter implementation stores a request attribute for
	 * "already filtered", proceeding without filtering again if the
	 * attribute is already there.
	 * @see #getAlreadyFilteredAttributeName
	 * @see #shouldNotFilter
	 * @see #doFilterInternal
	 */
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
		if (request.getAttribute(alreadyFilteredAttributeName) != null || shouldNotFilter(httpRequest)) {
			filterChain.doFilter(request, response);
			return;
		}
		else {
			request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
			doFilterInternal(httpRequest, httpResponse, filterChain);
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request
	 * is already filtered. Default implementation takes the actual class name
	 * and appends ".FILTERED".
	 * <p>Subclasses can override this to get more specific identification.
	 * For example, a certain filter class could be applied multiple times
	 * but with different configuration: The attribute name should identify
	 * the particular configuration then.
	 * @see #ALREADY_FILTERED_SUFFIX
	 */
	protected String getAlreadyFilteredAttributeName() {
		return getClass().getName() + ALREADY_FILTERED_SUFFIX;
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
	 * Same contract as for doFilter, but guaranteed to be just invoked once per
	 * request. Provides HttpServletRequest and HttpServletResponse arguments
	 * instead of the default ServletRequest and ServletResponse ones.
	 */
	protected abstract void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
																					 FilterChain filterChain) throws ServletException, IOException;

}
