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

/**
 * Callback that allows for initialization of a binder with
 * custom editors before the binding. Used by BindUtils.
 * @author Jean-Pierre PAWLAK
 * @since 08.05.2003
 * @see BindUtils#bind(ServletRequest,Object,String,BindInitializer)
 * @see BindUtils#bindAndValidate(ServletRequest,Object,String,org.springframework.validation.Validator,BindInitializer)
 */
public interface BindInitializer {

	/**
	 * Initialize the given binder instance, e.g. with custom editors.
	 * Called by BindUtils#bind. This method allows you to register custom
	 * editors for certain fields of your command class. For instance, you will
	 * be able to transform Date objects into a String pattern and back, in order
	 * to allow your JavaBeans to have Date properties and still be able to
	 * set and display them in for instance an HTML interface.
	 * @param request current request
	 * @param binder new binder instance
	 * @throws ServletException in case of invalid state or arguments
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 * @see BindUtils#bind(ServletRequest,Object,String,BindInitializer)
	 */
	public void initBinder(ServletRequest request, ServletRequestDataBinder binder)
			throws ServletException ;
}
