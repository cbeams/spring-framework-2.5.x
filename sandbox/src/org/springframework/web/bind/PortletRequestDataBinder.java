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

import javax.portlet.PortletRequest;

import org.springframework.validation.DataBinder;


/**
 * Use this class to perform manual data binding from portlett
 * parameters to JavaBeans.
 * @author
 */
public class PortletRequestDataBinder extends DataBinder {

	/**
	 * Create a new DataBinder instance.
	 * @param target target object to bind onto
	 * @param name name of the target object
	 */
	public PortletRequestDataBinder(Object target, String name) {
		super(target, name);
	}

	
	/**
	 * Bind the parameters of the given request to this binder's target,
	 * <p>This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * @param request request with parameters to bind
	 */
	public void bind(PortletRequest request) {
		// bind normal HTTP parameters
		bind(new PortletRequestParameterPropertyValues(request));
	}

	/**
	 * Treats errors as fatal. Use this method only if
	 * it's an error if the input isn't valid.
	 * This might be appropriate if all input is from dropdowns, for example.
	 * @throws ServletRequestBindingException subclass of PortletException on any binding problem
	 */
	public void closeNoCatch() throws PortletRequestBindingException {
		if (getErrors().hasErrors()) {
			throw new PortletRequestBindingException("Errors binding onto object '" + getErrors().getObjectName() + "'",
																							 getErrors());
		}
	}

}
