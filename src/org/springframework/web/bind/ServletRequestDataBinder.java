/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.bind;

import javax.servlet.ServletRequest;

import org.springframework.validation.DataBinder;

/**
 * Use this class to perform manual data binding from servlet request parameters
 * to JavaBeans.
 * @author Rod Johnson
 */
public class ServletRequestDataBinder extends DataBinder  {
	
	public ServletRequestDataBinder(Object target, String name) {
		super(target, name);
	}

	/**
	 * Bind the parameters of the given request to this binder's target.
	 * This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * @param request request with parameters to bind
	 */
	public void bind(ServletRequest request) {
		bind(new ServletRequestParameterPropertyValues(request));
	}

	/**
	 * Treats errors as fatal. Use this method only if 
	 * it's an error if the input isn't valid. 
	 * This might be appropriate
	 * if all input is from dropdowns, for example.
	 * @throws ServletRequestBindingException subclass of ServletException on any binding problem
	 */
	public void closeNoCatch() throws ServletRequestBindingException {
		if (hasErrors()) {
			throw new ServletRequestBindingException("Errors binding onto class " + getTarget(), this);
		}
	}
	
}
