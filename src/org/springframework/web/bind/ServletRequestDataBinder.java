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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Special binder to perform data binding from servlet request parameters
 * to JavaBeans, including support for multipart files.
 *
 * <p>Used by Spring web MVC's BaseCommandController and MultiActionController.
 * Note that BaseCommandController and its subclasses allow for easy customization
 * of the binder instances that they use, for example registering custom editors.
 *
 * <p>Can also be used for manual data binding in custom web controllers.
 * Simply instantiate a ServletRequestDataBinder for each binding process,
 * and invoke <code>bind</code> with the current ServletRequest as argument.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #bind(javax.servlet.ServletRequest)
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 * @see org.springframework.web.servlet.mvc.multiaction.MultiActionController
 */
public class ServletRequestDataBinder extends DataBinder {

	/**
	 * Prefix that checkbox marker parameters start with, followed
	 * by the field name: e.g. "_checkbox.subscribeToNewsletter"
	 * for a field "subscribeToNewsletter".
	 */
	public static final String CHECKBOX_MARKER_PREFIX = "_checkbox.";

	private boolean resetEmptyCheckboxFields = true;

	private boolean bindEmptyMultipartFiles = true;


	/**
	 * Create a new DataBinder instance.
	 * @param target target object to bind onto
	 * @param objectName objectName of the target object
	 */
	public ServletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}

	/**
	 * Set whether to automatically reset empty checkbox fields that are marked
	 * through "_checkbox.FIELD" parameters. Default is true.
	 * <p>HTML checkboxes only send a value when they're checked, so it is not
	 * possible to detect that a formerly checked box has just been unchecked,
	 * at least not with standard HTML means.
	 * <p>One way to address this is to look for a checkbox parameter value if
	 * you know that the checkbox has been visible in the form, resetting the
	 * checkbox if no value found. In Spring web MVC, this typically happens
	 * in a custom <code>onBind</code> implementation.
	 * <p>This auto-reset mechanism addresses this deficiency, provided
	 * that a marker parameter is sent for each checkbox field, like
	 * "_checkbox.subscribeToNewsletter" for a "subscribeToNewsletter" field.
	 * As the marker parameter is sent in any case, the data binder can detect
	 * an empty checkbox field and automatically reset its value.
	 * @see #CHECKBOX_MARKER_PREFIX
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind
	 */
	public void setResetEmptyCheckboxFields(boolean resetEmptyCheckboxFields) {
		this.resetEmptyCheckboxFields = resetEmptyCheckboxFields;
	}

	/**
	 * Set whether to bind empty MultipartFile parameters. Default is true.
	 * <p>Turn this off if you want to keep an already bound MultipartFile
	 * when the user resubmits the form without choosing a different file.
	 * Else, the already bound MultipartFile will be replaced by an empty
	 * MultipartFile holder.
	 */
	public void setBindEmptyMultipartFiles(boolean bindEmptyMultipartFiles) {
		this.bindEmptyMultipartFiles = bindEmptyMultipartFiles;
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
	 */
	public void bind(ServletRequest request) {
		// bind normal HTTP parameters
		MutablePropertyValues pvs = new ServletRequestParameterPropertyValues(request);

		// check for special checkbox markers
		if (this.resetEmptyCheckboxFields) {
			PropertyValue[] pvArray = pvs.getPropertyValues();
			for (int i = 0; i < pvArray.length; i++) {
				PropertyValue pv = pvArray[i];
				if (pv.getName().startsWith(CHECKBOX_MARKER_PREFIX)) {
					String field = pv.getName().substring(CHECKBOX_MARKER_PREFIX.length());
					if (!pvs.contains(field)) {
						try {
							Class type = getBeanWrapper().getPropertyType(field);
							// special check for boolean property
							if (type != null && boolean.class.equals(type) || Boolean.class.equals(type)) {
								pvs.addPropertyValue(field, Boolean.FALSE);
							}
							else {
								pvs.addPropertyValue(field, null);
							}
						}
						catch (InvalidPropertyException ex) {
							// ignore if no matching property
						}
					}
				}
			}
		}

		// bind multipart files
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map fileMap = multipartRequest.getFileMap();
			for (Iterator it = fileMap.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				MultipartFile value = (MultipartFile) fileMap.get(key);
				if (this.bindEmptyMultipartFiles || !value.isEmpty()) {
					pvs.addPropertyValue(key, value);
				}
			}
		}
		
		bind(pvs);
	}

	/**
	 * Treats errors as fatal. Use this method only if
	 * it's an error if the input isn't valid.
	 * This might be appropriate if all input is from dropdowns, for example.
	 * @throws ServletRequestBindingException subclass of ServletException on any binding problem
	 */
	public void closeNoCatch() throws ServletRequestBindingException {
		if (getErrors().hasErrors()) {
			throw new ServletRequestBindingException("Errors binding onto object '" + getErrors().getObjectName() + "'",
																							 getErrors());
		}
	}

}
