/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.servlet.support;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationObjectSupport;

/**
 * Convenient superclass for any kind of web content generator,
 * like AbstractController and WebContentInterceptor. Can also be
 * used for custom handlers that have their own HandlerAdapter.
 *
 * <p>Supports HTTP cache control options. The usage of corresponding
 * HTTP headers can be determined via the "useExpiresHeader" and
 * "userCacheControlHeader" properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setUseExpiresHeader
 * @see #setUseCacheControlHeader
 * @see org.springframework.web.servlet.mvc.AbstractController
 * @see org.springframework.web.servlet.mvc.WebContentInterceptor
 */
public abstract class WebContentGenerator extends WebApplicationObjectSupport {

	public static final String METHOD_GET = "GET";

	public static final String METHOD_POST = "POST";

	public static final String HEADER_PRAGMA = "Pragma";

	public static final String HEADER_EXPIRES = "Expires";

	public static final String HEADER_CACHE_CONTROL = "Cache-Control";


	/** Set of supported methods. GET and POST by default. */
	private Set	supportedMethods;

	private boolean requireSession = false;

	/** Use HTTP 1.0 expires header? */
	private boolean useExpiresHeader = true;

	/** Use HTTP 1.1 cache-control header? */
	private boolean useCacheControlHeader = true;

	private int cacheSeconds = -1;


	/**
	 * Create a new WebContentGenerator supporting GET and POST methods.
	 */
	public WebContentGenerator() {
		this.supportedMethods = new HashSet();
		this.supportedMethods.add(METHOD_GET);
		this.supportedMethods.add(METHOD_POST);
	}

	/**
	 * Set the HTTP methods that this content generator should support.
	 * Default is GET and POST.
	 */
	public final void setSupportedMethods(String[] supportedMethodsArray) {
		if (supportedMethodsArray == null || supportedMethodsArray.length == 0) {
			throw new IllegalArgumentException("supportedMethods must not be empty");
		}
		this.supportedMethods.clear();
		for (int i = 0; i < supportedMethodsArray.length; i++) {
			this.supportedMethods.add(supportedMethodsArray[i]);
		}
	}

	/**
	 * Return the HTTP methods that this content generator supports.
	 */
	public final String[] getSupportedMethods() {
		return (String[]) this.supportedMethods.toArray(new String[this.supportedMethods.size()]);
	}

	/**
	 * Set whether a session should be required to handle requests.
	 */
	public final void setRequireSession(boolean requireSession) {
		this.requireSession = requireSession;
	}

	/**
	 * Return whether a session is required to handle requests.
	 */
	public final boolean isRequireSession() {
		return requireSession;
	}

	/**
	 * Set whether to use the HTTP 1.0 expires header. Default is true.
	 * <p>Note: Cache headers will only get applied if caching is enabled
	 * for the current request.
	 */
	public final void setUseExpiresHeader(boolean useExpiresHeader) {
		this.useExpiresHeader = useExpiresHeader;
	}

