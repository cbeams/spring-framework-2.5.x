/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.bind;

import javax.servlet.ServletRequest;

import org.springframework.validation.DataBinder;
import org.springframework.web.servlet.MultipartHttpServletRequest;
import org.springframework.web.servlet.MultipartResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

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
	 * Create the binder with multipart property editors if applicable
	 */
	public ServletRequestDataBinder(Object target, String name, ServletRequest request) {
		super(target, name);

		if (request instanceof MultipartHttpServletRequest) {
			// Even though getMultipartResolver(request) can return null, we 
			// don't explicitly check since it must be there if we have a 
			// MultipartHttpServletRequest.  The try/catch is a fallback in case
			// of programmer error when implementing custom resolvers, and simply
			// rethrows with a friendlier message	
			try {
				MultipartResolver resolver = RequestContextUtils.getMultipartResolver((MultipartHttpServletRequest)request);				
				resolver.registerMultipartEditors((MultipartHttpServletRequest)request, this);
			} catch (NullPointerException npex) {
				throw new IllegalArgumentException("no MultipartResolver was found even though the request is a MultipartHttpServletRequest");
			}
		}
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
