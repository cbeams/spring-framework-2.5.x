package org.springframework.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet 2.3 Filter that allows to specify a character encoding for requests.
 * This is useful because current browsers typically do not set a character
 * encoding even if specified in the HTML page respectively form.
 *
 * <p>Can either just apply this filter's encoding if the request does not
 * already specify an encoding, or apply this filter's encoding in any case.
 *
 * @author Juergen Hoeller
 * @since 15.03.2004
 * @see #setEncoding
 * @see #setForceEncoding
 */
public class CharacterEncodingFilter extends OncePerRequestFilter {

	private String encoding;

	private boolean forceEncoding;

	/**
	 * Set the encoding to use for requests. This encoding will be
	 * passed into a ServletRequest.setCharacterEncoding call.
	 * <p>Whether this encoding will override existing request
	 * encodings depends on the "forceEncoding" flag.
	 * @see #setForceEncoding
	 * @see javax.servlet.ServletRequest#setCharacterEncoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set whether the encoding of this filter should override existing
	 * request encodings. Default is false, i.e. do not modify encoding
	 * if ServletRequest.getCharacterEncoding returns a non-null value.
	 * @see #setEncoding
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 */
	public void setForceEncoding(boolean forceEncoding) {
		this.forceEncoding = forceEncoding;
	}

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		if (this.forceEncoding || request.getCharacterEncoding() == null) {
			request.setCharacterEncoding(this.encoding);
		}
		filterChain.doFilter(request, response);
	}

}
