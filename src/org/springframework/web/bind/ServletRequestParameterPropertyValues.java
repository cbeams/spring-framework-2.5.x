package org.springframework.web.bind;

import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.web.util.WebUtils;

/**
 * PropertyValues implementation created from parameters in a ServletRequest.
 * Looks for all property values beginning with a certain prefix
 * and prefix separator. This class is immutable once initialized.
 * @author Rod Johnson
 * @version $Id: ServletRequestParameterPropertyValues.java,v 1.2 2003-10-08 14:16:19 jhoeller Exp $
 */
public class ServletRequestParameterPropertyValues implements PropertyValues {

	protected static final Log logger = LogFactory.getLog(ServletRequestParameterPropertyValues.class);

	/** Default prefix separator */
	public static final String DEFAULT_PREFIX_SEPARATOR = "_";

	/**
	 * PropertyValues delegate. We use delegation rather than simply subclass
	 * MutablePropertyValues as we don't want to expose MutablePropertyValues's
	 * update methods. This class is immutable once initialized.
	 */
	private MutablePropertyValues mutablePropertyValues;

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
	 * Create new ServletRequestPropertyValues using no prefix
	 * (and hence, no prefix separator).
	 * @param request HTTP Request
	 */
	public ServletRequestParameterPropertyValues(ServletRequest request) {
		this(request, null, null);
	}

	/**
	 * Create new ServletRequestPropertyValues supplying both prefix and prefix separator.
	 * @param request HTTP Request
	 * @param prefix prefix for properties
	 * @param prefixSeparator Separator delimiting prefix (e.g. user) from property name
	 * (e.g. age) to build a request parameter name such as user_age
	 */
	public ServletRequestParameterPropertyValues(ServletRequest request, String prefix, String prefixSeparator) {
		String base = (prefix != null) ? prefix + prefixSeparator : null;
		Map params = WebUtils.getParametersStartingWith(request, base);
		this.mutablePropertyValues = new MutablePropertyValues(params);
		if (logger.isDebugEnabled()) {
			logger.debug("Found PropertyValues in request: " + mutablePropertyValues);
    }
	}

	public PropertyValue[] getPropertyValues() {
		// We simply let the delegate handle this
		return mutablePropertyValues.getPropertyValues();
	}

	public boolean contains(String propertyName) {
		// Just pass it to the delegate...
		return mutablePropertyValues.contains(propertyName);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		// Just pass it to the delegate...
		return mutablePropertyValues.getPropertyValue(propertyName);
	}

	public PropertyValues changesSince(PropertyValues old) {
		// Just pass it to the delegate...
		return mutablePropertyValues.changesSince(old);
	}

}
