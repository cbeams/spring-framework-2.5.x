/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.bind;

import java.util.Iterator;
import java.beans.PropertyDescriptor;

import javax.servlet.ServletRequest;

import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartException;
import org.springframework.beans.MutablePropertyValues;

/**
 * Use this class to perform manual data binding from servlet request parameters
 * to JavaBeans, including support for multipart files.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class ServletRequestDataBinder extends DataBinder {
	
	public ServletRequestDataBinder(Object target, String name) {
		super(target, name);
	}

	/**
	 * Bind the parameters of the given request to this binder's target,
	 * also binding multipart files in case of a multipart request.
	 * <p>This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * <p>Multipart files are bound via their parameter name, just like normal
	 * HTTP parameters: i.e. "uploadedFile" to an "uploadedFile" bean property,
	 * invoking a "setUploadedFile" setter method.
	 * <p>The type of the target property for a multipart file can be MultipartFile,
	 * byte[], or String. The latter two receive the contents of the uploaded file;
	 * all metadata like original file name, content type, etc are lost in those cases.
	 * @param request request with parameters to bind (can be multipart)
	 * @see org.springframework.web.multipart.MultipartHttpServletRequest
	 * @see org.springframework.web.multipart.MultipartFile
	 * @throws org.springframework.web.multipart.MultipartException
	 */
	public void bind(ServletRequest request) throws MultipartException {
		// bind normal HTTP parameters
		bind(new ServletRequestParameterPropertyValues(request));

		// bind multipart files
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MutablePropertyValues pvs = new MutablePropertyValues();
			Iterator fileNames = multipartRequest.getFileNames();
			while (fileNames.hasNext()) {
				String fileName = (String) fileNames.next();
				PropertyDescriptor descriptor = getBeanWrapper().getPropertyDescriptor(fileName);
				if (descriptor != null) {
					MultipartFile file = multipartRequest.getFile(fileName);
					if (descriptor.getPropertyType().equals(byte[].class)) {
						pvs.addPropertyValue(fileName, file.getBytes());
					}
					else if (descriptor.getPropertyType().equals(String.class)) {
						pvs.addPropertyValue(fileName, new String(file.getBytes()));
					}
					else {
						pvs.addPropertyValue(fileName, file);
					}
				}
			}
			bind(pvs);
		}
	}

	/**
	 * Treats errors as fatal. Use this method only if 
	 * it's an error if the input isn't valid. 
	 * This might be appropriate if all input is from dropdowns, for example.
	 * @throws ServletRequestBindingException subclass of ServletException on any binding problem
	 */
	public void closeNoCatch() throws ServletRequestBindingException {
		if (hasErrors()) {
			throw new ServletRequestBindingException("Errors binding onto class " + getTarget(), this);
		}
	}
	
}
