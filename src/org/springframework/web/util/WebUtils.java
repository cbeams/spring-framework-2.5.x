package org.springframework.web.util;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;

/**
 * Miscellaneous utilities for web applications.
 * Also used by various framework classes.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class WebUtils {

	/**
	 * Web app root key parameter at the servlet context level
	 * (i.e. web.xml): "webAppRootKey".
	 */
	public static final String WEB_APP_ROOT_KEY_PARAM = "webAppRootKey";

	/** Default web app root key: "webapp.root" */
	public static final String DEFAULT_WEB_APP_ROOT_KEY = "webapp.root";

	/**
	 * Standard Servlet spec context attribute that specifies a temporary
	 * directory for the current web application, of type java.io.File
	 */
	public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

	/**
	 * Standard servlet spec request attributes for include URI and paths.
	 * <p>If included via a RequestDispatcher, the current resource will see the
	 * original request. Its own URI and paths are exposed as request attributes. 
	 */
	public static final String INCLUDE_URI_REQUEST_ATTRIBUTE = "javax.servlet.include.request_uri";
	public static final String INCLUDE_CONTEXT_PATH_REQUEST_ATTRIBUTE = "javax.servlet.include.context_path";
	public static final String INCLUDE_SERVLET_PATH_REQUEST_ATTRIBUTE = "javax.servlet.include.servlet_path";

	/** Name suffixes in case of image buttons */
	public static final String[] SUBMIT_IMAGE_SUFFIXES = {".x", ".y"};

	/**
	 * Set a system property to the web application root directory.
	 * The key of the system property can be defined with the "webAppRootKey"
	 * context-param in web.xml. Default is "webapp.root".
	 * <p>Can be used for toolkits that support substition with System.getProperty
	 * values, like Log4J's "${key}" syntax within log file locations.
	 * @param servletContext the servlet context of the web application
	 * @throws IllegalStateException if the system property is already set,
	 * or if the WAR file is not expanded
	 * @see #WEB_APP_ROOT_KEY_PARAM
	 * @see #DEFAULT_WEB_APP_ROOT_KEY
	 * @see WebAppRootListener
	 */
	public static void setWebAppRootSystemProperty(ServletContext servletContext) throws IllegalStateException {
		String param = servletContext.getInitParameter(WEB_APP_ROOT_KEY_PARAM);
		String key = (param != null ? param : DEFAULT_WEB_APP_ROOT_KEY);
		String oldValue = System.getProperty(key);
		if (oldValue != null) {
			throw new IllegalStateException("WARNING: Web app root system property already set: " + key + " = " +
																			oldValue + " - Choose unique webAppRootKey values in your web.xml files!");
		}
		String root = servletContext.getRealPath("/");
		if (root == null) {
			throw new IllegalStateException("Cannot set web app root system property when WAR file is not expanded");
		}
		System.setProperty(key, root);
		servletContext.log("Set web app root system property: " + key + " = " + root);
	}

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @param servletContext the servlet context of the web application
	 * @return the File representing the temporary directory
	 */
	public static File getTempDir(ServletContext servletContext) {
		return (File) servletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
	}

	/**
	 * Check the given request for a session attribute of the given name.
	 * Returns null if there is no session or if the session has no such attribute.
	 * Does not create a new session if none has existed before!
	 * @param request current HTTP request
	 * @param name the name of the session attribute
	 * @return the value of the session attribute, or null if not found
	 */
	public static Object getSessionAttribute(HttpServletRequest request, String name) {
		HttpSession session = request.getSession(false);
		return (session != null ? session.getAttribute(name) : null);
	}

	/**
	 * Set the session attribute with the given name to the given value.
	 * Removes the session attribute if value is null, if a session existed at all.
	 * Does not create a new session on remove if none has existed before!
	 * @param request current HTTP request
	 * @param name the name of the session attribute
	 */
	public static void setSessionAttribute(HttpServletRequest request, String name, Object value) {
		if (value != null) {
			request.getSession().setAttribute(name, value);
		}
		else {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.removeAttribute(name);
			}
		}
	}

	/**
	 * Get the specified session attribute, creating and setting a new attribute if
	 * no existing found. The given class needs to have a public no-arg constructor.
	 * Useful for on-demand state objects in a web tier, like shopping carts.
	 * @param request current HTTP request
	 * @param name the name of the session attribute
	 * @param clazz the class to instantiate for a new attribute
	 * @return the value of the session attribute, newly created if not found
	 */
	public static Object getOrCreateSessionAttribute(HttpServletRequest request, String name, Class clazz) {
		Object sessionObject = getSessionAttribute(request, name);
		if (sessionObject == null) {
			sessionObject = BeanUtils.instantiateClass(clazz);
			request.getSession(true).setAttribute(name, sessionObject);
		}
		return sessionObject;
	}

	/**
	 * Retrieve the first cookie with the given name. Note that multiple
	 * cookies can have the same name but different paths or domains.
	 * @param name cookie name
	 * @return the first cookie with the given name, or null if none is found
	 */
	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie cookies[] = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (name.equals(cookies[i].getName()))
					return cookies[i];
			}
		}
		return null;
	}

	/**
	 * Return the URL of the root of the current application.
	 * @param request current HTTP request
	 */
	public static String getUrlToApplication(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
	}

	/**
	 * Return the correct request URI for the given request.
	 * <p>Regards include request URL if called within a RequestDispatcher include.
	 * <p>The URI that the web container resolves <i>should</i> be correct, but some
	 * containers like JBoss/Jetty incorrectly include ";" strings like ";jsessionid"
	 * in the URI. This method cuts off such incorrect appendices.
	 * @param request current HTTP request
	 * @return the correct request URI
	 */
	public static String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(INCLUDE_URI_REQUEST_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		int semicolonIndex = uri.indexOf(';');
		return (semicolonIndex != -1 ? uri.substring(0, semicolonIndex) : uri);
	}

	/**
	 * Return the path within the web application for the given request.
	 * <p>Regards include request URL if called within a RequestDispatcher include.
	 * @param request current HTTP request
	 * @return the path within the web application
	 */
	public static String getPathWithinApplication(HttpServletRequest request) {
		String contextPath = (String) request.getAttribute(INCLUDE_CONTEXT_PATH_REQUEST_ATTRIBUTE);
		if (contextPath == null) {
			contextPath = request.getContextPath();
		}
		return getRequestUri(request).substring(contextPath.length());
	}

	/**
	 * Return the path within the servlet mapping for the given request,
	 * i.e. the part of the request's URL beyond the part that called the servlet,
	 * or "" if the whole URL has been used to identify the servlet.
	 * <p>Regards include request URL if called within a RequestDispatcher include.
	 * <p>E.g.: servlet mapping = "/test/*"; request URI = "/test/a" -> "/a".
	 * <p>E.g.: servlet mapping = "/test"; request URI = "/test" -> "".
	 * <p>E.g.: servlet mapping = "/*.test"; request URI = "/a.test" -> "".
	 * @param request current HTTP request
	 * @return the path within the servlet mapping, or ""
	 */
	public static String getPathWithinServletMapping(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(INCLUDE_SERVLET_PATH_REQUEST_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		return getPathWithinApplication(request).substring(servletPath.length());
	}

	/**
	 * Return the mapping lookup path for the given request, within the current
	 * servlet mapping if applicable, else within the web application.
	 * <p>Regards include request URL if called within a RequestDispatcher include.
	 * @param request current HTTP request
	 * @param alwaysUseFullPath if the full path within the context
	 * should be used in any case
	 * @return the lookup path
	 * @see #getPathWithinApplication
	 * @see #getPathWithinServletMapping
	 */
	public static String getLookupPathForRequest(HttpServletRequest request, boolean alwaysUseFullPath) {
		// always use full path within current servlet context?
		if (alwaysUseFullPath) {
			return WebUtils.getPathWithinApplication(request);
		}
		// else use path within current servlet mapping if applicable
		String rest = WebUtils.getPathWithinServletMapping(request);
		if (!"".equals(rest))
			return rest;
		else
			return WebUtils.getPathWithinApplication(request);
	}

	/**
	 * Given a servlet path string, determine the directory within the WAR
	 * this belongs to, ending with a /. For example, /cat/dog/test.html would be
	 * returned as /cat/dog/. /test.html would be returned as /
	 */
	public static String getDirectoryForServletPath(String servletPath) {
		// Arg will be of form /dog/cat.jsp. We want to see /dog/
		if (servletPath == null || servletPath.indexOf("/") == -1)
			return "/";
		String left = servletPath.substring(0, servletPath.lastIndexOf("/") + 1);
		return left;
	}

	/**
	 * Convenience method to return a map from un-prefixed property names
	 * to values. E.g. with a prefix of price, price_1, price_2 produce
	 * a properties object with mappings for 1, 2 to the same values.
	 * Maps single values to String and multiple values to String array.
	 * @param request HTTP request in which to look for parameters
	 * @param base beginning of parameter name
	 * (if this is null or the empty string, all parameters will match)
	 * @return map containing request parameters <b>without the prefix</b>,
	 * containing either a String or a String[] as values
	 */
	public static Map getParametersStartingWith(ServletRequest request, String base) {
		Enumeration enum = request.getParameterNames();
		Map params = new HashMap();
		if (base == null) {
			base = "";
		}
		while (enum != null && enum.hasMoreElements()) {
			String paramName = (String) enum.nextElement();
			if (base == null || "".equals(base) || paramName.startsWith(base)) {
				String unprefixed = paramName.substring(base.length());
				String[] values = request.getParameterValues(paramName);
				if (values == null) {
					// do nothing, no values found at all
				}
				else if (values.length > 1) {
					params.put(unprefixed, values);
				}
				else {
					params.put(unprefixed, values[0]);
				}
			}
		}
		return params;
	}

	/**
	 * Checks if a specific input type="submit" parameter was sent in the request,
	 * either via a button (directly with name) or via an image (name + ".x" or
	 * name + ".y").
	 * @param request current HTTP request
	 * @param name name of the parameter
	 * @return if the parameter was sent
	 * @see #SUBMIT_IMAGE_SUFFIXES
	 */
	public static boolean hasSubmitParameter(ServletRequest request, String name) {
		if (request.getParameter(name) != null) {
			return true;
		}
		for (int i = 0; i < SUBMIT_IMAGE_SUFFIXES.length; i++) {
			String suffix = SUBMIT_IMAGE_SUFFIXES[i];
			if (request.getParameter(name + suffix) != null) {
				return true;
			}
		}
		return false;
	}

}
