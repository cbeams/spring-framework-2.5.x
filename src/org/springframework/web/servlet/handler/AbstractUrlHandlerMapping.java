package org.springframework.web.servlet.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

/**
 * Abstract base class for URL-mapped HandlerMapping implementations.
 * Provides infrastructure for mapping handlers to URLs and configurable
 * URL lookup. For information on the latter, see alwaysUseFullPath property.
 *
 * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
 * and various Ant-style pattern matches, e.g. a registered "/t*" matches
 * both "/test" and "/team". For details, see the PathMatcher class.
 *
 * @author Juergen Hoeller
 * @since 16.04.2003
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 * @see org.springframework.util.PathMatcher
 */
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping {

	private final UrlPathHelper urlPathHelper = new UrlPathHelper();

	private Map handlerMap = new HashMap();

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
	 * Look up a handler for the URL path of the given request.
	 * @param request current HTTP request
	 * @return the looked up handler instance, or null
	 */
	protected Object getHandlerInternal(HttpServletRequest request) throws BeansException {
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		logger.debug("Looking up handler for [" + lookupPath + "]");
		return lookupHandler(lookupPath);
	}

	/**
	 * Look up a handler instance for the given URL path.
	 * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
	 * and various Ant-style pattern matches, e.g. a registered "/t*" matches
	 * both "/test" and "/team". For details, see the PathMatcher class.
	 * @param urlPath URL the bean is mapped to
	 * @return the associated handler instance, or null if not found
	 * @see org.springframework.util.PathMatcher
	 */
	protected Object lookupHandler(String urlPath) throws BeansException {
		// direct match?
		Object handler = this.handlerMap.get(urlPath);
		if (handler == null) {
			// pattern match?
			for (Iterator it = this.handlerMap.keySet().iterator(); it.hasNext();) {
				String registeredPath = (String) it.next();
				if (PathMatcher.match(registeredPath, urlPath)) {
					handler = this.handlerMap.get(registeredPath);
				}
			}
		}
		return handler;
	}

	/**
	 * Register the given handler instance for the given URL path.
	 * @param urlPath URL the bean is mapped to
	 * @param handler the handler instance
	 * @throws BeansException if the handler couldn't be registered
	 */
	protected void registerHandler(String urlPath, Object handler) throws BeansException {
		Object mappedHandler = this.handlerMap.get(urlPath);
		if (mappedHandler != null) {
			throw new ApplicationContextException("Cannot map handler [" + handler + "] to URL path [" + urlPath +
			                                      "]: there's already handler [" + mappedHandler + "] mapped");
		}

		// eagerly resolve handler if referencing singleton via name
		if (handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				handler = getApplicationContext().getBean(handlerName);
			}
		}

		if (urlPath.equals("/*")) {
			setDefaultHandler(handler);
		}
		else {
			this.handlerMap.put(urlPath, handler);
			logger.info("Mapped URL path [" + urlPath + "] onto handler [" + handler + "]");
		}
	}

}
