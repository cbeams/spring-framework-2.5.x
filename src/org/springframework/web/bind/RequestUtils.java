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

package org.springframework.web.bind;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Parameter extraction methods, for an approach distinct from data binding,
 * in which parameters of specific types are required.
 *
 * <p>This approach is very useful for simple submissions, where binding
 * request parameters to a command object would be overkill.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 */
public abstract class RequestUtils {

	/**
	 * Throw a ServletException if the given HTTP request method should be rejected.
	 * @param request request to check
	 * @param method method (such as "GET") which should be rejected
	 * @throws ServletException if the given HTTP request is rejected
	 */
	public static void rejectRequestMethod(HttpServletRequest request, String method)
			throws ServletException {
		if (request.getMethod().equals(method)) {
			throw new ServletException("This resource does not support request method '" + method + "'");
		}
	}

	/**
	 * Get an int parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static int getIntParameter(HttpServletRequest request, String name, int defaultVal) {
		try {
			return getRequiredIntParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an int parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int getRequiredIntParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null) {
			throw new ServletRequestBindingException("Required int parameter '" + name + "' was not supplied");
		}
		if ("".equals(s)) {
			throw new ServletRequestBindingException("Required int parameter '" + name + "' contained no value");
		}
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException ex) {
				throw new ServletRequestBindingException(
				    "Required int parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}

	/**
	 * Get an int parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static long getLongParameter(HttpServletRequest request, String name, long defaultVal) {
		try {
			return getRequiredLongParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a long parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long getRequiredLongParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null) {
			throw new ServletRequestBindingException("Required long parameter '" + name + "' was not supplied");
		}
		if ("".equals(s)) {
			throw new ServletRequestBindingException("Required long parameter '" + name + "' contained no value");
		}
		try {
			return Long.parseLong(s);
		}
		catch (NumberFormatException ex) {
				throw new ServletRequestBindingException(
				    "Required long parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}

	/**
	 * Get a double parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static float getFloatParameter(HttpServletRequest request, String name, float defaultVal) {
		try {
			return getRequiredFloatParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a double parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static float getRequiredFloatParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null) {
			throw new ServletRequestBindingException("Required float parameter '" + name + "' was not supplied");
		}
		if ("".equals(s)) {
			throw new ServletRequestBindingException("Required float parameter '" + name + "' contained no value");
		}
		try {
			return Float.parseFloat(s);
		}
		catch (NumberFormatException ex) {
			throw new ServletRequestBindingException(
			    "Required float parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}

	/**
	 * Get a double parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static double getDoubleParameter(HttpServletRequest request, String name, double defaultVal) {
		try {
			return getRequiredDoubleParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a double parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double getRequiredDoubleParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null) {
			throw new ServletRequestBindingException("Required double parameter '" + name + "' was not supplied");
		}
		if ("".equals(s)) {
			throw new ServletRequestBindingException("Required double parameter '" + name + "' contained no value");
		}
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex) {
			throw new ServletRequestBindingException(
			    "Required double parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}
	
	/**
	 * Get a boolean parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultVal) {
		try {
			return getRequiredBooleanParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a boolean parameter, throwing an exception if it isn't found or isn't a boolean.
	 * True is "true" or "yes" or "on" (ignoring the case) or "1".
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean getRequiredBooleanParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null) {
			throw new ServletRequestBindingException("Required boolean parameter '" + name + "' was not supplied");
		}
		if ("".equals(s)) {
			throw new ServletRequestBindingException("Required boolean parameter '" + name + "' contained no value");
		}
		return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on") || s.equals("1");
	}

	/**
	 * Get a string parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static String getStringParameter(HttpServletRequest request, String name, String defaultVal) {
		try {
			return getRequiredStringParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get a string parameter, throwing an exception if it isn't found or is empty.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String getRequiredStringParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String s = request.getParameter(name);
		if (s == null) {
			throw new ServletRequestBindingException("Required string parameter '" + name + "' was not supplied");
		}
		if ("".equals(s)) {
			throw new ServletRequestBindingException("Required string parameter '" + name + "' contained no value");
		}
		return s;
	}

}
