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
import javax.servlet.ServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Offers convenience methods for binding servlet request parameters
 * to objects, including optional validation.
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 10.03.2003
 */
public abstract class BindUtils {

	/**
	 * Bind the parameters from the given request to the given object.
	 * @param request request containing the parameters
	 * @param object object to bind the parameters to
	 * @param objectName name of the bind object
	 * @return the binder used (can be treated as BindException or Errors instance)
	 */
	public static BindException bind(ServletRequest request, Object object, String objectName) {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(object, objectName);
		binder.bind(request);
		return binder.getErrors();
	}

	/**
	 * Bind the parameters from the given request to the given object,
	 * allowing for optional custom editors set in an bind initializer.
	 * @param request request containing the parameters
	 * @param object object to bind the parameters to
	 * @param objectName name of the bind object
	 * @param initializer implementation of the BindInitializer interface
	 * which will be able to set custom editors
	 * @return the binder used (can be treated as BindException or Errors instance)
	 * @throws ServletException if thrown by the BindInitializer
	 */
	public static BindException bind(ServletRequest request, Object object, String objectName,
																	 BindInitializer initializer) throws ServletException  {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(object, objectName);
		if (initializer != null) {
			initializer.initBinder(request, binder);
		}
		binder.bind(request);
		return binder.getErrors();
	}

	/**
	 * Bind the parameters from the given request to the given object,
	 * invoking the given validator.
	 * @param request request containing the parameters
	 * @param object object to bind the parameters to
	 * @param objectName name of the bind object
	 * @param validator validator to be invoked, or null if no validation
	 * @return the binder used (can be treated as Errors instance)
	 */
	public static BindException bindAndValidate(ServletRequest request, Object object, String objectName,
																							Validator validator) {
		BindException binder = bind(request, object, objectName);
		ValidationUtils.invokeValidator(validator, object, binder);
		return binder;
	}

	/**
	 * Bind the parameters from the given request to the given object,
	 * invoking the given validator, and allowing for optional custom editors
	 * set in an bind initializer.
	 * @param request request containing the parameters
	 * @param object object to bind the parameters to
	 * @param objectName name of the bind object
	 * @param validator validator to be invoked, or null if no validation
	 * @param initializer Implementation of the BindInitializer interface which will be able to set custom editors
	 * @return the binder used (can be treated as Errors instance)
	 * @throws ServletException if thrown by the BindInitializer
	 */
	public static BindException bindAndValidate(ServletRequest request,	Object object, String objectName,
																							Validator validator, BindInitializer initializer)
	    throws ServletException  {
		BindException binder = bind(request, object, objectName, initializer);
		ValidationUtils.invokeValidator(validator, object, binder);
		return binder;
	}

}
