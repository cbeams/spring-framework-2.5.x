
package org.springframework.web.util;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author robh
 */
public abstract class AbstractRequestLoggingFilter extends OncePerRequestFilter {

	private boolean includeQueryString;

	public void setIncludeQueryString(boolean includeQueryString) {
		this.includeQueryString = includeQueryString;
	}

	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		beforeRequest(request);
		filterChain.doFilter(request, response);
		afterRequest(request);
	}

	protected String getMessage(HttpServletRequest request) {
		if (this.includeQueryString) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(request.getRequestURI());
			buffer.append('?');
			buffer.append(request.getQueryString());

			return buffer.toString();
		}
		else {
			return request.getRequestURI();
		}
	}

	protected abstract void beforeRequest(HttpServletRequest request);

	protected abstract void afterRequest(HttpServletRequest request);

}
