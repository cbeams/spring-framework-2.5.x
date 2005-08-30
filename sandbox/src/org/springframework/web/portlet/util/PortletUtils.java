/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.portlet.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

/**
 * Miscellaneous utilities for portlet applications.
 * Used by various framework classes.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 */
public abstract class PortletUtils extends org.springframework.web.util.WebUtils {

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the portlet container.
	 * @param portletContext the portlet context of the web application
	 * @return the File representing the temporary directory
	 */
	public static File getTempDir(PortletContext portletContext) {
		return (File) portletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
	}

	/**
	 * Return the real path of the given path within the web application,
	 * as provided by the portlet container.
	 * <p>Prepends a slash if the path does not already start with a slash,
	 * and throws a FileNotFoundException if the path cannot be resolved to
	 * a resource (in contrast to PortletContext's <code>getRealPath</code>,
	 * which returns null).
	 * @param portletContext the portlet context of the web application
	 * @param path the relative path within the web application
	 * @return the corresponding real path
	 * @throws FileNotFoundException if the path cannot be resolved to a resource
	 * @see javax.portlet.PortletContext#getRealPath
	 */
	public static String getRealPath(PortletContext portletContext, String path) throws FileNotFoundException {
		// Interpret location as relative to the web application root directory.
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String realPath = portletContext.getRealPath(path);
		if (realPath == null) {
			throw new FileNotFoundException(
					"PortletContext resource [" + path + "] cannot be resolved to absolute file path - " +
					"web application archive not expanded?");
		}
		return realPath;
	}

	/**
	 * Check the given request for a session attribute of the given name under the <code>PORTLET_SCOPE</code>.
	 * Returns null if there is no session or if the session has no such attribute in that scope.
	 * Does not create a new session if none has existed before!
	 * @param request current portlet request
	 * @param name the name of the session attribute
	 * @return the value of the session attribute, or null if not found
	 */
	public static Object getSessionAttribute(PortletRequest request, String name) {
		return getSessionAttribute(request, name, PortletSession.PORTLET_SCOPE);
	}

	/**
	 * Check the given request for a session attribute of the given name in the given scope.
	 * Returns null if there is no session or if the session has no such attribute in that scope.
	 * Does not create a new session if none has existed before!
	 * @param request current portlet request
	 * @param name the name of the session attribute
	 * @param scope session scope of this attribute
	 * @return the value of the session attribute, or null if not found
	 */
	public static Object getSessionAttribute(PortletRequest request, String name, int scope) {
		PortletSession session = request.getPortletSession(false);
		return (session != null ? session.getAttribute(name, scope) : null);
	}

	/**
	 * Check the given request for a session attribute of the given name under the <code>PORTLET_SCOPE</code>.
	 * Throws an exception if there is no session or if the session has no such attribute in that scope.
	 * Does not create a new session if none has existed before!
	 * @param request current portlet request
	 * @param name the name of the session attribute
	 * @return the value of the session attribute
	 * @throws IllegalStateException if the session attribute could not be found
	 */
	public static Object getRequiredSessionAttribute(PortletRequest request, String name)
			throws IllegalStateException {
		return getRequiredSessionAttribute(request, name, PortletSession.PORTLET_SCOPE);
	}

	/**
	 * Check the given request for a session attribute of the given name in the given scope.
	 * Throws an exception if there is no session or if the session has no such attribute in that scope.
	 * Does not create a new session if none has existed before!
	 * @param request current portlet request
	 * @param name the name of the session attribute
	 * @param scope session scope of this attribute
	 * @return the value of the session attribute
	 * @throws IllegalStateException if the session attribute could not be found
	 */
	public static Object getRequiredSessionAttribute(PortletRequest request, String name, int scope)
	    	throws IllegalStateException {
		Object attr = getSessionAttribute(request, name, scope);
		if (attr == null)
			throw new IllegalStateException("No session attribute '" + name + "' found");
		return attr;
	}

	/**
	 * Set the session attribute with the given name to the given value under the <code>PORTLET_SCOPE</code>.
	 * Removes the session attribute if value is null, if a session existed at all.
	 * Does not create a new session if not necessary!
	 * @param request current portlet request
	 * @param name the name of the session attribute
	 * @param value the value of the session attribute
	 */
	public static void setSessionAttribute(PortletRequest request, String name, Object value) {
	    setSessionAttribute(request, name, value, PortletSession.PORTLET_SCOPE);
	}

	/**
	 * Set the session attribute with the given name to the given value in the given scope.
	 * Removes the session attribute if value is null, if a session existed at all.
	 * Does not create a new session if not necessary!
	 * @param request current portlet request
	 * @param name the name of the session attribute
	 * @param value the value of the session attribute
	 * @param scope session scope of this attribute
	 */
	public static void setSessionAttribute(PortletRequest request, String name, Object value, int scope) {
		if (value != null) {
			request.getPortletSession().setAttribute(name, value, scope);
		}
		else {
			PortletSession session = request.getPortletSession(false);
			if (session != null)
				session.removeAttribute(name, scope);
		}
	}

