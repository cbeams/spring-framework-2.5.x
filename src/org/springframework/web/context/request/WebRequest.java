/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.context.request;

import java.util.Locale;
import java.util.Map;

/**
 * Generic interface for a web request. Mainly intended for generic web
 * request interceptors, giving them access to general request metadata,
 * not for actual handling of the request.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see WebRequestInterceptor
 */
public interface WebRequest extends RequestAttributes {

	/**
	 * Return the request parameter of the given name, or <code>null</code> if none.
	 * <p>Retrieves the first parameter value in case of a multi-value parameter.
	 */
	String getParameter(String paramName);

	/**
	 * Return the request parameter values for the given parameter name,
	 * or <code>null</code> if none.
	 * <p>A single-value parameter will be exposed as an array with a single element.
	 */
	String[] getParameterValues(String paramName);

	/**
	 * Return a immutable Map of the request parameters, with parameter names as map keys
	 * and parameter values as map values. The map values will be of type String array.
	 * <p>A single-value parameter will be exposed as an array with a single element.
	 */
	Map getParameterMap();

	/**
	 * Return the primary Locale for this request.
	 */
	Locale getLocale();

}
