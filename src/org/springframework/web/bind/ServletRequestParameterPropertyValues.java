package org.springframework.web.bind;

import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.util.WebUtils;

/**
 * PropertyValues implementation created from parameters in a ServletRequest.
 * Looks for all property values beginning with a certain prefix
 * and prefix separator.
 *
 * <p>This class is not immutable to be able to efficiently remove property
 * values that should be ignored for binding.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: ServletRequestParameterPropertyValues.java,v 1.3 2004-02-27 13:05:45 jhoeller Exp $
 */
public class ServletRequestParameterPropertyValues extends MutablePropertyValues {

	/** Default prefix separator */
	public static final String DEFAULT_PREFIX_SEPARATOR = "_";

	/**
	 * Create new ServletRequestPropertyValues using no prefix
	 * (and hence, no prefix separator).
	 * @param request HTTP Request
	 */
	public ServletRequestParameterPropertyValues(ServletRequest request) {
		this(request, null, null);
	}

	/**
	 * Create new ServletRequestPropertyValues using the default prefix
	 * separator and the given prefix (the underscore character "_").
	 * @param request HTTP Request
	 * @param prefix prefix for properties
	 */
	public ServletRequestParameterPropertyValues(ServletRequest request, String prefix) {
		this(request, prefix, DEFAULT_PREFIX_SEPARATOR);
	}

	/**
	 * Create new ServletRequestPropertyValues supplying both prefix and prefix separator.
	 * @param request HTTP Request
	 * @param prefix prefix for properties
	 * @param prefixSeparator Separator delimiting prefix (e.g. user) from property name
	 * (e.g. age) to build a request parameter name such as user_age
	 */
	public ServletRequestParameterPropertyValues(ServletRequest request, String prefix, String prefixSeparator) {
		super(WebUtils.getParametersStartingWith(request, (prefix != null) ? prefix + prefixSeparator : null));
	}

}
