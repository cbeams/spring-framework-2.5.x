/*
 * Copyright 2002-2005 the original author or authors.
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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Special DataBinder to perform data binding from servlet request parameters
 * to JavaBeans, including support for multipart files.
 *
 * <p>See the DataBinder/WebDataBinder superclasses for customization options,
 * which include specifying allowed/required fields, and registering custom
 * property editors.
 *
 * <p>Used by Spring web MVC's BaseCommandController and MultiActionController.
 * Note that BaseCommandController and its subclasses allow for easy customization
 * of the binder instances that they use through overriding <code>initBinder</code>.
 *
 * <p>Can also be used for manual data binding in custom web controllers:
 * for example, in a plain Controller implementation or in a MultiActionController
 * handler method. Simply instantiate a ServletRequestDataBinder for each binding
 * process, and invoke <code>bind</code> with the current ServletRequest as argument:
 *
 * <pre>
 * MyBean myBean = new MyBean();
 * // apply binder to custom target object
 * ServletRequestDataBinder binder = new ServletRequestDataBinder(myBean);
 * // register custom editors, if desired
 * binder.registerCustomEditor(...);
 * // trigger actual binding of request parameters
 * binder.bind(request);
 * // optionally evaluate binding errors
 * Errors errors = binder.getErrors();
 * ...</pre>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #bind(javax.servlet.ServletRequest)
 * @see #registerCustomEditor
 * @see #setAllowedFields
 * @see #setRequiredFields
 * @see #setFieldMarkerPrefix
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 * @see org.springframework.web.servlet.mvc.multiaction.MultiActionController#initBinder
 */
public class ServletRequestDataBinder extends WebDataBinder {

	private boolean bindEmptyMultipartFiles = true;
	

	/**
	 * Create a new ServletRequestDataBinder instance, with default object name.
	 * @param target target object to bind onto
	 * @see #DEFAULT_OBJECT_NAME
	 */
	public ServletRequestDataBinder(Object target) {
		super(target);
	}

	/**
	 * Create a new ServletRequestDataBinder instance.
	 * @param target target object to bind onto
	 * @param objectName objectName of the target object
	 */
	public ServletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}


	/**
	 * Set whether to bind empty MultipartFile parameters. Default is "true".
	 * <p>Turn this off if you want to keep an already bound MultipartFile
	 * when the user resubmits the form without choosing a different file.
	 * Else, the already bound MultipartFile will be replaced by an empty
	 * MultipartFile holder.
	 */
	public void setBindEmptyMultipartFiles(boolean bindEmptyMultipartFiles) {
		this.bindEmptyMultipartFiles = bindEmptyMultipartFiles;
	}

	/**
	 * Return whether to bind empty MultipartFile parameters.
	 */
	public boolean isBindEmptyMultipartFiles() {
		return bindEmptyMultipartFiles;
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
	 * @see #checkMultipartFiles
	 * @see #bind(org.springframework.beans.PropertyValues)
	 */
	public void bind(ServletRequest request) {
		MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
		checkMultipartFiles(request, mpvs);
		doBind(mpvs);
	}

	/**
	 * Check the multipart files contained in the given request, if any
	 * (in case of a multipart request).
	 * <p>Multipart files will only be added to the property values if they
	 * are not empty or if we're configured to bind empty multipart files too.
	 * @param request current request (can be multipart)
	 * @param mpvs the property values to be bound (can be modified)
	 * @see org.springframework.web.multipart.MultipartHttpServletRequest
	 * @see org.springframework.web.multipart.MultipartFile
	 * @see #isBindEmptyMultipartFiles
	 */
	protected void checkMultipartFiles(ServletRequest request, MutablePropertyValues mpvs) {
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map fileMap = multipartRequest.getFileMap();
			for (Iterator it = fileMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				MultipartFile value = (MultipartFile) entry.getValue();
				if (isBindEmptyMultipartFiles() || !value.isEmpty()) {
					mpvs.addPropertyValue(key, value);
				}
			}
		}
	}

	/**
	 * Treats errors as fatal. Use this method only if
	 * it's an error if the input isn't valid.
	 * This might be appropriate if all input is from dropdowns, for example.
	 * @throws ServletRequestBindingException subclass of ServletException on any binding problem
	 */
	public void closeNoCatch() throws ServletRequestBindingException {
		if (getErrors().hasErrors()) {
			throw new ServletRequestBindingException(
					"Errors binding onto object '" + getErrors().getObjectName() + "'", getErrors());
		}
	}

}
