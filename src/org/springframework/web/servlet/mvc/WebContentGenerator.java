package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationObjectSupport;

/**
 * Convenient superclass for any kind of web content generator,
 * like AbstractController and MultiActionController.
 *
 * <p>Supports HTTP cache control options. The usage of respective
 * HTTP headers can be determined via the "useExpiresHeader" and
 * "userCacheControlHeader" properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setUseExpiresHeader
 * @see #setUseCacheControlHeader
 */
public class WebContentGenerator extends WebApplicationObjectSupport {
	
	/** Use HTTP 1.0 expires header? */
	private boolean useExpiresHeader = true;

	/** Use HTTP 1.1 cache-control header? */
	private boolean useCacheControlHeader = true;

	/**
	 * Set whether to use the HTTP 1.0 expires header. Default is true.
	 * <p>Note: Cache headers will only get applied if caching is enabled
	 * for the current request.
	 */
	public final void setUseExpiresHeader(boolean useExpiresHeader) {
		this.useExpiresHeader = useExpiresHeader;
	}

	/**
	 * Set whether to use the HTTP 1.1 cache-control header. Default is true.
	 * <p>Note: Cache headers will only get applied if caching is enabled
	 * for the current request.
	 */
	public final void setUseCacheControlHeader(boolean useCacheControlHeader) {
		this.useCacheControlHeader = useCacheControlHeader;
	}

	/**
	 * Prevent the response being cached.
	 * See www.mnot.net.cache docs.
	 */
	protected final void preventCaching(HttpServletResponse response) {
		response.setHeader("Pragma", "No-cache");
		if (this.useExpiresHeader) {
			// HTTP 1.0 header
			response.setDateHeader("Expires", 1L);
		}
		if (this.useCacheControlHeader) {
			// HTTP 1.1 header
			response.setHeader("Cache-Control", "no-cache");
		}
	}

	/**
	 * Set HTTP headers to allow caching for the given number of seconds.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds number of seconds into the future that the response
	 * should be cacheable for
	 * @see #cacheForSeconds(HttpServletResponse, int, boolean)
	 */
	protected final void cacheForSeconds(HttpServletResponse response, int seconds) {
		cacheForSeconds(response, seconds, false);
	}

	/**
	 * Set HTTP headers to allow caching for the given number of seconds.
	 * Tells the browser to revalidate the resource if mustRevalidate is true.
	 * @param response current HTTP response
	 * @param seconds number of seconds into the future that the response
	 * should be cacheable for
	 * @param mustRevalidate whether the client should revalidate the resource
	 * (typically only necessary for controllers with last-modified support)
	 */
	protected final void cacheForSeconds(HttpServletResponse response, int seconds, boolean mustRevalidate) {
		if (this.useExpiresHeader) {
			// HTTP 1.0 header
			response.setDateHeader("Expires", System.currentTimeMillis() + seconds * 1000L);
		}
		if (this.useCacheControlHeader) {
			// HTTP 1.1 header
			String hval = "max-age=" + seconds;
			if (mustRevalidate) {
				hval += ", must-revalidate";
			}
			response.setHeader("Cache-Control", hval);
		}
	}

	/**
	 * Apply the given cache seconds and generate respective HTTP headers,
	 * i.e. allow caching for the given number of seconds in case of a positive
	 * value, prevent caching if given a 0 value, do nothing else.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching,
	 * @see #cacheForSeconds(HttpServletResponse, int, boolean)
	 */
	protected final void applyCacheSeconds(HttpServletResponse response, int seconds) {
		applyCacheSeconds(response, seconds, false);
	}

	/**
	 * Apply the given cache seconds and generate respective HTTP headers,
	 * i.e. allow caching for the given number of seconds in case of a positive
	 * value, prevent caching if given a 0 value, do nothing else.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds number of seconds into the future that the response
	 * should be cacheable for
	 * @param mustRevalidate whether the client should revalidate the resource
	 * (typically only necessary for controllers with last-modified support)
	 */
	protected final void applyCacheSeconds(HttpServletResponse response, int seconds, boolean mustRevalidate) {
		if (seconds > 0) {
			cacheForSeconds(response, seconds, mustRevalidate);
		}
		else if (seconds == 0) {
			preventCaching(response);
		}
		// Leave caching to the client otherwise
	}

}
