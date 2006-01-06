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

package org.springframework.web.portlet.multipart;

import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionRequest;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface which provides additional methods for dealing with multipart
 * content within a portlet request, allowing to access uploaded files.
 * Implementations also need to override the standard ActionRequest
 * methods for parameter access, making multipart parameters available.
 *
 * <p>A concrete implementation is DefaultMultipartActionRequest.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see PortletMultipartResolver
 * @see org.springframework.web.multipart.MultipartFile
 * @see javax.portlet.ActionRequest#getParameter
 * @see javax.portlet.ActionRequest#getParameterNames
 * @see javax.portlet.ActionRequest#getParameterMap
 * @see DefaultMultipartActionRequest
 */
public interface MultipartActionRequest extends ActionRequest {

	/**
	 * Return an Iterator of String objects containing the parameter names of the
	 * multipart files contained in this request. These are the field names of
	 * the form (like with normal parameters), not the original file names.
	 * @return the names of the files
	 */
	Iterator getFileNames();

	/**
	 * Return the contents plus description of an uploaded file in this request,
	 * or <code>null</code> if it does not exist.
	 * @param name a String specifying the parameter name of the multipart file
	 * @return the uploaded content in the form of a MultipartFile object
	 */
	MultipartFile getFile(String name);

	/**
	 * Return a Map of the multipart files contained in this request.
	 * @return a map containing the parameter names as keys, and the
	 * MultipartFile objects as values
	 * @see org.springframework.web.multipart.MultipartFile
	 */
	Map getFileMap();

}
