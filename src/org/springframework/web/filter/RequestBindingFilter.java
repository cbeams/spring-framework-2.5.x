package org.springframework.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.RequestHolder;

/**
 * Servlet filter that binds the request to the current thread.
 * Useful for middle tier objects that need access to the current request.
 * Does not require any configuration parameters.
 * @see org.springframework.web.util.RequestHolder
 * @since 1.3
 * @author Rod Johnson
 */
public class RequestBindingFilter extends OncePerRequestFilter {

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		try {
			RequestHolder.bind(request);
			logger.debug("Bound request to thread");
			filterChain.doFilter(request, response);
		}
		finally {
			// Ensure thread local is cleared
			RequestHolder.clear();
			logger.debug("Cleared thread-bound request");
		}
	}

}