	/**
	 * Get the specified session attribute under the <code>PORTLET_SCOPE</code>,
	 * creating and setting a new attribute if no existing found. The given class 
	 * needs to have a public no-arg constructor.
	 * Useful for on-demand state objects in a web tier, like shopping carts.
	 * @param session current portlet session
	 * @param name the name of the session attribute
	 * @param clazz the class to instantiate for a new attribute
	 * @return the value of the session attribute, newly created if not found
	 * @throws IllegalArgumentException if the session attribute could not be instantiated
	 */
	public static Object getOrCreateSessionAttribute(PortletSession session, String name, Class clazz)
			throws IllegalArgumentException {
		return getOrCreateSessionAttribute(session, name, clazz, PortletSession.PORTLET_SCOPE);
	}

	/**
	 * Get the specified session attribute in the given scope,
	 * creating and setting a new attribute if no existing found. The given class 
	 * needs to have a public no-arg constructor.
	 * Useful for on-demand state objects in a web tier, like shopping carts.
	 * @param session current portlet session
	 * @param name the name of the session attribute
	 * @param clazz the class to instantiate for a new attribute
	 * @param scope session scope of this attribute
	 * @return the value of the session attribute, newly created if not found
	 * @throws IllegalArgumentException if the session attribute could not be instantiated
	 */
	public static Object getOrCreateSessionAttribute(PortletSession session, String name, Class clazz, int scope)
			throws IllegalArgumentException {
		Object sessionObject = session.getAttribute(name, scope);
		if (sessionObject == null) {
			try {
				sessionObject = clazz.newInstance();
			}
			catch (InstantiationException ex) {
				throw new IllegalArgumentException("Could not instantiate class [" + clazz.getName() +
				                                   "] for session attribute '" + name + "': " + ex.getMessage());
			}
			catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Could not access default constructor of class [" + clazz.getName() +
				                                   "] for session attribute '" + name + "': " + ex.getMessage());
			}
			session.setAttribute(name, sessionObject, scope);
		}
		return sessionObject;
	}

	/**
	 * Expose the given Map as request attributes, using the keys as attribute names
	 * and the values as corresponding attribute values. Keys need to be Strings.
	 * @param request current portlet request
	 * @param attributes the attributes Map
	 * @throws IllegalArgumentException if an invalid key is found in the Map
	 */
	public static void exposeRequestAttributes(PortletRequest request, Map attributes)
			throws IllegalArgumentException {
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			if (!(entry.getKey() instanceof String)) {
				throw new IllegalArgumentException(
						"Invalid key [" + entry.getKey() + "] in attributes Map - only Strings allowed as attribute keys");
			}
			request.setAttribute((String) entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Check if a specific input type="submit" parameter was sent in the request,
	 * either via a button (directly with name) or via an image (name + ".x" or
	 * name + ".y").
	 * @param request current portlet request
	 * @param name name of the parameter
	 * @return if the parameter was sent
	 * @see #SUBMIT_IMAGE_SUFFIXES
	 */
	public static boolean hasSubmitParameter(PortletRequest request, String name) {
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

	/**
	 * Return the full name of a specific input type="submit" parameter 
	 * if it was sent in the request, either via a button (directly with name)
	 * or via an image (name + ".x" or name + ".y").
	 * @param request current portlet request
	 * @param name name of the parameter
	 * @return the actual parameter name with suffix if needed - null if not present
	 * @see #SUBMIT_IMAGE_SUFFIXES
	 */
	public static String getSubmitParameter(PortletRequest request, String name) {
		if (request.getParameter(name) != null) {
			return name;
		}
		for (int i = 0; i < SUBMIT_IMAGE_SUFFIXES.length; i++) {
			String suffix = SUBMIT_IMAGE_SUFFIXES[i];
			if (request.getParameter(name + suffix) != null) {
				return name + suffix;
			}
		}
		return null;
	}

	/**
	 * Return a map containing all parameters with the given prefix.
	 * Maps single values to String and multiple values to String array.
	 * <p>For example, with a prefix of "spring_", "spring_param1" and
	 * "spring_param2" result in a Map with "param1" and "param2" as keys.
	 * <p>Similar to portlet <code>PortletRequest.getParameterMap</code>,
	 * but more flexible.
	 * @param request portlet request in which to look for parameters
	 * @param prefix the beginning of parameter names
	 * (if this is null or the empty string, all parameters will match)
	 * @return map containing request parameters <b>without the prefix</b>,
	 * containing either a String or a String array as values
	 * @see javax.portlet.PortletRequest#getParameterNames
	 * @see javax.portlet.PortletRequest#getParameterValues
	 * @see javax.portlet.PortletRequest#getParameterMap
	 */
	public static Map getParametersStartingWith(PortletRequest request, String prefix) {
		Enumeration paramNames = request.getParameterNames();
		Map params = new TreeMap();
		if (prefix == null) {
			prefix = "";
		}
		while (paramNames != null && paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			if ("".equals(prefix) || paramName.startsWith(prefix)) {
				String unprefixed = paramName.substring(prefix.length());
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

}
