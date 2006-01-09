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

package org.springframework.web.bind;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

/**
 * Callback that allows for initialization of a binder with
 * custom editors before kicking off the binding process.
 *
 * <p>This is effectively a way to factor binder initialization out into
 * a dedicated object, to be invoked through BindUtils. In all other
 * respects, it is equivalent to the <code>initBinder</code> template
 * method defined by the BaseCommandController class.
 *
 * @author Jean-Pierre Pawlak
 * @since 08.05.2003
 * @deprecated since Spring 1.2.7: prefer direct ServletRequestDataBinder usage,
 * potentially in combination with a PropertyEditorRegistrar
 * @see ServletRequestDataBinder
 * @see org.springframework.beans.PropertyEditorRegistrar
 */
public interface BindInitializer {

	/**
	 * Initialize the given binder instance, e.g. with custom editors.
	 * Called by <code>BindUtils.bind</code>.
	 * <p>This method allows you to register custom editors for certain fields
	 * of your command class. For instance, you will be able to transform Date
	 * objects into a String pattern and back, in order to allow your JavaBeans
	 * to have Date properties and still be able to set and display them in,
	 * for instance, an HTML interface.
	 * @param request current request
	 * @param binder new binder instance
	 * @throws ServletException in case of invalid state or arguments
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 */
	void initBinder(ServletRequest request, ServletRequestDataBinder binder) throws ServletException;

}
