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

package org.springframework.web.multipart;

import org.springframework.core.NestedIOException;

/**
 * Exception thrown on multipart resolution.
 *
 * <p>Extends IOException for convenient throwing in any Servlet/Portlet resource
 * (such as a Filter), and NestedIOException for proper root cause handling.
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see MultipartResolver#resolveMultipart
 * @see org.springframework.web.multipart.support.MultipartFilter
 * @see org.springframework.core.NestedIOException
 */
public class MultipartException extends NestedIOException {

	/**
	 * Constructor for MultipartException.
	 * @param msg the detail message
	 */
	public MultipartException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for MultipartException.
	 * @param msg the detail message
	 * @param cause the root cause from the multipart parsing API in use
	 */
	public MultipartException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
