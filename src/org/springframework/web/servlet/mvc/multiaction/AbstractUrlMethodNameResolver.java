package org.springframework.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.util.UrlPathHelper;

/**
 * Abstract base class for URL-based MethodNameResolver implementations.
 *
 * <p>Provides infrastructure for mapping handlers to URLs and configurable
 * URL lookup. For information on the latter, see alwaysUseFullPath property.
 *
 * @author Juergen Hoeller
 * @since 14.01.2004
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 */
public abstract class AbstractUrlMethodNameResolver implements MethodNameResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	private final UrlPathHelper urlPathHelper = new UrlPathHelper();

	/**
	 * Set if URL lookup should always use full path within current servlet
	 * context. Else, the path within the current servlet mapping is used
	 * if applicable (i.e. in the case of a ".../*" servlet mapping in web.xml).
	 * Default is false.
	 * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
	}

	/**
	 * Set if context path and request URI should be URL-decoded.
	 * Both are returned <i>undecoded</i> by the Servlet API,
	 * in contrast to the servlet path.
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * <p>Note: Setting this to true requires J2SE 1.4, as J2SE 1.3's
	 * URLDecoder class does not offer a way to specify the encoding.
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
	}

	/**
	 * This implementation of getHandlerMethodName retrieves the URL path
	 * to use for lookup and delegates to getHandlerMethodNameForUrlPath.
	 * Converts null values to NoSuchRequestHandlingMethodExceptions.
	 */
	public final String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
		String urlPath = this.urlPathHelper.getLookupPathForRequest(request);
		String name = getHandlerMethodNameForUrlPath(urlPath);
		if (name == null) {
			throw new NoSuchRequestHandlingMethodException(request);
		}
		logger.debug("Returning MultiActionController method name '" + name + "' for lookup path '" + urlPath + "'");
		return name;
	}

	/**
	 * Return a method name that can handle this request, based on the
	 * given lookup path. Called by this class' getHandlerMethodName.
	 * @param urlPath the URL path to use for lookup,
	 * according to the settings in this class
	 * @return a method name that can handle this request.
	 * Should return null if no matching method found.
	 * @see #getHandlerMethodName
	 * @see #setAlwaysUseFullPath
	 * @see #setUrlDecode
	 */
	protected abstract String getHandlerMethodNameForUrlPath(String urlPath);

}