	/**
	 * Return whether the HTTP 1.0 expires header is used.
	 */
	public final boolean isUseExpiresHeader() {
		return useExpiresHeader;
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
	 * Return whether the HTTP 1.1 cache-control header is used.
	 */
	public final boolean isUseCacheControlHeader() {
		return useCacheControlHeader;
	}

	/**
	 * Cache content for the given number of seconds. Default is -1,
	 * indicating no generation of cache-related headers.
	 * <p>Only if this is set to 0 (no cache) or a positive value (cache for
	 * this many seconds) will this class generate cache headers.
	 * <p>The headers can be overwritten by subclasses, before content is generated.
	 */
	public final void setCacheSeconds(int seconds) {
		this.cacheSeconds = seconds;
	}

	/**
	 * Return the number of seconds that content is cached.
	 */
	public final int getCacheSeconds() {
		return cacheSeconds;
	}


	/**
	 * Check and prepare the given request and response according to the settings
	 * of this generator. Checks for supported methods and a required session,
	 * and applies the number of cache seconds specified for this generator.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param lastModified if the mapped handler provides Last-Modified support
	 * @throws ServletException if the request cannot be handled because a check failed
	 */
	protected final void checkAndPrepare(
			HttpServletRequest request, HttpServletResponse response, boolean lastModified)
	    throws ServletException {
		checkAndPrepare(request, response, this.cacheSeconds, lastModified);
	}

	/**
	 * Check and prepare the given request and response according to the settings
	 * of this generator. Checks for supported methods and a required session,
	 * and applies the given number of cache seconds.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param cacheSeconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching
	 * @param lastModified if the mapped handler provides Last-Modified support
	 * @throws ServletException if the request cannot be handled because a check failed
	 */
	protected final void checkAndPrepare(
			HttpServletRequest request, HttpServletResponse response, int cacheSeconds, boolean lastModified)
	    throws ServletException {

		// check whether we should support the request method
		String method = request.getMethod();
		if (!this.supportedMethods.contains(method)) {
			throw new RequestMethodNotSupportedException("Request method '" + method + "' not supported");
		}

		// check whether session is required
		if (this.requireSession) {
			if (request.getSession(false) == null) {
				throw new SessionRequiredException("Pre-existing session required but none found");
			}
		}

		// Do declarative cache control.
		// Revalidate if the controller supports last-modified.
		applyCacheSeconds(response, cacheSeconds, lastModified);
	}

	/**
	 * Prevent the response from being cached.
	 * See www.mnot.net.cache docs.
	 */
	protected final void preventCaching(HttpServletResponse response) {
		response.setHeader(HEADER_PRAGMA, "No-cache");
		if (this.useExpiresHeader) {
			// HTTP 1.0 header
			response.setDateHeader(HEADER_EXPIRES, 1L);
		}
		if (this.useCacheControlHeader) {
			// HTTP 1.1 header
			response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
		}
	}

	/**
	 * Set HTTP headers to allow caching for the given number of seconds.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds number of seconds into the future that the response
	 * should be cacheable for
	 * @see #cacheForSeconds(javax.servlet.http.HttpServletResponse, int, boolean)
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
	protected final void cacheForSeconds(
			HttpServletResponse response, int seconds, boolean mustRevalidate) {
		if (this.useExpiresHeader) {
			// HTTP 1.0 header
			response.setDateHeader(HEADER_EXPIRES, System.currentTimeMillis() + seconds * 1000L);
		}
		if (this.useCacheControlHeader) {
			// HTTP 1.1 header
			String headerValue = "max-age=" + seconds;
			if (mustRevalidate) {
				headerValue += ", must-revalidate";
			}
			response.setHeader(HEADER_CACHE_CONTROL, headerValue);
		}
	}

	/**
	 * Apply the given cache seconds and generate corresponding HTTP headers,
	 * i.e. allow caching for the given number of seconds in case of a positive
	 * value, prevent caching if given a 0 value, do nothing else.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching
	 * @see #cacheForSeconds(javax.servlet.http.HttpServletResponse, int, boolean)
	 */
	protected final void applyCacheSeconds(HttpServletResponse response, int seconds) {
		applyCacheSeconds(response, seconds, false);
	}

	/**
	 * Apply the given cache seconds and generate respective HTTP headers,
	 * i.e. allow caching for the given number of seconds in case of a positive
	 * value, prevent caching if given a 0 value, do nothing else.
	 * @param response current HTTP response
	 * @param seconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching
	 * @param mustRevalidate whether the client should revalidate the resource
	 * (typically only necessary for controllers with last-modified support)
	 */
	protected final void applyCacheSeconds(
			HttpServletResponse response, int seconds, boolean mustRevalidate) {
		if (seconds > 0) {
			cacheForSeconds(response, seconds, mustRevalidate);
		}
		else if (seconds == 0) {
			preventCaching(response);
		}
		// leave caching to the client otherwise
	}

}
