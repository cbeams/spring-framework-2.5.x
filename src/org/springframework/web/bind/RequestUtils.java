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
 * @author Keith Donald
 */
public abstract class RequestUtils {

	private abstract static class ParameterParser {
		public Object parse(String name, String parameter) throws ServletRequestBindingException {
			validateRequiredParameter(name, parameter);
			try {
				return doParse(parameter);
			}
			catch (NumberFormatException ex) {
				throw new ServletRequestBindingException("Required " + getType() + " parameter '" + name + "' with value of '"
						+ parameter + "' is not a valid number");
			}
		}

		private void validateRequiredParameter(String name, String parameter) throws ServletRequestBindingException {
			if (parameter == null) {
				throw new ServletRequestBindingException("Required " + getType() + " parameter '" + name + "' is not present");
			}
			if ("".equals(parameter)) {
				throw new ServletRequestBindingException("Required " + getType() + " parameter '" + name
						+ "' contains no value");
			}
		}

		public int parseInt(String name, String parameter) throws ServletRequestBindingException {
			return ((Number)parse(name, parameter)).intValue();
		}

		public int[] parseInts(String name, String[] values) throws ServletRequestBindingException {
			int[] parameters = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseInt(name, values[i]);
			}
			return parameters;
		}

		public long parseLong(String name, String parameter) throws ServletRequestBindingException {
			return ((Number)parse(name, parameter)).longValue();
		}

		public long[] parseLongs(String name, String[] values) throws ServletRequestBindingException {
			long[] parameters = new long[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseLong(name, values[i]);
			}
			return parameters;
		}

		public float parseFloat(String name, String parameter) throws ServletRequestBindingException {
			return ((Number)parse(name, parameter)).floatValue();
		}

		public float[] parseFloats(String name, String[] values) throws ServletRequestBindingException {
			float[] parameters = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseFloat(name, values[i]);
			}
			return parameters;
		}

		public double parseDouble(String name, String parameter) throws ServletRequestBindingException {
			return ((Number)parse(name, parameter)).doubleValue();
		}

		public double[] parseDoubles(String name, String[] values) throws ServletRequestBindingException {
			double[] parameters = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseDouble(name, values[i]);
			}
			return parameters;
		}

		public boolean parseBoolean(String name, String parameter) throws ServletRequestBindingException {
			return ((Boolean)parse(name, parameter)).booleanValue();
		}

		public boolean[] parseBooleans(String name, String[] values) throws ServletRequestBindingException {
			boolean[] parameters = new boolean[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseBoolean(name, values[i]);
			}
			return parameters;
		}

		public String[] validateRequiredStrings(String name, String[] values) throws ServletRequestBindingException {
			for (int i = 0; i < values.length; i++) {
				validateRequiredParameter(name, values[i]);
			}
			return values;
		}

		protected abstract String getType();

		protected abstract Object doParse(String parameter) throws NumberFormatException;
	}

	private static final ParameterParser INT_PARSER = new ParameterParser() {
		protected String getType() {
			return "int";
		}

		protected Object doParse(String s) throws NumberFormatException {
			return Integer.valueOf(s);
		}
	};

	private static final ParameterParser LONG_PARSER = new ParameterParser() {
		protected String getType() {
			return "long";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return Long.valueOf(parameter);
		}
	};

	private static final ParameterParser FLOAT_PARSER = new ParameterParser() {
		protected String getType() {
			return "float";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return Float.valueOf(parameter);
		}
	};

	private static final ParameterParser DOUBLE_PARSER = new ParameterParser() {
		protected String getType() {
			return "double";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return new Double(parameter);
		}
	};

	private static final ParameterParser BOOLEAN_PARSER = new ParameterParser() {
		protected String getType() {
			return "boolean";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return Boolean.valueOf(parameter.equalsIgnoreCase("true") || parameter.equalsIgnoreCase("yes")
					|| parameter.equalsIgnoreCase("on") || parameter.equals("1"));
		}
	};

	private static final ParameterParser STRING_PARSER = new ParameterParser() {
		protected String getType() {
			return "string";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return parameter;
		}
	};

	/**
	 * Throw a ServletException if the given HTTP request method should be rejected.
	 * @param request request to check
	 * @param method method (such as "GET") which should be rejected
	 * @throws ServletException if the given HTTP request is rejected
	 */
	public static void rejectRequestMethod(HttpServletRequest request, String method) throws ServletException {
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
		return INT_PARSER.parseInt(name, request.getParameter(name));
	}

	/**
	 * Get an array of int parameters, throwing an exception if not found or one is not a number..
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int[] getRequiredIntParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return INT_PARSER.parseInts(name, getRequiredParameterValues(request, name));
	}

	private static String[] getRequiredParameterValues(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		String[] values = request.getParameterValues(name);
		if (values == null) {
			throw new ServletRequestBindingException("Required parameter array '" + name + "' is not present");
		}
		return values;
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
		return LONG_PARSER.parseLong(name, request.getParameter(name));
	}

	/**
	 * Get an array of long parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long[] getRequiredLongParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return LONG_PARSER.parseLongs(name, getRequiredParameterValues(request, name));
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
		return FLOAT_PARSER.parseFloat(name, request.getParameter(name));
	}

	/**
	 * Get an array of float parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static float[] getRequiredFloatParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return FLOAT_PARSER.parseFloats(name, getRequiredParameterValues(request, name));
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
		return DOUBLE_PARSER.parseDouble(name, request.getParameter(name));
	}

	/**
	 * Get an array of double parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double[] getRequiredDoubleParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return DOUBLE_PARSER.parseDoubles(name, getRequiredParameterValues(request, name));
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
	 * Get an array of boolean parameters, throwing an exception if it isn't found or isn't a boolean.
	 * True is "true" or "yes" or "on" (ignoring the case) or "1".
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean getRequiredBooleanParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return BOOLEAN_PARSER.parseBoolean(name, request.getParameter(name));
	}

	/**
	 * Get an array of boolean parameters, throwing an exception if not found or one isn't a boolean.
	 * True is "true" or "yes" or "on" (ignoring the case) or "1".
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean[] getRequiredBooleanParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return BOOLEAN_PARSER.parseBooleans(name, getRequiredParameterValues(request, name));
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
		return (String)STRING_PARSER.parse(name, request.getParameter(name));
	}

	/**
	 * Get an array of string parameters, throwing an exception if not found or one is empty.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException: subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String[] getRequiredStringParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {
		return STRING_PARSER.validateRequiredStrings(name, getRequiredParameterValues(request, name));
	}

}